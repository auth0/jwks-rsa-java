package com.auth0.jwk;

import java.math.BigInteger;
import java.security.spec.KeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.auth0.jwk.kyt.EllipticCurve;
import com.auth0.jwk.kyt.JwkTests;

public class AbstractJwkTest extends JwkTests{

	@Rule
    public ExpectedException expectedException = ExpectedException.none();
	
	@Test
    public void shouldThrowNoSuchAlgorithmExceptionOnNonEsitingKeyType() throws InvalidPublicKeyException {
    	final String kid = randomKeyId();
        AbstractJwk jwk = new AbstractJwk(kid, "dummykeytype", null, null, null, null, null, null, null) {
			
			@Override
			protected String getKeyType() {
				return type;
			}
			
			@Override
			protected KeySpec getKeySpecification() throws InvalidPublicKeyException {
				return null;
			}
		};
		
		expectedException.expect(InvalidPublicKeyException.class);
        expectedException.expectMessage("Invalid algorithm to generate key");
        jwk.getPublicKey();
    }
	
	@Test
	public void shouldThrowInvalidKeySpecExceptionOnWrongKeySpecification() throws InvalidPublicKeyException {
		final String kid = randomKeyId();
		Map<String, Object> values = new HashMap<>();
		values.put("crv", P256);
		values.put("x", ES256_P256_x);
        values.put("y", ES256_P256_Y);
		values.put("n", MODULUS);
        values.put("e", EXPONENT);
		AbstractJwk jwk = new EllipticCurve(kid, ES256, SIG, null, null, null, null, values) {
			@Override
			protected KeySpec getKeySpecification() throws InvalidPublicKeyException {
				BigInteger modulus = new BigInteger(1, Base64.decodeBase64(stringValue("n")));
		        BigInteger exponent = new BigInteger(1, Base64.decodeBase64(stringValue("e")));
		    	return new RSAPublicKeySpec(modulus, exponent);
			}
		};
		
		expectedException.expect(InvalidPublicKeyException.class);
        expectedException.expectMessage("Invalid public key");
        jwk.getPublicKey();
	}
	
}
