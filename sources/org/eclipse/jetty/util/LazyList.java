package org.eclipse.jetty.util;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
/* loaded from: classes.dex */
public class LazyList implements Cloneable, Serializable {
    private static final String[] __EMTPY_STRING_ARRAY = new String[0];

    private LazyList() {
    }

    public static Object add(Object list, Object item) {
        if (list == null) {
            if ((item instanceof List) || item == null) {
                List<Object> l = new ArrayList<>();
                l.add(item);
                return l;
            }
            return item;
        } else if (list instanceof List) {
            ((List) list).add(item);
            return list;
        } else {
            List<Object> l2 = new ArrayList<>();
            l2.add(list);
            l2.add(item);
            return l2;
        }
    }

    public static Object add(Object list, int index, Object item) {
        if (list == null) {
            if (index > 0 || (item instanceof List) || item == null) {
                List<Object> l = new ArrayList<>();
                l.add(index, item);
                return l;
            }
            return item;
        } else if (list instanceof List) {
            ((List) list).add(index, item);
            return list;
        } else {
            List<Object> l2 = new ArrayList<>();
            l2.add(list);
            l2.add(index, item);
            return l2;
        }
    }

    public static Object addCollection(Object list, Collection<?> collection) {
        Iterator<?> i = collection.iterator();
        while (i.hasNext()) {
            list = add(list, i.next());
        }
        return list;
    }

    public static Object addArray(Object list, Object[] array) {
        for (int i = 0; array != null && i < array.length; i++) {
            list = add(list, array[i]);
        }
        return list;
    }

    public static Object ensureSize(Object list, int initialSize) {
        if (list == null) {
            return new ArrayList(initialSize);
        }
        if (list instanceof ArrayList) {
            ArrayList<?> ol = (ArrayList) list;
            if (ol.size() > initialSize) {
                return ol;
            }
            ArrayList<Object> nl = new ArrayList<>(initialSize);
            nl.addAll(ol);
            return nl;
        }
        List<Object> l = new ArrayList<>(initialSize);
        l.add(list);
        return l;
    }

    public static Object remove(Object list, Object o) {
        if (list == null) {
            return null;
        }
        if (list instanceof List) {
            List<?> l = (List) list;
            l.remove(o);
            if (l.size() == 0) {
                return null;
            }
            return list;
        } else if (list.equals(o)) {
            return null;
        } else {
            return list;
        }
    }

    public static Object remove(Object list, int i) {
        if (list == null) {
            return null;
        }
        if (list instanceof List) {
            List<?> l = (List) list;
            l.remove(i);
            if (l.size() == 0) {
                return null;
            }
            return list;
        } else if (i == 0) {
            return null;
        } else {
            return list;
        }
    }

    public static <E> List<E> getList(Object list) {
        return getList(list, false);
    }

    public static <E> List<E> getList(Object list, boolean nullForEmpty) {
        if (list == null) {
            if (nullForEmpty) {
                return null;
            }
            return Collections.emptyList();
        } else if (list instanceof List) {
            return (List) list;
        } else {
            return Collections.singletonList(list);
        }
    }

    public static String[] toStringArray(Object list) {
        if (list == null) {
            return __EMTPY_STRING_ARRAY;
        }
        if (!(list instanceof List)) {
            return new String[]{list.toString()};
        }
        List<?> l = (List) list;
        String[] a = new String[l.size()];
        int i = l.size();
        while (true) {
            int i2 = i - 1;
            if (i > 0) {
                Object o = l.get(i2);
                if (o != null) {
                    a[i2] = o.toString();
                }
                i = i2;
            } else {
                return a;
            }
        }
    }

    public static Object toArray(Object list, Class<?> clazz) {
        if (list == null) {
            return Array.newInstance(clazz, 0);
        }
        if (list instanceof List) {
            List<?> l = (List) list;
            if (clazz.isPrimitive()) {
                Object a = Array.newInstance(clazz, l.size());
                for (int i = 0; i < l.size(); i++) {
                    Array.set(a, i, l.get(i));
                }
                return a;
            }
            return l.toArray((Object[]) Array.newInstance(clazz, l.size()));
        }
        Object a2 = Array.newInstance(clazz, 1);
        Array.set(a2, 0, list);
        return a2;
    }

    public static int size(Object list) {
        if (list == null) {
            return 0;
        }
        if (list instanceof List) {
            return ((List) list).size();
        }
        return 1;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static <E> E get(Object list, int i) {
        if (list == 0) {
            throw new IndexOutOfBoundsException();
        }
        if (list instanceof List) {
            return (E) ((List) list).get(i);
        }
        if (i == 0) {
            return list;
        }
        throw new IndexOutOfBoundsException();
    }

    public static boolean contains(Object list, Object item) {
        if (list == null) {
            return false;
        }
        if (list instanceof List) {
            return ((List) list).contains(item);
        }
        return list.equals(item);
    }

    public static Object clone(Object list) {
        if (list == null) {
            return null;
        }
        if (list instanceof List) {
            return new ArrayList((List) list);
        }
        return list;
    }

    public static String toString(Object list) {
        if (list == null) {
            return "[]";
        }
        if (list instanceof List) {
            return list.toString();
        }
        return "[" + list + "]";
    }

    public static <E> Iterator<E> iterator(Object list) {
        if (list == null) {
            List<E> empty = Collections.emptyList();
            return empty.iterator();
        } else if (list instanceof List) {
            return ((List) list).iterator();
        } else {
            List<E> l = getList(list);
            return l.iterator();
        }
    }

    public static <E> ListIterator<E> listIterator(Object list) {
        if (list == null) {
            List<E> empty = Collections.emptyList();
            return empty.listIterator();
        } else if (list instanceof List) {
            return ((List) list).listIterator();
        } else {
            List<E> l = getList(list);
            return l.listIterator();
        }
    }

    public static <E> List<E> array2List(E[] array) {
        if (array == null || array.length == 0) {
            return new ArrayList();
        }
        return new ArrayList(Arrays.asList(array));
    }

    public static <T> T[] addToArray(T[] array, T item, Class<?> type) {
        if (array == null) {
            if (type == null && item != null) {
                type = item.getClass();
            }
            T[] na = (T[]) ((Object[]) Array.newInstance(type, 1));
            na[0] = item;
            return na;
        }
        Class<?> c = array.getClass().getComponentType();
        T[] na2 = (T[]) ((Object[]) Array.newInstance(c, Array.getLength(array) + 1));
        System.arraycopy(array, 0, na2, 0, array.length);
        na2[array.length] = item;
        return na2;
    }

    public static <T> T[] removeFromArray(T[] array, Object item) {
        if (item == null || array == null) {
            return array;
        }
        int i = array.length;
        while (true) {
            int i2 = i - 1;
            if (i > 0) {
                if (!item.equals(array[i2])) {
                    i = i2;
                } else {
                    Class<?> c = array == null ? item.getClass() : array.getClass().getComponentType();
                    T[] na = (T[]) ((Object[]) Array.newInstance(c, Array.getLength(array) - 1));
                    if (i2 > 0) {
                        System.arraycopy(array, 0, na, 0, i2);
                    }
                    if (i2 + 1 < array.length) {
                        System.arraycopy(array, i2 + 1, na, i2, array.length - (i2 + 1));
                    }
                    return na;
                }
            } else {
                return array;
            }
        }
    }
}
