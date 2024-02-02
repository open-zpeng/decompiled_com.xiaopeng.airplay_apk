package com.apple.dnssd;

import java.io.UnsupportedEncodingException;
/* loaded from: classes.dex */
public class TXTRecord {
    protected static final byte kAttrSep = 61;
    protected byte[] fBytes;

    public TXTRecord() {
        this.fBytes = new byte[0];
    }

    public TXTRecord(byte[] initBytes) {
        this.fBytes = (byte[]) initBytes.clone();
    }

    public void set(String key, String value) {
        byte[] valBytes = value != null ? value.getBytes() : null;
        set(key, valBytes);
    }

    public void set(String key, byte[] value) {
        int valLen = value != null ? value.length : 0;
        try {
            byte[] keyBytes = key.getBytes("US-ASCII");
            for (byte b : keyBytes) {
                if (b == 61) {
                    throw new IllegalArgumentException();
                }
            }
            int i = keyBytes.length;
            if (i + valLen >= 255) {
                throw new ArrayIndexOutOfBoundsException();
            }
            int prevLoc = remove(key);
            if (prevLoc == -1) {
                prevLoc = size();
            }
            insert(keyBytes, value, prevLoc);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException();
        }
    }

    protected void insert(byte[] keyBytes, byte[] value, int index) {
        byte[] oldBytes = this.fBytes;
        int valLen = value != null ? value.length : 0;
        int insertion = 0;
        for (int insertion2 = 0; insertion2 < index && insertion < this.fBytes.length; insertion2++) {
            insertion += 255 & (this.fBytes[insertion] + 1);
        }
        int i = keyBytes.length;
        int avLen = i + valLen + (value != null ? 1 : 0);
        int newLen = oldBytes.length + avLen + 1;
        this.fBytes = new byte[newLen];
        System.arraycopy(oldBytes, 0, this.fBytes, 0, insertion);
        int secondHalfLen = oldBytes.length - insertion;
        System.arraycopy(oldBytes, insertion, this.fBytes, newLen - secondHalfLen, secondHalfLen);
        this.fBytes[insertion] = (byte) avLen;
        System.arraycopy(keyBytes, 0, this.fBytes, insertion + 1, keyBytes.length);
        if (value != null) {
            this.fBytes[insertion + 1 + keyBytes.length] = kAttrSep;
            System.arraycopy(value, 0, this.fBytes, keyBytes.length + insertion + 2, valLen);
        }
    }

    public int remove(String key) {
        int avStart = 0;
        int avStart2 = 0;
        while (avStart < this.fBytes.length) {
            int avLen = this.fBytes[avStart];
            if (key.length() <= avLen && (key.length() == avLen || this.fBytes[key.length() + avStart + 1] == 61)) {
                String s = new String(this.fBytes, avStart + 1, key.length());
                if (key.compareToIgnoreCase(s) == 0) {
                    byte[] oldBytes = this.fBytes;
                    this.fBytes = new byte[(oldBytes.length - avLen) - 1];
                    System.arraycopy(oldBytes, 0, this.fBytes, 0, avStart);
                    System.arraycopy(oldBytes, avStart + avLen + 1, this.fBytes, avStart, ((oldBytes.length - avStart) - avLen) - 1);
                    return avStart2;
                }
            }
            avStart += 255 & (avLen + 1);
            avStart2++;
        }
        return -1;
    }

    public int size() {
        int i = 0;
        int avStart = 0;
        while (avStart < this.fBytes.length) {
            avStart += 255 & (this.fBytes[avStart] + 1);
            i++;
        }
        return i;
    }

    public boolean contains(String key) {
        int i = 0;
        while (true) {
            String s = getKey(i);
            if (s == null) {
                return false;
            }
            if (key.compareToIgnoreCase(s) != 0) {
                i++;
            } else {
                return true;
            }
        }
    }

    public String getKey(int index) {
        int avStart = 0;
        for (int i = 0; i < index && avStart < this.fBytes.length; i++) {
            avStart += this.fBytes[avStart] + 1;
        }
        if (avStart < this.fBytes.length) {
            int avLen = this.fBytes[avStart];
            int aLen = 0;
            while (aLen < avLen && this.fBytes[avStart + aLen + 1] != 61) {
                aLen++;
            }
            return new String(this.fBytes, avStart + 1, aLen);
        }
        return null;
    }

    public byte[] getValue(int index) {
        int avStart = 0;
        for (int avStart2 = 0; avStart2 < index && avStart < this.fBytes.length; avStart2++) {
            avStart += this.fBytes[avStart] + 1;
        }
        if (avStart >= this.fBytes.length) {
            return null;
        }
        int avLen = this.fBytes[avStart];
        for (int aLen = 0; aLen < avLen; aLen++) {
            if (this.fBytes[avStart + aLen + 1] == 61) {
                byte[] value = new byte[(avLen - aLen) - 1];
                System.arraycopy(this.fBytes, avStart + aLen + 2, value, 0, (avLen - aLen) - 1);
                return value;
            }
        }
        return null;
    }

    public String getValueAsString(int index) {
        byte[] value = getValue(index);
        if (value != null) {
            return new String(value);
        }
        return null;
    }

    public byte[] getValue(String forKey) {
        int i = 0;
        while (true) {
            String s = getKey(i);
            if (s != null) {
                if (forKey.compareToIgnoreCase(s) != 0) {
                    i++;
                } else {
                    return getValue(i);
                }
            } else {
                return null;
            }
        }
    }

    public String getValueAsString(String forKey) {
        byte[] val = getValue(forKey);
        if (val != null) {
            return new String(val);
        }
        return null;
    }

    public byte[] getRawBytes() {
        return (byte[]) this.fBytes.clone();
    }

    public String toString() {
        String av;
        String result = null;
        int i = 0;
        while (true) {
            String a = getKey(i);
            if (a == null) {
                break;
            }
            String av2 = String.valueOf(i) + "={" + a;
            String val = getValueAsString(i);
            if (val != null) {
                av = av2 + "=" + val + "}";
            } else {
                av = av2 + "}";
            }
            if (result == null) {
                result = av;
            } else {
                result = result + ", " + av;
            }
            i++;
        }
        return result != null ? result : "";
    }
}
