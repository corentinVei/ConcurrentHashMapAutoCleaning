package cc.corentin.util;


import java.io.Closeable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;



/**
 * <h2>Important Notes</h2>
 * <strong>/!\This class must be closed to disable automatic cleaning and the thread/!\</strong>
 * <h2>Overview</h2>
 * This class implements the ConcurrentMap interface and allows the creation of a map that automatically cleans up entries that have exceeded a predefined lifespan,
 * which must be defined in the constructor.
 *
 * <h2>Usage</h2>
 * To clean up the map of entries that have exceeded the predefined lifespan,
 * the setCleanPeriodMillis method must be called with a value greater than 0 in the constructor or using the setCleanPeriod(long cleanPeriodMillis) method.
 * A thread will be launched to clean up the map every cleanPeriodMillis milliseconds. If cleanPeriodMillis is set to 0, the thread will be stopped.
 * The cleanBlocking() method can be used to clean up the map manually without using a thread.
 *
 * <h2>Key Concepts</h2>
 * This class is designed to store a value along with its creation time and time of last use.
 * It is important to understand the distinction between the creationTimeMillis and lifeTimeMillis variables.
 * The former represents the date and time at which the value was created, while the latter represents the duration for which the value should remain valid.
 * For example, if the creationTimeMillis is 18/03/2023 12:00:00 and the lifeTimeMillis is set to 1 hour, the value will remain valid until 13:00:00.
 * The same logic applies to the lastTimeUsedMillis and extraLifeTimeAfterUseMillis variables.
 * The former represents the date and time at which the value was last used, while the latter represents the additional time that the value should remain valid after its last use.
 * For instance, if the lastTimeUsedMillis is 18/03/2023 12:59:00 and the extraLifeTimeAfterUseMillis is set to 10 minutes, the value will remain valid until 13:09:00.
 * Even if the combination of creationTimeMillis + lifeTimeMillis exceeds this period.
 * Note that the classic get method does not update the lastTimeUsedMillis variable, only the getSinceLastUseMillis(K key) method updates it.
 */
public class ConcurrentHashMapAutoCleaning<K, V> extends AbstractMap<K, V> implements ConcurrentMap<K, V>, Closeable {

    /**
     * A concurrent map that associates keys with a {@link ValueWithTime} object that stores the creation time and the last usage time of the associated value.
     */
    private final ConcurrentMap<K, ValueWithTime<V>> map;

    /**
     * The lifespan of entries is measured in milliseconds
     */
    private final long lifeTimeMillis;
    /**
     * extraLifeTimeAfterUseMillis The duration of time during which the key remains usable after its last usage.
     */
    private final long extraLifeTimeAfterUseMillis;
    /**
     * Thread name
     */
    private final String threadName;
    /**
     * Time between each cleanup in milliseconds
     */
    private long cleanPeriodMillis = -1;
    /**
     * TimerTask is used to clean up the map
     */
    private TimerTask timerTask;
    /**
     * Timer to clean up the map
     */
    private Timer timer;
    /**
     * List of listeners for removed entries
     */
    private final List<OnEntryRemovedListener<K, V>> listeners = new LinkedList<>();


    /**
     * The constructor launches the cleaning process upon the creation of the object, provided that the time between each cleaning is greater than 0.
     *
     * @param lifeTimeMillis              the lifetime of entries in milliseconds
     * @param extraLifeTimeAfterUseMillis is the time that the key can be used after the last usage.
     * @param cleanPeriodMillis           the time between each cleaning in milliseconds
     * @param threadName                  the name of the thread
     * @param initialCapacity             the initial capacity. The implementation
     *                                    performs internal sizing to accommodate this many elements,
     *                                    given the specified load factor.
     * @param loadFactor                  the load factor (table density) for
     *                                    establishing the initial table size
     * @param concurrencyLevel            the estimated number of concurrently
     *                                    updating threads. The implementation may use this value as
     *                                    a sizing hint.
     * @throws IllegalArgumentException if the initial capacity is
     *                                  negative or the load factor or concurrencyLevel are
     *                                  nonpositive
     */
    public ConcurrentHashMapAutoCleaning(long lifeTimeMillis, long extraLifeTimeAfterUseMillis, long cleanPeriodMillis, int initialCapacity,
                                         float loadFactor, int concurrencyLevel, String threadName) {
        this.map = new ConcurrentHashMap<>(initialCapacity, loadFactor, concurrencyLevel);
        this.lifeTimeMillis = lifeTimeMillis;
        this.extraLifeTimeAfterUseMillis = extraLifeTimeAfterUseMillis;
        this.threadName = threadName;
        setCleanPeriod(cleanPeriodMillis);
    }

    /**
     * The constructor launches the cleaning process upon the creation of the object, provided that the time between each cleaning is greater than 0.
     *
     * @param lifeTimeMillis              the lifetime of entries in milliseconds
     * @param extraLifeTimeAfterUseMillis The duration of time during which the key remains usable after its last usage.
     * @param cleanPeriodMillis           the time between each cleaning in milliseconds
     * @param threadName                  the name of the thread
     * @param initialCapacity             the initial capacity. The implementation
     *                                    performs internal sizing to accommodate this many elements,
     *                                    given the specified load factor.
     * @param loadFactor                  the load factor (table density) for
     *                                    establishing the initial table size
     * @throws IllegalArgumentException if the initial capacity is
     *                                  negative or the load factor or concurrencyLevel are
     *                                  nonpositive
     */
    public ConcurrentHashMapAutoCleaning(long lifeTimeMillis, long extraLifeTimeAfterUseMillis, long cleanPeriodMillis, int initialCapacity,
                                         float loadFactor, String threadName) {
        // Default value for concurrencyLevel is 1
        this(lifeTimeMillis, extraLifeTimeAfterUseMillis, cleanPeriodMillis, initialCapacity, loadFactor, 1, threadName);
    }

    /**
     * The constructor launches the cleaning process upon the creation of the object, provided that the time between each cleaning is greater than 0.
     *
     * @param lifeTimeMillis              the lifetime of entries in milliseconds
     * @param extraLifeTimeAfterUseMillis The duration of time during which the key remains usable after its last usage.
     * @param cleanPeriodMillis           the time between each cleaning in milliseconds
     * @param threadName                  the name of the thread
     * @param initialCapacity             the initial capacity. The implementation
     *                                    performs internal sizing to accommodate this many elements,
     *                                    given the specified load factor.
     * @throws IllegalArgumentException if the initial capacity of elements is negative
     */
    public ConcurrentHashMapAutoCleaning(long lifeTimeMillis, long extraLifeTimeAfterUseMillis, long cleanPeriodMillis, int initialCapacity, String threadName) {
        // Default value for loadFactor is 0.75f and concurrencyLevel is 1
        this(lifeTimeMillis, extraLifeTimeAfterUseMillis, cleanPeriodMillis, initialCapacity, 0.75f, 1, threadName);
    }

    /**
     * The constructor launches the cleaning process upon the creation of the object, provided that the time between each cleaning is greater than 0.
     *
     * @param lifeTimeMillis              the lifetime of entries in milliseconds
     * @param extraLifeTimeAfterUseMillis The duration of time during which the key remains usable after its last usage.
     * @param cleanPeriodMillis           the time between each cleaning in milliseconds
     * @param threadName                  the name of the thread
     */
    public ConcurrentHashMapAutoCleaning(long lifeTimeMillis, long extraLifeTimeAfterUseMillis, long cleanPeriodMillis, String threadName) {
        // Default values for initialCapacity, loadFactor and concurrencyLevel are 16, 0.75f and 1 respectively
        this(lifeTimeMillis, extraLifeTimeAfterUseMillis, cleanPeriodMillis, 16, 0.75f, 1, threadName);
    }

    /**
     * The constructor launches the cleaning process upon the creation of the object, provided that the time between each cleaning is greater than 0.
     *
     * @param lifeTimeMillis              the lifetime of entries in milliseconds
     * @param extraLifeTimeAfterUseMillis The duration of time during which the key remains usable after its last usage.
     * @param cleanPeriodMillis           the time between each cleaning in milliseconds
     */
    public ConcurrentHashMapAutoCleaning(long lifeTimeMillis, long extraLifeTimeAfterUseMillis, long cleanPeriodMillis) {
        this(lifeTimeMillis, extraLifeTimeAfterUseMillis, cleanPeriodMillis, "ConcurrentHashMapAutoCleaning");
    }

    /**
     * The constructor launches the cleaning process upon the creation of the object, provided that the time between each cleaning is greater than 0.
     *
     * @param lifeTimeMillis              the lifetime of entries in milliseconds
     * @param extraLifeTimeAfterUseMillis The duration of time during which the key remains usable after its last usage.
     */
    public ConcurrentHashMapAutoCleaning(long lifeTimeMillis, long extraLifeTimeAfterUseMillis) {
        this(lifeTimeMillis, extraLifeTimeAfterUseMillis, -1);
    }

    /**
     * The constructor launches the cleaning process upon the creation of the object, provided that the time between each cleaning is greater than 0.
     *
     * @param lifeTimeMillis the lifetime of entries in milliseconds
     */
    public ConcurrentHashMapAutoCleaning(long lifeTimeMillis) {
        this(lifeTimeMillis, 0, -1);
    }


    /**
     * Adds a listener function that will be invoked whenever an entry is removed by the cleaning thread of the ConcurrentHashMapAutoCleaning.
     *
     * @param listener the listener to add
     */
    public void addListener(OnEntryRemovedListener<K, V> listener) {
        listeners.add(listener);
    }


    /**
     * Returns the creation time of the entry associated with the specified key, in milliseconds.
     *
     * @param key the key associated with the entry whose creation time to retrieve.
     * @return The creation time of the entry in milliseconds, or -1 if the entry does not exist
     * @throws ClassCastException if the key is of an inappropriate type for this map
     */
    public long getCreationTimeMillis(Object key) {
        ValueWithTime<V> valueWithTime = map.get(key);
        if (valueWithTime == null) {
            return -1;
        }
        return valueWithTime.getCreationTimeMillis();
    }

    /**
     * Sets the creation time of the entry associated with the specified key.
     *
     * @param key                the key associated with the entry whose creation time to set.
     * @param creationTimeMillis the creation time of the entry, in milliseconds.
     * @return the old creation time in milliseconds or -1 if the entry does not exist or is no longer valid.
     */
    public long setCreationTimeMillis(Object key, long creationTimeMillis) {
        ValueWithTime<V> valueWithTime = map.get(key);
        if (valueWithTime == null || !valueWithTime.isValid()) {
            return -1;
        }
        return valueWithTime.setCreationTimeMillis(creationTimeMillis);
    }

    /**
     * Sets the lifespan of the entry associated with the specified key.
     *
     * @param key            the key associated with the entry whose lifespan to set.
     * @param lifeTimeMillis the lifespan of the entry, in milliseconds.
     * @return The previous lifeTime of the entry in milliseconds or -1 if the entry does not exist or is no longer valid.
     */
    public long setLifeTimeMillis(Object key, long lifeTimeMillis) {
        ValueWithTime<V> valueWithTime = map.get(key);
        if (valueWithTime == null || !valueWithTime.isValid()) {
            return -1;
        }
        return valueWithTime.setLifeTimeMillis(lifeTimeMillis);
    }

    /**
     * Returns the time in milliseconds since the last use of the entry with the specified key.
     *
     * @param key the key associated with the entry whose last usage time to retrieve.
     * @return The time in milliseconds since the last use of the entry, or -1 if the entry does not exist.
     */
    public long getLastTimeUsedMillis(Object key) {
        ValueWithTime<V> valueWithTime = map.get(key);
        if (valueWithTime == null) {
            return -1;
        }
        return valueWithTime.getLastTimeUsedMillis();
    }

    /**
     * Sets the additional lifespan of the entry associated with the specified key after its last usage
     *
     * @param key                            the key associated with the entry whose additional lifespan to set.
     * @param setExtraLifeTimeAfterUseMillis The duration of time during which the key remains usable after its last usage.
     * @return the old ExtraLifeTimeAfterUseMillis in milliseconds, or -1 if the entry does not exist or is no longer valid.
     */
    public long setExtraLifeTimeAfterUseMillis(Object key, long setExtraLifeTimeAfterUseMillis) {
        ValueWithTime<V> valueWithTime = map.get(key);
        if (valueWithTime == null || !valueWithTime.isValid()) {
            return -1;
        }
        return valueWithTime.setExtraLifeTimeAfterUseMillis(setExtraLifeTimeAfterUseMillis);
    }


    /**
     * This method provides a way to manually clean the map of entries that have exceeded their lifespan. It is a blocking method that
     * iterates over the map and removes all invalid entries. However, it is recommended to use the {@link #setCleanPeriod(long)} method instead,
     * which automatically cleans the map at defined intervals.
     */
    public void cleanBlocking() {
        Iterator<K> iterator = map.keySet().iterator();
        // Browse all keys and remove invalid entries.
        while (iterator.hasNext()) {
            K key = iterator.next();
            ValueWithTime<V> valueWithTime = map.get(key);
            if (!valueWithTime.isValid()) {
                iterator.remove();
            }
        }
    }

    /**
     * This method removes entries from the map that have exceeded their specified lifetime.
     * This method is used to clean the map by the Timer.
     */
    private void cleanTimer() {
        Iterator<K> iterator = map.keySet().iterator();
        // Iterate through all keys in the view and remove invalid entries
        while (iterator.hasNext()) {
            K key = iterator.next();
            ValueWithTime<V> valueWithTime = map.get(key);
            if (!valueWithTime.isValid()) {
                listeners.forEach(listener -> listener.onEntryRemoved(key, valueWithTime.getValue()));
                iterator.remove();
            }
        }
    }


    /**
     * Creates a named Timer with a specified thread name and sets the thread's priority to minimum.
     *
     * @return a Timer object
     */
    private Timer createNamedTimer() {
        Timer timer = new Timer(threadName, true);
        timer.schedule(new TimerTask() {
            public void run() {
                Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            }
        }, 0);
        return timer;
    }


    /**
     * This method allows changing the time between each map cleanup or disabling the cleanup by setting the value to 0.
     *
     * @param cleanPeriodMillis The time between each cleanup in milliseconds
     */
    public void setCleanPeriod(long cleanPeriodMillis) {
        if (cleanPeriodMillis <= 0) {
            disableCleaning();
            return;
        }
        if (cleanPeriodMillis == this.cleanPeriodMillis) {
            return;
        }
        this.cleanPeriodMillis = cleanPeriodMillis;
        reprogramCleaningProcess();
    }

    /**
     * This method disables the cleaning process, note that the Thread is removed
     */
    private void disableCleaning() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    /**
     * This method reprograms the cleaning process for a new period.
     */
    private void reprogramCleaningProcess() {
        if (timerTask != null) {
            timerTask.cancel();
        }
        if (timer == null) {
            timer = createNamedTimer();
        }
        timerTask = new TimerTask() {
            @Override
            public void run() {
                cleanTimer();
            }
        };
        timer.schedule(timerTask, 0, cleanPeriodMillis);
    }


    /**
     * This function allows to disable the cleaning
     * It's important to call it once we no longer need the object to close the thread.
     */
    @Override
    public void close() {
        disableCleaning();
    }

    /**
     * <strong>/!\ This function may take a while to execute if the map is large. /!\</strong>
     * The reason why this function is slow is that it is not possible to know how many invalid values are in the map.
     * And there is no solution that does not increase the complexity of the put.
     * Returns the size of the map.
     * {@inheritDoc}
     */
    @Override
    public int size() {
        int size = 0;
        for (ValueWithTime<V> valueWithTime : map.values()) {
            if (valueWithTime.isValid()) {
                size++;
            }
        }
        return size;
    }

    /**
     * @return the size of the map, invalid entries are also include.
     */
    public int sizeWithExpired() {
        return map.size();
    }

    /**
     * <strong>/!\ This function may take a while if the map is large. /!\</strong>
     * Returns true if the map is empty.
     * This method manually iterates through the values of the map instead of using the size() method of the ConcurrentHashMap class,
     * because it is possible that some invalid entries might not have been removed from the map yet.
     * The isEmptyWithInvalid() function exists to check if the map is empty with invalid entries.
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        for (ValueWithTime<V> valueWithTime : map.values()) {
            if (valueWithTime.isValid()) {
                return false;
            }
        }
        return true;
    }


    /**
     * @return true if the map is empty, invalid entries are include.
     */
    public boolean isEmptyWithInvalid() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        if (key == null)
            throw new NullPointerException();
        ValueWithTime<V> valueWithTime = map.get(key);
        return valueWithTime != null && valueWithTime.isValid();
    }

    @Override
    public boolean containsValue(Object value) {
        if (value == null)
            throw new NullPointerException();
        for (ValueWithTime<V> valueWithTime : map.values()) {
            if (valueWithTime.isValid() && valueWithTime.getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(Object key) {
        if (key == null)
            throw new NullPointerException();
        ValueWithTime<V> valueWithTime = map.get(key);
        if (valueWithTime == null) {
            return null;
        }
        return valueWithTime.getValueIfValid();
    }

    /**
     * This method returns the value associated with the specified key in the map, or null if the key is not mapped to any value.
     * Also, it updates the time since the key was last used.
     * @param key the key whose associated value is to be returned
     * @return the value associated with the specified key in this map. If the key is not mapped to any value, this method returns null.
     */
    public V getAndUpdateTimeSinceLastUse(Object key) {
        if (key == null)
            throw new NullPointerException();
        ValueWithTime<V> valueWithTime = map.get(key);
        if (valueWithTime == null) {
            return null;
        }
        if (valueWithTime.isValid()) {
            valueWithTime.updateLastTimeUsedMillis();
        }
        return valueWithTime.getValueIfValid();
    }

    /**
     * Add a value to the map with the default lifetime and the default time since the last usage.
     * @throws IllegalStateException if no default lifetime is set
     * {@inheritDoc}
     */
    @Override
    public V put(K key, V value) {
        if (key == null || value == null)
            throw new NullPointerException();
        if (lifeTimeMillis <= -1) {
            throw new IllegalStateException("no default life time set in the constructor, use put(K key, V value, long lifeTimeMillis) instead");
        }
        return put(key, value, lifeTimeMillis, extraLifeTimeAfterUseMillis);
    }

    /**
     * add a value to the map with a specific lifetime and the default time since last use
     *
     * @param key            key with which the specified value is to be associated
     * @param value          value to be associated with the specified key
     * @param lifeTimeMillis the lifetime of the value in milliseconds
     * @return the previous value associated with key, or null if there was no mapping for key.
     * @throws IllegalStateException if no default time since last use is set
     */
    public V put(K key, V value, long lifeTimeMillis) {
        if (key == null || value == null)
            throw new NullPointerException();
        return put(key, value, lifeTimeMillis, extraLifeTimeAfterUseMillis);
    }

    /**
     * add a value to the map with a specific lifetime and a specific time since last use
     *
     * @param key                         key with which the specified value is to be associated
     * @param value                       value to be associated with the specified key
     * @param lifeTimeMillis              the lifetime of the value in milliseconds
     * @param ExtraLifeTimeAfterUseMillis The duration of time during which the key remains usable after its last usage.
     * @return the previous value associated with key, or null if there was no mapping for key.
     */
    public V put(K key, V value, long lifeTimeMillis, long ExtraLifeTimeAfterUseMillis) {
        if (key == null || value == null)
            throw new NullPointerException();
        ValueWithTime<V> oldValue = map.put(key, new ValueWithTime<>(value, lifeTimeMillis, ExtraLifeTimeAfterUseMillis));
        if (oldValue == null) {
            return null;
        }
        return oldValue.getValueIfValid();
    }


    @Override
    public V remove(Object key) {
        if (key == null)
            throw new NullPointerException();
        ValueWithTime<V> valueWithTime = map.remove(key);
        if (valueWithTime == null) {
            return null;
        }
        return valueWithTime.getValueIfValid();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        if (m == null)
            throw new NullPointerException();
        for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<K> keySet() {
        return new ConcurrentHashmapAutoCleaningKeySet<K,V>(map, this);
    }

    @Override
    public Collection<V> values() {
        return new ConcurrentHashmapAutoCleaningCollection<K,V>(map, this);
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new ConcurrentHashmapAutoCleaningEntrySet<K,V>(map, this);
    }

    @Override
    public V putIfAbsent(K key, V value) {
        if (key == null || value == null)
            throw new NullPointerException();
        if (lifeTimeMillis <= -1) {
            throw new IllegalStateException("no default life time set, use put(K key, V value, long lifeTimeMillis) instead");
        }
        return putIfAbsent(key, value, lifeTimeMillis, extraLifeTimeAfterUseMillis);
    }

    /**
     * If the specified key is not already associated with a value (or is mapped to null) associates it with the given value and returns null, else returns the current value.
     *
     * @param key            key with which the specified value is to be associated
     * @param value          value to be associated with the specified key
     * @param lifeTimeMillis the lifetime of the value in milliseconds
     * @return the previous value associated with key, or null if there was no mapping for key.
     */
    public V putIfAbsent(K key, V value, long lifeTimeMillis) {
        if (key == null || value == null)
            throw new NullPointerException();
        return putIfAbsent(key, value, lifeTimeMillis, extraLifeTimeAfterUseMillis);
    }

    /**
     * If the specified key is not already associated with a value (or is mapped to null) associates it with the given value and returns null, else returns the current value.
     *
     * @param key                         key with which the specified value is to be associated
     * @param value                       value to be associated with the specified key
     * @param lifeTimeMillis              the lifetime of the value in milliseconds
     * @param extraLifeTimeAfterUseMillis is the time that the key can be used after the last usage.
     * @return the previous value associated with key, or null if there was no mapping for key.
     */
    public V putIfAbsent(K key, V value, long lifeTimeMillis, long extraLifeTimeAfterUseMillis) {
        if (key == null || value == null)
            throw new NullPointerException();
        if (containsKey(key)) {
            return get(key);
        } else {
            map.put(key, new ValueWithTime<>(value, lifeTimeMillis, extraLifeTimeAfterUseMillis));
            return null;
        }
    }


    @Override
    public boolean remove(Object key, Object value) {
        // We check that the key exists and also that it is still valid to know if we return true or false.
        // The function must not return true if the key exists but the value is no longer valid.
        if (containsKey(key) && Objects.equals(map.get(key), value)) {
            map.remove(key);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        if (lifeTimeMillis <= -1) {
            throw new IllegalStateException("no default life time set, use put(K key, V value, long lifeTimeMillis) instead");
        }
        return replace(key, oldValue, newValue, lifeTimeMillis, extraLifeTimeAfterUseMillis);
    }

    /**
     * If the specified key is not already associated with a value (or is mapped to null) associates it with the given value and returns null, else returns the current value.
     *
     * @param key            key with which the specified value is to be associated
     * @param oldValue       value expected to be associated with the specified key
     * @param newValue       value to be associated with the specified key
     * @param lifeTimeMillis the lifetime of the value in milliseconds
     * @return the previous value associated with key, or null if there was no mapping for key.
     */
    public boolean replace(K key, V oldValue, V newValue, long lifeTimeMillis) {
        return replace(key, oldValue, newValue, lifeTimeMillis, extraLifeTimeAfterUseMillis);
    }

    /**
     * If the specified key is not already associated with a value (or is mapped to null) associates it with the given value and returns null, else returns the current value.
     *
     * @param key                         key with which the specified value is to be associated
     * @param oldValue                    value expected to be associated with the specified key
     * @param newValue                    value to be associated with the specified key
     * @param lifeTimeMillis              the lifetime of the value in milliseconds
     * @param ExtraLifeTimeAfterUseMillis is the time that the key can be used after the last usage.
     * @return the previous value associated with key, or null if there was no mapping for key.
     */
    public boolean replace(K key, V oldValue, V newValue, long lifeTimeMillis, long ExtraLifeTimeAfterUseMillis) {
        ValueWithTime<V> v = map.get(key);
        if (v == null) {
            return false;
        } else {
            if (v.isValid() && Objects.equals(v.getValue(), oldValue)) {
                map.replace(key, new ValueWithTime<>(newValue, lifeTimeMillis, ExtraLifeTimeAfterUseMillis));
                return true;
            } else {
                return false;
            }
        }
    }


    @Override
    public V replace(K key, V value) {
        if (lifeTimeMillis <= -1) {
            throw new IllegalStateException("no default life time set, use put(K key, V value, long lifeTimeMillis) instead");
        }
        return replace(key, value, lifeTimeMillis, extraLifeTimeAfterUseMillis);
    }

    /**
     * Replaces the entry for a key only if currently mapped to some value.
     *
     * @param key            key with which the specified value is associated
     * @param value          value to be associated with the specified key
     * @param lifeTimeMillis the lifetime of the value in milliseconds
     * @return the previous value associated with the specified key, or null if there was no mapping for the key
     */
    public V replace(K key, V value, long lifeTimeMillis) {
        return replace(key, value, lifeTimeMillis, extraLifeTimeAfterUseMillis);
    }

    /**
     * Replaces the entry for a key only if currently mapped to some value.
     *
     * @param key                         key with which the specified value is associated
     * @param value                       value to be associated with the specified key
     * @param lifeTimeMillis              the lifetime of the value in milliseconds
     * @param ExtraLifeTimeAfterUseMillis is the time that the key can be used after the last usage.
     * @return the previous value associated with the specified key, or null if there was no mapping for the key
     */
    public V replace(K key, V value, long lifeTimeMillis, long ExtraLifeTimeAfterUseMillis) {
        ValueWithTime<V> v = map.get(key);
        if (v == null) {
            return null;
        } else {
            if (v.isValid()) {
                ValueWithTime<V> oldValue = map.replace(key, new ValueWithTime<>(value, lifeTimeMillis, ExtraLifeTimeAfterUseMillis));
                if (oldValue == null) {
                    return null;
                } else {
                    return oldValue.getValueIfValid();
                }
            } else {
                return null;
            }
        }
    }
}


