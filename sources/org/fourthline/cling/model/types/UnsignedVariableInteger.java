package org.fourthline.cling.model.types;

import java.util.logging.Logger;
/* loaded from: classes.dex */
public abstract class UnsignedVariableInteger {
    private static final Logger log = Logger.getLogger(UnsignedVariableInteger.class.getName());
    protected long value;

    public abstract Bits getBits();

    /* loaded from: classes.dex */
    public enum Bits {
        EIGHT(255),
        SIXTEEN(65535),
        TWENTYFOUR(16777215),
        THIRTYTWO(4294967295L);
        
        private long maxValue;

        Bits(long maxValue) {
            this.maxValue = maxValue;
        }

        public long getMaxValue() {
            return this.maxValue;
        }
    }

    protected UnsignedVariableInteger() {
    }

    public UnsignedVariableInteger(long value) throws NumberFormatException {
        setValue(value);
    }

    public UnsignedVariableInteger(String s) throws NumberFormatException {
        if (s.startsWith("-")) {
            Logger logger = log;
            logger.warning("Invalid negative integer value '" + s + "', assuming value 0!");
            s = "0";
        }
        setValue(Long.parseLong(s.trim()));
    }

    protected UnsignedVariableInteger setValue(long value) {
        isInRange(value);
        this.value = value;
        return this;
    }

    public Long getValue() {
        return Long.valueOf(this.value);
    }

    public void isInRange(long value) throws NumberFormatException {
        if (value < getMinValue() || value > getBits().getMaxValue()) {
            throw new NumberFormatException("Value must be between " + getMinValue() + " and " + getBits().getMaxValue() + ": " + value);
        }
    }

    public int getMinValue() {
        return 0;
    }

    public UnsignedVariableInteger increment(boolean rolloverToOne) {
        if (this.value + 1 > getBits().getMaxValue()) {
            this.value = rolloverToOne ? 1L : 0L;
        } else {
            this.value++;
        }
        return this;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UnsignedVariableInteger that = (UnsignedVariableInteger) o;
        if (this.value == that.value) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return (int) (this.value ^ (this.value >>> 32));
    }

    public String toString() {
        return Long.toString(this.value);
    }
}
