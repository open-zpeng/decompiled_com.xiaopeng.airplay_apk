package javax.enterprise.context;
/* loaded from: classes.dex */
public interface Conversation {
    void begin();

    void begin(String str);

    void end();

    String getId();

    long getTimeout();

    boolean isTransient();

    void setTimeout(long j);
}
