package org.fourthline.cling.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.StateVariable;
import org.fourthline.cling.model.state.StateVariableAccessor;
import org.fourthline.cling.model.state.StateVariableValue;
import org.seamless.util.Exceptions;
import org.seamless.util.Reflections;
/* loaded from: classes.dex */
public class DefaultServiceManager<T> implements ServiceManager<T> {
    private static Logger log = Logger.getLogger(DefaultServiceManager.class.getName());
    protected final ReentrantLock lock;
    protected PropertyChangeSupport propertyChangeSupport;
    protected final LocalService<T> service;
    protected final Class<T> serviceClass;
    protected T serviceImpl;

    protected DefaultServiceManager(LocalService<T> service) {
        this(service, null);
    }

    public DefaultServiceManager(LocalService<T> service, Class<T> serviceClass) {
        this.lock = new ReentrantLock(true);
        this.service = service;
        this.serviceClass = serviceClass;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void lock() {
        try {
            if (this.lock.tryLock(getLockTimeoutMillis(), TimeUnit.MILLISECONDS)) {
                if (log.isLoggable(Level.FINEST)) {
                    log.finest("Acquired lock");
                    return;
                }
                return;
            }
            throw new RuntimeException("Failed to acquire lock in milliseconds: " + getLockTimeoutMillis());
        } catch (InterruptedException e) {
            throw new RuntimeException("Failed to acquire lock:" + e);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void unlock() {
        if (log.isLoggable(Level.FINEST)) {
            log.finest("Releasing lock");
        }
        this.lock.unlock();
    }

    protected int getLockTimeoutMillis() {
        return 500;
    }

    @Override // org.fourthline.cling.model.ServiceManager
    public LocalService<T> getService() {
        return this.service;
    }

    @Override // org.fourthline.cling.model.ServiceManager
    public T getImplementation() {
        lock();
        try {
            if (this.serviceImpl == null) {
                init();
            }
            return this.serviceImpl;
        } finally {
            unlock();
        }
    }

    @Override // org.fourthline.cling.model.ServiceManager
    public PropertyChangeSupport getPropertyChangeSupport() {
        lock();
        try {
            if (this.propertyChangeSupport == null) {
                init();
            }
            return this.propertyChangeSupport;
        } finally {
            unlock();
        }
    }

    @Override // org.fourthline.cling.model.ServiceManager
    public void execute(Command<T> cmd) throws Exception {
        lock();
        try {
            cmd.execute(this);
        } finally {
            unlock();
        }
    }

    @Override // org.fourthline.cling.model.ServiceManager
    public Collection<StateVariableValue> getCurrentState() throws Exception {
        StateVariable[] stateVariables;
        lock();
        try {
            Collection<StateVariableValue> values = readInitialEventedStateVariableValues();
            if (values != null) {
                log.fine("Obtained initial state variable values for event, skipping individual state variable accessors");
                return values;
            }
            Collection<StateVariableValue> values2 = new ArrayList<>();
            for (StateVariable stateVariable : getService().getStateVariables()) {
                if (stateVariable.getEventDetails().isSendEvents()) {
                    StateVariableAccessor accessor = getService().getAccessor(stateVariable);
                    if (accessor == null) {
                        throw new IllegalStateException("No accessor for evented state variable");
                    }
                    values2.add(accessor.read(stateVariable, getImplementation()));
                }
            }
            return values2;
        } finally {
            unlock();
        }
    }

    protected Collection<StateVariableValue> getCurrentState(String[] variableNames) throws Exception {
        lock();
        try {
            Collection<StateVariableValue> values = new ArrayList<>();
            for (String variableName : variableNames) {
                String variableName2 = variableName.trim();
                StateVariable stateVariable = getService().getStateVariable(variableName2);
                if (stateVariable != null && stateVariable.getEventDetails().isSendEvents()) {
                    StateVariableAccessor accessor = getService().getAccessor(stateVariable);
                    if (accessor == null) {
                        log.warning("Ignoring evented state variable without accessor: " + variableName2);
                    } else {
                        values.add(accessor.read(stateVariable, getImplementation()));
                    }
                }
                log.fine("Ignoring unknown or non-evented state variable: " + variableName2);
            }
            return values;
        } finally {
            unlock();
        }
    }

    protected void init() {
        log.fine("No service implementation instance available, initializing...");
        try {
            this.serviceImpl = createServiceInstance();
            this.propertyChangeSupport = createPropertyChangeSupport(this.serviceImpl);
            this.propertyChangeSupport.addPropertyChangeListener(createPropertyChangeListener(this.serviceImpl));
        } catch (Exception ex) {
            throw new RuntimeException("Could not initialize implementation: " + ex, ex);
        }
    }

    protected T createServiceInstance() throws Exception {
        if (this.serviceClass == null) {
            throw new IllegalStateException("Subclass has to provide service class or override createServiceInstance()");
        }
        try {
            return this.serviceClass.getConstructor(LocalService.class).newInstance(getService());
        } catch (NoSuchMethodException e) {
            Logger logger = log;
            logger.fine("Creating new service implementation instance with no-arg constructor: " + this.serviceClass.getName());
            return this.serviceClass.newInstance();
        }
    }

    protected PropertyChangeSupport createPropertyChangeSupport(T serviceImpl) throws Exception {
        Method m = Reflections.getGetterMethod(serviceImpl.getClass(), "propertyChangeSupport");
        if (m != null && PropertyChangeSupport.class.isAssignableFrom(m.getReturnType())) {
            Logger logger = log;
            logger.fine("Service implementation instance offers PropertyChangeSupport, using that: " + serviceImpl.getClass().getName());
            return (PropertyChangeSupport) m.invoke(serviceImpl, new Object[0]);
        }
        Logger logger2 = log;
        logger2.fine("Creating new PropertyChangeSupport for service implementation: " + serviceImpl.getClass().getName());
        return new PropertyChangeSupport(serviceImpl);
    }

    protected PropertyChangeListener createPropertyChangeListener(T serviceImpl) throws Exception {
        return new DefaultPropertyChangeListener();
    }

    protected Collection<StateVariableValue> readInitialEventedStateVariableValues() throws Exception {
        return null;
    }

    public String toString() {
        return "(" + getClass().getSimpleName() + ") Implementation: " + this.serviceImpl;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* loaded from: classes.dex */
    public class DefaultPropertyChangeListener implements PropertyChangeListener {
        protected DefaultPropertyChangeListener() {
        }

        @Override // java.beans.PropertyChangeListener
        public void propertyChange(PropertyChangeEvent e) {
            Logger logger = DefaultServiceManager.log;
            logger.finer("Property change event on local service: " + e.getPropertyName());
            if (e.getPropertyName().equals(ServiceManager.EVENTED_STATE_VARIABLES)) {
                return;
            }
            String[] variableNames = ModelUtil.fromCommaSeparatedList(e.getPropertyName());
            Logger logger2 = DefaultServiceManager.log;
            logger2.fine("Changed variable names: " + Arrays.toString(variableNames));
            try {
                Collection<StateVariableValue> currentValues = DefaultServiceManager.this.getCurrentState(variableNames);
                if (!currentValues.isEmpty()) {
                    DefaultServiceManager.this.getPropertyChangeSupport().firePropertyChange(ServiceManager.EVENTED_STATE_VARIABLES, (Object) null, currentValues);
                }
            } catch (Exception ex) {
                Logger logger3 = DefaultServiceManager.log;
                Level level = Level.SEVERE;
                logger3.log(level, "Error reading state of service after state variable update event: " + Exceptions.unwrap(ex), (Throwable) ex);
            }
        }
    }
}
