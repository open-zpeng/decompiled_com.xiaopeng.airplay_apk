package org.eclipse.jetty.security;
/* loaded from: classes.dex */
public enum UserDataConstraint {
    None,
    Integral,
    Confidential;

    public static UserDataConstraint get(int dataConstraint) {
        if (dataConstraint >= -1 && dataConstraint <= 2) {
            return dataConstraint == -1 ? None : values()[dataConstraint];
        }
        throw new IllegalArgumentException("Expected -1, 0, 1, or 2, not: " + dataConstraint);
    }

    public UserDataConstraint combine(UserDataConstraint other) {
        return compareTo(other) < 0 ? this : other;
    }
}
