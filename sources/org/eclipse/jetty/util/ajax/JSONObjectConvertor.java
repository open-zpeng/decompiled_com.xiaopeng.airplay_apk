package org.eclipse.jetty.util.ajax;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.eclipse.jetty.util.ajax.JSON;
/* loaded from: classes.dex */
public class JSONObjectConvertor implements JSON.Convertor {
    private Set _excluded;
    private boolean _fromJSON;

    public JSONObjectConvertor() {
        this._excluded = null;
        this._fromJSON = false;
    }

    public JSONObjectConvertor(boolean fromJSON) {
        this._excluded = null;
        this._fromJSON = fromJSON;
    }

    public JSONObjectConvertor(boolean fromJSON, String[] excluded) {
        this._excluded = null;
        this._fromJSON = fromJSON;
        if (excluded != null) {
            this._excluded = new HashSet(Arrays.asList(excluded));
        }
    }

    @Override // org.eclipse.jetty.util.ajax.JSON.Convertor
    public Object fromJSON(Map map) {
        if (this._fromJSON) {
            throw new UnsupportedOperationException();
        }
        return map;
    }

    @Override // org.eclipse.jetty.util.ajax.JSON.Convertor
    public void toJSON(Object obj, JSON.Output out) {
        String name;
        try {
            obj.getClass();
            if (this._fromJSON) {
                out.addClass(obj.getClass());
            }
            Method[] methods = obj.getClass().getMethods();
            for (Method m : methods) {
                if (!Modifier.isStatic(m.getModifiers()) && m.getParameterTypes().length == 0 && m.getReturnType() != null && m.getDeclaringClass() != Object.class) {
                    String name2 = m.getName();
                    if (name2.startsWith("is")) {
                        name = name2.substring(2, 3).toLowerCase(Locale.ENGLISH) + name2.substring(3);
                    } else if (name2.startsWith("get")) {
                        name = name2.substring(3, 4).toLowerCase(Locale.ENGLISH) + name2.substring(4);
                    }
                    if (includeField(name, obj, m)) {
                        out.add(name, m.invoke(obj, null));
                    }
                }
            }
        } catch (Throwable e) {
            throw new IllegalArgumentException(e);
        }
    }

    protected boolean includeField(String name, Object o, Method m) {
        return this._excluded == null || !this._excluded.contains(name);
    }
}
