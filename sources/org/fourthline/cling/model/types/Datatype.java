package org.fourthline.cling.model.types;

import java.net.URI;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
/* loaded from: classes.dex */
public interface Datatype<V> {
    Builtin getBuiltin();

    String getDisplayString();

    String getString(V v) throws InvalidValueException;

    boolean isHandlingJavaType(Class cls);

    boolean isValid(V v);

    V valueOf(String str) throws InvalidValueException;

    /* loaded from: classes.dex */
    public enum Default {
        BOOLEAN(Boolean.class, Builtin.BOOLEAN),
        BOOLEAN_PRIMITIVE(Boolean.TYPE, Builtin.BOOLEAN),
        SHORT(Short.class, Builtin.I2_SHORT),
        SHORT_PRIMITIVE(Short.TYPE, Builtin.I2_SHORT),
        INTEGER(Integer.class, Builtin.I4),
        INTEGER_PRIMITIVE(Integer.TYPE, Builtin.I4),
        UNSIGNED_INTEGER_ONE_BYTE(UnsignedIntegerOneByte.class, Builtin.UI1),
        UNSIGNED_INTEGER_TWO_BYTES(UnsignedIntegerTwoBytes.class, Builtin.UI2),
        UNSIGNED_INTEGER_FOUR_BYTES(UnsignedIntegerFourBytes.class, Builtin.UI4),
        FLOAT(Float.class, Builtin.R4),
        FLOAT_PRIMITIVE(Float.TYPE, Builtin.R4),
        DOUBLE(Double.class, Builtin.FLOAT),
        DOUBLE_PRIMTIIVE(Double.TYPE, Builtin.FLOAT),
        CHAR(Character.class, Builtin.CHAR),
        CHAR_PRIMITIVE(Character.TYPE, Builtin.CHAR),
        STRING(String.class, Builtin.STRING),
        CALENDAR(Calendar.class, Builtin.DATETIME),
        BYTES(byte[].class, Builtin.BIN_BASE64),
        URI(URI.class, Builtin.URI);
        
        private Builtin builtinType;
        private Class javaType;

        Default(Class javaType, Builtin builtinType) {
            this.javaType = javaType;
            this.builtinType = builtinType;
        }

        public Class getJavaType() {
            return this.javaType;
        }

        public Builtin getBuiltinType() {
            return this.builtinType;
        }

        public static Default getByJavaType(Class javaType) {
            Default[] values;
            for (Default d : values()) {
                if (d.getJavaType().equals(javaType)) {
                    return d;
                }
            }
            return null;
        }

        @Override // java.lang.Enum
        public String toString() {
            return getJavaType() + " => " + getBuiltinType();
        }
    }

    /* loaded from: classes.dex */
    public enum Builtin {
        UI1("ui1", new UnsignedIntegerOneByteDatatype()),
        UI2("ui2", new UnsignedIntegerTwoBytesDatatype()),
        UI4("ui4", new UnsignedIntegerFourBytesDatatype()),
        I1("i1", new IntegerDatatype(1)),
        I2("i2", new IntegerDatatype(2)),
        I2_SHORT("i2", new ShortDatatype()),
        I4("i4", new IntegerDatatype(4)),
        INT("int", new IntegerDatatype(4)),
        R4("r4", new FloatDatatype()),
        R8("r8", new DoubleDatatype()),
        NUMBER("number", new DoubleDatatype()),
        FIXED144("fixed.14.4", new DoubleDatatype()),
        FLOAT("float", new DoubleDatatype()),
        CHAR("char", new CharacterDatatype()),
        STRING("string", new StringDatatype()),
        DATE("date", new DateTimeDatatype(new String[]{"yyyy-MM-dd"}, "yyyy-MM-dd")),
        DATETIME("dateTime", new DateTimeDatatype(new String[]{"yyyy-MM-dd", "yyyy-MM-dd'T'HH:mm:ss"}, "yyyy-MM-dd'T'HH:mm:ss")),
        DATETIME_TZ("dateTime.tz", new DateTimeDatatype(new String[]{"yyyy-MM-dd", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ssZ"}, "yyyy-MM-dd'T'HH:mm:ssZ")),
        TIME("time", new DateTimeDatatype(new String[]{"HH:mm:ss"}, "HH:mm:ss")),
        TIME_TZ("time.tz", new DateTimeDatatype(new String[]{"HH:mm:ssZ", "HH:mm:ss"}, "HH:mm:ssZ")),
        BOOLEAN("boolean", new BooleanDatatype()),
        BIN_BASE64("bin.base64", new Base64Datatype()),
        BIN_HEX("bin.hex", new BinHexDatatype()),
        URI("uri", new URIDatatype()),
        UUID("uuid", new StringDatatype());
        
        private static Map<String, Builtin> byName = new HashMap<String, Builtin>() { // from class: org.fourthline.cling.model.types.Datatype.Builtin.1
            {
                Builtin[] values;
                for (Builtin b : Builtin.values()) {
                    if (!containsKey(b.getDescriptorName().toLowerCase(Locale.ROOT))) {
                        put(b.getDescriptorName().toLowerCase(Locale.ROOT), b);
                    }
                }
            }
        };
        private Datatype datatype;
        private String descriptorName;

        Builtin(String descriptorName, AbstractDatatype abstractDatatype) {
            abstractDatatype.setBuiltin(this);
            this.descriptorName = descriptorName;
            this.datatype = abstractDatatype;
        }

        public String getDescriptorName() {
            return this.descriptorName;
        }

        public Datatype getDatatype() {
            return this.datatype;
        }

        public static Builtin getByDescriptorName(String descriptorName) {
            if (descriptorName == null) {
                return null;
            }
            return byName.get(descriptorName.toLowerCase(Locale.ROOT));
        }

        public static boolean isNumeric(Builtin builtin) {
            return builtin != null && (builtin.equals(UI1) || builtin.equals(UI2) || builtin.equals(UI4) || builtin.equals(I1) || builtin.equals(I2) || builtin.equals(I4) || builtin.equals(INT));
        }
    }
}
