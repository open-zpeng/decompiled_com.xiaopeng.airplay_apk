package org.fourthline.cling.model.resource;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.fourthline.cling.model.ExpirationDetails;
/* loaded from: classes.dex */
public class Resource<M> {
    private M model;
    private URI pathQuery;

    public Resource(URI pathQuery, M model) {
        try {
            this.pathQuery = new URI(null, null, pathQuery.getPath(), pathQuery.getQuery(), null);
            this.model = model;
            if (model == null) {
                throw new IllegalArgumentException("Model instance must not be null");
            }
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    public URI getPathQuery() {
        return this.pathQuery;
    }

    public M getModel() {
        return this.model;
    }

    public boolean matches(URI pathQuery) {
        return pathQuery.equals(getPathQuery());
    }

    public void maintain(List<Runnable> pendingExecutions, ExpirationDetails expirationDetails) {
    }

    public void shutdown() {
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Resource resource = (Resource) o;
        if (getPathQuery().equals(resource.getPathQuery())) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return getPathQuery().hashCode();
    }

    public String toString() {
        return "(" + getClass().getSimpleName() + ") URI: " + getPathQuery();
    }
}
