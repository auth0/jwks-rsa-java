package com.auth0.jwk;

import org.junit.Test;

import static com.auth0.jwk.JwkUrlFactory.WELL_KNOWN_JWKS_PATH;
import static com.auth0.jwk.JwkUrlFactory.forNormalizedDomain;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class JwkUrlFactoryTest {

    @Test
    public void shouldNotBeNull() {
        String domain = "https://samples.auth0.com";
        assertThat(forNormalizedDomain(domain), notNullValue());
    }

    @Test
    public void shouldWorkOnNormalizedDomain() {
        String domain = "https://samples.auth0.com";
        assertThat(forNormalizedDomain(domain).toString(), equalTo(domain + WELL_KNOWN_JWKS_PATH));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailOnNonNormalizedDomain() {
        String domain = "samples.auth0.com";
        forNormalizedDomain(domain);
    }

    @Test
    public void shouldWorkOnNormalizedDomainWithSlash() {
        String domain = "https://samples.auth0.com";
        String domainWithSlash = domain + "/";
        assertThat(forNormalizedDomain(domainWithSlash).toString(), equalTo(domain + WELL_KNOWN_JWKS_PATH));
    }

    @Test
    public void shouldUseOnlyDomain() {
        String domain = "https://samples.auth0.com";
        String domainWithSubPath = domain + "/sub/path";
        assertThat(forNormalizedDomain(domainWithSubPath).toString(), equalTo(domain + WELL_KNOWN_JWKS_PATH));
    }
}