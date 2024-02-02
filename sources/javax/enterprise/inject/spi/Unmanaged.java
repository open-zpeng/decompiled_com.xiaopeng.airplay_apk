package javax.enterprise.inject.spi;

import javax.enterprise.context.spi.CreationalContext;
/* loaded from: classes.dex */
public class Unmanaged<T> {
    private final BeanManager beanManager;
    private final InjectionTarget<T> injectionTarget;

    public Unmanaged(BeanManager manager, Class<T> clazz) {
        this.beanManager = manager;
        AnnotatedType<T> type = manager.createAnnotatedType(clazz);
        this.injectionTarget = manager.getInjectionTargetFactory(type).createInjectionTarget(null);
    }

    public Unmanaged(Class<T> clazz) {
        this(CDI.current().getBeanManager(), clazz);
    }

    public UnmanagedInstance<T> newInstance() {
        return new UnmanagedInstance<>(this.beanManager, this.injectionTarget);
    }

    /* loaded from: classes.dex */
    public static class UnmanagedInstance<T> {
        private final CreationalContext<T> ctx;
        private boolean disposed;
        private final InjectionTarget<T> injectionTarget;
        private T instance;

        private UnmanagedInstance(BeanManager beanManager, InjectionTarget<T> injectionTarget) {
            this.disposed = false;
            this.injectionTarget = injectionTarget;
            this.ctx = beanManager.createCreationalContext(null);
        }

        public T get() {
            return this.instance;
        }

        public UnmanagedInstance<T> produce() {
            if (this.instance != null) {
                throw new IllegalStateException("Trying to call produce() on already constructed instance");
            }
            if (this.disposed) {
                throw new IllegalStateException("Trying to call produce() on an already disposed instance");
            }
            this.instance = this.injectionTarget.produce(this.ctx);
            return this;
        }

        public UnmanagedInstance<T> inject() {
            if (this.instance == null) {
                throw new IllegalStateException("Trying to call inject() before produce() was called");
            }
            if (this.disposed) {
                throw new IllegalStateException("Trying to call inject() on already disposed instance");
            }
            this.injectionTarget.inject(this.instance, this.ctx);
            return this;
        }

        public UnmanagedInstance<T> postConstruct() {
            if (this.instance == null) {
                throw new IllegalStateException("Trying to call postConstruct() before produce() was called");
            }
            if (this.disposed) {
                throw new IllegalStateException("Trying to call postConstruct() on already disposed instance");
            }
            this.injectionTarget.postConstruct(this.instance);
            return this;
        }

        public UnmanagedInstance<T> preDestroy() {
            if (this.instance == null) {
                throw new IllegalStateException("Trying to call preDestroy() before produce() was called");
            }
            if (this.disposed) {
                throw new IllegalStateException("Trying to call preDestroy() on already disposed instance");
            }
            this.injectionTarget.preDestroy(this.instance);
            return this;
        }

        public UnmanagedInstance<T> dispose() {
            if (this.instance == null) {
                throw new IllegalStateException("Trying to call dispose() before produce() was called");
            }
            if (this.disposed) {
                throw new IllegalStateException("Trying to call dispose() on already disposed instance");
            }
            this.disposed = true;
            this.injectionTarget.dispose(this.instance);
            this.ctx.release();
            return this;
        }
    }
}
