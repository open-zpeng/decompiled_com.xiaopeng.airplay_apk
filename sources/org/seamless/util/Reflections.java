package org.seamless.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/* loaded from: classes.dex */
public class Reflections {
    public static Object invoke(Method method, Object target, Object... args) throws Exception {
        try {
            return method.invoke(target, args);
        } catch (IllegalArgumentException iae) {
            String message = "Could not invoke method by reflection: " + toString(method);
            if (args != null && args.length > 0) {
                message = message + " with parameters: (" + toClassNameString(", ", args) + ')';
            }
            throw new IllegalArgumentException(message + " on: " + target.getClass().getName(), iae);
        } catch (InvocationTargetException ite) {
            if (ite.getCause() instanceof Exception) {
                throw ((Exception) ite.getCause());
            }
            throw ite;
        }
    }

    public static Object get(Field field, Object target) throws Exception {
        boolean accessible = field.isAccessible();
        try {
            try {
                field.setAccessible(true);
                return field.get(target);
            } catch (IllegalArgumentException iae) {
                String message = "Could not get field value by reflection: " + toString(field) + " on: " + target.getClass().getName();
                throw new IllegalArgumentException(message, iae);
            }
        } finally {
            field.setAccessible(accessible);
        }
    }

    public static Method getMethod(Class clazz, String name) {
        for (Class superClass = clazz; superClass != null && superClass != Object.class; superClass = superClass.getSuperclass()) {
            try {
                return superClass.getDeclaredMethod(name, new Class[0]);
            } catch (NoSuchMethodException e) {
            }
        }
        throw new IllegalArgumentException("No such method: " + clazz.getName() + '.' + name);
    }

    public static void set(Field field, Object target, Object value) throws Exception {
        String message;
        boolean accessible = field.isAccessible();
        try {
            try {
                field.setAccessible(true);
                field.set(target, value);
            } catch (IllegalArgumentException iae) {
                String message2 = "Could not set field value by reflection: " + toString(field) + " on: " + field.getDeclaringClass().getName();
                if (value == null) {
                    message = message2 + " with null value";
                } else {
                    message = message2 + " with value: " + value.getClass();
                }
                throw new IllegalArgumentException(message, iae);
            }
        } finally {
            field.setAccessible(accessible);
        }
    }

    public static String getMethodPropertyName(String methodName) {
        if (methodName.startsWith("get")) {
            String methodPropertyName = decapitalize(methodName.substring(3));
            return methodPropertyName;
        } else if (methodName.startsWith("is")) {
            String methodPropertyName2 = decapitalize(methodName.substring(2));
            return methodPropertyName2;
        } else if (!methodName.startsWith("set")) {
            return null;
        } else {
            String methodPropertyName3 = decapitalize(methodName.substring(3));
            return methodPropertyName3;
        }
    }

    public static Method getGetterMethod(Class clazz, String name) {
        Method[] declaredMethods;
        for (Class superClass = clazz; superClass != null && superClass != Object.class; superClass = superClass.getSuperclass()) {
            for (Method method : superClass.getDeclaredMethods()) {
                String methodName = method.getName();
                if (method.getParameterTypes().length == 0) {
                    if (methodName.startsWith("get")) {
                        if (decapitalize(methodName.substring(3)).equals(name)) {
                            return method;
                        }
                    } else if (methodName.startsWith("is") && decapitalize(methodName.substring(2)).equals(name)) {
                        return method;
                    }
                }
            }
        }
        return null;
    }

    public static List<Method> getMethods(Class clazz, Class annotation) {
        Method[] declaredMethods;
        List<Method> methods = new ArrayList<>();
        for (Class superClass = clazz; superClass != null && superClass != Object.class; superClass = superClass.getSuperclass()) {
            for (Method method : superClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(annotation)) {
                    methods.add(method);
                }
            }
        }
        return methods;
    }

    public static Field getField(Class clazz, String name) {
        for (Class superClass = clazz; superClass != null && superClass != Object.class; superClass = superClass.getSuperclass()) {
            try {
                return superClass.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
            }
        }
        return null;
    }

    public static List<Field> getFields(Class clazz, Class annotation) {
        Field[] declaredFields;
        List<Field> fields = new ArrayList<>();
        for (Class superClass = clazz; superClass != null && superClass != Object.class; superClass = superClass.getSuperclass()) {
            for (Field field : superClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(annotation)) {
                    fields.add(field);
                }
            }
        }
        return fields;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static <T> List<Class<?>> getTypeArguments(Class<T> baseClass, Class<? extends T> childClass) {
        int i;
        Type[] actualTypeArguments;
        Map<Type, Type> resolvedTypes = new HashMap<>();
        Type type = childClass;
        while (true) {
            i = 0;
            if (getClass(type).equals(baseClass)) {
                break;
            } else if (type instanceof Class) {
                type = type.getGenericSuperclass();
            } else {
                ParameterizedType parameterizedType = type;
                Class<?> rawType = (Class) parameterizedType.getRawType();
                Type[] actualTypeArguments2 = parameterizedType.getActualTypeArguments();
                TypeVariable<?>[] typeParameters = rawType.getTypeParameters();
                while (i < actualTypeArguments2.length) {
                    resolvedTypes.put(typeParameters[i], actualTypeArguments2[i]);
                    i++;
                }
                if (!rawType.equals(baseClass)) {
                    type = rawType.getGenericSuperclass();
                }
            }
        }
        if (type instanceof Class) {
            actualTypeArguments = type.getTypeParameters();
        } else {
            actualTypeArguments = type.getActualTypeArguments();
        }
        List<Class<?>> typeArgumentsAsClasses = new ArrayList<>();
        int length = actualTypeArguments.length;
        while (i < length) {
            Type baseType = actualTypeArguments[i];
            while (resolvedTypes.containsKey(baseType)) {
                baseType = resolvedTypes.get(baseType);
            }
            typeArgumentsAsClasses.add(getClass(baseType));
            i++;
        }
        return typeArgumentsAsClasses;
    }

    public static Class<?> getClass(Type type) {
        if (type instanceof Class) {
            return (Class) type;
        }
        if (type instanceof ParameterizedType) {
            return getClass(((ParameterizedType) type).getRawType());
        }
        if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) type).getGenericComponentType();
            Class<?> componentClass = getClass(componentType);
            if (componentClass != null) {
                return Array.newInstance(componentClass, 0).getClass();
            }
            return null;
        }
        return null;
    }

    public static Object getAndWrap(Field field, Object target) {
        boolean accessible = field.isAccessible();
        try {
            try {
                field.setAccessible(true);
                return get(field, target);
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw ((RuntimeException) e);
                }
                throw new IllegalArgumentException("exception setting: " + field.getName(), e);
            }
        } finally {
            field.setAccessible(accessible);
        }
    }

    public static void setAndWrap(Field field, Object target, Object value) {
        boolean accessible = field.isAccessible();
        try {
            try {
                field.setAccessible(true);
                set(field, target, value);
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw ((RuntimeException) e);
                }
                throw new IllegalArgumentException("exception setting: " + field.getName(), e);
            }
        } finally {
            field.setAccessible(accessible);
        }
    }

    public static Object invokeAndWrap(Method method, Object target, Object... args) {
        try {
            return invoke(method, target, args);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw ((RuntimeException) e);
            }
            throw new RuntimeException("exception invoking: " + method.getName(), e);
        }
    }

    public static String toString(Member member) {
        return unqualify(member.getDeclaringClass().getName()) + '.' + member.getName();
    }

    public static Class classForName(String name) throws ClassNotFoundException {
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(name);
        } catch (Exception e) {
            return Class.forName(name);
        }
    }

    public static boolean isClassAvailable(String name) {
        try {
            classForName(name);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static Class getCollectionElementType(Type collectionType) {
        if (!(collectionType instanceof ParameterizedType)) {
            throw new IllegalArgumentException("collection type not parameterized");
        }
        Type[] typeArguments = ((ParameterizedType) collectionType).getActualTypeArguments();
        if (typeArguments.length == 0) {
            throw new IllegalArgumentException("no type arguments for collection type");
        }
        Type typeArgument = typeArguments.length == 1 ? typeArguments[0] : typeArguments[1];
        if (typeArgument instanceof ParameterizedType) {
            typeArgument = ((ParameterizedType) typeArgument).getRawType();
        }
        if (!(typeArgument instanceof Class)) {
            throw new IllegalArgumentException("type argument not a class");
        }
        return (Class) typeArgument;
    }

    public static Class getMapKeyType(Type collectionType) {
        if (!(collectionType instanceof ParameterizedType)) {
            throw new IllegalArgumentException("collection type not parameterized");
        }
        Type[] typeArguments = ((ParameterizedType) collectionType).getActualTypeArguments();
        if (typeArguments.length == 0) {
            throw new IllegalArgumentException("no type arguments for collection type");
        }
        Type typeArgument = typeArguments[0];
        if (!(typeArgument instanceof Class)) {
            throw new IllegalArgumentException("type argument not a class");
        }
        return (Class) typeArgument;
    }

    public static Method getSetterMethod(Class clazz, String name) {
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            String methodName = method.getName();
            if (methodName.startsWith("set") && method.getParameterTypes().length == 1 && decapitalize(methodName.substring(3)).equals(name)) {
                return method;
            }
        }
        throw new IllegalArgumentException("no such setter method: " + clazz.getName() + '.' + name);
    }

    public static Method getMethod(Annotation annotation, String name) {
        try {
            return annotation.annotationType().getMethod(name, new Class[0]);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    public static boolean isInstanceOf(Class clazz, String name) {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
        for (Class c = clazz; c != Object.class; c = c.getSuperclass()) {
            if (instanceOf(c, name)) {
                return true;
            }
        }
        return false;
    }

    private static boolean instanceOf(Class clazz, String name) {
        if (name.equals(clazz.getName())) {
            return true;
        }
        boolean found = false;
        Class[] interfaces = clazz.getInterfaces();
        for (int i = 0; i < interfaces.length && !found; i++) {
            found = instanceOf(interfaces[i], name);
        }
        return found;
    }

    public static String toClassNameString(String sep, Object... objects) {
        if (objects.length == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (Object object : objects) {
            builder.append(sep);
            if (object == null) {
                builder.append("null");
            } else {
                builder.append(object.getClass().getName());
            }
        }
        return builder.substring(sep.length());
    }

    public static String unqualify(String name) {
        return unqualify(name, '.');
    }

    public static String unqualify(String name, char sep) {
        return name.substring(name.lastIndexOf(sep) + 1, name.length());
    }

    public static String decapitalize(String name) {
        if (name == null) {
            return null;
        }
        if (name.length() == 0 || (name.length() > 1 && Character.isUpperCase(name.charAt(1)))) {
            return name;
        }
        char[] chars = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }
}
