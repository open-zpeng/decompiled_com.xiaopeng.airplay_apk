package org.fourthline.cling.model.state;

import java.lang.reflect.Method;
import org.seamless.util.Reflections;
/* loaded from: classes.dex */
public class GetterStateVariableAccessor extends StateVariableAccessor {
    private Method getter;

    public GetterStateVariableAccessor(Method getter) {
        this.getter = getter;
    }

    public Method getGetter() {
        return this.getter;
    }

    @Override // org.fourthline.cling.model.state.StateVariableAccessor
    public Class<?> getReturnType() {
        return getGetter().getReturnType();
    }

    @Override // org.fourthline.cling.model.state.StateVariableAccessor
    public Object read(Object serviceImpl) throws Exception {
        return Reflections.invoke(getGetter(), serviceImpl, new Object[0]);
    }

    @Override // org.fourthline.cling.model.state.StateVariableAccessor
    public String toString() {
        return super.toString() + " Method: " + getGetter();
    }
}
