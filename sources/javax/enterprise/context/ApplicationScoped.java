package javax.enterprise.context;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.enterprise.util.AnnotationLiteral;
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@NormalScope
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
/* loaded from: classes.dex */
public @interface ApplicationScoped {

    /* loaded from: classes.dex */
    public static final class Literal extends AnnotationLiteral<ApplicationScoped> implements ApplicationScoped {
        public static final Literal INSTANCE = new Literal();
        private static final long serialVersionUID = 1;
    }
}
