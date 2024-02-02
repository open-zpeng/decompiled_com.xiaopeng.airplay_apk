package javax.enterprise.context;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Scope;
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.FIELD})
@Inherited
@Scope
@Documented
@Retention(RetentionPolicy.RUNTIME)
/* loaded from: classes.dex */
public @interface Dependent {

    /* loaded from: classes.dex */
    public static final class Literal extends AnnotationLiteral<Dependent> implements Dependent {
        public static final Literal INSTANCE = new Literal();
        private static final long serialVersionUID = 1;
    }
}
