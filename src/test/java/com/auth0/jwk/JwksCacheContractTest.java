package com.auth0.jwk;

import com.google.common.base.Ticker;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Verifies that the caching configured on the builder is the only thing deciding how fresh returned keys are.
 */
public class JwksCacheContractTest {

    private static final Duration TTL = Duration.ofMinutes(10);

    private URL url;
    private FakeJwksServer server;
    private FakeTicker ticker;

    @Before
    public void setUp() throws Exception {
        url = new URL("https://example.com/.well-known/jwks.json");
        server = new FakeJwksServer();
        ticker = new FakeTicker();
    }

    private JwkProviderBuilder builder() {
        return new JwkProviderBuilder(url)
                .httpClient(server)
                .cached(5, TTL)
                .ticker(ticker);
    }

    @Test
    public void shouldServeCachedKeyUntilExpiryThenObserveReplacement() throws Exception {
        server.publish("a", "v1");
        JwkProvider provider = builder().build();
        assertThat(provider.get("a").getAdditionalAttributes().get("n"), is("v1"));

        server.publish("a", "v2");
        ticker.advance(TTL.minusSeconds(1));
        assertThat(provider.get("a").getAdditionalAttributes().get("n"), is("v1"));

        ticker.advance(Duration.ofSeconds(2));
        assertThat(provider.get("a").getAdditionalAttributes().get("n"), is("v2"));
    }

    @Test
    public void shouldFetchCurrentJwksOnEveryLookupWhenCachingDisabled() throws Exception {
        server.publish("a", "v1");
        JwkProvider provider = new JwkProviderBuilder(url)
                .httpClient(server)
                .cached(false)
                .rateLimited(false)
                .build();
        assertThat(provider.get("a").getAdditionalAttributes().get("n"), is("v1"));

        server.publish("a", "v2");
        assertThat(provider.get("a").getAdditionalAttributes().get("n"), is("v2"));

        server.publish("b", "v1");
        try {
            provider.get("a");
            fail("removed key must not be served");
        } catch (SigningKeyNotFoundException expected) {
        }
        assertThat(server.fetches(), is(3));
    }

    @Test
    public void shouldServeEveryKeyFromOneFetchPerExpiry() throws Exception {
        server.publish("a", "v1", "b", "v1");
        JwkProvider provider = builder().build();

        provider.get("a");
        provider.get("b");
        assertThat(server.fetches(), is(1));

        ticker.advance(TTL.plusSeconds(1));
        provider.get("a");
        provider.get("b");
        assertThat(server.fetches(), is(2));
    }

    @Test
    public void shouldRetainNoMoreKeysThanCacheSize() throws Exception {
        server.publish("a", "v1", "b", "v1", "c", "v1", "d", "v1", "e", "v1", "f", "v1", "g", "v1");
        JwkProvider provider = builder().build();

        provider.get("a");
        provider.get("b");
        assertThat(server.fetches(), is(2));

        provider.get("a");
        provider.get("b");
        assertThat(server.fetches(), is(2));
    }

    @Test
    public void shouldDropRemovedKeyOnNextRefresh() throws Exception {
        server.publish("a", "v1", "b", "v1");
        JwkProvider provider = builder().build();
        provider.get("a");
        provider.get("b");

        server.publish("a", "v1", "c", "v1");
        provider.get("c");

        try {
            provider.get("b");
            fail("key removed by the issuer must not be served once a refresh has seen it gone");
        } catch (SigningKeyNotFoundException expected) {
        }
    }

    @Test
    public void shouldReportUnknownKidLikeUncachedProvider() throws Exception {
        server.publish("a", "v1");
        JwkProvider cached = builder().build();
        JwkProvider uncached = new UrlJwkProvider(url, server);

        assertThat(messageOf(cached, "missing"), is(messageOf(uncached, "missing")));
    }

    private static String messageOf(JwkProvider provider, String keyId) throws JwkException {
        try {
            provider.get(keyId);
        } catch (SigningKeyNotFoundException e) {
            return e.getMessage();
        }
        throw new AssertionError("expected SigningKeyNotFoundException");
    }

    @Test
    public void shouldApplyRateLimitToRefreshes() throws Exception {
        server.publish("a", "v1");
        JwkProvider provider = builder().rateLimited(1, 1, TimeUnit.MINUTES).build();

        provider.get("a");
        try {
            provider.get("unknown");
            fail();
        } catch (RateLimitReachedException expected) {
        }
        assertThat(server.fetches(), is(1));
    }

    @Test
    public void shouldPropagateFailuresAndRecover() throws Exception {
        server.publish("a", "v1");
        JwkProvider provider = builder().build();
        provider.get("a");

        server.publish("a", "v1", "b", "v1");
        server.failNext(1);
        try {
            provider.get("b");
            fail();
        } catch (NetworkException expected) {
        }

        // a failed refresh neither evicts cached keys nor is itself cached
        assertThat(provider.get("a").getId(), is("a"));
        assertThat(provider.get("b").getId(), is("b"));
    }

    @Test
    public void shouldNotServeExpiredKeyWhenRefreshFails() throws Exception {
        server.publish("a", "v1");
        JwkProvider provider = builder().build();
        provider.get("a");

        ticker.advance(TTL.plusSeconds(1));
        server.failNext(2);
        for (int i = 0; i < 2; i++) {
            try {
                provider.get("a");
                fail("expired key must not be served, and a failed refresh must not extend its freshness");
            } catch (NetworkException expected) {
            }
        }

        assertThat(provider.get("a").getId(), is("a"));
    }

    @Test
    public void shouldAnswerMissingKidOnlyWhileJwksHasOneKey() throws Exception {
        server.publish("a", "v1");
        JwkProvider provider = builder().build();
        assertThat(provider.get(null).getId(), is("a"));

        server.publish("a", "v1", "b", "v1");
        provider.get("b");
        try {
            provider.get(null);
            fail("a missing kid is ambiguous once the JWKS holds several keys");
        } catch (SigningKeyNotFoundException expected) {
        }
    }

    @Test
    public void shouldResolveDuplicateKidLikeUncachedProvider() throws Exception {
        server.publish("a", "v1", "a", "v2", "b", "v1");
        JwkProvider cached = builder().build();
        JwkProvider uncached = new UrlJwkProvider(url, server);

        cached.get("b");

        assertThat(cached.get("a").getAdditionalAttributes().get("n"),
                is(uncached.get("a").getAdditionalAttributes().get("n")));
    }

    @Test
    public void shouldRejectNegativeExpiry() {
        try {
            builder().cached(5, Duration.ofSeconds(-1)).build();
            fail("a negative expiry must be rejected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void shouldShareOneFetchBetweenConcurrentMisses() throws Exception {
        final int threads = 8;
        server.publish("a", "v1", "b", "v1");
        server.blockFetches();
        final JwkProvider provider = builder().build();

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        try {
            final CountDownLatch ready = new CountDownLatch(threads);
            List<Future<Jwk>> results = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                final String kid = i % 2 == 0 ? "a" : "b";
                results.add(executor.submit(new Callable<Jwk>() {
                    @Override
                    public Jwk call() throws Exception {
                        ready.countDown();
                        return provider.get(kid);
                    }
                }));
            }
            assertTrue(ready.await(5, TimeUnit.SECONDS));
            assertTrue(server.awaitFetchStarted());
            server.releaseFetches();
            for (Future<Jwk> result : results) {
                assertThat(result.get(5, TimeUnit.SECONDS), notNullValue());
            }
        } finally {
            executor.shutdownNow();
        }
        assertThat(server.fetches(), is(1));
    }

    private static class FakeTicker extends Ticker {
        private final AtomicLong nanos = new AtomicLong();

        @Override
        public long read() {
            return nanos.get();
        }

        void advance(Duration duration) {
            nanos.addAndGet(duration.toNanos());
        }
    }

    private static class FakeJwksServer implements JwksHttpClient {
        private final AtomicInteger fetches = new AtomicInteger();
        private final AtomicInteger failures = new AtomicInteger();
        private volatile String body = "{\"keys\":[]}";
        private volatile CountDownLatch release = new CountDownLatch(0);
        private final CountDownLatch started = new CountDownLatch(1);

        void publish(String... kidAndModulus) {
            StringBuilder sb = new StringBuilder("{\"keys\":[");
            for (int i = 0; i < kidAndModulus.length; i += 2) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append("{\"kid\":\"").append(kidAndModulus[i]).append("\",\"kty\":\"RSA\",\"n\":\"")
                        .append(kidAndModulus[i + 1]).append("\"}");
            }
            body = sb.append("]}").toString();
        }

        void failNext(int count) {
            failures.set(count);
        }

        void blockFetches() {
            release = new CountDownLatch(1);
        }

        void releaseFetches() {
            release.countDown();
        }

        boolean awaitFetchStarted() throws InterruptedException {
            return started.await(5, TimeUnit.SECONDS);
        }

        int fetches() {
            return fetches.get();
        }

        @Override
        public JwksHttpResponse fetch(URL url) throws IOException {
            fetches.incrementAndGet();
            started.countDown();
            try {
                if (!release.await(5, TimeUnit.SECONDS)) {
                    throw new IOException("fetch was never released");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException(e);
            }
            if (failures.getAndUpdate(n -> n > 0 ? n - 1 : 0) > 0) {
                throw new IOException("simulated outage");
            }
            return new JwksHttpResponse(body, Collections.<String, List<String>>emptyMap());
        }
    }
}
