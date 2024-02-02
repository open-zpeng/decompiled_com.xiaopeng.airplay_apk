package org.fourthline.cling.support.model.dlna.types;
/* loaded from: classes.dex */
public class ScmsFlagType {
    private boolean copyright;
    private boolean original;

    public ScmsFlagType() {
        this.copyright = true;
        this.original = true;
    }

    public ScmsFlagType(boolean copyright, boolean original) {
        this.copyright = copyright;
        this.original = original;
    }

    public boolean isCopyright() {
        return this.copyright;
    }

    public boolean isOriginal() {
        return this.original;
    }
}
