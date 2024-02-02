package javax.enterprise.inject.spi;
/* loaded from: classes.dex */
public interface ProcessSessionBean<X> extends ProcessManagedBean<Object> {
    String getEjbName();

    SessionBeanType getSessionBeanType();
}
