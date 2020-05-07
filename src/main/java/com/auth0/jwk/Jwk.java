package com.auth0.jwk;

import com.auth0.jwk.kyt.JwkRsa;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;

import java.security.PublicKey;
import java.util.*;

/**
 * Represents a JSON Web Key (JWK) used to verify the signature of JWTs
 */
@SuppressWarnings("WeakerAccess")
public abstract class Jwk {
    protected static final String PUBLIC_KEY_RSA_ALGORITHM = "RSA";

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
    public Jwk(String id, String type, String algorithm, String usage, List<String> operations, String certificateUrl, List<String> certificateChain, String certificateThumbprint, Map<String, Object> additionalAttributes) {
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

    /**
     * Creates a new Jwk
     *
     * @param id
     * @param type
     * @param algorithm
     * @param usage
     * @param operations
     * @param certificateUrl
     * @param certificateChain
     * @param certificateThumbprint
     * @param additionalAttributes
     * @deprecated The specification states that the 'key_ops' (operations) parameter contains an array value.
     * Use {@link #Jwk(String, String, String, String, List, String, List, String, Map)}
     */
    @Deprecated
    @SuppressWarnings("WeakerAccess")
    public Jwk(String id, String type, String algorithm, String usage, String operations, String certificateUrl, List<String> certificateChain, String certificateThumbprint, Map<String, Object> additionalAttributes) {
        this(id, type, algorithm, usage, Collections.singletonList(operations), certificateUrl, certificateChain, certificateThumbprint, additionalAttributes);
    }

    @SuppressWarnings("unchecked")
    public static Jwk fromValues(Map<String, Object> map) {
        Map<String, Object> values = Maps.newHashMap(map);
        String kid = (String) values.remove("kid");
        String kty = (String) values.remove("kty");
        String alg = (String) values.remove("alg");
        String use = (String) values.remove("use");
        Object keyOps = values.remove("key_ops");
        String x5u = (String) values.remove("x5u");
        List<String> x5c = (List<String>) values.remove("x5c");
        String x5t = (String) values.remove("x5t");
        if (kty == null) {
            throw new IllegalArgumentException("Attributes " + map + " are not from a valid jwk");
        }
        if (keyOps instanceof String) {
            return new JwkRsa(kid, kty, alg, use, Arrays.asList((String) keyOps), x5u, x5c, x5t, values);
        } else {
            return new JwkRsa(kid, kty, alg, use, (List<String>) keyOps, x5u, x5c, x5t, values);
        }
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
    public abstract PublicKey getPublicKey() throws InvalidPublicKeyException;

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
