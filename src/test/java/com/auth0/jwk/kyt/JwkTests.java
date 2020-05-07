package com.auth0.jwk.kyt;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.codec.binary.Base64;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

public class JwkTests {

    protected static final String RS_256 = "RS256";
    protected static final String RSA = "RSA";
    protected static final String SIG = "sig";
    protected static final String THUMBPRINT = "THUMBPRINT";
    protected static final String MODULUS = "vGChUGMTWZNfRsXxd-BtzC4RDYOMqtIhWHol--HNib5SgudWBg6rEcxvR6LWrx57N6vfo68wwT9_FHlZpaK6NXA_dWFW4f3NftfWLL7Bqy90sO4vijM6LMSE6rnl5VB9_Gsynk7_jyTgYWdTwKur0YRec93eha9oCEXmy7Ob1I2dJ8OQmv2GlvA7XZalMxAq4rFnXLzNQ7hCsHrUJP1p7_7SolWm9vTokkmckzSI_mAH2R27Z56DmI7jUkL9fLU-jz-fz4bkNg-mPz4R-kUmM_ld3-xvto79BtxJvOw5qqtLNnRjiDzoqRv-WrBdw5Vj8Pvrg1fwscfVWHlmq-1pFQ";
    protected static final String EXPONENT = "AQAB";
    protected static final String CERT_CHAIN = "CERT_CHAIN";
    protected static final List<String> KEY_OPS_LIST = Lists.newArrayList("sign");
    protected static final String KEY_OPS_STRING = "sign";

    protected static final String EC = "EC";
    protected static final String ES256 = "ES256";
    protected static final String P256 = "P-256";
    protected static final String P384 = "P-384";
    protected static final String P521 = "P-521";
    protected static final String ES256_P256_x = "CUQ5b1mWWWhheWhVHxWVq35nPdkPWVPC4Q8tOLoV9Ds";
    protected static final String ES256_P256_Y = "5nYXpwmhrk3SB_N3ldbyxqJhZKkgTuoEYdX2vTsUWQw";

    protected static String randomKeyId() {
        byte[] bytes = new byte[50];
        new SecureRandom().nextBytes(bytes);
        return Base64.encodeBase64String(bytes);
    }

    protected static Map<String, Object> publicRsaKeyValues(String kid, Object keyOps) {
        Map<String, Object> values = Maps.newHashMap();
        values.put("alg", RS_256);
        values.put("kty", RSA);
        values.put("use", SIG);
        values.put("key_ops", keyOps);
        values.put("x5c", Lists.newArrayList(CERT_CHAIN));
        values.put("x5t", THUMBPRINT);
        values.put("kid", kid);
        values.put("n", MODULUS);
        values.put("e", EXPONENT);
        return values;
    }

    protected static Map<String, Object> publicEcKeyValues(String kid) {
        Map<String, Object> values = Maps.newHashMap();
        values.put("alg", ES256);
        values.put("kty", EC);
        values.put("use", SIG);
        values.put("kid", kid);
        values.put("crv", P256);
        values.put("x", ES256_P256_x);
        values.put("y", ES256_P256_Y);
        return values;
    }

    protected static Map<String, Object> unkownKeyValues(String kid) {
        Map<String, Object> values = Maps.newHashMap();
        values.put("alg", "AES_256");
        values.put("kty", "AES");
        values.put("use", SIG);
        values.put("kid", kid);
        return values;
    }

}
