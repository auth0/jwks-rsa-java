package com.auth0.jwk;

import com.google.common.collect.Maps;
import org.apache.commons.codec.binary.Base64;

import java.math.BigInteger;
import java.security.spec.KeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represents a JSON Web Key (JWK) used to verify the signature of JWTs
 */
@SuppressWarnings("WeakerAccess")
public class Jwk extends AbstractJwk {

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
        super(id, type, algorithm, usage, operations, certificateUrl, certificateChain, certificateThumbprint, additionalAttributes);
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
        List<String> keyOpsList = keyOps instanceof String ? Collections.singletonList((String) keyOps) : (List<String>) keyOps;
        String x5u = (String) values.remove("x5u");
        List<String> x5c = (List<String>) values.remove("x5c");
        String x5t = (String) values.remove("x5t");
        if (kty == null) {
            throw new IllegalArgumentException("Attributes " + map + " are not from a valid jwk");
        }
        return new Jwk(kid, kty, alg, use, keyOpsList, x5u, x5c, x5t, values);
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