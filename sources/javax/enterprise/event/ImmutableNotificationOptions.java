package javax.enterprise.event;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import javax.enterprise.event.NotificationOptions;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class ImmutableNotificationOptions implements NotificationOptions {
    private final Executor executor;
    private final Map<String, Object> options;

    private ImmutableNotificationOptions(Executor executor, Map<String, Object> options) {
        this.executor = executor;
        this.options = new HashMap(options);
    }

    @Override // javax.enterprise.event.NotificationOptions
    public Executor getExecutor() {
        return this.executor;
    }

    @Override // javax.enterprise.event.NotificationOptions
    public Object get(String name) {
        return this.options.get(name);
    }

    /* loaded from: classes.dex */
    static class Builder implements NotificationOptions.Builder {
        private Executor executor;
        private Map<String, Object> options = new HashMap();

        @Override // javax.enterprise.event.NotificationOptions.Builder
        public Builder setExecutor(Executor executor) {
            this.executor = executor;
            return this;
        }

        @Override // javax.enterprise.event.NotificationOptions.Builder
        public Builder set(String name, Object value) {
            this.options.put(name, value);
            return this;
        }

        @Override // javax.enterprise.event.NotificationOptions.Builder
        public ImmutableNotificationOptions build() {
            return new ImmutableNotificationOptions(this.executor, this.options);
        }
    }
}
