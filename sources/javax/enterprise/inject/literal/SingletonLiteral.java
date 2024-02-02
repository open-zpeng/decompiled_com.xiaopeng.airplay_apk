package javax.enterprise.inject.literal;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Singleton;
/* loaded from: classes.dex */
public final class SingletonLiteral extends AnnotationLiteral<Singleton> implements Singleton {
    public static final SingletonLiteral INSTANCE = new SingletonLiteral();
    private static final long serialVersionUID = 1;
}
