package org.seamless.statemachine;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
/* loaded from: classes.dex */
public class StateMachineInvocationHandler implements InvocationHandler {
    public static final String METHOD_ON_ENTRY = "onEntry";
    public static final String METHOD_ON_EXIT = "onExit";
    private static Logger log = Logger.getLogger(StateMachineInvocationHandler.class.getName());
    Object currentState;
    final Class initialStateClass;
    final Map<Class, Object> stateObjects = new ConcurrentHashMap();

    /* JADX INFO: Access modifiers changed from: package-private */
    public StateMachineInvocationHandler(List<Class<?>> stateClasses, Class<?> initialStateClass, Class[] constructorArgumentTypes, Object[] constructorArguments) {
        Object state;
        Logger logger = log;
        logger.fine("Creating state machine with initial state: " + initialStateClass);
        this.initialStateClass = initialStateClass;
        for (Class<?> stateClass : stateClasses) {
            if (constructorArgumentTypes != null) {
                try {
                    state = stateClass.getConstructor(constructorArgumentTypes).newInstance(constructorArguments);
                    continue;
                } catch (NoSuchMethodException ex) {
                    throw new RuntimeException("State " + stateClass.getName() + " has the wrong constructor: " + ex, ex);
                } catch (Exception ex2) {
                    throw new RuntimeException("State " + stateClass.getName() + " can't be instantiated: " + ex2, ex2);
                }
            } else {
                state = stateClass.newInstance();
                continue;
            }
            Logger logger2 = log;
            logger2.fine("Adding state instance: " + state.getClass().getName());
            this.stateObjects.put(stateClass, state);
        }
        if (!this.stateObjects.containsKey(initialStateClass)) {
            throw new RuntimeException("Initial state not in list of states: " + initialStateClass);
        }
        this.currentState = this.stateObjects.get(initialStateClass);
        synchronized (this) {
            invokeEntryMethod(this.currentState);
        }
    }

    @Override // java.lang.reflect.InvocationHandler
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        synchronized (this) {
            if (StateMachine.METHOD_CURRENT_STATE.equals(method.getName()) && method.getParameterTypes().length == 0) {
                return this.currentState;
            } else if (StateMachine.METHOD_FORCE_STATE.equals(method.getName()) && method.getParameterTypes().length == 1 && args.length == 1 && args[0] != null && (args[0] instanceof Class)) {
                Object forcedState = this.stateObjects.get((Class) args[0]);
                if (forcedState == null) {
                    throw new TransitionException("Can't force to invalid state: " + args[0]);
                }
                Logger logger = log;
                logger.finer("Forcing state machine into state: " + forcedState.getClass().getName());
                invokeExitMethod(this.currentState);
                this.currentState = forcedState;
                invokeEntryMethod(forcedState);
                return null;
            } else {
                Method signalMethod = getMethodOfCurrentState(method);
                Logger logger2 = log;
                logger2.fine("Invoking signal method of current state: " + signalMethod.toString());
                Object methodReturn = signalMethod.invoke(this.currentState, args);
                if (methodReturn != null && (methodReturn instanceof Class)) {
                    Class nextStateClass = (Class) methodReturn;
                    if (this.stateObjects.containsKey(nextStateClass)) {
                        Logger logger3 = log;
                        logger3.fine("Executing transition to next state: " + nextStateClass.getName());
                        invokeExitMethod(this.currentState);
                        this.currentState = this.stateObjects.get(nextStateClass);
                        invokeEntryMethod(this.currentState);
                    }
                }
                return methodReturn;
            }
        }
    }

    private Method getMethodOfCurrentState(Method method) {
        try {
            return this.currentState.getClass().getMethod(method.getName(), method.getParameterTypes());
        } catch (NoSuchMethodException e) {
            throw new TransitionException("State '" + this.currentState.getClass().getName() + "' doesn't support signal '" + method.getName() + "'");
        }
    }

    private void invokeEntryMethod(Object state) {
        Logger logger = log;
        logger.fine("Trying to invoke entry method of state: " + state.getClass().getName());
        try {
            Method onEntryMethod = state.getClass().getMethod(METHOD_ON_ENTRY, new Class[0]);
            onEntryMethod.invoke(state, new Object[0]);
        } catch (NoSuchMethodException e) {
            Logger logger2 = log;
            logger2.finer("No entry method found on state: " + state.getClass().getName());
        } catch (Exception ex) {
            throw new TransitionException("State '" + state.getClass().getName() + "' entry method threw exception: " + ex, ex);
        }
    }

    private void invokeExitMethod(Object state) {
        Logger logger = log;
        logger.finer("Trying to invoking exit method of state: " + state.getClass().getName());
        try {
            Method onExitMethod = state.getClass().getMethod(METHOD_ON_EXIT, new Class[0]);
            onExitMethod.invoke(state, new Object[0]);
        } catch (NoSuchMethodException e) {
            Logger logger2 = log;
            logger2.finer("No exit method found on state: " + state.getClass().getName());
        } catch (Exception ex) {
            throw new TransitionException("State '" + state.getClass().getName() + "' exit method threw exception: " + ex, ex);
        }
    }
}
