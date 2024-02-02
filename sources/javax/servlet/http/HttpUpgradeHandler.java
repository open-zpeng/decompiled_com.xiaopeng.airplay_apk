package javax.servlet.http;
/* loaded from: classes.dex */
public interface HttpUpgradeHandler {
    void destroy();

    void init(WebConnection webConnection);
}
