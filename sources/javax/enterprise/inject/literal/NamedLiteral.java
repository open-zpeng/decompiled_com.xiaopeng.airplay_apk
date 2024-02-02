package javax.enterprise.inject.literal;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Named;
/* loaded from: classes.dex */
public final class NamedLiteral extends AnnotationLiteral<Named> implements Named {
    public static final Named INSTANCE = of("");
    private static final long serialVersionUID = 1;
    private final String value;

    public static NamedLiteral of(String value) {
        return new NamedLiteral(value);
    }

    @Override // javax.inject.Named
    public String value() {
        return this.value;
    }

    private NamedLiteral(String value) {
        this.value = value;
    }
}
