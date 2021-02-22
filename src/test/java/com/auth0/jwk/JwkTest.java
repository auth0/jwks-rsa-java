package com.auth0.jwk;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.codec.binary.Base64;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.security.SecureRandom;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class JwkTest {

    private static final String RS_256 = "RS256";
    private static final String RSA = "RSA";

    private static final String ES_256 = "ES256";
    private static final String ES_384 = "ES384";
    private static final String ES_521 = "ES521";
    private static final String ES_UNSUPPORTED = "ES999";
    private static final String EC = "EC";
    private static final String CRV_P_256 = "P-256";
    private static final String CRV_P_384 = "P-384";
    private static final String CRV_P_521 = "P-521";
    private static final String CRV_UNSUPPORTED = "P-111";
    private static final String EC_P_256_X = "3xlexeXJEVNEJIRZKLJxjksjerixlwqEIEI586ss__3";
    private static final String EC_P_256_Y = "398jKSKSxnbnbmvirt794798cvijaoowrvjvjeiKSLS";
    private static final String EC_P_384_X = "3xlexeXJEVNEJIRZKLJxjksjerixlwqEIEI586ss__32929lxkXKWWI1___221XX";
    private static final String EC_P_384_Y = "398jKSKSxnbnbmvirt794798cvijaoowrvjvjeiKSLSkdksl383838zzze342246";
    private static final String EC_P_521_X = "3xlexeXJEVNEJIRZKLJxjksjerixlwqEIEI586ss__32929lxkXKWWI1___221XXx39392xXetixeiazqwernvbc";
    private static final String EC_P_521_Y = "398jKSKSxnbnbmvirt794798cvijaoowrvjvjeiKSLSkdksl383838zzze342246_349809iubjairlKDSLDLKxx";

    private static final String AES = "AES";
    private static final String SIG = "sig";
    private static final String THUMBPRINT = "THUMBPRINT";
    private static final String MODULUS = "vGChUGMTWZNfRsXxd-BtzC4RDYOMqtIhWHol--HNib5SgudWBg6rEcxvR6LWrx57N6vfo68wwT9_FHlZpaK6NXA_dWFW4f3NftfWLL7Bqy90sO4vijM6LMSE6rnl5VB9_Gsynk7_jyTgYWdTwKur0YRec93eha9oCEXmy7Ob1I2dJ8OQmv2GlvA7XZalMxAq4rFnXLzNQ7hCsHrUJP1p7_7SolWm9vTokkmckzSI_mAH2R27Z56DmI7jUkL9fLU-jz-fz4bkNg-mPz4R-kUmM_ld3-xvto79BtxJvOw5qqtLNnRjiDzoqRv-WrBdw5Vj8Pvrg1fwscfVWHlmq-1pFQ";
    private static final String EXPONENT = "AQAB";
    private static final String CERT_CHAIN = "CERT_CHAIN";
    private static final List<String> KEY_OPS_LIST = Lists.newArrayList("sign");
    private static final String KEY_OPS_STRING = "sign";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldBuildRsaWithMap() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = publicKeyRsaValues(kid, KEY_OPS_LIST);
        Jwk jwk = Jwk.fromValues(values);

        assertThat(jwk.getId(), equalTo(kid));
        assertThat(jwk.getAlgorithm(), equalTo(RS_256));
        assertThat(jwk.getType(), equalTo(RSA));
        assertThat(jwk.getUsage(), equalTo(SIG));
        assertThat(jwk.getOperationsAsList(), equalTo(KEY_OPS_LIST));
        assertThat(jwk.getOperations(), is(KEY_OPS_STRING));
        assertThat(jwk.getCertificateThumbprint(), equalTo(THUMBPRINT));
        assertThat(jwk.getCertificateChain(), contains(CERT_CHAIN));
    }

    @Test
    public void shouldBuildEllipticCurveWithMap() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = publicKeyEllipticCurveValues(kid, ES_256, KEY_OPS_LIST, CRV_P_256, EC_P_256_X, EC_P_256_Y);
        Jwk jwk = Jwk.fromValues(values);

        assertThat(jwk.getId(), equalTo(kid));
        assertThat(jwk.getAlgorithm(), equalTo(ES_256));
        assertThat(jwk.getType(), equalTo(EC));
        assertThat(jwk.getUsage(), equalTo(SIG));
        assertThat(jwk.getOperationsAsList(), equalTo(KEY_OPS_LIST));
        assertThat(jwk.getOperations(), is(KEY_OPS_STRING));
        assertThat(jwk.getCertificateThumbprint(), nullValue());
        assertThat(jwk.getCertificateChain(), nullValue());
    }

    @Test
    public void shouldReturnRsaPublicKey() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = publicKeyRsaValues(kid, KEY_OPS_LIST);
        Jwk jwk = Jwk.fromValues(values);

        assertThat(jwk.getPublicKey(), notNullValue());
        assertThat(jwk.getPublicKey(), instanceOf(RSAPublicKey.class));
        assertThat(jwk.getOperationsAsList(), is(KEY_OPS_LIST));
        assertThat(jwk.getOperations(), is(KEY_OPS_STRING));
    }

    @Test
    public void shouldReturnEllipticCurveP256PublicKey() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = publicKeyEllipticCurveValues(kid, ES_256, KEY_OPS_LIST, CRV_P_256, EC_P_256_X, EC_P_256_Y);
        Jwk jwk = Jwk.fromValues(values);

        assertThat(jwk.getPublicKey(), notNullValue());
        assertThat(jwk.getPublicKey(), instanceOf(ECPublicKey.class));
        assertThat(jwk.getOperationsAsList(), is(KEY_OPS_LIST));
        assertThat(jwk.getOperations(), is(KEY_OPS_STRING));
    }

    @Test
    public void shouldReturnEllipticCurveP384PublicKey() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = publicKeyEllipticCurveValues(kid, ES_384, KEY_OPS_LIST, CRV_P_384, EC_P_384_X, EC_P_384_Y);
        Jwk jwk = Jwk.fromValues(values);

        assertThat(jwk.getPublicKey(), notNullValue());
        assertThat(jwk.getPublicKey(), instanceOf(ECPublicKey.class));
        assertThat(jwk.getOperationsAsList(), is(KEY_OPS_LIST));
        assertThat(jwk.getOperations(), is(KEY_OPS_STRING));
    }

    @Test
    public void shouldReturnEllipticCurveP521PublicKey() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = publicKeyEllipticCurveValues(kid, ES_521, KEY_OPS_LIST, CRV_P_521, EC_P_521_X, EC_P_521_Y);
        Jwk jwk = Jwk.fromValues(values);

        assertThat(jwk.getPublicKey(), notNullValue());
        assertThat(jwk.getPublicKey(), instanceOf(ECPublicKey.class));
        assertThat(jwk.getOperationsAsList(), is(KEY_OPS_LIST));
        assertThat(jwk.getOperations(), is(KEY_OPS_STRING));
    }

    @Test
    public void shouldThrowForUnsupportedEllipticCurvePublicKey() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = publicKeyEllipticCurveValues(kid, ES_UNSUPPORTED, KEY_OPS_LIST, CRV_UNSUPPORTED, EC_P_521_X, EC_P_521_Y);
        Jwk jwk = Jwk.fromValues(values);
        expectedException.expect(InvalidPublicKeyException.class);
        expectedException.expectMessage("Invalid or unsupported curve type " + CRV_UNSUPPORTED);
        jwk.getPublicKey();
    }

    @Test
    public void shouldReturnPublicKeyForStringKeyOpsParam() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = publicKeyRsaValues(kid, KEY_OPS_STRING);
        Jwk jwk = Jwk.fromValues(values);

        assertThat(jwk.getPublicKey(), notNullValue());
        assertThat(jwk.getOperationsAsList(), is(KEY_OPS_LIST));
        assertThat(jwk.getOperations(), is(KEY_OPS_STRING));
    }

    @Test
    public void shouldReturnPublicKeyForNullKeyOpsParam() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = publicKeyRsaValues(kid, null);
        Jwk jwk = Jwk.fromValues(values);

        assertThat(jwk.getPublicKey(), notNullValue());
        assertThat(jwk.getOperationsAsList(), nullValue());
        assertThat(jwk.getOperations(), nullValue());
    }

    @Test
    public void shouldReturnPublicKeyForEmptyKeyOpsParam() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = publicKeyRsaValues(kid, Lists.newArrayList());
        Jwk jwk = Jwk.fromValues(values);

        assertThat(jwk.getPublicKey(), notNullValue());
        assertThat(jwk.getOperationsAsList(), notNullValue());
        assertThat(jwk.getOperationsAsList().size(), equalTo(0));
        assertThat(jwk.getOperations(), nullValue());
    }

    @Test
    public void shouldThrowForUnsupportedKeyType() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = unsupportedValues(kid);
        Jwk jwk = Jwk.fromValues(values);
        expectedException.expect(InvalidPublicKeyException.class);
        expectedException.expectMessage("The key type of " + AES + " is not supported");
        jwk.getPublicKey();
    }
    
    @Test
    public void shouldNotThrowInvalidArgumentExceptionOnMissingKidParam() throws Exception {
        //kid is optional - https://tools.ietf.org/html/rfc7517#section-4.5
        final String kid = randomKeyId();
        Map<String, Object> values = publicKeyRsaValues(kid, KEY_OPS_LIST);
        values.remove("kid");
        Jwk.fromValues(values);
    }

    @Test
    public void shouldThrowInvalidArgumentExceptionOnMissingKtyParam() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = publicKeyRsaValues(kid, KEY_OPS_LIST);
        values.remove("kty");
        expectedException.expect(IllegalArgumentException.class);
        Jwk.fromValues(values);
    }

    @Test
    public void shouldReturnKeyWithMissingAlgParam() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = publicKeyRsaValues(kid, KEY_OPS_LIST);
        values.remove("alg");
        Jwk jwk = Jwk.fromValues(values);
        assertThat(jwk.getPublicKey(), notNullValue());
    }

    private static String randomKeyId() {
        byte[] bytes = new byte[50];
        new SecureRandom().nextBytes(bytes);
        return Base64.encodeBase64String(bytes);
    }

    private static Map<String, Object> unsupportedValues(String kid) {
        Map<String, Object> values = Maps.newHashMap();
        values.put("alg", "AES_256");
        values.put("kty", AES);
        values.put("use", SIG);
        values.put("kid", kid);
        return values;
    }

    private static Map<String, Object> publicKeyRsaValues(String kid, Object keyOps) {
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

    private static Map<String, Object> publicKeyEllipticCurveValues(String kid, String alg, Object keyOps, String crv, String x, String y) {
        Map<String, Object> values = Maps.newHashMap();
        values.put("alg", alg);
        values.put("kty", EC);
        values.put("use", SIG);
        values.put("key_ops", keyOps);
        values.put("kid", kid);
        values.put("x", x);
        values.put("y", y);
        values.put("crv", crv);
        return values;
    }
}
