package javax.servlet.descriptor;

import java.util.Collection;
/* loaded from: classes.dex */
public interface JspConfigDescriptor {
    Collection<JspPropertyGroupDescriptor> getJspPropertyGroups();

    Collection<TaglibDescriptor> getTaglibs();
}
