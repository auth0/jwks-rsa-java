package com.auth0.jwk;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.auth0.jwk.kyt.JwkTests;

public class JwkFactoryTest extends JwkTests {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    
    @Test
    public void shouldProduceRSAJwtWithAllFields() {
        final String kid = randomKeyId();
        Map<String, Object> values = publicKeyValues(kid, KEY_OPS_LIST);
        AbstractJwk jwk = JwkFactory.fromValues(values);

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
    public void shouldProduceRSAJwtWithMandatoryFields() {
        final String kid = randomKeyId();
        Map<String, Object> values = new HashMap<>();
        values.put("kid", kid);
        values.put("kty", RSA);
        
        AbstractJwk jwk = JwkFactory.fromValues(values);

        assertThat(jwk.getId(), equalTo(kid));
        assertThat(jwk.getAlgorithm(), nullValue());
        assertThat(jwk.getType(), equalTo(RSA));
        assertThat(jwk.getUsage(), nullValue());
        assertThat(jwk.getOperationsAsList(), nullValue());
        assertThat(jwk.getOperations(), nullValue());
        assertThat(jwk.getCertificateThumbprint(), nullValue());
        assertThat(jwk.getCertificateChain(), nullValue());
    }
    
    @Test
    public void shouldProduceECJwtWithAllFields() {
        final String kid = randomKeyId();
        Map<String, Object> values = publicECKeyValues(kid, KEY_OPS_STRING);
        AbstractJwk jwk = JwkFactory.fromValues(values);

        assertThat(jwk.getId(), equalTo(kid));
        assertThat(jwk.getAlgorithm(), equalTo(ES256));
        assertThat(jwk.getType(), equalTo(EC));
        assertThat(jwk.getUsage(), equalTo(SIG));
        assertThat(jwk.getOperationsAsList(), equalTo(KEY_OPS_LIST));
        assertThat(jwk.getOperations(), is(KEY_OPS_STRING));
        assertThat(jwk.getCertificateThumbprint(), equalTo(THUMBPRINT));
        assertThat(jwk.getCertificateChain(), contains(CERT_CHAIN));
    }
    
    @Test
    public void shouldProduceECJwtWithMandatoryFields() {
        final String kid = randomKeyId();
        Map<String, Object> values = new HashMap<>();
        values.put("kid", kid);
        values.put("kty", EC);
        values.put("crv", P256);
        values.put("x", ES256_P256_x);
        values.put("y", ES256_P256_Y);
        
        AbstractJwk jwk = JwkFactory.fromValues(values);

        assertThat(jwk.getId(), equalTo(kid));
        assertThat(jwk.getAlgorithm(), nullValue());
        assertThat(jwk.getType(), equalTo(EC));
        assertThat(jwk.getUsage(), nullValue());
        assertThat(jwk.getOperationsAsList(), nullValue());
        assertThat(jwk.getOperations(), nullValue());
        assertThat(jwk.getCertificateThumbprint(), nullValue());
        assertThat(jwk.getCertificateChain(), nullValue());
    }
    
    @Test
    public void shouldThrowIllegalArgumentExceptionOnUnknownKeyType() {
        final String kid = randomKeyId();
        Map<String, Object> values = new HashMap<>();
        values.put("kid", kid);
        values.put("kty", "dummykeytype");
        
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("kty value must be either \"RSA\" or \"EC\". \"dummykeytype\" value found.");
        
        JwkFactory.fromValues(values);
    }
    
    @Test
    public void shouldThrowIllegalArgumentExceptionOnMissingCrv() {
        final String kid = randomKeyId();
        Map<String, Object> values = new HashMap<>();
        values.put("kid", kid);
        values.put("kty", EC);
        values.put("x", ES256_P256_x);
        values.put("y", ES256_P256_Y);
        
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("The key has no curve specification");
        
        JwkFactory.fromValues(values);
    }
    
    @Test
    public void shouldThrowIllegalArgumentExceptionOnMissingX() {
        final String kid = randomKeyId();
        Map<String, Object> values = new HashMap<>();
        values.put("kid", kid);
        values.put("kty", EC);
        values.put("crv", P256);
        values.put("y", ES256_P256_Y);
        
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("The key has no curve specification");
        
        JwkFactory.fromValues(values);
    }
    
    @Test
    public void shouldThrowIllegalArgumentExceptionOnMissingY() {
        final String kid = randomKeyId();
        Map<String, Object> values = new HashMap<>();
        values.put("kid", kid);
        values.put("kty", EC);
        values.put("crv", P256);
        values.put("x", ES256_P256_x);
        
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("The key has no curve specification");
        
        JwkFactory.fromValues(values);
    }
    
}
