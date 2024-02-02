package javax.servlet.http;
/* loaded from: classes.dex */
public interface HttpServletMapping {
    MappingMatch getMappingMatch();

    String getMatchValue();

    String getPattern();

    String getServletName();
}
