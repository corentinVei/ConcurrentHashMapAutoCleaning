package cc.corentin.util;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * A collection containing the values of the valid entries in the map.
 *
 * @param <K> The type of the key in the hashmap.
 * @param <V> The type of the value in the hashmap.
 */
class ConcurrentHashmapAutoCleaningKeySet<K, V> extends AbstractSet<K> {

    private final ConcurrentMap<K, ValueWithTime<V>> map;
    private final ConcurrentHashMapAutoCleaning<K, V> concurrentHashMapAutoCleaning;

    public ConcurrentHashmapAutoCleaningKeySet(ConcurrentMap<K, ValueWithTime<V>> map, ConcurrentHashMapAutoCleaning<K, V> concurrentHashMapAutoCleaning) {
        this.map = map;
        this.concurrentHashMapAutoCleaning = concurrentHashMapAutoCleaning;
    }

    @Override
    public boolean contains(Object o) {
        return concurrentHashMapAutoCleaning.containsKey(o);
    }


    @Override
    public Iterator<K> iterator() {
        return new ConcurrentHashmapAutoCleaningSetIterator(map.entrySet().iterator(), map);
    }

    class ConcurrentHashmapAutoCleaningSetIterator extends ConcurrentHashmapAutoCleaningBaseIterator<K, K, V> {


        public ConcurrentHashmapAutoCleaningSetIterator(Iterator<Map.Entry<K, ValueWithTime<V>>> iterator, ConcurrentMap<K, ValueWithTime<V>> map) {
            super(iterator, map);
        }

        @Override
        protected K getT(Map.Entry<K, ValueWithTime<V>> entry) {
            return entry.getKey();
        }
    }

    @Override
    public boolean add(K k) {
        throw new UnsupportedOperationException("An concurrentMapSet is not modifiable");
    }

    @Override
    public boolean remove(Object o) {
        return concurrentHashMapAutoCleaning.remove(o) != null;
    }

    @Override
    public int size() {
        return concurrentHashMapAutoCleaning.size();
    }

    @Override
    public boolean isEmpty() {
        return concurrentHashMapAutoCleaning.isEmpty();
    }
}
