package javax.enterprise.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
/* loaded from: classes.dex */
public @interface Nonbinding {

    /* loaded from: classes.dex */
    public static final class Literal extends AnnotationLiteral<Nonbinding> implements Nonbinding {
        public static final Literal INSTANCE = new Literal();
        private static final long serialVersionUID = 1;
    }
}
