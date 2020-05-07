package com.auth0.jwk.kyt;

import com.auth0.jwk.InvalidPublicKeyException;
import com.auth0.jwk.Jwk;
import com.google.common.collect.Lists;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class RsaTest extends  JwkTests {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldBuildWithMap() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = publicRsaKeyValues(kid, KEY_OPS_LIST);
        Jwk jwk = Jwk.fromValues(values);

        assertThat(jwk.getId(), equalTo(kid));
        assertThat(jwk.getAlgorithm(), equalTo(RS_256));
        assertThat(jwk.getType(), equalTo(RSA));
        assertThat(jwk.getUsage(), equalTo(SIG));
        assertThat(jwk.getOperationsAsList(), equalTo(KEY_OPS_LIST));
        assertThat(jwk.getOperations(), is(KEY_OPS_STRING));
        assertThat(jwk.getCertificateThumbprint(), equalTo(THUMBPRINT));
        assertThat(jwk.getCertificateChain(), contains(CERT_CHAIN));
    }

    @Test
    public void shouldReturnPublicKey() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = publicRsaKeyValues(kid, KEY_OPS_LIST);
        Jwk jwk = Jwk.fromValues(values);

        assertThat(jwk.getPublicKey(), notNullValue());
        assertThat(jwk.getOperationsAsList(), is(KEY_OPS_LIST));
        assertThat(jwk.getOperations(), is(KEY_OPS_STRING));
    }

    @Test
    public void shouldReturnPublicKeyForStringKeyOpsParam() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = publicRsaKeyValues(kid, KEY_OPS_STRING);
        Jwk jwk = Jwk.fromValues(values);

        assertThat(jwk.getPublicKey(), notNullValue());
        assertThat(jwk.getOperationsAsList(), is(KEY_OPS_LIST));
        assertThat(jwk.getOperations(), is(KEY_OPS_STRING));
    }

    @Test
    public void shouldReturnPublicKeyForNullKeyOpsParam() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = publicRsaKeyValues(kid, null);
        Jwk jwk = Jwk.fromValues(values);

        assertThat(jwk.getPublicKey(), notNullValue());
        assertThat(jwk.getOperationsAsList(), nullValue());
        assertThat(jwk.getOperations(), nullValue());
    }

    @Test
    public void shouldReturnPublicKeyForEmptyKeyOpsParam() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = publicRsaKeyValues(kid, Lists.newArrayList());
        Jwk jwk = Jwk.fromValues(values);

        assertThat(jwk.getPublicKey(), notNullValue());
        assertThat(jwk.getOperationsAsList(), notNullValue());
        assertThat(jwk.getOperationsAsList().size(), equalTo(0));
        assertThat(jwk.getOperations(), nullValue());
    }

    @Test
    public void shouldThrowForNonRSAKey() throws Exception {
        final String kid = randomKeyId();
        Jwk jwk = new Rsa(kid, "AES", "AES_256", SIG, null, null, null, null, new HashMap<>());
        expectedException.expect(InvalidPublicKeyException.class);
        expectedException.expectMessage("The key is not of type RSA");
        jwk.getPublicKey();
    }
    
    @Test
    public void shouldNotThrowInvalidArgumentExceptionOnMissingKidParam() throws Exception {
        //kid is optional - https://tools.ietf.org/html/rfc7517#section-4.5
        final String kid = randomKeyId();
        Map<String, Object> values = publicRsaKeyValues(kid, KEY_OPS_LIST);
        values.remove("kid");
        Jwk.fromValues(values);
    }

    @Test
    public void shouldThrowInvalidArgumentExceptionOnMissingKtyParam() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = publicRsaKeyValues(kid, KEY_OPS_LIST);
        values.remove("kty");
        expectedException.expect(IllegalArgumentException.class);
        Jwk.fromValues(values);
    }

    @Test
    public void shouldReturnKeyWithMissingAlgParam() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = publicRsaKeyValues(kid, KEY_OPS_LIST);
        values.remove("alg");
        Jwk jwk = Jwk.fromValues(values);
        assertThat(jwk.getPublicKey(), notNullValue());
    }

}
