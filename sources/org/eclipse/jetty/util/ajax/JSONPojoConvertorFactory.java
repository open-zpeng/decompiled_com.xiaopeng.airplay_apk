package org.eclipse.jetty.util.ajax;

import java.util.Map;
import org.eclipse.jetty.util.Loader;
import org.eclipse.jetty.util.ajax.JSON;
import org.seamless.xhtml.XHTML;
/* loaded from: classes.dex */
public class JSONPojoConvertorFactory implements JSON.Convertor {
    private final boolean _fromJson;
    private final JSON _json;

    public JSONPojoConvertorFactory(JSON json) {
        if (json == null) {
            throw new IllegalArgumentException();
        }
        this._json = json;
        this._fromJson = true;
    }

    public JSONPojoConvertorFactory(JSON json, boolean fromJSON) {
        if (json == null) {
            throw new IllegalArgumentException();
        }
        this._json = json;
        this._fromJson = fromJSON;
    }

    @Override // org.eclipse.jetty.util.ajax.JSON.Convertor
    public void toJSON(Object obj, JSON.Output out) {
        String clsName = obj.getClass().getName();
        JSON.Convertor convertor = this._json.getConvertorFor(clsName);
        if (convertor == null) {
            try {
                Class cls = Loader.loadClass(JSON.class, clsName);
                convertor = new JSONPojoConvertor(cls, this._fromJson);
                this._json.addConvertorFor(clsName, convertor);
            } catch (ClassNotFoundException e) {
                JSON.LOG.warn(e);
            }
        }
        if (convertor != null) {
            convertor.toJSON(obj, out);
        }
    }

    @Override // org.eclipse.jetty.util.ajax.JSON.Convertor
    public Object fromJSON(Map object) {
        String clsName = (String) object.get(XHTML.ATTR.CLASS);
        if (clsName != null) {
            JSON.Convertor convertor = this._json.getConvertorFor(clsName);
            if (convertor == null) {
                try {
                    Class cls = Loader.loadClass(JSON.class, clsName);
                    convertor = new JSONPojoConvertor(cls, this._fromJson);
                    this._json.addConvertorFor(clsName, convertor);
                } catch (ClassNotFoundException e) {
                    JSON.LOG.warn(e);
                }
            }
            if (convertor != null) {
                return convertor.fromJSON(object);
            }
        }
        return object;
    }
}
