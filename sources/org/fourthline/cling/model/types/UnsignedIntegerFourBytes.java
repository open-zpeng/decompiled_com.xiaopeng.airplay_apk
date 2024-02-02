package org.fourthline.cling.model.types;

import org.fourthline.cling.model.types.UnsignedVariableInteger;
/* loaded from: classes.dex */
public final class UnsignedIntegerFourBytes extends UnsignedVariableInteger {
    public UnsignedIntegerFourBytes(long value) throws NumberFormatException {
        super(value);
    }

    public UnsignedIntegerFourBytes(String s) throws NumberFormatException {
        super(s);
    }

    @Override // org.fourthline.cling.model.types.UnsignedVariableInteger
    public UnsignedVariableInteger.Bits getBits() {
        return UnsignedVariableInteger.Bits.THIRTYTWO;
    }
}
