package org.eclipse.jetty.util.ajax;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.eclipse.jetty.util.ajax.JSON;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class JSONPojoConvertor implements JSON.Convertor {
    protected Set<String> _excluded;
    protected boolean _fromJSON;
    protected Map<String, Method> _getters;
    protected Class<?> _pojoClass;
    protected Map<String, Setter> _setters;
    private static final Logger LOG = Log.getLogger(JSONPojoConvertor.class);
    public static final Object[] GETTER_ARG = new Object[0];
    public static final Object[] NULL_ARG = {null};
    private static final Map<Class<?>, NumberType> __numberTypes = new HashMap();
    public static final NumberType SHORT = new NumberType() { // from class: org.eclipse.jetty.util.ajax.JSONPojoConvertor.1
        @Override // org.eclipse.jetty.util.ajax.JSONPojoConvertor.NumberType
        public Object getActualValue(Number number) {
            return new Short(number.shortValue());
        }
    };
    public static final NumberType INTEGER = new NumberType() { // from class: org.eclipse.jetty.util.ajax.JSONPojoConvertor.2
        @Override // org.eclipse.jetty.util.ajax.JSONPojoConvertor.NumberType
        public Object getActualValue(Number number) {
            return new Integer(number.intValue());
        }
    };
    public static final NumberType FLOAT = new NumberType() { // from class: org.eclipse.jetty.util.ajax.JSONPojoConvertor.3
        @Override // org.eclipse.jetty.util.ajax.JSONPojoConvertor.NumberType
        public Object getActualValue(Number number) {
            return new Float(number.floatValue());
        }
    };
    public static final NumberType LONG = new NumberType() { // from class: org.eclipse.jetty.util.ajax.JSONPojoConvertor.4
        @Override // org.eclipse.jetty.util.ajax.JSONPojoConvertor.NumberType
        public Object getActualValue(Number number) {
            return number instanceof Long ? number : new Long(number.longValue());
        }
    };
    public static final NumberType DOUBLE = new NumberType() { // from class: org.eclipse.jetty.util.ajax.JSONPojoConvertor.5
        @Override // org.eclipse.jetty.util.ajax.JSONPojoConvertor.NumberType
        public Object getActualValue(Number number) {
            return number instanceof Double ? number : new Double(number.doubleValue());
        }
    };

    /* loaded from: classes.dex */
    public interface NumberType {
        Object getActualValue(Number number);
    }

    static {
        __numberTypes.put(Short.class, SHORT);
        __numberTypes.put(Short.TYPE, SHORT);
        __numberTypes.put(Integer.class, INTEGER);
        __numberTypes.put(Integer.TYPE, INTEGER);
        __numberTypes.put(Long.class, LONG);
        __numberTypes.put(Long.TYPE, LONG);
        __numberTypes.put(Float.class, FLOAT);
        __numberTypes.put(Float.TYPE, FLOAT);
        __numberTypes.put(Double.class, DOUBLE);
        __numberTypes.put(Double.TYPE, DOUBLE);
    }

    public static NumberType getNumberType(Class<?> clazz) {
        return __numberTypes.get(clazz);
    }

    public JSONPojoConvertor(Class<?> pojoClass) {
        this(pojoClass, null, true);
    }

    public JSONPojoConvertor(Class<?> pojoClass, String[] excluded) {
        this(pojoClass, new HashSet(Arrays.asList(excluded)), true);
    }

    public JSONPojoConvertor(Class<?> pojoClass, Set<String> excluded) {
        this(pojoClass, excluded, true);
    }

    public JSONPojoConvertor(Class<?> pojoClass, Set<String> excluded, boolean fromJSON) {
        this._getters = new HashMap();
        this._setters = new HashMap();
        this._pojoClass = pojoClass;
        this._excluded = excluded;
        this._fromJSON = fromJSON;
        init();
    }

    public JSONPojoConvertor(Class<?> pojoClass, boolean fromJSON) {
        this(pojoClass, null, fromJSON);
    }

    protected void init() {
        String name;
        Method[] methods = this._pojoClass.getMethods();
        for (Method m : methods) {
            if (!Modifier.isStatic(m.getModifiers()) && m.getDeclaringClass() != Object.class) {
                String name2 = m.getName();
                switch (m.getParameterTypes().length) {
                    case 0:
                        if (m.getReturnType() == null) {
                            break;
                        } else {
                            if (name2.startsWith("is") && name2.length() > 2) {
                                name = name2.substring(2, 3).toLowerCase(Locale.ENGLISH) + name2.substring(3);
                            } else if (name2.startsWith("get") && name2.length() > 3) {
                                name = name2.substring(3, 4).toLowerCase(Locale.ENGLISH) + name2.substring(4);
                            }
                            if (!includeField(name, m)) {
                                break;
                            } else {
                                addGetter(name, m);
                                continue;
                            }
                        }
                    case 1:
                        if (name2.startsWith("set")) {
                            if (name2.length() > 3) {
                                String name3 = name2.substring(3, 4).toLowerCase(Locale.ENGLISH) + name2.substring(4);
                                if (includeField(name3, m)) {
                                    addSetter(name3, m);
                                    break;
                                } else {
                                    break;
                                }
                            } else {
                                break;
                            }
                        } else {
                            continue;
                        }
                }
            }
        }
    }

    protected void addGetter(String name, Method method) {
        this._getters.put(name, method);
    }

    protected void addSetter(String name, Method method) {
        this._setters.put(name, new Setter(name, method));
    }

    protected Setter getSetter(String name) {
        return this._setters.get(name);
    }

    protected boolean includeField(String name, Method m) {
        return this._excluded == null || !this._excluded.contains(name);
    }

    protected int getExcludedCount() {
        if (this._excluded == null) {
            return 0;
        }
        return this._excluded.size();
    }

    @Override // org.eclipse.jetty.util.ajax.JSON.Convertor
    public Object fromJSON(Map object) {
        try {
            Object obj = this._pojoClass.newInstance();
            setProps(obj, object);
            return obj;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int setProps(Object obj, Map<?, ?> props) {
        int count = 0;
        for (Map.Entry<?, ?> entry : props.entrySet()) {
            Setter setter = getSetter((String) entry.getKey());
            if (setter != null) {
                try {
                    setter.invoke(obj, entry.getValue());
                    count++;
                } catch (Exception e) {
                    Logger logger = LOG;
                    logger.warn(this._pojoClass.getName() + "#" + setter.getPropertyName() + " not set from " + entry.getValue().getClass().getName() + "=" + entry.getValue().toString(), new Object[0]);
                    log(e);
                }
            }
        }
        return count;
    }

    @Override // org.eclipse.jetty.util.ajax.JSON.Convertor
    public void toJSON(Object obj, JSON.Output out) {
        if (this._fromJSON) {
            out.addClass(this._pojoClass);
        }
        for (Map.Entry<String, Method> entry : this._getters.entrySet()) {
            try {
                out.add(entry.getKey(), entry.getValue().invoke(obj, GETTER_ARG));
            } catch (Exception e) {
                LOG.warn("{} property '{}' excluded. (errors)", this._pojoClass.getName(), entry.getKey());
                log(e);
            }
        }
    }

    protected void log(Throwable t) {
        LOG.ignore(t);
    }

    /* loaded from: classes.dex */
    public static class Setter {
        protected Class<?> _componentType;
        protected NumberType _numberType;
        protected String _propertyName;
        protected Method _setter;
        protected Class<?> _type;

        public Setter(String propertyName, Method method) {
            this._propertyName = propertyName;
            this._setter = method;
            this._type = method.getParameterTypes()[0];
            this._numberType = (NumberType) JSONPojoConvertor.__numberTypes.get(this._type);
            if (this._numberType == null && this._type.isArray()) {
                this._componentType = this._type.getComponentType();
                this._numberType = (NumberType) JSONPojoConvertor.__numberTypes.get(this._componentType);
            }
        }

        public String getPropertyName() {
            return this._propertyName;
        }

        public Method getMethod() {
            return this._setter;
        }

        public NumberType getNumberType() {
            return this._numberType;
        }

        public Class<?> getType() {
            return this._type;
        }

        public Class<?> getComponentType() {
            return this._componentType;
        }

        public boolean isPropertyNumber() {
            return this._numberType != null;
        }

        public void invoke(Object obj, Object value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
            if (value == null) {
                this._setter.invoke(obj, JSONPojoConvertor.NULL_ARG);
            } else {
                invokeObject(obj, value);
            }
        }

        protected void invokeObject(Object obj, Object value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
            if (this._type.isEnum()) {
                if (value instanceof Enum) {
                    this._setter.invoke(obj, value);
                } else {
                    this._setter.invoke(obj, Enum.valueOf(this._type, value.toString()));
                }
            } else if (this._numberType != null && (value instanceof Number)) {
                this._setter.invoke(obj, this._numberType.getActualValue((Number) value));
            } else if (Character.TYPE.equals(this._type) || Character.class.equals(this._type)) {
                this._setter.invoke(obj, Character.valueOf(String.valueOf(value).charAt(0)));
            } else if (this._componentType != null && value.getClass().isArray()) {
                if (this._numberType == null) {
                    int len = Array.getLength(value);
                    Object array = Array.newInstance(this._componentType, len);
                    try {
                        System.arraycopy(value, 0, array, 0, len);
                        this._setter.invoke(obj, array);
                        return;
                    } catch (Exception e) {
                        JSONPojoConvertor.LOG.ignore(e);
                        this._setter.invoke(obj, value);
                        return;
                    }
                }
                Object[] old = (Object[]) value;
                Object array2 = Array.newInstance(this._componentType, old.length);
                for (int i = 0; i < old.length; i++) {
                    try {
                        Array.set(array2, i, this._numberType.getActualValue((Number) old[i]));
                    } catch (Exception e2) {
                        JSONPojoConvertor.LOG.ignore(e2);
                        this._setter.invoke(obj, value);
                        return;
                    }
                }
                this._setter.invoke(obj, array2);
            } else {
                this._setter.invoke(obj, value);
            }
        }
    }
}
