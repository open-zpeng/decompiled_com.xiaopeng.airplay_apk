package javax.enterprise.inject.spi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.lang.reflect.Member;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
/* loaded from: classes.dex */
public interface AnnotatedParameter<X> extends Annotated {
    AnnotatedCallable<X> getDeclaringCallable();

    int getPosition();

    default Parameter getJavaParameter() {
        Member member = getDeclaringCallable().getJavaMember();
        if (!(member instanceof Executable)) {
            throw new IllegalStateException("Parameter does not belong to an executable: " + member);
        }
        Executable executable = (Executable) member;
        return executable.getParameters()[getPosition()];
    }

    @Override // javax.enterprise.inject.spi.Annotated
    default <T extends Annotation> Set<T> getAnnotations(Class<T> annotationType) {
        return new LinkedHashSet(Arrays.asList(getJavaParameter().getAnnotationsByType(annotationType)));
    }
}
