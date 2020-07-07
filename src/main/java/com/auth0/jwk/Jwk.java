package com.auth0.jwk;

import com.auth0.jwk.kyt.EllipticCurve;
import com.auth0.jwk.kyt.Rsa;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.*;

/**
 * Represents a JSON Web Key (JWK) used to verify the signature of JWTs
 */
@SuppressWarnings("WeakerAccess")
public abstract class Jwk {
    protected static final String PUBLIC_KEY_RSA_ALGORITHM = "RSA";
    protected static final String PUBLIC_KEY_EC_ALGORITHM = "EC";

    private final String id;
    private final String type;
    private final String algorithm;
    private final String usage;
    private final List<String> operations;
    private final String certificateUrl;
    private final List<String> certificateChain;
    private final String certificateThumbprint;
    private final Map<String, Object> additionalAttributes;

    /**
     * Creates a new Jwk
     *
     * @param id                    kid
     * @param type                  kyt
     * @param algorithm             alg
     * @param usage                 use
     * @param operations            key_ops
     * @param certificateUrl        x5u
     * @param certificateChain      x5c
     * @param certificateThumbprint x5t
     * @param additionalAttributes  additional attributes not part of the standard ones
     */
    @SuppressWarnings("WeakerAccess")
    protected Jwk(String id, String type, String algorithm, String usage, List<String> operations, String certificateUrl, List<String> certificateChain, String certificateThumbprint, Map<String, Object> additionalAttributes) {
        this.id = id;
        this.type = type;
        this.algorithm = algorithm;
        this.usage = usage;
        this.operations = operations;
        this.certificateUrl = certificateUrl;
        this.certificateChain = certificateChain;
        this.certificateThumbprint = certificateThumbprint;
        this.additionalAttributes = additionalAttributes;
    }

    @SuppressWarnings("unchecked")
    public static Jwk fromValues(Map<String, Object> map) {
        Map<String, Object> values = Maps.newHashMap(map);
        String kid = (String) values.remove("kid");
        String kty = (String) values.remove("kty");
        String alg = (String) values.remove("alg");
        String use = (String) values.remove("use");
        Object keyOps = values.remove("key_ops");
        List<String> keyOpsList = keyOps instanceof String ? Collections.singletonList((String) keyOps) : (List<String>) keyOps;
        String x5u = (String) values.remove("x5u");
        List<String> x5c = (List<String>) values.remove("x5c");
        String x5t = (String) values.remove("x5t");
        if (kty == null) {
            throw new IllegalArgumentException("Attributes " + map + " are not from a valid jwk");
        }
        if (PUBLIC_KEY_RSA_ALGORITHM.equals(kty)) {
            return new Rsa(kid, kty, alg, use, keyOpsList, x5u, x5c, x5t, values);
        } else if (PUBLIC_KEY_EC_ALGORITHM.equals(kty)) {
            return new EllipticCurve(kid, kty, alg, use, keyOpsList, x5u, x5c, x5t, values);
        }
        throw new IllegalArgumentException("Unsupported key type: " + kty);
    }

    @SuppressWarnings("WeakerAccess")
    public String getId() {
        return id;
    }

    @SuppressWarnings("WeakerAccess")
    public String getType() {
        return type;
    }

    @SuppressWarnings("WeakerAccess")
    public String getAlgorithm() {
        return algorithm;
    }

    @SuppressWarnings("WeakerAccess")
    public String getUsage() {
        return usage;
    }

    @SuppressWarnings("WeakerAccess")
    public String getOperations() {
        if (operations == null || operations.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        String delimiter = ",";
        for (String operation : operations) {
            sb.append(operation);
            sb.append(delimiter);
        }
        String ops = sb.toString();
        return ops.substring(0, ops.length() - delimiter.length());
    }

    @SuppressWarnings("WeakerAccess")
    public List<String> getOperationsAsList() {
        return operations;
    }

    @SuppressWarnings("WeakerAccess")
    public String getCertificateUrl() {
        return certificateUrl;
    }

    @SuppressWarnings("WeakerAccess")
    public List<String> getCertificateChain() {
        return certificateChain;
    }

    @SuppressWarnings("WeakerAccess")
    public String getCertificateThumbprint() {
        return certificateThumbprint;
    }

    public Map<String, Object> getAdditionalAttributes() {
        return additionalAttributes;
    }

    /**
     * Returns a {@link PublicKey} if the {@code 'alg'} is {@code 'RSA'}
     *
     * @return a public key
     * @throws InvalidPublicKeyException if the key cannot be built or the key type is not RSA
     */
    @SuppressWarnings("WeakerAccess")
    public PublicKey getPublicKey() throws InvalidPublicKeyException {
        if (!getKeyType().equalsIgnoreCase(getType())) {
            throw new InvalidPublicKeyException("The key is not of type " + getKeyType());
        }
        try {
            KeyFactory kf = KeyFactory.getInstance(getType());
            return kf.generatePublic(getKeySpecification());
        } catch (InvalidKeySpecException e) {
            throw new InvalidPublicKeyException("Invalid public key", e);
        } catch (NoSuchAlgorithmException e) {
            throw new InvalidPublicKeyException("Invalid algorithm to generate key", e);
        }
    }

    /**
     * Returns the name of the key type of the current JWT entry.
     * @return key type name
     */
    protected abstract String getKeyType();

    /**
     * Returns the Java key specification of the key specified by the current JWT entry.
     * @return key specification if the key specification cannot be created because some parameter was missing
     * @throws InvalidPublicKeyException
     */
    protected abstract KeySpec getKeySpecification() throws InvalidPublicKeyException;

    protected String stringValue(String key) {
        return (String) additionalAttributes.get(key);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("kid", id)
                .add("kyt", type)
                .add("alg", algorithm)
                .add("use", usage)
                .add("extras", additionalAttributes)
                .toString();
    }
}
