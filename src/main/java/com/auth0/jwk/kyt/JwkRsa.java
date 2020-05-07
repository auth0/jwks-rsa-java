package com.auth0.jwk.kyt;

import com.auth0.jwk.InvalidPublicKeyException;
import com.auth0.jwk.Jwk;
import org.apache.commons.codec.binary.Base64;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.List;
import java.util.Map;

/**
 * JWK entry with RSA key stype is implementing the corresponding public key extraction.
 */
public class JwkRsa extends Jwk {

    public JwkRsa(String id, String type, String algorithm, String usage, List<String> operations, String certificateUrl, List<String> certificateChain, String certificateThumbprint, Map<String, Object> additionalAttributes) {
        super(id, type, algorithm, usage, operations, certificateUrl, certificateChain, certificateThumbprint, additionalAttributes);
    }

    @SuppressWarnings("WeakerAccess")
    @Override
    public PublicKey getPublicKey() throws InvalidPublicKeyException {
        if (!PUBLIC_KEY_RSA_ALGORITHM.equalsIgnoreCase(getType())) {
            throw new InvalidPublicKeyException("The key is not of type RSA", null);
        }
        try {
            KeyFactory kf = KeyFactory.getInstance(PUBLIC_KEY_RSA_ALGORITHM);
            BigInteger modulus = new BigInteger(1, Base64.decodeBase64(stringValue("n")));
            BigInteger exponent = new BigInteger(1, Base64.decodeBase64(stringValue("e")));
            return kf.generatePublic(new RSAPublicKeySpec(modulus, exponent));
        } catch (InvalidKeySpecException e) {
            throw new InvalidPublicKeyException("Invalid public key", e);
        } catch (NoSuchAlgorithmException e) {
            throw new InvalidPublicKeyException("Invalid algorithm to generate key", e);
        }
    }

}
