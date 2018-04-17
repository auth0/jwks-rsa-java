package com.auth0.jwk;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URL;

import static com.auth0.jwk.UrlJwkProvider.WELL_KNOWN_JWKS_PATH;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class UrlJwkProviderTest {

    private static final String KID = "NkJCQzIyQzRBMEU4NjhGNUU4MzU4RkY0M0ZDQzkwOUQ0Q0VGNUMwQg";
    private UrlJwkProvider provider;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        provider = new UrlJwkProvider(getClass().getResource("/jwks.json"));
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
    public void shouldReturnSingleJwkById() throws Exception {
        assertThat(provider.get(KID), notNullValue());
    }

    @Test
    public void shouldFailToLoadSingleWhenUrlHasNothing() throws Exception {
        expectedException.expect(SigningKeyNotFoundException.class);
        provider = new UrlJwkProvider(new URL("file:///not_found.file"));
        provider.get(KID);
    }

    @Test
    public void shouldFailToLoadSingleWhenKeysIsEmpty() throws Exception {
        expectedException.expect(SigningKeyNotFoundException.class);
        provider = new UrlJwkProvider(getClass().getResource("/empty-jwks.json"));
        provider.get(KID);
    }


    @Test
    public void shouldFailToLoadSingleWhenJsonIsInvalid() throws Exception {
        expectedException.expect(SigningKeyNotFoundException.class);
        provider = new UrlJwkProvider(getClass().getResource("/invalid-jwks.json"));
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
    public void shouldUseOnlyDomain() {
        String domain = "samples.auth0.com";
        String domainWithSubPath = domain + "/sub/path/";
        String actualJwksUrl = new UrlJwkProvider(domainWithSubPath).url.toString();
        assertThat(actualJwksUrl, equalTo("https://" + domain + WELL_KNOWN_JWKS_PATH));
    }
}