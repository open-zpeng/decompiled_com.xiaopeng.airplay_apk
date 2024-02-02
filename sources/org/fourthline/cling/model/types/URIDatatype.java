package org.fourthline.cling.model.types;

import java.net.URI;
import java.net.URISyntaxException;
/* loaded from: classes.dex */
public class URIDatatype extends AbstractDatatype<URI> {
    @Override // org.fourthline.cling.model.types.AbstractDatatype, org.fourthline.cling.model.types.Datatype
    public URI valueOf(String s) throws InvalidValueException {
        if (s.equals("")) {
            return null;
        }
        try {
            return new URI(s);
        } catch (URISyntaxException ex) {
            throw new InvalidValueException(ex.getMessage(), ex);
        }
    }
}
