package org.eclipse.jetty.util.ajax;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.eclipse.jetty.util.Loader;
import org.eclipse.jetty.util.ajax.JSON;
import org.seamless.xhtml.XHTML;
/* loaded from: classes.dex */
public class JSONCollectionConvertor implements JSON.Convertor {
    @Override // org.eclipse.jetty.util.ajax.JSON.Convertor
    public void toJSON(Object obj, JSON.Output out) {
        out.addClass(obj.getClass());
        out.add("list", ((Collection) obj).toArray());
    }

    @Override // org.eclipse.jetty.util.ajax.JSON.Convertor
    public Object fromJSON(Map object) {
        try {
            Collection result = (Collection) Loader.loadClass(getClass(), (String) object.get(XHTML.ATTR.CLASS)).newInstance();
            Collections.addAll(result, (Object[]) object.get("list"));
            return result;
        } catch (Exception x) {
            if (x instanceof RuntimeException) {
                throw ((RuntimeException) x);
            }
            throw new RuntimeException(x);
        }
    }
}
