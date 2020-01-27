package com.auth0.jwk;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class JwkSetTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldThrowWhenCreatedWithNullKey() {
        expectedException.expect(IllegalArgumentException.class);
        new JwkSet((Jwk)null);
    }

    @Test
    public void shouldThrowWhenCreatedWithNullList() {
        expectedException.expect(IllegalArgumentException.class);
        new JwkSet((List<Jwk>)null);
    }

    @Test
    public void shouldFindKeyWithId() {
        Jwk jwk1 = getJwk("KID");
        Jwk jwk2 = getJwk("KID2");

        JwkSet jwkSet = new JwkSet(Arrays.asList(jwk1, jwk2));
        assertThat(jwkSet.getKeys().size(), is(2));
        assertThat(jwkSet.getKey("KID"), equalTo(jwk1));
        assertThat(jwkSet.getKey("KID2"), equalTo(jwk2));
        assertThat(jwkSet.getKey("NOPE"), nullValue());
    }

    @Test
    public void shouldReturnNullIfSetIsEmpty() {
        JwkSet jwkSet = new JwkSet();

        assertThat(jwkSet.getKey("KID"), nullValue());
        assertThat(jwkSet.getKeys(), emptyCollectionOf(Jwk.class));
    }

    @Test
    public void shouldReturnNullIfKeyIdIsNull() {
        Jwk jwk = getJwk(null);
        JwkSet jwkSet = new JwkSet(Collections.singletonList(jwk));

        assertThat(jwkSet.getKey(null), nullValue());
    }

    @Test
    public void shouldReturnSingleKeyWhenKidIsNullAndSetContainsSingleKey() {
        Jwk jwk = getJwk("KID");
        JwkSet jwkSet = new JwkSet(jwk);

        Jwk foundKey = jwkSet.getKeyByIdOrSingleEntry(null);
        assertThat(foundKey, equalTo(jwk));
    }

    @Test
    public void shouldReturnNullWhenKidIsNullAndSetIsEmpty() {
        JwkSet jwkSet = new JwkSet();

        Jwk foundKey = jwkSet.getKeyByIdOrSingleEntry(null);
        assertThat(foundKey, nullValue());
    }

    @Test
    public void shouldReturnNullWhenKidIsNullAndSetHasMultipleKeys() {
        Jwk jwk1 = getJwk("KID");
        Jwk jwk2 = getJwk("KID2");

        JwkSet jwkSet = new JwkSet(Arrays.asList(jwk1, jwk2));

        Jwk foundKey = jwkSet.getKeyByIdOrSingleEntry(null);
        assertThat(foundKey, nullValue());
    }

    private Jwk getJwk(String kid) {
        return new Jwk(kid, "type", "alg", "usage", Collections.<String>emptyList(), "certUrl",
                Collections.<String>emptyList(), "certThumbprint", Collections.<String, Object>emptyMap());
    }
}
