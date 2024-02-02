package org.seamless.statemachine;

import java.lang.reflect.Proxy;
import java.util.Arrays;
/* loaded from: classes.dex */
public class StateMachineBuilder {
    public static <T extends StateMachine> T build(Class<T> stateMachine, Class initialState) {
        return (T) build(stateMachine, initialState, null, null);
    }

    public static <T extends StateMachine> T build(Class<T> stateMachine, Class initialState, Class[] constructorArgumentTypes, Object[] constructorArguments) {
        return (T) Proxy.newProxyInstance(stateMachine.getClassLoader(), new Class[]{stateMachine}, new StateMachineInvocationHandler(Arrays.asList(((States) stateMachine.getAnnotation(States.class)).value()), initialState, constructorArgumentTypes, constructorArguments));
    }
}
