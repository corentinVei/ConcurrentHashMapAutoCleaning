package cc.corentin.util;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentMap;

/**
 * An abstract class that allows creating iterators over valid entries
 *
 * @param <T> The type of the iterator
 * @param <K> The type of the key in the hashmap
 * @param <V> The type of the value in the hashmap
 */
abstract class ConcurrentHashmapAutoCleaningBaseIterator<T, K, V> implements Iterator<T> {
    private final Iterator<Map.Entry<K, ValueWithTime<V>>> iterator;
    private final ConcurrentMap<K, ValueWithTime<V>> map;
    private Map.Entry<K, ValueWithTime<V>> nextEntry;
    private Map.Entry<K, ValueWithTime<V>> lastEntry;

    public ConcurrentHashmapAutoCleaningBaseIterator(Iterator<Map.Entry<K, ValueWithTime<V>>> iterator, ConcurrentMap<K, ValueWithTime<V>> map) {
        this.iterator = iterator;
        this.map = map;
        if (iterator.hasNext()) {
            nextEntry = iterator.next();
            if (!nextEntry.getValue().isValid()) {
                goToNextValidEntry();
            }
        }

    }

    /**
     * can return null if the next entry is not valid
     *
     * @throws IllegalStateException if the entry has been removed
     */
    @Override
    public T next() {
        if (nextEntry == null) {
            throw new NoSuchElementException();
        }
        if (!nextEntry.getValue().isValid()) {
            goToNextValidEntry();
            if (nextEntry == null) {
                return null;
            }
        }
        lastEntry = nextEntry;
        goToNextValidEntry();
        return getT(lastEntry);
    }

    abstract protected T getT(Map.Entry<K, ValueWithTime<V>> entry);

    @Override
    public boolean hasNext() {
        return nextEntry != null;

    }

    private void goToNextValidEntry() {
        while (iterator.hasNext()) {
            nextEntry = iterator.next();
            if (nextEntry.getValue().isValid()) {
                return;
            }
        }
        nextEntry = null;
    }

    @Override
    public void remove() {
        if (lastEntry == null) {
            throw new IllegalStateException();
        }
        map.remove(lastEntry.getKey());
        lastEntry = null;
    }
}
