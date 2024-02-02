package javax.servlet;

import java.util.Set;
/* loaded from: classes.dex */
public interface ServletContainerInitializer {
    void onStartup(Set<Class<?>> set, ServletContext servletContext) throws ServletException;
}
