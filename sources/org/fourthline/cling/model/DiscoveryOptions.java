package org.fourthline.cling.model;
/* loaded from: classes.dex */
public class DiscoveryOptions {
    private static String simpleName = DiscoveryOptions.class.getSimpleName();
    protected boolean advertised;
    protected boolean byeByeBeforeFirstAlive;

    public DiscoveryOptions(boolean advertised) {
        this.advertised = advertised;
    }

    public DiscoveryOptions(boolean advertised, boolean byeByeBeforeFirstAlive) {
        this.advertised = advertised;
        this.byeByeBeforeFirstAlive = byeByeBeforeFirstAlive;
    }

    public boolean isAdvertised() {
        return this.advertised;
    }

    public boolean isByeByeBeforeFirstAlive() {
        return this.byeByeBeforeFirstAlive;
    }

    public String toString() {
        return "(" + simpleName + ") advertised: " + isAdvertised() + " byebyeBeforeFirstAlive: " + isByeByeBeforeFirstAlive();
    }
}
