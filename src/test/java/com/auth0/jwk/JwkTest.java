package com.auth0.jwk;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.codec.binary.Base64;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URL;
import java.security.SecureRandom;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class JwkTest {

    private static final String RS_256 = "RS256";
    private static final String RSA = "RSA";
    private static final String SIG = "sig";
    private static final String THUMBPRINT = "THUMBPRINT";
    private static final String MODULUS = "vGChUGMTWZNfRsXxd-BtzC4RDYOMqtIhWHol--HNib5SgudWBg6rEcxvR6LWrx57N6vfo68wwT9_FHlZpaK6NXA_dWFW4f3NftfWLL7Bqy90sO4vijM6LMSE6rnl5VB9_Gsynk7_jyTgYWdTwKur0YRec93eha9oCEXmy7Ob1I2dJ8OQmv2GlvA7XZalMxAq4rFnXLzNQ7hCsHrUJP1p7_7SolWm9vTokkmckzSI_mAH2R27Z56DmI7jUkL9fLU-jz-fz4bkNg-mPz4R-kUmM_ld3-xvto79BtxJvOw5qqtLNnRjiDzoqRv-WrBdw5Vj8Pvrg1fwscfVWHlmq-1pFQ";
    private static final String EXPONENT = "AQAB";
    private static final String CERT_CHAIN = "CERT_CHAIN";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldBuildWithMap() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = publicKeyValues(kid);
        Jwk jwk = Jwk.fromValues(values);

        assertThat(jwk.getId(), equalTo(kid));
        assertThat(jwk.getAlgorithm(), equalTo(RS_256));
        assertThat(jwk.getType(), equalTo(RSA));
        assertThat(jwk.getUsage(), equalTo(SIG));
        assertThat(jwk.getCertificateThumbprint(), equalTo(THUMBPRINT));
        assertThat(jwk.getCertificateChain(), contains(CERT_CHAIN));
    }

    @Test
    public void shouldReturnPublicKey() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = publicKeyValues(kid);
        Jwk jwk = Jwk.fromValues(values);

        assertThat(jwk.getPublicKey(), notNullValue());
    }

    @Test
    public void shouldReturnNullForNonRSAKey() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = nonRSAValues(kid);
        Jwk jwk = Jwk.fromValues(values);
        assertThat(jwk.getPublicKey(), nullValue());
    }

    private static String randomKeyId() {
        byte[] bytes = new byte[50];
        new SecureRandom().nextBytes(bytes);
        return Base64.encodeBase64String(bytes);
    }

    private static Map<String, Object> nonRSAValues(String kid) {
        Map<String, Object> values = Maps.newHashMap();
        values.put("alg", "AES_256");
        values.put("kty", "AES");
        values.put("use", SIG);
        values.put("kid", kid);
        return values;
    }

    private static Map<String, Object> publicKeyValues(String kid) {
        Map<String, Object> values = Maps.newHashMap();
        values.put("alg", RS_256);
        values.put("kty", RSA);
        values.put("use", SIG);
        values.put("x5c", Lists.newArrayList(CERT_CHAIN));
        values.put("x5t", THUMBPRINT);
        values.put("kid", kid);
        values.put("n", MODULUS);
        values.put("e", EXPONENT);
        return values;
    }
}