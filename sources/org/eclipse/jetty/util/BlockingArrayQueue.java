package org.eclipse.jetty.util;

import com.apple.dnssd.DNSSD;
import java.util.AbstractList;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
/* loaded from: classes.dex */
public class BlockingArrayQueue<E> extends AbstractList<E> implements BlockingQueue<E> {
    public final int DEFAULT_CAPACITY;
    public final int DEFAULT_GROWTH;
    private volatile int _capacity;
    private Object[] _elements;
    private final int _growCapacity;
    private int _head;
    private final ReentrantLock _headLock;
    private final int _limit;
    private final Condition _notEmpty;
    private final AtomicInteger _size;
    private long _space0;
    private long _space1;
    private long _space2;
    private long _space3;
    private long _space4;
    private long _space5;
    private long _space6;
    private long _space7;
    private int _tail;
    private final ReentrantLock _tailLock;

    public BlockingArrayQueue() {
        this.DEFAULT_CAPACITY = DNSSD.REGISTRATION_DOMAINS;
        this.DEFAULT_GROWTH = 64;
        this._size = new AtomicInteger();
        this._headLock = new ReentrantLock();
        this._notEmpty = this._headLock.newCondition();
        this._tailLock = new ReentrantLock();
        this._elements = new Object[DNSSD.REGISTRATION_DOMAINS];
        this._growCapacity = 64;
        this._capacity = this._elements.length;
        this._limit = Integer.MAX_VALUE;
    }

    public BlockingArrayQueue(int limit) {
        this.DEFAULT_CAPACITY = DNSSD.REGISTRATION_DOMAINS;
        this.DEFAULT_GROWTH = 64;
        this._size = new AtomicInteger();
        this._headLock = new ReentrantLock();
        this._notEmpty = this._headLock.newCondition();
        this._tailLock = new ReentrantLock();
        this._elements = new Object[limit];
        this._capacity = this._elements.length;
        this._growCapacity = -1;
        this._limit = limit;
    }

    public BlockingArrayQueue(int capacity, int growBy) {
        this.DEFAULT_CAPACITY = DNSSD.REGISTRATION_DOMAINS;
        this.DEFAULT_GROWTH = 64;
        this._size = new AtomicInteger();
        this._headLock = new ReentrantLock();
        this._notEmpty = this._headLock.newCondition();
        this._tailLock = new ReentrantLock();
        this._elements = new Object[capacity];
        this._capacity = this._elements.length;
        this._growCapacity = growBy;
        this._limit = Integer.MAX_VALUE;
    }

    public BlockingArrayQueue(int capacity, int growBy, int limit) {
        this.DEFAULT_CAPACITY = DNSSD.REGISTRATION_DOMAINS;
        this.DEFAULT_GROWTH = 64;
        this._size = new AtomicInteger();
        this._headLock = new ReentrantLock();
        this._notEmpty = this._headLock.newCondition();
        this._tailLock = new ReentrantLock();
        if (capacity > limit) {
            throw new IllegalArgumentException();
        }
        this._elements = new Object[capacity];
        this._capacity = this._elements.length;
        this._growCapacity = growBy;
        this._limit = limit;
    }

    public int getCapacity() {
        return this._capacity;
    }

    public int getLimit() {
        return this._limit;
    }

    @Override // java.util.AbstractList, java.util.AbstractCollection, java.util.Collection, java.util.List, java.util.concurrent.BlockingQueue, java.util.Queue
    public boolean add(E e) {
        return offer(e);
    }

    @Override // java.util.Queue
    public E element() {
        E e = peek();
        if (e == null) {
            throw new NoSuchElementException();
        }
        return e;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r1v5, types: [java.lang.Object[]] */
    /* JADX WARN: Type inference failed for: r1v6 */
    @Override // java.util.Queue
    public E peek() {
        if (this._size.get() == 0) {
            return null;
        }
        E e = null;
        this._headLock.lock();
        try {
            if (this._size.get() > 0) {
                e = this._elements[this._head];
            }
            return e;
        } finally {
            this._headLock.unlock();
        }
    }

    @Override // java.util.concurrent.BlockingQueue, java.util.Queue
    public boolean offer(E e) {
        if (e == null) {
            throw new NullPointerException();
        }
        this._tailLock.lock();
        try {
            if (this._size.get() < this._limit) {
                if (this._size.get() == this._capacity) {
                    this._headLock.lock();
                    if (grow()) {
                        this._headLock.unlock();
                    } else {
                        this._headLock.unlock();
                    }
                }
                this._elements[this._tail] = e;
                this._tail = (this._tail + 1) % this._capacity;
                boolean not_empty = this._size.getAndIncrement() == 0;
                if (not_empty) {
                    this._headLock.lock();
                    try {
                        this._notEmpty.signal();
                    } finally {
                        this._headLock.unlock();
                    }
                }
                return true;
            }
            return false;
        } finally {
            this._tailLock.unlock();
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r3v0, types: [java.lang.Object[]] */
    /* JADX WARN: Type inference failed for: r3v1 */
    @Override // java.util.Queue
    public E poll() {
        if (this._size.get() == 0) {
            return null;
        }
        E e = null;
        this._headLock.lock();
        try {
            if (this._size.get() > 0) {
                int head = this._head;
                e = this._elements[head];
                this._elements[head] = null;
                this._head = (head + 1) % this._capacity;
                if (this._size.decrementAndGet() > 0) {
                    this._notEmpty.signal();
                }
            }
            return e;
        } finally {
            this._headLock.unlock();
        }
    }

    @Override // java.util.concurrent.BlockingQueue
    public E take() throws InterruptedException {
        this._headLock.lockInterruptibly();
        while (this._size.get() == 0) {
            try {
                try {
                    this._notEmpty.await();
                } catch (InterruptedException ie) {
                    this._notEmpty.signal();
                    throw ie;
                }
            } finally {
                this._headLock.unlock();
            }
        }
        int head = this._head;
        E e = (E) this._elements[head];
        this._elements[head] = null;
        this._head = (head + 1) % this._capacity;
        if (this._size.decrementAndGet() > 0) {
            this._notEmpty.signal();
        }
        return e;
    }

    @Override // java.util.concurrent.BlockingQueue
    public E poll(long time, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(time);
        this._headLock.lockInterruptibly();
        while (this._size.get() == 0) {
            try {
                try {
                    if (nanos <= 0) {
                        return null;
                    }
                    nanos = this._notEmpty.awaitNanos(nanos);
                } catch (InterruptedException ie) {
                    this._notEmpty.signal();
                    throw ie;
                }
            } finally {
                this._headLock.unlock();
            }
        }
        E e = (E) this._elements[this._head];
        this._elements[this._head] = null;
        this._head = (this._head + 1) % this._capacity;
        if (this._size.decrementAndGet() > 0) {
            this._notEmpty.signal();
        }
        return e;
    }

    @Override // java.util.Queue
    public E remove() {
        E e = poll();
        if (e == null) {
            throw new NoSuchElementException();
        }
        return e;
    }

    @Override // java.util.AbstractList, java.util.AbstractCollection, java.util.Collection, java.util.List
    public void clear() {
        this._tailLock.lock();
        try {
            this._headLock.lock();
            this._head = 0;
            this._tail = 0;
            this._size.set(0);
            this._headLock.unlock();
        } finally {
            this._tailLock.unlock();
        }
    }

    @Override // java.util.AbstractCollection, java.util.Collection, java.util.List
    public boolean isEmpty() {
        return this._size.get() == 0;
    }

    @Override // java.util.AbstractCollection, java.util.Collection, java.util.List
    public int size() {
        return this._size.get();
    }

    @Override // java.util.AbstractList, java.util.List
    public E get(int index) {
        this._tailLock.lock();
        try {
            this._headLock.lock();
            if (index < 0 || index >= this._size.get()) {
                throw new IndexOutOfBoundsException("!(0<" + index + "<=" + this._size + ")");
            }
            int i = this._head + index;
            if (i >= this._capacity) {
                i -= this._capacity;
            }
            E e = (E) this._elements[i];
            this._headLock.unlock();
            return e;
        } finally {
            this._tailLock.unlock();
        }
    }

    @Override // java.util.AbstractList, java.util.List
    public E remove(int index) {
        this._tailLock.lock();
        try {
            this._headLock.lock();
            if (index < 0 || index >= this._size.get()) {
                throw new IndexOutOfBoundsException("!(0<" + index + "<=" + this._size + ")");
            }
            int i = this._head + index;
            if (i >= this._capacity) {
                i -= this._capacity;
            }
            E old = (E) this._elements[i];
            if (i < this._tail) {
                System.arraycopy(this._elements, i + 1, this._elements, i, this._tail - i);
                this._tail--;
                this._size.decrementAndGet();
            } else {
                System.arraycopy(this._elements, i + 1, this._elements, i, (this._capacity - i) - 1);
                if (this._tail > 0) {
                    this._elements[this._capacity] = this._elements[0];
                    System.arraycopy(this._elements, 1, this._elements, 0, this._tail - 1);
                    this._tail--;
                } else {
                    this._tail = this._capacity - 1;
                }
                this._size.decrementAndGet();
            }
            this._headLock.unlock();
            return old;
        } finally {
            this._tailLock.unlock();
        }
    }

    @Override // java.util.AbstractList, java.util.List
    public E set(int index, E e) {
        if (e == null) {
            throw new NullPointerException();
        }
        this._tailLock.lock();
        try {
            this._headLock.lock();
            if (index < 0 || index >= this._size.get()) {
                throw new IndexOutOfBoundsException("!(0<" + index + "<=" + this._size + ")");
            }
            int i = this._head + index;
            if (i >= this._capacity) {
                i -= this._capacity;
            }
            E old = (E) this._elements[i];
            this._elements[i] = e;
            this._headLock.unlock();
            return old;
        } finally {
            this._tailLock.unlock();
        }
    }

    @Override // java.util.AbstractList, java.util.List
    public void add(int index, E e) {
        if (e == null) {
            throw new NullPointerException();
        }
        this._tailLock.lock();
        try {
            this._headLock.lock();
            if (index < 0 || index > this._size.get()) {
                throw new IndexOutOfBoundsException("!(0<" + index + "<=" + this._size + ")");
            }
            if (index == this._size.get()) {
                add(e);
            } else {
                if (this._tail == this._head && !grow()) {
                    throw new IllegalStateException("full");
                }
                int i = this._head + index;
                if (i >= this._capacity) {
                    i -= this._capacity;
                }
                this._size.incrementAndGet();
                this._tail = (this._tail + 1) % this._capacity;
                if (i < this._tail) {
                    System.arraycopy(this._elements, i, this._elements, i + 1, this._tail - i);
                    this._elements[i] = e;
                } else {
                    if (this._tail > 0) {
                        System.arraycopy(this._elements, 0, this._elements, 1, this._tail);
                        this._elements[0] = this._elements[this._capacity - 1];
                    }
                    System.arraycopy(this._elements, i, this._elements, i + 1, (this._capacity - i) - 1);
                    this._elements[i] = e;
                }
            }
            this._headLock.unlock();
        } finally {
            this._tailLock.unlock();
        }
    }

    private boolean grow() {
        int new_tail;
        if (this._growCapacity <= 0) {
            return false;
        }
        this._tailLock.lock();
        try {
            this._headLock.lock();
            int head = this._head;
            int tail = this._tail;
            Object[] elements = new Object[this._capacity + this._growCapacity];
            if (head < tail) {
                new_tail = tail - head;
                System.arraycopy(this._elements, head, elements, 0, new_tail);
            } else {
                if (head <= tail && this._size.get() <= 0) {
                    new_tail = 0;
                }
                new_tail = (this._capacity + tail) - head;
                int cut = this._capacity - head;
                System.arraycopy(this._elements, head, elements, 0, cut);
                System.arraycopy(this._elements, 0, elements, cut, tail);
            }
            this._elements = elements;
            this._capacity = this._elements.length;
            this._head = 0;
            this._tail = new_tail;
            this._headLock.unlock();
            return true;
        } finally {
            this._tailLock.unlock();
        }
    }

    @Override // java.util.concurrent.BlockingQueue
    public int drainTo(Collection<? super E> c) {
        throw new UnsupportedOperationException();
    }

    @Override // java.util.concurrent.BlockingQueue
    public int drainTo(Collection<? super E> c, int maxElements) {
        throw new UnsupportedOperationException();
    }

    @Override // java.util.concurrent.BlockingQueue
    public boolean offer(E o, long timeout, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override // java.util.concurrent.BlockingQueue
    public void put(E o) throws InterruptedException {
        if (!add(o)) {
            throw new IllegalStateException("full");
        }
    }

    @Override // java.util.concurrent.BlockingQueue
    public int remainingCapacity() {
        this._tailLock.lock();
        try {
            this._headLock.lock();
            int capacity = getCapacity() - size();
            this._headLock.unlock();
            return capacity;
        } finally {
            this._tailLock.unlock();
        }
    }

    long sumOfSpace() {
        long j = this._space0;
        this._space0 = j + 1;
        long j2 = this._space1;
        this._space1 = j2 + 1;
        long j3 = j + j2;
        long j4 = this._space2;
        this._space2 = j4 + 1;
        long j5 = j3 + j4;
        long j6 = this._space3;
        this._space3 = j6 + 1;
        long j7 = j5 + j6;
        long j8 = this._space4;
        this._space4 = j8 + 1;
        long j9 = j7 + j8;
        long j10 = this._space5;
        this._space5 = j10 + 1;
        long j11 = j9 + j10;
        long j12 = this._space6;
        this._space6 = j12 + 1;
        long j13 = j11 + j12;
        long j14 = this._space7;
        this._space7 = 1 + j14;
        return j13 + j14;
    }
}
