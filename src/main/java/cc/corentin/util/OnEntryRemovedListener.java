package cc.corentin.util;

/**
 * This interface defines a callback method that is called when an entry is removed by the cleaning thread of ConcurrentHashMapAutoCleaning.
 *
 * @param <K> the type of keys maintained by the map
 * @param <V> the type of mapped values
 */
interface OnEntryRemovedListener<K, V> {

    /**
     * This method is called when an entry is removed by the cleaning thread of ConcurrentHashMapAutoCleaning.
     *
     * @param key   the key of the removed entry
     * @param value the value of the removed entry
     */
    void onEntryRemoved(K key, V value);
}
