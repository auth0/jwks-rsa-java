package com.auth0.jwk.kyt;

import com.auth0.jwk.InvalidPublicKeyException;
import com.auth0.jwk.Jwk;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;

import java.math.BigInteger;
import java.security.spec.*;
import java.util.List;
import java.util.Map;

/**
 * JWK entry with Elliptic Curve key stype is implementing corresponding public key extraction.
 */
public class EllipticCurve extends Jwk {

    public EllipticCurve(String id, String type, String algorithm, String usage, List<String> operations, String certificateUrl, List<String> certificateChain, String certificateThumbprint, Map<String, Object> additionalAttributes) {
        super(id, type, algorithm, usage, operations, certificateUrl, certificateChain, certificateThumbprint, additionalAttributes);
    }

    @Override
    protected String getKeyType() {
        return PUBLIC_KEY_EC_ALGORITHM;
    }

    @Override
    protected KeySpec getKeySpecification() throws InvalidPublicKeyException {
        Curve curve = Curve.findByName(stringValue("crv"));
        if (null == curve) {
            throw new InvalidPublicKeyException("The key has no curve specification");
        }
        ECNamedCurveParameterSpec curveParameterSpec = ECNamedCurveTable.getParameterSpec(curve.getStandardName());
        return new ECPublicKeySpec(
                new ECPoint(
                        new BigInteger(1, Base64.decodeBase64(stringValue("x"))),
                        new BigInteger(1, Base64.decodeBase64(stringValue("y")))),
                getParameterSpec(curveParameterSpec));
    }

    private ECParameterSpec getParameterSpec(ECNamedCurveParameterSpec parameterSpec) {
        return new ECParameterSpec(
                new java.security.spec.EllipticCurve(
                        new ECFieldFp(parameterSpec.getCurve().getField().getCharacteristic()),
                        parameterSpec.getCurve().getA().toBigInteger(),
                        parameterSpec.getCurve().getB().toBigInteger()),
                new ECPoint(
                        parameterSpec.getG().getAffineXCoord().toBigInteger(),
                        parameterSpec.getG().getAffineYCoord().toBigInteger()),
                parameterSpec.getN(),
                1);
    }

}
