package com.auth0.jwk.kyt;

import java.math.BigInteger;

/**
 * Elliptic curve names, standard names and parameters associated. Used the standard names to look
 * up constant values in SEC 2: Recommended Elliptic Curve Domain Parameters (http://www.secg.org/sec2-v2.pdf).
 * - P-256 (secp256r1) at 2.4.2 on page 10
 * - P-384 (secp384r1) at 2.5.1 on page 11
 * - P-521 (secp521r1) at 2.6.1 on page 12
 * 
 * Note that p, a, b, n values are taken directly form the standard. G coordinates (x, y) are extracted
 * from the value in the standard (in G value in leading 04 must be removed and the remaining part must be cut in
 * half, first half will contain x and second half is the y value).
 */
public enum Curve {
    P_256("P-256", "secp256r1",
            new BigInteger("FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFF", 16),
            new BigInteger("ffffffff00000001000000000000000000000000fffffffffffffffffffffffc", 16),
            new BigInteger("5ac635d8aa3a93e7b3ebbd55769886bc651d06b0cc53b0f63bce3c3e27d2604b", 16),
            new BigInteger("6b17d1f2e12c4247f8bce6e563a440f277037d812deb33a0f4a13945d898c296", 16),
            new BigInteger("4fe342e2fe1a7f9b8ee7eb4a7c0f9e162bce33576b315ececbb6406837bf51f5", 16),
            new BigInteger("FFFFFFFF00000000FFFFFFFFFFFFFFFFBCE6FAADA7179E84F3B9CAC2FC632551", 16)),
    P_384("P-384", "secp384r1",
            new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFFFF0000000000000000FFFFFFFF", 16),
            new BigInteger("fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffeffffffff0000000000000000fffffffc", 16),
            new BigInteger("b3312fa7e23ee7e4988e056be3f82d19181d9c6efe8141120314088f5013875ac656398d8a2ed19d2a85c8edd3ec2aef", 16),
            new BigInteger("aa87ca22be8b05378eb1c71ef320ad746e1d3b628ba79b9859f741e082542a385502f25dbf55296c3a545e3872760ab7", 16),
            new BigInteger("3617de4a96262c6f5d9e98bf9292dc29f8f41dbd289a147ce9da3113b5f0b8c00a60b1ce1d7e819d7a431d7c90ea0e5f", 16),
            new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFC7634D81F4372DDF581A0DB248B0A77AECEC196ACCC52973", 16)),
    P_521("P-521", "secp521r1",
            new BigInteger("1FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16),
            new BigInteger("1fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc", 16),
            new BigInteger("051953eb9618e1c9a1f929a21a0b68540eea2da725b99b315f3b8b489918ef109e156193951ec7e937b1652c0bd3bb1bf073573df883d2c34f1ef451fd46b503f00", 16),
            new BigInteger("0c6858e06b70404e9cd9e3ecb662395b4429c648139053fb521f828af606b4d3dbaa14b5e77efe75928fe1dc127a2ffa8de3348b3c1856a429bf97e7e31c2e5bd66", 16),
            new BigInteger("11839296a789a3bc0045c8a5fb42c7d1bd998f54449579b446817afbd17273e662c97ee72995ef42640c550b9013fad0761353c7086a272c24088be94769fd16650", 16),
            new BigInteger("1FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFA51868783BF2F966B7FCC0148F709A5D03BB5C9B8899C47AEBB6FB71E91386409", 16));

    private final String curveName;

    private final String standardName;
    
    private final BigInteger p;
    
    private final BigInteger a;
    
    private final BigInteger b;
    
    private final BigInteger gx;
    
    private final BigInteger gy;
    
    private final BigInteger n;

    public static Curve findByName(String name) {
        for (Curve curve : values()) {
            if (curve.curveName.equals(name)) {
                return curve;
            }
        }
        return null;
    }

    Curve(String curveName, String standardName, BigInteger p, BigInteger a, BigInteger b, BigInteger gx, BigInteger gy, BigInteger n) {
        this.curveName = curveName;
        this.standardName = standardName;
        this.p = p;
        this.a = a;
        this.b = b;
        this.gx = gx;
        this.gy = gy;
        this.n = n;
    }

    public String getStandardName() {
        return standardName;
    }

    public BigInteger getP() {
        return p;
    }

    public BigInteger getA() {
        return a;
    }

    public BigInteger getB() {
        return b;
    }

    public BigInteger getGx() {
        return gx;
    }

    public BigInteger getGy() {
        return gy;
    }

    public BigInteger getN() {
        return n;
    }
}
