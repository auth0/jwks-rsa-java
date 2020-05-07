package com.auth0.jwk.kyt;

import com.auth0.jwk.Jwk;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class JwkTest extends JwkTests {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldReturnRsaJwk() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = publicRsaKeyValues(kid, KEY_OPS_LIST);
        Jwk jwk = Jwk.fromValues(values);

        assertThat(jwk, instanceOf(Rsa.class));
        assertThat(jwk.getId(), equalTo(kid));
    }

    @Test
    public void shouldReturnEcJwk() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = publicEcKeyValues(kid);
        Jwk jwk = Jwk.fromValues(values);

        assertThat(jwk, instanceOf(EllipticCurve.class));
        assertThat(jwk.getId(), equalTo(kid));
    }

    @Test
    public void shouldThrowForUnknownKey() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = unkownKeyValues(kid);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Unsupported key type: AES");

        Jwk.fromValues(values);
    }

}
