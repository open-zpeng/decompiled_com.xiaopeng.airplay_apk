package javax.enterprise.util;

import java.io.Serializable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
/* loaded from: classes.dex */
public abstract class TypeLiteral<T> implements Serializable {
    private static final long serialVersionUID = 1;
    private transient Type actualType;

    protected TypeLiteral() {
    }

    public final Type getType() {
        if (this.actualType == null) {
            Class<?> typeLiteralSubclass = getTypeLiteralSubclass(getClass());
            if (typeLiteralSubclass == null) {
                throw new RuntimeException(getClass() + " is not a subclass of TypeLiteral");
            }
            this.actualType = getTypeParameter(typeLiteralSubclass);
            if (this.actualType == null) {
                throw new RuntimeException(getClass() + " does not specify the type parameter T of TypeLiteral<T>");
            }
        }
        return this.actualType;
    }

    public final Class<T> getRawType() {
        Type type = getType();
        if (type instanceof Class) {
            return (Class) type;
        }
        if (type instanceof ParameterizedType) {
            return (Class) ((ParameterizedType) type).getRawType();
        }
        if (type instanceof GenericArrayType) {
            return Object[].class;
        }
        throw new RuntimeException("Illegal type");
    }

    private static Class<?> getTypeLiteralSubclass(Class<?> clazz) {
        Class<?> superclass = clazz.getSuperclass();
        if (superclass.equals(TypeLiteral.class)) {
            return clazz;
        }
        if (superclass.equals(Object.class)) {
            return null;
        }
        return getTypeLiteralSubclass(superclass);
    }

    private static Type getTypeParameter(Class<?> superclass) {
        Type type = superclass.getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            if (parameterizedType.getActualTypeArguments().length == 1) {
                return parameterizedType.getActualTypeArguments()[0];
            }
            return null;
        }
        return null;
    }

    public boolean equals(Object obj) {
        if (obj instanceof TypeLiteral) {
            TypeLiteral<?> that = (TypeLiteral) obj;
            return getType().equals(that.getType());
        }
        return false;
    }

    public int hashCode() {
        return getType().hashCode();
    }

    public String toString() {
        return getType().toString();
    }
}
