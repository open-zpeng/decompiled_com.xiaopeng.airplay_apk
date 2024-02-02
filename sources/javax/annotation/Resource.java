package javax.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
@Repeatable(Resources.class)
@Retention(RetentionPolicy.RUNTIME)
/* loaded from: classes.dex */
public @interface Resource {

    /* loaded from: classes.dex */
    public enum AuthenticationType {
        CONTAINER,
        APPLICATION
    }

    AuthenticationType authenticationType() default AuthenticationType.CONTAINER;

    String description() default "";

    String lookup() default "";

    String mappedName() default "";

    String name() default "";

    boolean shareable() default true;

    Class<?> type() default Object.class;
}
