package com.auth0.jwk;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;

import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represents a JSON Web Key (JWK) used to verify the signature of JWTs
 */
@SuppressWarnings("WeakerAccess")
public class Jwk {
    private static final String ALGORITHM_RSA = "RSA";
    private static final String ALGORITHM_ELLIPTIC_CURVE = "EC";
    private static final String ELLIPTIC_CURVE_TYPE_P256 = "P-256";
    private static final String ELLIPTIC_CURVE_TYPE_P384 = "P-384";
    private static final String ELLIPTIC_CURVE_TYPE_P521 = "P-521";

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
     * @param type                  kty
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
            return new Jwk(kid, kty, alg, use, (String) keyOps, x5u, x5c, x5t, values);
        } else {
            return new Jwk(kid, kty, alg, use, (List<String>) keyOps, x5u, x5c, x5t, values);
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
     * Returns a {@link PublicKey} if the {@code 'alg'} is {@code 'RSA'} or {@code 'EC'}
     *
     * @return a public key
     * @throws InvalidPublicKeyException if the key cannot be built or the key type is not a supported type of RSA or EC
     */
    @SuppressWarnings("WeakerAccess")
    public PublicKey getPublicKey() throws InvalidPublicKeyException {
        PublicKey publicKey = null;

        switch (type) {
            case ALGORITHM_RSA:
                try {
                    KeyFactory kf = KeyFactory.getInstance(ALGORITHM_RSA);
                    BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode(stringValue("n")));
                    BigInteger exponent = new BigInteger(1, Base64.getUrlDecoder().decode(stringValue("e")));
                    publicKey = kf.generatePublic(new RSAPublicKeySpec(modulus, exponent));
                } catch (InvalidKeySpecException e) {
                    throw new InvalidPublicKeyException("Invalid public key", e);
                } catch (NoSuchAlgorithmException e) {
                    throw new InvalidPublicKeyException("Invalid algorithm to generate key", e);
                }
                break;

            case ALGORITHM_ELLIPTIC_CURVE:
                try {
                    KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_ELLIPTIC_CURVE);
                    ECPoint ecPoint = new ECPoint(new BigInteger(Base64.getUrlDecoder().decode(stringValue("x"))),
                            new BigInteger(Base64.getUrlDecoder().decode(stringValue("y"))));
                    AlgorithmParameters algorithmParameters = AlgorithmParameters.getInstance(ALGORITHM_ELLIPTIC_CURVE);

                    String curve = stringValue("crv");
                    switch (curve) {
                        case ELLIPTIC_CURVE_TYPE_P256:
                            algorithmParameters.init(new ECGenParameterSpec("secp256r1"));
                            break;
                        case ELLIPTIC_CURVE_TYPE_P384:
                            algorithmParameters.init(new ECGenParameterSpec("secp384r1"));
                            break;
                        case ELLIPTIC_CURVE_TYPE_P521:
                            algorithmParameters.init(new ECGenParameterSpec("secp521r1"));
                            break;
                        default:
                            throw new InvalidPublicKeyException("Invalid or unsupported curve type " + curve);
                    }
                    ECParameterSpec ecParameterSpec = algorithmParameters.getParameterSpec(ECParameterSpec.class);
                    ECPublicKeySpec ecPublicKeySpec = new ECPublicKeySpec(ecPoint, ecParameterSpec);
                    publicKey = keyFactory.generatePublic(ecPublicKeySpec);
                } catch (NoSuchAlgorithmException e) {
                    throw new InvalidPublicKeyException("Invalid algorithm to generate key", e);
                } catch (InvalidKeySpecException | InvalidParameterSpecException e) {
                    throw new InvalidPublicKeyException("Invalid public key", e);
                }
                break;

            default:
                throw new InvalidPublicKeyException("The key type of " + type + " is not supported");
        }

        return publicKey;
    }

    private String stringValue(String key) {
        return (String) additionalAttributes.get(key);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("kid", id)
                .add("kty", type)
                .add("alg", algorithm)
                .add("use", usage)
                .add("extras", additionalAttributes)
                .toString();
    }
}
