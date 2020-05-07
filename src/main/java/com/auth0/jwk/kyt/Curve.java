package com.auth0.jwk.kyt;

/**
 * Elliptic curve names and standard names associated to use in Bouncy Castle reference.
 */
public enum Curve {
    P_256("P-256", "secp256r1"),
    P_384("P-384", "secp384r1"),
    P_521("P-521", "secp521r1");

    private final String curveName;

    private final String standardName;

    public static Curve findByName(String name) {
        for (Curve curve : values()) {
            if (curve.curveName.equals(name)) {
                return curve;
            }
        }
        return null;
    }

    Curve(String curveName, String standardName) {
        this.curveName = curveName;
        this.standardName = standardName;
    }

    public String getStandardName() {
        return standardName;
    }
}
