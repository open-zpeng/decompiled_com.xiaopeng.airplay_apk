package javax.enterprise.context;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})
@Qualifier
@Documented
@Retention(RetentionPolicy.RUNTIME)
/* loaded from: classes.dex */
public @interface BeforeDestroyed {
    Class<? extends Annotation> value();

    /* loaded from: classes.dex */
    public static final class Literal extends AnnotationLiteral<BeforeDestroyed> implements BeforeDestroyed {
        private static final long serialVersionUID = 1;
        private final Class<? extends Annotation> value;
        public static final Literal REQUEST = of(RequestScoped.class);
        public static final Literal CONVERSATION = of(ConversationScoped.class);
        public static final Literal SESSION = of(SessionScoped.class);
        public static final Literal APPLICATION = of(ApplicationScoped.class);

        public static Literal of(Class<? extends Annotation> value) {
            return new Literal(value);
        }

        private Literal(Class<? extends Annotation> value) {
            this.value = value;
        }

        @Override // javax.enterprise.context.BeforeDestroyed
        public Class<? extends Annotation> value() {
            return this.value;
        }
    }
}
