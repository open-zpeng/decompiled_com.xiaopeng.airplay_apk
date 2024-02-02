package org.fourthline.cling.model;

import java.util.Date;
/* loaded from: classes.dex */
public class ExpirationDetails {
    public static final int UNLIMITED_AGE = 0;
    private static String simpleName = ExpirationDetails.class.getSimpleName();
    private long lastRefreshTimestampSeconds;
    private int maxAgeSeconds;

    public ExpirationDetails() {
        this.maxAgeSeconds = 0;
        this.lastRefreshTimestampSeconds = getCurrentTimestampSeconds();
    }

    public ExpirationDetails(int maxAgeSeconds) {
        this.maxAgeSeconds = 0;
        this.lastRefreshTimestampSeconds = getCurrentTimestampSeconds();
        this.maxAgeSeconds = maxAgeSeconds;
    }

    public int getMaxAgeSeconds() {
        return this.maxAgeSeconds;
    }

    public long getLastRefreshTimestampSeconds() {
        return this.lastRefreshTimestampSeconds;
    }

    public void setLastRefreshTimestampSeconds(long lastRefreshTimestampSeconds) {
        this.lastRefreshTimestampSeconds = lastRefreshTimestampSeconds;
    }

    public void stampLastRefresh() {
        setLastRefreshTimestampSeconds(getCurrentTimestampSeconds());
    }

    public boolean hasExpired() {
        return hasExpired(false);
    }

    public boolean hasExpired(boolean halfTime) {
        if (this.maxAgeSeconds != 0) {
            if (this.lastRefreshTimestampSeconds + (this.maxAgeSeconds / (halfTime ? 2 : 1)) < getCurrentTimestampSeconds()) {
                return true;
            }
        }
        return false;
    }

    public long getSecondsUntilExpiration() {
        if (this.maxAgeSeconds == 0) {
            return 2147483647L;
        }
        return (this.lastRefreshTimestampSeconds + this.maxAgeSeconds) - getCurrentTimestampSeconds();
    }

    protected long getCurrentTimestampSeconds() {
        return new Date().getTime() / 1000;
    }

    public String toString() {
        return "(" + simpleName + ") MAX AGE: " + this.maxAgeSeconds;
    }
}
