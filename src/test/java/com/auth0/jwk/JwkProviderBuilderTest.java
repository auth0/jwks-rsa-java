package com.auth0.jwk;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;

public class JwkProviderBuilderTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldCreateForDomain() throws Exception {
        assertThat(new JwkProviderBuilder().forDomain("samples.auth0.com").build(), notNullValue());
    }

    @Test
    public void shouldCreateForHttpUrl() throws Exception {
        assertThat(new JwkProviderBuilder().forDomain("https://samples.auth0.com").build(), notNullValue());
    }

    @Test
    public void shouldFailWhenNoUrlIsProvided() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Cannot build provider without domain");
        new JwkProviderBuilder().build();
    }

    @Test
    public void shouldFailWhenOnlySpecifyingCache() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Cannot build provider without domain");
        new JwkProviderBuilder().cached(false).build();
    }

    @Test
    public void shouldCreateCachedProvider() throws Exception {
        assertThat(new JwkProviderBuilder().cached(true).forDomain("samples.auth0.com").build(), notNullValue());
    }

    @Test
    public void shouldCreateCachedProviderWithCustomValues() throws Exception {
        assertThat(new JwkProviderBuilder().cached(10, 24, TimeUnit.HOURS).forDomain("samples.auth0.com").build(), notNullValue());
    }

}