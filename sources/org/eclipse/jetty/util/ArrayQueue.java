package org.eclipse.jetty.util;

import java.util.AbstractList;
import java.util.NoSuchElementException;
import java.util.Queue;
/* loaded from: classes.dex */
public class ArrayQueue<E> extends AbstractList<E> implements Queue<E> {
    public static final int DEFAULT_CAPACITY = 64;
    public static final int DEFAULT_GROWTH = 32;
    protected Object[] _elements;
    protected final int _growCapacity;
    protected final Object _lock;
    protected int _nextE;
    protected int _nextSlot;
    protected int _size;

    public ArrayQueue() {
        this(64, -1);
    }

    public ArrayQueue(int capacity) {
        this(capacity, -1);
    }

    public ArrayQueue(int initCapacity, int growBy) {
        this(initCapacity, growBy, null);
    }

    public ArrayQueue(int initCapacity, int growBy, Object lock) {
        this._lock = lock == null ? this : lock;
        this._growCapacity = growBy;
        this._elements = new Object[initCapacity];
    }

    public int getCapacity() {
        int length;
        synchronized (this._lock) {
            length = this._elements.length;
        }
        return length;
    }

    @Override // java.util.AbstractList, java.util.AbstractCollection, java.util.Collection, java.util.List, java.util.Queue
    public boolean add(E e) {
        if (!offer(e)) {
            throw new IllegalStateException("Full");
        }
        return true;
    }

    @Override // java.util.Queue
    public boolean offer(E e) {
        boolean enqueue;
        synchronized (this._lock) {
            enqueue = enqueue(e);
        }
        return enqueue;
    }

    private boolean enqueue(E e) {
        if (this._size != this._elements.length || grow()) {
            this._size++;
            Object[] objArr = this._elements;
            int i = this._nextSlot;
            this._nextSlot = i + 1;
            objArr[i] = e;
            if (this._nextSlot == this._elements.length) {
                this._nextSlot = 0;
            }
            return true;
        }
        return false;
    }

    public void addUnsafe(E e) {
        if (!enqueue(e)) {
            throw new IllegalStateException("Full");
        }
    }

    @Override // java.util.Queue
    public E element() {
        E at;
        synchronized (this._lock) {
            if (isEmpty()) {
                throw new NoSuchElementException();
            }
            at = at(this._nextE);
        }
        return at;
    }

    private E at(int index) {
        return (E) this._elements[index];
    }

    @Override // java.util.Queue
    public E peek() {
        synchronized (this._lock) {
            if (isEmpty()) {
                return null;
            }
            return at(this._nextE);
        }
    }

    @Override // java.util.Queue
    public E poll() {
        synchronized (this._lock) {
            if (this._size == 0) {
                return null;
            }
            return dequeue();
        }
    }

    private E dequeue() {
        E e = at(this._nextE);
        this._elements[this._nextE] = null;
        this._size--;
        int i = this._nextE + 1;
        this._nextE = i;
        if (i == this._elements.length) {
            this._nextE = 0;
        }
        return e;
    }

    @Override // java.util.Queue
    public E remove() {
        E dequeue;
        synchronized (this._lock) {
            if (this._size == 0) {
                throw new NoSuchElementException();
            }
            dequeue = dequeue();
        }
        return dequeue;
    }

    @Override // java.util.AbstractList, java.util.AbstractCollection, java.util.Collection, java.util.List
    public void clear() {
        synchronized (this._lock) {
            this._size = 0;
            this._nextE = 0;
            this._nextSlot = 0;
        }
    }

    @Override // java.util.AbstractCollection, java.util.Collection, java.util.List
    public boolean isEmpty() {
        boolean z;
        synchronized (this._lock) {
            z = this._size == 0;
        }
        return z;
    }

    @Override // java.util.AbstractCollection, java.util.Collection, java.util.List
    public int size() {
        int i;
        synchronized (this._lock) {
            i = this._size;
        }
        return i;
    }

    @Override // java.util.AbstractList, java.util.List
    public E get(int index) {
        E unsafe;
        synchronized (this._lock) {
            if (index >= 0) {
                try {
                    if (index < this._size) {
                        unsafe = getUnsafe(index);
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
            throw new IndexOutOfBoundsException("!(0<" + index + "<=" + this._size + ")");
        }
        return unsafe;
    }

    public E getUnsafe(int index) {
        int i = (this._nextE + index) % this._elements.length;
        return at(i);
    }

    @Override // java.util.AbstractList, java.util.List
    public E remove(int index) {
        E old;
        synchronized (this._lock) {
            if (index >= 0) {
                try {
                    if (index < this._size) {
                        int i = (this._nextE + index) % this._elements.length;
                        old = at(i);
                        if (i < this._nextSlot) {
                            System.arraycopy(this._elements, i + 1, this._elements, i, this._nextSlot - i);
                            this._nextSlot--;
                            this._size--;
                        } else {
                            System.arraycopy(this._elements, i + 1, this._elements, i, (this._elements.length - i) - 1);
                            if (this._nextSlot > 0) {
                                this._elements[this._elements.length - 1] = this._elements[0];
                                System.arraycopy(this._elements, 1, this._elements, 0, this._nextSlot - 1);
                                this._nextSlot--;
                            } else {
                                this._nextSlot = this._elements.length - 1;
                            }
                            this._size--;
                        }
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
            throw new IndexOutOfBoundsException("!(0<" + index + "<=" + this._size + ")");
        }
        return old;
    }

    @Override // java.util.AbstractList, java.util.List
    public E set(int index, E element) {
        E old;
        synchronized (this._lock) {
            if (index >= 0) {
                try {
                    if (index < this._size) {
                        int i = this._nextE + index;
                        if (i >= this._elements.length) {
                            i -= this._elements.length;
                        }
                        old = at(i);
                        this._elements[i] = element;
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
            throw new IndexOutOfBoundsException("!(0<" + index + "<=" + this._size + ")");
        }
        return old;
    }

    @Override // java.util.AbstractList, java.util.List
    public void add(int index, E element) {
        synchronized (this._lock) {
            if (index >= 0) {
                try {
                    if (index <= this._size) {
                        if (this._size == this._elements.length && !grow()) {
                            throw new IllegalStateException("Full");
                        }
                        if (index == this._size) {
                            add(element);
                        } else {
                            int i = this._nextE + index;
                            if (i >= this._elements.length) {
                                i -= this._elements.length;
                            }
                            this._size++;
                            this._nextSlot++;
                            if (this._nextSlot == this._elements.length) {
                                this._nextSlot = 0;
                            }
                            if (i < this._nextSlot) {
                                System.arraycopy(this._elements, i, this._elements, i + 1, this._nextSlot - i);
                                this._elements[i] = element;
                            } else {
                                if (this._nextSlot > 0) {
                                    System.arraycopy(this._elements, 0, this._elements, 1, this._nextSlot);
                                    this._elements[0] = this._elements[this._elements.length - 1];
                                }
                                System.arraycopy(this._elements, i, this._elements, i + 1, (this._elements.length - i) - 1);
                                this._elements[i] = element;
                            }
                        }
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
            throw new IndexOutOfBoundsException("!(0<" + index + "<=" + this._size + ")");
        }
    }

    protected boolean grow() {
        synchronized (this._lock) {
            if (this._growCapacity <= 0) {
                return false;
            }
            Object[] elements = new Object[this._elements.length + this._growCapacity];
            int split = this._elements.length - this._nextE;
            if (split > 0) {
                System.arraycopy(this._elements, this._nextE, elements, 0, split);
            }
            if (this._nextE != 0) {
                System.arraycopy(this._elements, 0, elements, split, this._nextSlot);
            }
            this._elements = elements;
            this._nextE = 0;
            this._nextSlot = this._size;
            return true;
        }
    }
}
