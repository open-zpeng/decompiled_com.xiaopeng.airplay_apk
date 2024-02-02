package org.fourthline.cling.registry;

import org.fourthline.cling.model.ExpirationDetails;
/* loaded from: classes.dex */
class RegistryItem<K, I> {
    private ExpirationDetails expirationDetails;
    private I item;
    private K key;

    /* JADX INFO: Access modifiers changed from: package-private */
    public RegistryItem(K key) {
        this.expirationDetails = new ExpirationDetails();
        this.key = key;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public RegistryItem(K key, I item, int maxAgeSeconds) {
        this.expirationDetails = new ExpirationDetails();
        this.key = key;
        this.item = item;
        this.expirationDetails = new ExpirationDetails(maxAgeSeconds);
    }

    public K getKey() {
        return this.key;
    }

    public I getItem() {
        return this.item;
    }

    public ExpirationDetails getExpirationDetails() {
        return this.expirationDetails;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RegistryItem that = (RegistryItem) o;
        return this.key.equals(that.key);
    }

    public int hashCode() {
        return this.key.hashCode();
    }

    public String toString() {
        return "(" + getClass().getSimpleName() + ") " + getExpirationDetails() + " KEY: " + getKey() + " ITEM: " + getItem();
    }
}
