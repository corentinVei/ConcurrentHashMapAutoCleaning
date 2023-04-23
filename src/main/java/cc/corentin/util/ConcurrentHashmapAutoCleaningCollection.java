package cc.corentin.util;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * A collection containing the values of the valid entries in the map.
 *
 * @param <K> The type of the key in the hashmap.
 * @param <V> The type of the value in the hashmap.
 */
class ConcurrentHashmapAutoCleaningCollection<K, V> extends AbstractCollection<V> {

    private final ConcurrentMap<K, ValueWithTime<V>> map;
    private final ConcurrentHashMapAutoCleaning<K, V> concurrentHashMapAutoCleaning;

    public ConcurrentHashmapAutoCleaningCollection(ConcurrentMap<K, ValueWithTime<V>> map, ConcurrentHashMapAutoCleaning<K, V> concurrentHashMapAutoCleaning) {
        this.map = map;
        this.concurrentHashMapAutoCleaning = concurrentHashMapAutoCleaning;
    }

    @Override
    public Iterator<V> iterator() {
        return new ConcurrentHashmapAutoCleaningCollectionIterator(map.entrySet().iterator(), map);
    }

    class ConcurrentHashmapAutoCleaningCollectionIterator extends ConcurrentHashmapAutoCleaningBaseIterator<V, K, V> {


        public ConcurrentHashmapAutoCleaningCollectionIterator(Iterator<Map.Entry<K, ValueWithTime<V>>> iterator, ConcurrentMap<K, ValueWithTime<V>> map) {
            super(iterator, map);
        }

        @Override
        protected V getT(Map.Entry<K, ValueWithTime<V>> entry) {
            return entry.getValue().getValue();
        }
    }

    @Override
    public int size() {
        return concurrentHashMapAutoCleaning.size();
    }
}
