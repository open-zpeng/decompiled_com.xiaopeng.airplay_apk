package org.fourthline.cling.model.gena;

import android.util.Log;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import org.fourthline.cling.model.ServiceManager;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.StateVariable;
import org.fourthline.cling.model.state.StateVariableValue;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.seamless.util.Exceptions;
/* loaded from: classes.dex */
public abstract class LocalGENASubscription extends GENASubscription<LocalService> implements PropertyChangeListener {
    private static Logger log = Logger.getLogger(LocalGENASubscription.class.getName());
    private final String TAG;
    final List<URL> callbackURLs;
    final Map<String, Long> lastSentNumericValue;
    final Map<String, Long> lastSentTimestamp;

    public abstract void ended(CancelReason cancelReason);

    protected LocalGENASubscription(LocalService service, List<URL> callbackURLs) throws Exception {
        super(service);
        this.TAG = "LocalGENASubscription";
        this.lastSentTimestamp = new HashMap();
        this.lastSentNumericValue = new HashMap();
        this.callbackURLs = callbackURLs;
    }

    public LocalGENASubscription(LocalService service, Integer requestedDurationSeconds, List<URL> callbackURLs) throws Exception {
        super(service);
        this.TAG = "LocalGENASubscription";
        this.lastSentTimestamp = new HashMap();
        this.lastSentNumericValue = new HashMap();
        setSubscriptionDuration(requestedDurationSeconds);
        Log.i("LocalGENASubscription", "Reading initial state of local service at subscription time");
        long currentTime = new Date().getTime();
        this.currentValues.clear();
        Collection<StateVariableValue> values = getService().getManager().getCurrentState();
        Log.d("LocalGENASubscription", "Got evented state variable values: " + values.size());
        for (StateVariableValue value : values) {
            this.currentValues.put(value.getStateVariable().getName(), value);
            Log.i("LocalGENASubscription", "Read state variable value '" + value.getStateVariable().getName() + "': " + value.toString());
            this.lastSentTimestamp.put(value.getStateVariable().getName(), Long.valueOf(currentTime));
            if (value.getStateVariable().isModeratedNumericType()) {
                this.lastSentNumericValue.put(value.getStateVariable().getName(), Long.valueOf(value.toString()));
            }
        }
        this.subscriptionId = "uuid:" + UUID.randomUUID();
        this.currentSequence = new UnsignedIntegerFourBytes(0L);
        this.callbackURLs = callbackURLs;
    }

    public synchronized List<URL> getCallbackURLs() {
        return this.callbackURLs;
    }

    public synchronized void registerOnService() {
        getService().getManager().getPropertyChangeSupport().addPropertyChangeListener(this);
    }

    public synchronized void establish() {
        established();
    }

    public synchronized void end(CancelReason reason) {
        try {
            getService().getManager().getPropertyChangeSupport().removePropertyChangeListener(this);
        } catch (Exception ex) {
            Logger logger = log;
            logger.warning("Removal of local service property change listener failed: " + Exceptions.unwrap(ex));
        }
        ended(reason);
    }

    @Override // java.beans.PropertyChangeListener
    public synchronized void propertyChange(PropertyChangeEvent e) {
        Logger logger = log;
        logger.info("Eventing triggered, getting state for subscription: " + getSubscriptionId());
        if (e.getPropertyName().equals(ServiceManager.EVENTED_STATE_VARIABLES)) {
            long currentTime = new Date().getTime();
            Collection<StateVariableValue> newValues = (Collection) e.getNewValue();
            Set<String> excludedVariables = moderateStateVariables(currentTime, newValues);
            this.currentValues.clear();
            for (StateVariableValue newValue : newValues) {
                String name = newValue.getStateVariable().getName();
                if (!excludedVariables.contains(name)) {
                    Logger logger2 = log;
                    logger2.fine("Adding state variable value to current values of event: " + newValue.getStateVariable() + " = " + newValue);
                    this.currentValues.put(newValue.getStateVariable().getName(), newValue);
                    this.lastSentTimestamp.put(name, Long.valueOf(currentTime));
                    if (newValue.getStateVariable().isModeratedNumericType()) {
                        this.lastSentNumericValue.put(name, Long.valueOf(newValue.toString()));
                    }
                }
            }
            if (this.currentValues.size() > 0) {
                Logger logger3 = log;
                logger3.fine("Propagating new state variable values to subscription: " + this);
                eventReceived();
            } else {
                log.fine("No state variable values for event (all moderated out?), not triggering event");
            }
        } else if (e.getPropertyName().equals("LastChange")) {
            eventReceived();
        }
    }

    protected synchronized Set<String> moderateStateVariables(long currentTime, Collection<StateVariableValue> values) {
        Set<String> excludedVariables;
        Iterator<StateVariableValue> it;
        excludedVariables = new HashSet<>();
        Iterator<StateVariableValue> it2 = values.iterator();
        while (it2.hasNext()) {
            StateVariableValue stateVariableValue = it2.next();
            StateVariable stateVariable = stateVariableValue.getStateVariable();
            String stateVariableName = stateVariableValue.getStateVariable().getName();
            if (stateVariable.getEventDetails().getEventMaximumRateMilliseconds() == 0 && stateVariable.getEventDetails().getEventMinimumDelta() == 0) {
                log.finer("Variable is not moderated: " + stateVariable);
            } else if (!this.lastSentTimestamp.containsKey(stateVariableName)) {
                log.finer("Variable is moderated but was never sent before: " + stateVariable);
            } else {
                if (stateVariable.getEventDetails().getEventMaximumRateMilliseconds() > 0) {
                    long timestampLastSent = this.lastSentTimestamp.get(stateVariableName).longValue();
                    long timestampNextSend = stateVariable.getEventDetails().getEventMaximumRateMilliseconds() + timestampLastSent;
                    if (currentTime <= timestampNextSend) {
                        log.finer("Excluding state variable with maximum rate: " + stateVariable);
                        excludedVariables.add(stateVariableName);
                    }
                }
                if (stateVariable.isModeratedNumericType() && this.lastSentNumericValue.get(stateVariableName) != null) {
                    long oldValue = Long.valueOf(this.lastSentNumericValue.get(stateVariableName).longValue()).longValue();
                    long newValue = Long.valueOf(stateVariableValue.toString()).longValue();
                    long minDelta = stateVariable.getEventDetails().getEventMinimumDelta();
                    if (newValue > oldValue && newValue - oldValue < minDelta) {
                        Logger logger = log;
                        StringBuilder sb = new StringBuilder();
                        it = it2;
                        sb.append("Excluding state variable with minimum delta: ");
                        sb.append(stateVariable);
                        logger.finer(sb.toString());
                        excludedVariables.add(stateVariableName);
                    } else {
                        it = it2;
                        if (newValue < oldValue && oldValue - newValue < minDelta) {
                            log.finer("Excluding state variable with minimum delta: " + stateVariable);
                            excludedVariables.add(stateVariableName);
                        }
                    }
                } else {
                    it = it2;
                }
                it2 = it;
            }
        }
        return excludedVariables;
    }

    public synchronized void incrementSequence() {
        this.currentSequence.increment(true);
    }

    public synchronized void setSubscriptionDuration(Integer requestedDurationSeconds) {
        int intValue;
        if (requestedDurationSeconds == null) {
            intValue = 1800;
        } else {
            intValue = requestedDurationSeconds.intValue();
        }
        this.requestedDurationSeconds = intValue;
        setActualSubscriptionDurationSeconds(this.requestedDurationSeconds);
    }
}
