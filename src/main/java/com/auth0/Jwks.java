package com.auth0;

import org.apache.commons.codec.binary.Base64;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;


public class Jwks {
    private final String PUBLIC_KEY_ALGORITHM = "RSA";

    private final String algorithm;
    private final String id;
    private final String publicKey;
    private final String exp;

    public Jwks(String alg, String kid, String publicKey, String exp) {
        this.algorithm = alg;
        this.id = kid;
        this.publicKey = publicKey;
        this.exp = exp;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "{id: " + id + " }";
    }

    public PublicKey getPublicKey() throws InvalidPublicKeyException {
        try {
            KeyFactory kf = KeyFactory.getInstance(PUBLIC_KEY_ALGORITHM);
            BigInteger modulus = new BigInteger(1, Base64.decodeBase64(publicKey));
            BigInteger exponent = new BigInteger(1, Base64.decodeBase64(exp));
            return kf.generatePublic(new RSAPublicKeySpec(modulus, exponent));
        } catch (InvalidKeySpecException e) {
            throw new InvalidPublicKeyException("Invalid public key", e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Invalid algorithm to generate key. This should never happen", e);
        }
    }
}
