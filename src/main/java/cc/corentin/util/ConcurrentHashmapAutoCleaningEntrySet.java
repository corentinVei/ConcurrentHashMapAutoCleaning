package cc.corentin.util;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * A set containing the values of the valid entries in the map.
 *
 * @param <K> The type of the keys in the hashmap.
 * @param <V> The type of the values in the hashmap.
 */
class ConcurrentHashmapAutoCleaningEntrySet<K, V> extends AbstractSet<Map.Entry<K, V>> {

    private final ConcurrentMap<K, ValueWithTime<V>> map;
    private final ConcurrentHashMapAutoCleaning<K, V> concurrentHashMapAutoCleaning;

    public ConcurrentHashmapAutoCleaningEntrySet(ConcurrentMap<K, ValueWithTime<V>> map, ConcurrentHashMapAutoCleaning<K, V> concurrentHashMapAutoCleaning) {
        this.map = map;
        this.concurrentHashMapAutoCleaning = concurrentHashMapAutoCleaning;
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return new ConcurrentHashmapAutoCleaningSetIterator(map.entrySet().iterator(), map);
    }

    class ConcurrentHashmapAutoCleaningSetIterator extends ConcurrentHashmapAutoCleaningBaseIterator<Map.Entry<K, V>, K, V> {

        public ConcurrentHashmapAutoCleaningSetIterator(Iterator<Map.Entry<K, ValueWithTime<V>>> iterator, ConcurrentMap<K, ValueWithTime<V>> map) {
            super(iterator, map);
        }

        @Override
        protected Map.Entry<K, V> getT(Map.Entry<K, ValueWithTime<V>> entry) {
            return new ConcurrentHashMapAutoCleaningEntry<K,V>(entry);
        }

    }

    @Override
    public int size() {
        return concurrentHashMapAutoCleaning.size();
    }

    @Override
    public boolean contains(Object o) {
        if (!(o instanceof Map.Entry)) {
            return false;
        }
        @SuppressWarnings("unchecked")
        Map.Entry<K, V> entry = (Map.Entry<K, V>) o;

        ValueWithTime<V> valueWithTime = map.get(entry.getKey());
        return valueWithTime != null && valueWithTime.isValid() && valueWithTime.getValue().equals(entry.getValue());
    }

}
