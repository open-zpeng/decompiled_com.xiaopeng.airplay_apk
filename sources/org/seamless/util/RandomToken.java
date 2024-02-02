package org.seamless.util;

import java.security.SecureRandom;
import java.util.Random;
/* loaded from: classes.dex */
public class RandomToken {
    protected final Random random;

    public RandomToken() {
        try {
            this.random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            this.random.nextBytes(new byte[1]);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public String generate() {
        String token = null;
        while (true) {
            if (token == null || token.length() == 0) {
                long r0 = this.random.nextLong();
                if (r0 < 0) {
                    r0 = -r0;
                }
                long r1 = this.random.nextLong();
                if (r1 < 0) {
                    r1 = -r1;
                }
                token = Long.toString(r0, 36) + Long.toString(r1, 36);
            } else {
                return token;
            }
        }
    }
}
