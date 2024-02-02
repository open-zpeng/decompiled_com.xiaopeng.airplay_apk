package org.fourthline.cling.model.types.csv;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.model.types.Datatype;
import org.fourthline.cling.model.types.InvalidValueException;
import org.seamless.util.Reflections;
/* loaded from: classes.dex */
public abstract class CSV<T> extends ArrayList<T> {
    protected final Datatype.Builtin datatype = getBuiltinDatatype();

    public CSV() {
    }

    public CSV(String s) throws InvalidValueException {
        addAll(parseString(s));
    }

    protected List parseString(String s) throws InvalidValueException {
        String[] strings = ModelUtil.fromCommaSeparatedList(s);
        List values = new ArrayList();
        for (String string : strings) {
            values.add(this.datatype.getDatatype().valueOf(string));
        }
        return values;
    }

    protected Datatype.Builtin getBuiltinDatatype() throws InvalidValueException {
        Class csvType = Reflections.getTypeArguments(ArrayList.class, getClass()).get(0);
        Datatype.Default defaultType = Datatype.Default.getByJavaType(csvType);
        if (defaultType == null) {
            throw new InvalidValueException("No built-in UPnP datatype for Java type of CSV: " + csvType);
        }
        return defaultType.getBuiltinType();
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // java.util.AbstractCollection
    public String toString() {
        List<String> stringValues = new ArrayList<>();
        Iterator<T> it = iterator();
        while (it.hasNext()) {
            T t = it.next();
            stringValues.add(this.datatype.getDatatype().getString(t));
        }
        return ModelUtil.toCommaSeparatedList(stringValues.toArray(new Object[stringValues.size()]));
    }
}
