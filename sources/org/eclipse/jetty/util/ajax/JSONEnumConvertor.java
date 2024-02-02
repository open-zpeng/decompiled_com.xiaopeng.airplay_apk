package org.eclipse.jetty.util.ajax;

import java.lang.reflect.Method;
import java.util.Map;
import org.eclipse.jetty.util.Loader;
import org.eclipse.jetty.util.ajax.JSON;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.seamless.xhtml.XHTML;
/* loaded from: classes.dex */
public class JSONEnumConvertor implements JSON.Convertor {
    private static final Logger LOG = Log.getLogger(JSONEnumConvertor.class);
    private boolean _fromJSON;
    private Method _valueOf;

    public JSONEnumConvertor() {
        this(false);
    }

    public JSONEnumConvertor(boolean fromJSON) {
        try {
            Class<?> e = Loader.loadClass(getClass(), "java.lang.Enum");
            this._valueOf = e.getMethod("valueOf", Class.class, String.class);
            this._fromJSON = fromJSON;
        } catch (Exception e2) {
            throw new RuntimeException("!Enums", e2);
        }
    }

    @Override // org.eclipse.jetty.util.ajax.JSON.Convertor
    public Object fromJSON(Map map) {
        if (!this._fromJSON) {
            throw new UnsupportedOperationException();
        }
        try {
            Class c = Loader.loadClass(getClass(), (String) map.get(XHTML.ATTR.CLASS));
            return this._valueOf.invoke(null, c, map.get("value"));
        } catch (Exception e) {
            LOG.warn(e);
            return null;
        }
    }

    @Override // org.eclipse.jetty.util.ajax.JSON.Convertor
    public void toJSON(Object obj, JSON.Output out) {
        if (this._fromJSON) {
            out.addClass(obj.getClass());
            out.add("value", ((Enum) obj).name());
            return;
        }
        out.add(((Enum) obj).name());
    }
}
