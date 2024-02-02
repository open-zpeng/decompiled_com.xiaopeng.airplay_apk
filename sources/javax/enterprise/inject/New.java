package javax.enterprise.inject;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.TYPE})
@Qualifier
@Documented
@Retention(RetentionPolicy.RUNTIME)
/* loaded from: classes.dex */
public @interface New {
    Class<?> value() default New.class;

    /* loaded from: classes.dex */
    public static final class Literal extends AnnotationLiteral<New> implements New {
        public static final Literal INSTANCE = of(New.class);
        private static final long serialVersionUID = 1;
        private final Class<?> value;

        public static Literal of(Class<?> value) {
            return new Literal(value);
        }

        private Literal(Class<?> value) {
            this.value = value;
        }

        @Override // javax.enterprise.inject.New
        public Class<?> value() {
            return this.value;
        }
    }
}
