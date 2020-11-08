package com.auth0.jwk;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.List;
import java.util.Map;

import com.google.common.base.MoreObjects;

public abstract class AbstractJwk {

    public static final String PUBLIC_KEY_RSA_ALGORITHM = "RSA";
    public static final String PUBLIC_KEY_EC_ALGORITHM = "EC";
    
    protected final String id;
    protected final String type;
    protected final String algorithm;
    protected final String usage;
    protected final List<String> operations;
    protected final String certificateUrl;
    protected final List<String> certificateChain;
    protected final String certificateThumbprint;
    protected final Map<String, Object> additionalAttributes;
    
    /**
     * Creates a new AbstractJwk
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
    public AbstractJwk(String id, String type, String algorithm, String usage, List<String> operations, String certificateUrl, List<String> certificateChain, String certificateThumbprint, Map<String, Object> additionalAttributes) {
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
    
    protected String stringValue(String key) {
        return (String) additionalAttributes.get(key);
    }
    
    protected abstract String getKeyType();
    
    protected abstract KeySpec getKeySpecification() throws InvalidPublicKeyException;
    
    /**
     * Returns a {@link PublicKey} if the {@code 'alg'} is {@code 'RSA'}
     *
     * @return a public key
     * @throws InvalidPublicKeyException if the key cannot be built or the key type is not RSA
     */
    @SuppressWarnings("WeakerAccess")
    public PublicKey getPublicKey() throws InvalidPublicKeyException {
        if (!getKeyType().equalsIgnoreCase(type)) {
            throw new InvalidPublicKeyException(String.format("The key is not of type %s", getKeyType()), null);
        }
        try {
            KeyFactory kf = KeyFactory.getInstance(getKeyType());
            return kf.generatePublic(getKeySpecification());
        } catch (InvalidKeySpecException e) {
            throw new InvalidPublicKeyException("Invalid public key", e);
        } catch (NoSuchAlgorithmException e) {
            throw new InvalidPublicKeyException("Invalid algorithm to generate key", e);
        }
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
