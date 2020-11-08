package com.auth0.jwk.kyt;

import com.auth0.jwk.AbstractJwk;
import com.auth0.jwk.InvalidPublicKeyException;
import com.auth0.jwk.Jwk;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECFieldFp;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class EllipticCurveTest extends JwkTests {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldThrowInvalidArgumentExceptionOnMissingCrvParam() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = new HashMap<>();
        values.put("x", ES256_P256_x);
        values.put("y", ES256_P256_Y);
        expectedException.expect(InvalidPublicKeyException.class);
        expectedException.expectMessage("The key has no curve specification");
        new EllipticCurve(kid, ES256, SIG, null, null, null, null, values);
    }
    
    @Test
    public void shouldThrowInvalidArgumentExceptionOnMissingXParam() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = new HashMap<>();
        values.put("crv", EC);
        values.put("y", ES256_P256_Y);
        expectedException.expect(InvalidPublicKeyException.class);
        expectedException.expectMessage("The key has no curve specification");
        new EllipticCurve(kid, ES256, SIG, null, null, null, null, values);
    }
    
    @Test
    public void shouldThrowInvalidArgumentExceptionOnMissingYParam() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = new HashMap<>();
        values.put("crv", EC);
        values.put("x", ES256_P256_x);
        expectedException.expect(InvalidPublicKeyException.class);
        expectedException.expectMessage("The key has no curve specification");
        new EllipticCurve(kid, ES256, SIG, null, null, null, null, values);
    }

    @Test
    public void shouldReturnP256PublicKey() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = new HashMap<>();
        values.put("crv", P256);
        values.put("x", ES256_P256_x);
        values.put("y", ES256_P256_Y);
        AbstractJwk jwk = new EllipticCurve(kid, ES256, SIG, null, null, null, null, values);

        PublicKey publicKey = jwk.getPublicKey();
        assertThat(publicKey, notNullValue());
        assertThat(publicKey, instanceOf(ECPublicKey.class));
        ECPublicKey ecPublicKey = (ECPublicKey) publicKey;
        assertThat(((ECFieldFp)ecPublicKey.getParams().getCurve().getField()).getP(), equalTo(new BigInteger("115792089210356248762697446949407573530086143415290314195533631308867097853951")));
        assertThat(ecPublicKey.getParams().getCurve().getA(), equalTo(new BigInteger("ffffffff00000001000000000000000000000000fffffffffffffffffffffffc", 16)));
        assertThat(ecPublicKey.getParams().getCurve().getB(), equalTo(new BigInteger("5ac635d8aa3a93e7b3ebbd55769886bc651d06b0cc53b0f63bce3c3e27d2604b", 16)));
        assertThat(ecPublicKey.getParams().getGenerator().getAffineX(), equalTo(new BigInteger("6B17D1F2E12C4247F8BCE6E563A440F277037D812DEB33A0F4A13945D898C296", 16)));
        assertThat(ecPublicKey.getParams().getGenerator().getAffineY(), equalTo(new BigInteger("4FE342E2FE1A7F9B8EE7EB4A7C0F9E162BCE33576B315ECECBB6406837BF51F5", 16)));
        assertThat(ecPublicKey.getParams().getOrder(), equalTo(new BigInteger("FFFFFFFF00000000FFFFFFFFFFFFFFFFBCE6FAADA7179E84F3B9CAC2FC632551", 16)));
        assertThat(ecPublicKey.getParams().getCofactor(), equalTo(1));
        assertThat(ecPublicKey.getW().getAffineX(), equalTo(new BigInteger("944396F59965968617968551F1595AB7E673DD90F5953C2E10F2D38BA15F43B", 16)));
        assertThat(ecPublicKey.getW().getAffineY(), equalTo(new BigInteger("E67617A709A1AE4DD207F37795D6F2C6A26164A9204EEA0461D5F6BD3B14590C", 16)));
    }

    @Test
    public void shouldReturnP384PublicKey() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = new HashMap<>();
        values.put("crv", P384);
        values.put("x", ES256_P256_x);
        values.put("y", ES256_P256_Y);
        AbstractJwk jwk = new EllipticCurve(kid, ES256, SIG, null, null, null, null, values);

        PublicKey publicKey = jwk.getPublicKey();
        assertThat(publicKey, notNullValue());
        assertThat(publicKey, instanceOf(ECPublicKey.class));
        ECPublicKey ecPublicKey = (ECPublicKey) publicKey;
        assertThat(((ECFieldFp)ecPublicKey.getParams().getCurve().getField()).getP(), equalTo(new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFFFF0000000000000000FFFFFFFF", 16)));
        assertThat(ecPublicKey.getParams().getCurve().getA(), equalTo(new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFFFF0000000000000000FFFFFFFC", 16)));
        assertThat(ecPublicKey.getParams().getCurve().getB(), equalTo(new BigInteger("B3312FA7E23EE7E4988E056BE3F82D19181D9C6EFE8141120314088F5013875AC656398D8A2ED19D2A85C8EDD3EC2AEF", 16)));
        assertThat(ecPublicKey.getParams().getGenerator().getAffineX(), equalTo(new BigInteger("AA87CA22BE8B05378EB1C71EF320AD746E1D3B628BA79B9859F741E082542A385502F25DBF55296C3A545E3872760AB7", 16)));
        assertThat(ecPublicKey.getParams().getGenerator().getAffineY(), equalTo(new BigInteger("3617DE4A96262C6F5D9E98BF9292DC29F8F41DBD289A147CE9DA3113B5F0B8C00A60B1CE1D7E819D7A431D7C90EA0E5F", 16)));
        assertThat(ecPublicKey.getParams().getOrder(), equalTo(new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFC7634D81F4372DDF581A0DB248B0A77AECEC196ACCC52973", 16)));
        assertThat(ecPublicKey.getParams().getCofactor(), equalTo(1));
        assertThat(ecPublicKey.getW().getAffineX(), equalTo(new BigInteger("944396F59965968617968551F1595AB7E673DD90F5953C2E10F2D38BA15F43B", 16)));
        assertThat(ecPublicKey.getW().getAffineY(), equalTo(new BigInteger("E67617A709A1AE4DD207F37795D6F2C6A26164A9204EEA0461D5F6BD3B14590C", 16)));
    }

    @Test
    public void shouldReturnP521PublicKey() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = new HashMap<>();
        values.put("crv", P521);
        values.put("x", ES256_P256_x);
        values.put("y", ES256_P256_Y);
        AbstractJwk jwk = new EllipticCurve(kid, ES256, SIG, null, null, null, null, values);

        PublicKey publicKey = jwk.getPublicKey();
        assertThat(publicKey, notNullValue());
        assertThat(publicKey, instanceOf(ECPublicKey.class));
        ECPublicKey ecPublicKey = (ECPublicKey) publicKey;
        assertThat(((ECFieldFp)ecPublicKey.getParams().getCurve().getField()).getP(), equalTo(new BigInteger("01FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16)));
        assertThat(ecPublicKey.getParams().getCurve().getA(), equalTo(new BigInteger("01FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFC", 16)));
        assertThat(ecPublicKey.getParams().getCurve().getB(), equalTo(new BigInteger("0051953EB9618E1C9A1F929A21A0B68540EEA2DA725B99B315F3B8B489918EF109E156193951EC7E937B1652C0BD3BB1BF073573DF883D2C34F1EF451FD46B503F00", 16)));
        assertThat(ecPublicKey.getParams().getGenerator().getAffineX(), equalTo(new BigInteger("00C6858E06B70404E9CD9E3ECB662395B4429C648139053FB521F828AF606B4D3DBAA14B5E77EFE75928FE1DC127A2FFA8DE3348B3C1856A429BF97E7E31C2E5BD66", 16)));
        assertThat(ecPublicKey.getParams().getGenerator().getAffineY(), equalTo(new BigInteger("011839296A789A3BC0045C8A5FB42C7D1BD998F54449579B446817AFBD17273E662C97EE72995EF42640C550B9013FAD0761353C7086A272C24088BE94769FD16650", 16)));
        assertThat(ecPublicKey.getParams().getOrder(), equalTo(new BigInteger("01FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFA51868783BF2F966B7FCC0148F709A5D03BB5C9B8899C47AEBB6FB71E91386409", 16)));
        assertThat(ecPublicKey.getParams().getCofactor(), equalTo(1));
        assertThat(ecPublicKey.getW().getAffineX(), equalTo(new BigInteger("944396F59965968617968551F1595AB7E673DD90F5953C2E10F2D38BA15F43B", 16)));
        assertThat(ecPublicKey.getW().getAffineY(), equalTo(new BigInteger("E67617A709A1AE4DD207F37795D6F2C6A26164A9204EEA0461D5F6BD3B14590C", 16)));
    }

}
