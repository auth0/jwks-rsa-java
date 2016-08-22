package com.auth0.jwk.provider;

import com.auth0.jwk.SigningKeyNotFoundException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URL;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class UrlJwkProviderTest {

    private static final String KID = "NkJCQzIyQzRBMEU4NjhGNUU4MzU4RkY0M0ZDQzkwOUQ0Q0VGNUMwQg";
    private UrlJwkProvider provider;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        provider = new UrlJwkProvider(getClass().getResource("/jwks.json"));
    }

    @Test
    public void shouldFailWithNullUrl() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        new UrlJwkProvider((URL) null);
    }

    @Test
    public void shouldBuildCorrectUrl() throws Exception {
        assertThat(new UrlJwkProvider("https://samples.auth0.com").url.toString(), endsWith("/.well-known/jwks.json"));
    }

    @Test
    public void shouldFailToCreateWithInvalidDomain() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        new UrlJwkProvider("not https");
    }

    @Test
    public void shouldFailToCreateWithNullDomain() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        new UrlJwkProvider((String)null);
    }

    @Test
    public void shouldFailToCreateWithEmptyDomain() throws Exception {
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

}