package com.auth0.jwk;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.auth0.jwk.kyt.EllipticCurve;
import com.google.common.collect.Maps;

public class JwkFactory {

    @SuppressWarnings("unchecked")
    public static AbstractJwk fromValues(Map<String, Object> map) {
        Map<String, Object> values = Maps.newHashMap(map);
        // kid is optional - https://tools.ietf.org/html/rfc7517#section-4.5
        String kid = (String) values.remove("kid");
        
        // alg value is optional - https://tools.ietf.org/html/rfc7517#section-4.4
        // could be validated according to https://tools.ietf.org/html/rfc7518
        String alg = (String) values.remove("alg");
        
        // use value is optional - https://tools.ietf.org/html/rfc7517#section-4.2
        // "sig" or "enc" are valid value but different ones can be defined as well
        String use = (String) values.remove("use");
        
        // key_ops values are optional - https://tools.ietf.org/html/rfc7517#section-4.3
        // there can be other values than defined ones
        Object keyOps = values.remove("key_ops");
        List<String> keyOpsList = keyOps instanceof String ? Collections.singletonList((String) keyOps) : (List<String>) keyOps;
        
        // x5u value is optional - https://tools.ietf.org/html/rfc7517#section-4.6
        String x5u = (String) values.remove("x5u");
        
        // x5c values are optional - https://tools.ietf.org/html/rfc7517#section-4.7
        List<String> x5c = (List<String>) values.remove("x5c");
        
        // x5t value is optional - https://tools.ietf.org/html/rfc7517#section-4.8
        String x5t = (String) values.remove("x5t");
        
        // kty must be either RSA or EC = https://tools.ietf.org/html/rfc7517#section-4.1
        String kty = (String) values.remove("kty");
        if (AbstractJwk.PUBLIC_KEY_RSA_ALGORITHM.equals(kty)) {
            return new Jwk(kid, kty, alg, use, keyOpsList, x5u, x5c, x5t, values);
        } else if (AbstractJwk.PUBLIC_KEY_EC_ALGORITHM.equals(kty)) {
            try {
                return new EllipticCurve(kid, alg, use, keyOpsList, x5u, x5c, x5t, values);
            } catch (InvalidPublicKeyException e) {
                throw new IllegalArgumentException(e);
            }
        }
        throw new IllegalArgumentException(String.format("kty value must be either \"RSA\" or \"EC\". \"%s\" value found.", kty));
    }
    
    private JwkFactory() {
        // This class must not be instantiated
    }
    
}
