package com.auth0.jwk.kyt;

import com.auth0.jwk.InvalidPublicKeyException;
import com.auth0.jwk.Jwk;
import com.google.common.collect.Lists;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.security.spec.ECFieldFp;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.KeySpec;
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
    public void shouldThrowInvalidPublicKeyExceptionOnNonRSAKey() throws Exception {
        final String kid = randomKeyId();
        Jwk jwk = new Rsa(kid, "AES", "AES_256", SIG, null, null, null, null, new HashMap<>());
        expectedException.expect(InvalidPublicKeyException.class);
        expectedException.expectMessage("The key is not of type RSA");
        jwk.getPublicKey();
    }
    
    @Test
    public void shouldThrowInvalidPublicKeyExceptionOnInvalidAlgorithm() throws InvalidPublicKeyException {
    	final String kid = randomKeyId();
        Jwk jwk = new Rsa(kid, "AES", "AES_256", SIG, null, null, null, null, new HashMap<>()) {
        	
        	@Override
        	protected String getKeyType() {
        		return "AES";
        	}
        };
        expectedException.expect(InvalidPublicKeyException.class);
        expectedException.expectMessage("Invalid algorithm to generate key");
        jwk.getPublicKey();
    }
    
    @Test
    public void shouldThrowInvalidPublicKeyExceptionOnIvalidKeySpec() throws InvalidPublicKeyException {
    	Map<String, Object> params = new HashMap<>();
    	params.put("x", "ATevD8HSWBO6pFDCbfMGItGMjylY86MBfVEXZz28L2ju9Hf8YvHoQPXbbu7b0vBEcVRvXWD4S0OBKRhYXFCUY_4v");
    	params.put("y", "AVfn7m0viPg9oKbJWDJQoNzHRbDscs9IQOyzMvIgpzhUagp08dNFgEctjATcvJvkiaausq1FDk6Ly8J1MpWQ4yWe");
    	
    	
    	final String kid = randomKeyId();
        Jwk jwk = new Rsa(kid, RSA, RS_256, SIG, null, null, null, null, params) {
        	
        	@Override
        	protected KeySpec getKeySpecification() throws InvalidPublicKeyException {
        		// EC public key is not compatible with RSA, this will raise InvalidKeySpecException
        		ECNamedCurveParameterSpec parameterSpec = ECNamedCurveTable.getParameterSpec(Curve.P_256.getStandardName());
        		return new ECPublicKeySpec(
                        new ECPoint(
                                new BigInteger(1, Base64.decodeBase64(stringValue("x"))),
                                new BigInteger(1, Base64.decodeBase64(stringValue("y")))),
                        new ECParameterSpec(
                                new java.security.spec.EllipticCurve(
                                        new ECFieldFp(parameterSpec.getCurve().getField().getCharacteristic()),
                                        parameterSpec.getCurve().getA().toBigInteger(),
                                        parameterSpec.getCurve().getB().toBigInteger()),
                                new ECPoint(
                                        parameterSpec.getG().getAffineXCoord().toBigInteger(),
                                        parameterSpec.getG().getAffineYCoord().toBigInteger()),
                                parameterSpec.getN(),
                                1));
        	}
        };
        expectedException.expect(InvalidPublicKeyException.class);
        expectedException.expectMessage("Invalid public key");
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
