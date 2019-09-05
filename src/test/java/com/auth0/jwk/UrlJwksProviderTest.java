package com.auth0.jwk;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.*;

import static com.auth0.jwk.UrlJwksProvider.WELL_KNOWN_JWKS_PATH;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class UrlJwksProviderTest {

    private static final String KID = "NkJCQzIyQzRBMEU4NjhGNUU4MzU4RkY0M0ZDQzkwOUQ0Q0VGNUMwQg";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
    }

    @Test
    public void shouldFailWithNullUrl() {
        expectedException.expect(IllegalArgumentException.class);
        new UrlJwksProvider((URL) null);
    }

    @Test
    public void shouldFailToCreateWithNullDomain() {
        expectedException.expect(IllegalArgumentException.class);
        new UrlJwksProvider((String) null);
    }

    @Test
    public void shouldFailToCreateWithEmptyDomain() {
        expectedException.expect(IllegalArgumentException.class);
        new UrlJwksProvider("");
    }

    @Test
    public void shouldReturnWithoutIdWhenSingleJwk() throws Exception {
        JwkProvider provider = new DefaultJwkProvider(new UrlJwksProvider(getClass().getResource("/jwks-single-no-kid.json")));
        assertThat(provider.getJwk(null), notNullValue());

        // TODO why was this here?
        // JwkProvider provider2 = new DefaultJwkProvider(new UrlJwksProvider(getClass().getResource("/jwks-single.json")));
        // assertThat(provider2.get(null), notNullValue());
    }

    @Test
    public void shouldReturnByIdWhenSingleJwk() throws Exception {
        JwkProvider provider = new DefaultJwkProvider(new UrlJwksProvider(getClass().getResource("/jwks-single.json")));
        assertThat(provider.getJwk(KID), notNullValue());
    }

    @Test
    public void shouldReturnSingleJwkById() throws Exception {
        JwkProvider provider = new DefaultJwkProvider(new UrlJwksProvider(getClass().getResource("/jwks.json")));
        assertThat(provider.getJwk(KID), notNullValue());
    }

    @Test
    public void shouldFailToLoadSingleWithoutIdWhenMultipleJwk() throws Exception {
        expectedException.expect(SigningKeyNotFoundException.class);
        JwkProvider provider = new DefaultJwkProvider(new UrlJwksProvider(getClass().getResource("/jwks.json")));
        provider.getJwk(null);
    }

    @Test
    public void shouldFailToLoadByDifferentIdWhenSingleJwk() throws Exception {
        expectedException.expect(SigningKeyNotFoundException.class);
        JwkProvider provider = new DefaultJwkProvider(new UrlJwksProvider(getClass().getResource("/jwks-single-no-kid.json")));
        provider.getJwk("wrong-kid");
    }

    @Test
    public void shouldFailToLoadSingleWhenUrlHasNothing() throws Exception {
        expectedException.expect(SigningKeyUnavailableException.class);
        JwkProvider provider = new DefaultJwkProvider(new UrlJwksProvider(new URL("file:///not_found.file")));
        provider.getJwk(KID);
    }

    @Test
    public void shouldFailToLoadSingleWhenKeysIsEmpty() throws Exception {
        expectedException.expect(SigningKeyNotFoundException.class);
        JwkProvider provider = new DefaultJwkProvider(new UrlJwksProvider(getClass().getResource("/empty-jwks.json")));
        provider.getJwk(KID);
    }

    @Test
    public void shouldFailToLoadSingleWhenJsonIsInvalid() throws Exception {
        expectedException.expect(SigningKeyNotFoundException.class);
        JwkProvider provider = new DefaultJwkProvider(new UrlJwksProvider(getClass().getResource("/invalid-jwks.json")));
        provider.getJwk(KID);
    }

    @Test
    public void shouldBuildCorrectHttpsUrlOnDomain() {
        String domain = "samples.auth0.com";
        String actualJwksUrl = new UrlJwksProvider(domain).url.toString();
        assertThat(actualJwksUrl, equalTo("https://" + domain + WELL_KNOWN_JWKS_PATH));
    }

    @Test
    public void shouldWorkOnDomainWithSlash() {
        String domain = "samples.auth0.com";
        String domainWithSlash = domain + "/";
        String actualJwksUrl = new UrlJwksProvider(domainWithSlash).url.toString();
        assertThat(actualJwksUrl, equalTo("https://" + domain + WELL_KNOWN_JWKS_PATH));
    }

    @Test
    public void shouldBuildCorrectHttpsUrlOnDomainWithHttps() {
        String httpsDomain = "https://samples.auth0.com";
        String actualJwksUrl = new UrlJwksProvider(httpsDomain).url.toString();
        assertThat(actualJwksUrl, equalTo(httpsDomain + WELL_KNOWN_JWKS_PATH));
    }

    @Test
    public void shouldBuildCorrectHttpsUrlOnDomainWithHttpsAndSlash() {
        String httpsDomain = "https://samples.auth0.com";
        String httpsDomainWithSlash = httpsDomain + "/";
        String actualJwksUrl = new UrlJwksProvider(httpsDomainWithSlash).url.toString();
        assertThat(actualJwksUrl, equalTo(httpsDomain + WELL_KNOWN_JWKS_PATH));
    }

    @Test
    public void shouldBuildCorrectHttpUrlOnDomainWithHttp() {
        String httpDomain = "http://samples.auth0.com";
        String actualJwksUrl = new UrlJwksProvider(httpDomain).url.toString();
        assertThat(actualJwksUrl, equalTo(httpDomain + WELL_KNOWN_JWKS_PATH));
    }

    @Test
    public void shouldBuildCorrectHttpUrlOnDomainWithHttpAndSlash() {
        String httpDomain = "http://samples.auth0.com";
        String httpDomainWithSlash = httpDomain + "/";
        String actualJwksUrl = new UrlJwksProvider(httpDomainWithSlash).url.toString();
        assertThat(actualJwksUrl, equalTo(httpDomain + WELL_KNOWN_JWKS_PATH));
    }

    @Test
    public void shouldUseOnlyDomain() {
        String domain = "samples.auth0.com";
        String domainWithSubPath = domain + "/sub/path/";
        String actualJwksUrl = new UrlJwksProvider(domainWithSubPath).url.toString();
        assertThat(actualJwksUrl, equalTo("https://" + domain + WELL_KNOWN_JWKS_PATH));
    }

    @Test
    public void shouldFailOnInvalidProtocol() {
        expectedException.expect(IllegalArgumentException.class);
        String domainWithInvalidProtocol = "httptest://samples.auth0.com";
        new UrlJwksProvider(domainWithInvalidProtocol);
    }

    @Test
    public void shouldFailWithNegativeConnectTimeout() throws MalformedURLException {
        expectedException.expect(IllegalArgumentException.class);
        new UrlJwksProvider(new URL("https://localhost"), -1, null);
    }

    @Test
    public void shouldFailWithNegativeReadTimeout() throws MalformedURLException {
        expectedException.expect(IllegalArgumentException.class);
        new UrlJwksProvider(new URL("https://localhost"), null, -1);
    }

    private static class MockURLStreamHandlerFactory implements URLStreamHandlerFactory {

        // The weak reference is just a safeguard against objects not being released
        // for garbage collection
        private final WeakReference<URLConnection> value;

        public MockURLStreamHandlerFactory(URLConnection urlConnection) {
            this.value = new WeakReference<URLConnection>(urlConnection);
        }

        @Override
        public URLStreamHandler createURLStreamHandler(String protocol) {
            return "mock".equals(protocol) ? new URLStreamHandler() {
                protected URLConnection openConnection(URL url) throws IOException {
                    try {
                        return value.get();
                    } finally {
                        value.clear();
                    }
                }
            } : null;
        }
    }

    @Test
    public void shouldConfigureURLConnection() throws Exception {
        URLConnection urlConnection = mock(URLConnection.class);

        // Although somewhat of a hack, this approach gets the job done - this method can 
        // only be called once per virtual machine, but that is sufficient for now.
        URL.setURLStreamHandlerFactory(new MockURLStreamHandlerFactory(urlConnection));
        when(urlConnection.getInputStream()).thenReturn(getClass().getResourceAsStream("/jwks.json"));

        int connectTimeout = 10000;
        int readTimeout = 15000;

        JwkProvider urlJwkProvider = new DefaultJwkProvider(new UrlJwksProvider(new URL("mock://localhost"), connectTimeout, readTimeout));
        Jwk jwk = urlJwkProvider.getJwk("NkJCQzIyQzRBMEU4NjhGNUU4MzU4RkY0M0ZDQzkwOUQ0Q0VGNUMwQg");
        assertNotNull(jwk);

        //Request Timeout assertions
        ArgumentCaptor<Integer> connectTimeoutCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(urlConnection).setConnectTimeout(connectTimeoutCaptor.capture());
        assertThat(connectTimeoutCaptor.getValue(), is(connectTimeout));

        ArgumentCaptor<Integer> readTimeoutCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(urlConnection).setReadTimeout(readTimeoutCaptor.capture());
        assertThat(readTimeoutCaptor.getValue(), is(readTimeout));

        //Request Headers assertions
        verify(urlConnection).setRequestProperty("Accept", "application/json");
    }
}
