package com.auth0.jwk;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.*;
import java.util.Collections;
import java.util.List;

import static com.auth0.jwk.UrlJwkProvider.WELL_KNOWN_JWKS_PATH;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class UrlJwkProviderTest {

    private static final String KID = "NkJCQzIyQzRBMEU4NjhGNUU4MzU4RkY0M0ZDQzkwOUQ0Q0VGNUMwQg";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
    }

    @Test
    public void shouldFailWithNullUrl() {
        expectedException.expect(IllegalArgumentException.class);
        new UrlJwkProvider((URL) null);
    }

    @Test
    public void shouldFailToCreateWithNullDomain() {
        expectedException.expect(IllegalArgumentException.class);
        new UrlJwkProvider((String) null);
    }

    @Test
    public void shouldFailToCreateWithEmptyDomain() {
        expectedException.expect(IllegalArgumentException.class);
        new UrlJwkProvider("");
    }

    @Test
    public void shouldReturnWithoutIdWhenSingleJwk() throws Exception {
        UrlJwkProvider provider = new UrlJwkProvider(getClass().getResource("/jwks-single-no-kid.json"));
        assertThat(provider.get(null), notNullValue());

        UrlJwkProvider provider2 = new UrlJwkProvider(getClass().getResource("/jwks-single.json"));
        assertThat(provider2.get(null), notNullValue());
    }

    @Test
    public void shouldReturnByIdWhenSingleJwk() throws Exception {
        UrlJwkProvider provider = new UrlJwkProvider(getClass().getResource("/jwks-single.json"));
        assertThat(provider.get(KID), notNullValue());
    }

    @Test
    public void shouldReturnSingleJwkById() throws Exception {
        UrlJwkProvider provider = new UrlJwkProvider(getClass().getResource("/jwks.json"));
        assertThat(provider.get(KID), notNullValue());
    }

    @Test
    public void shouldFailToLoadSingleWithoutIdWhenMultipleJwk() throws Exception {
        expectedException.expect(SigningKeyNotFoundException.class);
        UrlJwkProvider provider = new UrlJwkProvider(getClass().getResource("/jwks.json"));
        provider.get(null);
    }

    @Test
    public void shouldFailToLoadByDifferentIdWhenSingleJwk() throws Exception {
        expectedException.expect(SigningKeyNotFoundException.class);
        UrlJwkProvider provider = new UrlJwkProvider(getClass().getResource("/jwks-single-no-kid.json"));
        provider.get("wrong-kid");
    }

    @Test
    public void shouldFailToLoadSingleWhenUrlHasNothing() throws Exception {
        expectedException.expect(SigningKeyNotFoundException.class);
        UrlJwkProvider provider = new UrlJwkProvider(new URL("file:///not_found.file"));
        provider.get(KID);
    }

    @Test
    public void shouldFailToLoadSingleWhenKeysIsEmpty() throws Exception {
        expectedException.expect(SigningKeyNotFoundException.class);
        UrlJwkProvider provider = new UrlJwkProvider(getClass().getResource("/empty-jwks.json"));
        provider.get(KID);
    }

    @Test
    public void shouldFailToLoadSingleWhenJsonIsInvalid() throws Exception {
        expectedException.expect(SigningKeyNotFoundException.class);
        UrlJwkProvider provider = new UrlJwkProvider(getClass().getResource("/invalid-jwks.json"));
        provider.get(KID);
    }

    @Test
    public void shouldBuildCorrectHttpsUrlOnDomain() {
        String domain = "samples.auth0.com";
        String actualJwksUrl = new UrlJwkProvider(domain).url.toString();
        assertThat(actualJwksUrl, equalTo("https://" + domain + WELL_KNOWN_JWKS_PATH));
    }

    @Test
    public void shouldWorkOnDomainWithSlash() {
        String domain = "samples.auth0.com";
        String domainWithSlash = domain + "/";
        String actualJwksUrl = new UrlJwkProvider(domainWithSlash).url.toString();
        assertThat(actualJwksUrl, equalTo("https://" + domain + WELL_KNOWN_JWKS_PATH));
    }

    @Test
    public void shouldBuildCorrectHttpsUrlOnDomainWithHttps() {
        String httpsDomain = "https://samples.auth0.com";
        String actualJwksUrl = new UrlJwkProvider(httpsDomain).url.toString();
        assertThat(actualJwksUrl, equalTo(httpsDomain + WELL_KNOWN_JWKS_PATH));
    }

    @Test
    public void shouldBuildCorrectHttpsUrlOnDomainWithHttpsAndSlash() {
        String httpsDomain = "https://samples.auth0.com";
        String httpsDomainWithSlash = httpsDomain + "/";
        String actualJwksUrl = new UrlJwkProvider(httpsDomainWithSlash).url.toString();
        assertThat(actualJwksUrl, equalTo(httpsDomain + WELL_KNOWN_JWKS_PATH));
    }

    @Test
    public void shouldBuildCorrectHttpUrlOnDomainWithHttp() {
        String httpDomain = "http://samples.auth0.com";
        String actualJwksUrl = new UrlJwkProvider(httpDomain).url.toString();
        assertThat(actualJwksUrl, equalTo(httpDomain + WELL_KNOWN_JWKS_PATH));
    }

    @Test
    public void shouldBuildCorrectHttpUrlOnDomainWithHttpAndSlash() {
        String httpDomain = "http://samples.auth0.com";
        String httpDomainWithSlash = httpDomain + "/";
        String actualJwksUrl = new UrlJwkProvider(httpDomainWithSlash).url.toString();
        assertThat(actualJwksUrl, equalTo(httpDomain + WELL_KNOWN_JWKS_PATH));
    }

    @Test
    public void shouldUseDomainAndPathWithSlashIfPresent() {
        String domain = "samples.auth0.com";
        String domainWithSubPath = domain + "/sub/path/";
        String actualJwksUrl = new UrlJwkProvider(domainWithSubPath).url.toString();
        assertThat(actualJwksUrl, equalTo("https://" + domain + "/sub/path" + WELL_KNOWN_JWKS_PATH));
    }

    @Test
    public void shouldUseDomainAndPathWithoutSlashIfPresent() {
        String domain = "samples.auth0.com";
        String domainWithSubPath = domain + "/sub/path";
        String actualJwksUrl = new UrlJwkProvider(domainWithSubPath).url.toString();
        assertThat(actualJwksUrl, equalTo("https://" + domain + "/sub/path" + WELL_KNOWN_JWKS_PATH));
    }

    @Test
    public void shouldUseDomainAndSinglePathWithSlashIfPresent() {
        String domain = "samples.auth0.com";
        String domainWithSubPath = domain + "/path/";
        String actualJwksUrl = new UrlJwkProvider(domainWithSubPath).url.toString();
        assertThat(actualJwksUrl, equalTo("https://" + domain + "/path" + WELL_KNOWN_JWKS_PATH));
    }

    @Test
    public void shouldUseDomainAndSinglePathWithoutSlashIfPresent() {
        String domain = "samples.auth0.com";
        String domainWithSubPath = domain + "/path";
        String actualJwksUrl = new UrlJwkProvider(domainWithSubPath).url.toString();
        assertThat(actualJwksUrl, equalTo("https://" + domain + "/path" + WELL_KNOWN_JWKS_PATH));
    }

    @Test
    public void shouldFailOnInvalidProtocol() {
        expectedException.expect(IllegalArgumentException.class);
        String domainWithInvalidProtocol = "httptest://samples.auth0.com";
        new UrlJwkProvider(domainWithInvalidProtocol);
    }

    @Test
    public void shouldFailWithNegativeConnectTimeout() throws MalformedURLException {
        expectedException.expect(IllegalArgumentException.class);
        new UrlJwkProvider(new URL("https://localhost"), -1, null);
    }

    @Test
    public void shouldFailWithNegativeReadTimeout() throws MalformedURLException {
        expectedException.expect(IllegalArgumentException.class);
        new UrlJwkProvider(new URL("https://localhost"), null, -1);
    }

    private static class MockURLStreamHandlerFactory implements URLStreamHandlerFactory {

        // The weak reference is just a safeguard against objects not being released
        // for garbage collection
        private final WeakReference<URLConnection> urlConnectionValue;
        public WeakReference<URL> urlUsed;
        public WeakReference<Proxy> proxyUsed;

        public MockURLStreamHandlerFactory(URLConnection urlConnection) {
            this.urlConnectionValue = new WeakReference<>(urlConnection);
            this.urlUsed = new WeakReference<>(null);
            this.proxyUsed = new WeakReference<>(null);
        }

        public void clear() {
            clearUsed();
            this.urlConnectionValue.clear();
        }

        private void clearUsed() {
            this.urlUsed.clear();
            this.proxyUsed.clear();
        }

        private void setUsed(URL u, Proxy p) {
            clearUsed();
            urlUsed = new WeakReference<>(u);
            proxyUsed = new WeakReference<>(p);
        }

        @Override
        public URLStreamHandler createURLStreamHandler(String protocol) {
            return "mock".equals(protocol) ? new URLStreamHandler() {
                @Override
                protected URLConnection openConnection(URL u, Proxy p) throws IOException {
                    setUsed(u, p);
                    return urlConnectionValue.get();
                }

                @Override
                protected URLConnection openConnection(URL u) throws IOException {
                    setUsed(u, null);
                    return urlConnectionValue.get();
                }
            } : null;
        }
    }

    @Test
    public void shouldConfigureURLConnection() throws Exception {
        URLConnection urlConnection = mock(URLConnection.class);
        when(urlConnection.getInputStream()).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return getClass().getResourceAsStream("/jwks.json");
            }
        });

        // Although somewhat of a hack, this approach gets the job done - this method can 
        // only be called once per virtual machine, but that is sufficient for now.
        MockURLStreamHandlerFactory mockFactory = new MockURLStreamHandlerFactory(urlConnection);
        URL.setURLStreamHandlerFactory(mockFactory);

        URL url = new URL("mock://localhost");
        int connectTimeout = 10000;
        int readTimeout = 15000;

        //Test creation: without Proxy
        UrlJwkProvider urlJwkProvider = new UrlJwkProvider(url, connectTimeout, readTimeout);
        assertThat(urlJwkProvider.proxy, is(nullValue()));

        Jwk jwk = urlJwkProvider.get("NkJCQzIyQzRBMEU4NjhGNUU4MzU4RkY0M0ZDQzkwOUQ0Q0VGNUMwQg");
        assertNotNull(jwk);
        assertThat(mockFactory.urlUsed.get(), is(url));
        assertThat(mockFactory.proxyUsed.get(), is(nullValue()));

        // Test creation: custom headers
        UrlJwkProvider urlJwkProviderWithHeaders = new UrlJwkProvider(url, connectTimeout, readTimeout, null,
            Collections.singletonMap("Accept", "application/jwks-set+json"));
        Jwk hJwk = urlJwkProviderWithHeaders.get("NkJCQzIyQzRBMEU4NjhGNUU4MzU4RkY0M0ZDQzkwOUQ0Q0VGNUMwQg");
        assertNotNull(hJwk);
        assertThat(mockFactory.urlUsed.get(), is(url));
        assertThat(mockFactory.proxyUsed.get(), is(nullValue()));

        //Test creation: with Proxy
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8080));
        URL pUrl = new URL("mock://localhost");
        UrlJwkProvider pUrlJwkProvider = new UrlJwkProvider(pUrl, connectTimeout, readTimeout, proxy);
        assertThat(pUrlJwkProvider.proxy, is(proxy));

        Jwk pJwk = pUrlJwkProvider.get("NkJCQzIyQzRBMEU4NjhGNUU4MzU4RkY0M0ZDQzkwOUQ0Q0VGNUMwQg");
        assertNotNull(pJwk);
        assertThat(mockFactory.urlUsed.get(), is(pUrl));
        Proxy usedProxy = mockFactory.proxyUsed.get();
        assertThat(usedProxy, is(notNullValue()));
        assertThat(usedProxy.address(), is(proxy.address()));

        //Test 1: Configuration
        //Request Timeout assertions
        ArgumentCaptor<Integer> connectTimeoutCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(urlConnection, times(3)).setConnectTimeout(connectTimeoutCaptor.capture());
        assertThat(connectTimeoutCaptor.getValue(), is(connectTimeout));

        ArgumentCaptor<Integer> readTimeoutCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(urlConnection, times(3)).setReadTimeout(readTimeoutCaptor.capture());
        assertThat(readTimeoutCaptor.getValue(), is(readTimeout));

        //Request Headers assertions
        verify(urlConnection, times(2)).setRequestProperty("Accept", "application/json");
        verify(urlConnection, times(1)).setRequestProperty("Accept", "application/jwks-set+json");

        //Test 2: Network errors
        Exception capturedException = null;
        try {
            IOException exception = mock(IOException.class);
            when(urlConnection.getInputStream()).thenThrow(exception);
            urlJwkProvider.get("NkJCQzIyQzRBMEU4NjhGNUU4MzU4RkY0M0ZDQzkwOUQ0Q0VGNUMwQg");
        } catch (Exception e) {
            capturedException = e;
        }

        assertThat(capturedException, is(notNullValue()));
        assertThat(capturedException, is(instanceOf(NetworkException.class)));

        //release
        mockFactory.clear();
    }
}