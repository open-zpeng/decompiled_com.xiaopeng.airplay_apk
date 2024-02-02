package javax.enterprise.event;

import java.util.concurrent.Executor;
import javax.enterprise.event.ImmutableNotificationOptions;
/* loaded from: classes.dex */
public interface NotificationOptions {

    /* loaded from: classes.dex */
    public interface Builder {
        NotificationOptions build();

        Builder set(String str, Object obj);

        Builder setExecutor(Executor executor);
    }

    Object get(String str);

    Executor getExecutor();

    static NotificationOptions ofExecutor(Executor executor) {
        return builder().setExecutor(executor).build();
    }

    static NotificationOptions of(String optionName, Object optionValue) {
        return builder().set(optionName, optionValue).build();
    }

    static Builder builder() {
        return new ImmutableNotificationOptions.Builder();
    }
}
