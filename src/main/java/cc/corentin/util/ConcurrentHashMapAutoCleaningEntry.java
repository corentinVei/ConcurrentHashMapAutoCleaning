package cc.corentin.util;

import java.util.Map;
import java.util.Objects;

/**
 * A class that represents an entry in the Concurrent Hashmap Auto Cleaning
 *
 * @param <K> The type of the key
 * @param <V> The type of the value
 */
class ConcurrentHashMapAutoCleaningEntry<K,V> implements Map.Entry<K, V> {
    private final Map.Entry<K, ValueWithTime<V>> entry;

    public ConcurrentHashMapAutoCleaningEntry(Map.Entry<K, ValueWithTime<V>> entry) {
        this.entry = entry;
    }

    @Override
    public K getKey() {
        return entry.getKey();
    }

    @Override
    public V getValue() {
        return entry.getValue().getValue();
    }

    @Override
    public V setValue(V value) {
        throw new UnsupportedOperationException("This method is not supported by this entrySet if you want to change the value of an entry, use the replace() method");
    }

    @Override
    public String toString() {
        return getKey() + "=" + getValue();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getKey()) ^ Objects.hashCode(getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Map.Entry<?, ?> e))
            return false;
        return Objects.equals(getKey(), e.getKey()) &&
                Objects.equals(getValue(), e.getValue());
    }
}
