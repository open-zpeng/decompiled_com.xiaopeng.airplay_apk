package org.fourthline.cling.support.model.dlna;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.seamless.util.Exceptions;
/* loaded from: classes.dex */
public abstract class DLNAAttribute<T> {
    private static final Logger log = Logger.getLogger(DLNAAttribute.class.getName());
    private T value;

    public abstract String getString();

    public abstract void setString(String str, String str2) throws InvalidDLNAProtocolAttributeException;

    /* loaded from: classes.dex */
    public enum Type {
        DLNA_ORG_PN("DLNA.ORG_PN", DLNAProfileAttribute.class),
        DLNA_ORG_OP("DLNA.ORG_OP", DLNAOperationsAttribute.class),
        DLNA_ORG_PS("DLNA.ORG_PS", DLNAPlaySpeedAttribute.class),
        DLNA_ORG_CI("DLNA.ORG_CI", DLNAConversionIndicatorAttribute.class),
        DLNA_ORG_FLAGS("DLNA.ORG_FLAGS", DLNAFlagsAttribute.class);
        
        private static Map<String, Type> byName = new HashMap<String, Type>() { // from class: org.fourthline.cling.support.model.dlna.DLNAAttribute.Type.1
            {
                Type[] values;
                for (Type t : Type.values()) {
                    put(t.getAttributeName().toUpperCase(Locale.ROOT), t);
                }
            }
        };
        private String attributeName;
        private Class<? extends DLNAAttribute>[] attributeTypes;

        @SafeVarargs
        Type(String attributeName, Class... clsArr) {
            this.attributeName = attributeName;
            this.attributeTypes = clsArr;
        }

        public String getAttributeName() {
            return this.attributeName;
        }

        public Class<? extends DLNAAttribute>[] getAttributeTypes() {
            return this.attributeTypes;
        }

        public static Type valueOfAttributeName(String attributeName) {
            if (attributeName == null) {
                return null;
            }
            return byName.get(attributeName.toUpperCase(Locale.ROOT));
        }
    }

    public void setValue(T value) {
        this.value = value;
    }

    public T getValue() {
        return this.value;
    }

    public static DLNAAttribute newInstance(Type type, String attributeValue, String contentFormat) {
        DLNAAttribute attr = null;
        for (int i = 0; i < type.getAttributeTypes().length && attr == null; i++) {
            Class<? extends DLNAAttribute> attributeClass = type.getAttributeTypes()[i];
            try {
                Logger logger = log;
                logger.finest("Trying to parse DLNA '" + type + "' with class: " + attributeClass.getSimpleName());
                attr = attributeClass.newInstance();
                if (attributeValue != null) {
                    attr.setString(attributeValue, contentFormat);
                }
            } catch (InvalidDLNAProtocolAttributeException ex) {
                Logger logger2 = log;
                logger2.finest("Invalid DLNA attribute value for tested type: " + attributeClass.getSimpleName() + " - " + ex.getMessage());
                attr = null;
            } catch (Exception ex2) {
                Logger logger3 = log;
                logger3.severe("Error instantiating DLNA attribute of type '" + type + "' with value: " + attributeValue);
                log.log(Level.SEVERE, "Exception root cause: ", Exceptions.unwrap(ex2));
            }
        }
        return attr;
    }

    public String toString() {
        return "(" + getClass().getSimpleName() + ") '" + getValue() + "'";
    }
}
