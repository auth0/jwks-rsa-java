package com.auth0.jwk.kyt;

import com.auth0.jwk.InvalidPublicKeyException;
import com.auth0.jwk.Jwk;
import org.apache.commons.codec.binary.Base64;

import java.math.BigInteger;
import java.security.spec.KeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.List;
import java.util.Map;

/**
 * JWK entry with RSA key stype is implementing the corresponding public key extraction.
 */
public class Rsa extends Jwk {

    public Rsa(String id, String type, String algorithm, String usage, List<String> operations, String certificateUrl, List<String> certificateChain, String certificateThumbprint, Map<String, Object> additionalAttributes) {
        super(id, type, algorithm, usage, operations, certificateUrl, certificateChain, certificateThumbprint, additionalAttributes);
    }

    @Override
    protected String getKeyType() {
        return PUBLIC_KEY_RSA_ALGORITHM;
    }

    @Override
    protected KeySpec getKeySpecification() throws InvalidPublicKeyException {
        BigInteger modulus = new BigInteger(1, Base64.decodeBase64(stringValue("n")));
        BigInteger exponent = new BigInteger(1, Base64.decodeBase64(stringValue("e")));
        return new RSAPublicKeySpec(modulus, exponent);
    }

}
