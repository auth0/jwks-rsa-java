package com.auth0.jwk;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.codec.binary.Base64;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.security.SecureRandom;
import java.util.List;
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
    private static final String PRIVATEEXPONENT = "X4cTteJY_gn4FYPsXB8rdXix5vwsg1FLN5E3EaG6RJoVH-HLLKD9M7dx5oo7GURknchnrRweUkC7hT5fJLM0WbFAKNLWY2vv7B6NqXSzUvxT0_YSfqijwp3RTzlBaCxWp4doFk5N2o8Gy_nHNKroADIkJ46pRUohsXywbReAdYaMwFs9tv8d_cPVY3i07a3t8MN6TNwm0dSawm9v47UiCl3Sk5ZiG7xojPLu4sbg1U2jx4IBTNBznbJSzFHK66jT8bgkuqsk0GjskDJk19Z4qwjwbsnn4j2WBii3RL-Us2lGVkY8fkFzme1z0HbIkfz0Y6mqnOYtqc0X4jfcKoAC8Q";
    private static final String PRIMEP = "83i-7IvMGXoMXCskv73TKr8637FiO7Z27zv8oj6pbWUQyLPQBQxtPVnwD20R-60eTDmD2ujnMt5PoqMrm8RfmNhVWDtjjMmCMjOpSXicFHj7XOuVIYQyqVWlWEh6dN36GVZYk93N8Bc9vY41xy8B9RzzOGVQzXvNEvn7O0nVbfs";
    private static final String PRIMEQ = "3dfOR9cuYq-0S-mkFLzgItgMEfFzB2q3hWehMuG0oCuqnb3vobLyumqjVZQO1dIrdwgTnCdpYzBcOfW5r370AFXjiWft_NGEiovonizhKpo9VVS78TzFgxkIdrecRezsZ-1kYd_s1qDbxtkDEgfAITAG9LUnADun4vIcb6yelxk";
    private static final String PRIMEEXPONENTP = "G4sPXkc6Ya9y8oJW9_ILj4xuppu0lzi_H7VTkS8xj5SdX3coE0oimYwxIi2emTAue0UOa5dpgFGyBJ4c8tQ2VF402XRugKDTP8akYhFo5tAA77Qe_NmtuYZc3C3m3I24G2GvR5sSDxUyAN2zq8Lfn9EUms6rY3Ob8YeiKkTiBj0";
    private static final String PRIMEEXPONENTQ = "s9lAH9fggBsoFR8Oac2R_E2gw282rT2kGOAhvIllETE1efrA6huUUvMfBcMpn8lqeW6vzznYY5SSQF7pMdC_agI3nG8Ibp1BUb0JUiraRNqUfLhcQb_d9GF4Dh7e74WbRsobRonujTYN1xCaP6TO61jvWrX-L18txXw494Q_cgk";
    private static final String CRTCOEFFICENT = "GyM_p6JrXySiz1toFgKbWV-JdI3jQ4ypu9rbMWx3rQJBfmt0FoYzgUIZEVFEcOqwemRN81zoDAaa-Bk0KWNGDjJHZDdDmFhW3AN7lI-puxk_mHZGJ11rxyR8O55XLSe3SPmRfKwZI6yU24ZxvQKFYItdldUKGzO6Ia6zTKhAVRU";
    private static final String CERT_CHAIN = "CERT_CHAIN";
    private static final List<String> KEY_OPS_LIST = Lists.newArrayList("sign");
    private static final String KEY_OPS_STRING = "sign";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldBuildWithMap() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = privateKeyValues(kid, KEY_OPS_LIST);
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
    public void shouldReturnPublicKey() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = publicKeyValues(kid, KEY_OPS_LIST);
        Jwk jwk = Jwk.fromValues(values);

        assertThat(jwk.getPublicKey(), notNullValue());
        assertThat(jwk.getOperationsAsList(), is(KEY_OPS_LIST));
        assertThat(jwk.getOperations(), is(KEY_OPS_STRING));
    }

    @Test
    public void shouldReturnPrivateKey() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = privateKeyValues(kid, KEY_OPS_LIST);
        Jwk jwk = Jwk.fromValues(values);

        assertThat(jwk.getPrivateKey(), notNullValue());
        assertThat(jwk.getOperationsAsList(), is(KEY_OPS_LIST));
        assertThat(jwk.getOperations(), is(KEY_OPS_STRING));
    }

    @Test
    public void shouldReturnPublicKeyForStringKeyOpsParam() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = publicKeyValues(kid, KEY_OPS_STRING);
        Jwk jwk = Jwk.fromValues(values);

        assertThat(jwk.getPublicKey(), notNullValue());
        assertThat(jwk.getOperationsAsList(), is(KEY_OPS_LIST));
        assertThat(jwk.getOperations(), is(KEY_OPS_STRING));
    }

    @Test
    public void shouldReturnPrivateKeyForStringKeyOpsParam() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = privateKeyValues(kid, KEY_OPS_STRING);
        Jwk jwk = Jwk.fromValues(values);

        assertThat(jwk.getPrivateKey(), notNullValue());
        assertThat(jwk.getOperationsAsList(), is(KEY_OPS_LIST));
        assertThat(jwk.getOperations(), is(KEY_OPS_STRING));
    }

    @Test
    public void shouldReturnPublicKeyForNullKeyOpsParam() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = publicKeyValues(kid, null);
        Jwk jwk = Jwk.fromValues(values);

        assertThat(jwk.getPublicKey(), notNullValue());
        assertThat(jwk.getOperationsAsList(), nullValue());
        assertThat(jwk.getOperations(), nullValue());
    }

    @Test
    public void shouldReturnPrivateKeyForNullKeyOpsParam() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = privateKeyValues(kid, null);
        Jwk jwk = Jwk.fromValues(values);

        assertThat(jwk.getPrivateKey(), notNullValue());
        assertThat(jwk.getOperationsAsList(), nullValue());
        assertThat(jwk.getOperations(), nullValue());
    }

    @Test
    public void shouldReturnPublicKeyForEmptyKeyOpsParam() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = publicKeyValues(kid, Lists.newArrayList());
        Jwk jwk = Jwk.fromValues(values);

        assertThat(jwk.getPublicKey(), notNullValue());
        assertThat(jwk.getOperationsAsList(), notNullValue());
        assertThat(jwk.getOperationsAsList().size(), equalTo(0));
        assertThat(jwk.getOperations(), nullValue());
    }

    @Test
    public void shouldReturnPrivateKeyForEmptyKeyOpsParam() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = privateKeyValues(kid, Lists.newArrayList());
        Jwk jwk = Jwk.fromValues(values);

        assertThat(jwk.getPrivateKey(), notNullValue());
        assertThat(jwk.getOperationsAsList(), notNullValue());
        assertThat(jwk.getOperationsAsList().size(), equalTo(0));
        assertThat(jwk.getOperations(), nullValue());
    }

    @Test
    public void shouldThrowForNonRSAKey() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = nonRSAValues(kid);
        Jwk jwk = Jwk.fromValues(values);
        expectedException.expect(InvalidPublicKeyException.class);
        expectedException.expectMessage("The key is not of type RSA");
        jwk.getPublicKey();
    }
    
    @Test
    public void shouldNotThrowInvalidArgumentExceptionOnMissingKidParam() throws Exception {
        //kid is optional - https://tools.ietf.org/html/rfc7517#section-4.5
        final String kid = randomKeyId();
        Map<String, Object> values = publicKeyValues(kid, KEY_OPS_LIST);
        Map<String, Object> privateValues = privateKeyValues(kid, KEY_OPS_LIST);
        values.remove("kid");
        privateValues.remove("kid");
        Jwk.fromValues(values);
        Jwk.fromValues(privateValues);
    }

    @Test
    public void shouldThrowInvalidArgumentExceptionOnMissingKtyParam() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = publicKeyValues(kid, KEY_OPS_LIST);
        values.remove("kty");
        expectedException.expect(IllegalArgumentException.class);
        Jwk.fromValues(values);
    }

    @Test
    public void shouldReturnKeyWithMissingAlgParam() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = publicKeyValues(kid, KEY_OPS_LIST);
        values.remove("alg");
        Jwk jwk = Jwk.fromValues(values);
        assertThat(jwk.getPublicKey(), notNullValue());
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

    private static Map<String, Object> publicKeyValues(String kid, Object keyOps) {
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

    private static Map<String, Object> privateKeyValues(String kid, Object keyOps) {
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
        values.put("d",PRIVATEEXPONENT);
        values.put("p",PRIMEP);
        values.put("q",PRIMEQ);
        values.put("dp",PRIMEEXPONENTP);
        values.put("dq",PRIMEEXPONENTQ);
        values.put("qi",CRTCOEFFICENT);
        return values;
    }
}
