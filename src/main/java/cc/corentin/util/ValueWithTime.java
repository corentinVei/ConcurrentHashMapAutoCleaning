package cc.corentin.util;

/**
 * This class allows for storing a value with its creation time and time of last use. It is used by the ConcurrentHashMapAutoCleaning class.
 * It is important to differentiate between the creationTimeMillis and lifeTimeMillis variables. The former represents the date and time the value was created,
 * while the latter represents the duration for which the value should remain valid.
 * For example, if the creationTimeMillis is 18/03/2023 12:00:00 and the lifeTimeMillis is 1 hour, then the value will remain valid until 13:00:00.
 * The same is true for the lastTimeUsedMillis and extraLifeTimeAfterUseMillis variables.
 * The former represents the date and time the value was last used,
 * while the latter represents the additional time that the value should remain valid after its last use.
 * For instance, if the lastTimeUsedMillis is 18/03/2023 12:59:00 and the extraLifeTimeAfterUseMillis is 10 minutes,
 * then the value will remain valid until 13:09:00, even if creationTimeMillis + lifeTimeMillis is exceeded.
 *
 * @param <V> the type of value being stored
 */
class ValueWithTime<V> {
    /**
     * The value
     */
    private final V value;
    /**
     * creationTimeMillis represents the UNIX date of the creation of the value
     */
    private long creationTimeMillis;
    /**
     * lifeTimeMillis the lifetime of entries in milliseconds
     */
    private long lifeTimeMillis;
    /**
     * lastTimeUsedMillis represents the UNIX date of the last use of the value
     */
    private long lastTimeUsedMillis;
    /**
     * extraLifeTimeAfterUseMillis is the time that the key can be used after the last usage.
     */
    private long extraLifeTimeAfterUseMillis;

    /**
     * Constructor
     *
     * @param value the value to store
     * @throws IllegalArgumentException if the value is null
     */
    public ValueWithTime(V value, long lifeTimeMillis, long extraLifeTimeAfterUseMillis) {
        if (value == null) {
            throw new IllegalArgumentException("value cannot be null");
        }
        this.value = value;
        this.creationTimeMillis = TimeHelper.currentTimeMillis();
        this.lastTimeUsedMillis = creationTimeMillis;
        this.lifeTimeMillis = lifeTimeMillis;
        this.extraLifeTimeAfterUseMillis = extraLifeTimeAfterUseMillis;
    }

    /**
     * This method allows to retrieve the value.
     *
     * @return the value.
     */
    public V getValue() {
        return value;
    }

    /**
     * This method updates the time of last use.
     */
    public void updateLastTimeUsedMillis() {
        lastTimeUsedMillis = TimeHelper.currentTimeMillis();
    }

    /**
     * This method sets the creation time.
     *
     * @param creationTimeMillis the creation time in milliseconds.
     * @return the old creation time in milliseconds.
     */
    public long setCreationTimeMillis(long creationTimeMillis) {
        long oldCreationTimeMillis = this.creationTimeMillis;
        this.creationTimeMillis = creationTimeMillis;
        return oldCreationTimeMillis;
    }

    /**
     * This method allows to retrieve the creation time.
     *
     * @return the creation time in milliseconds.
     */
    public long getCreationTimeMillis() {
        return creationTimeMillis;
    }

    /**
     * This method allows to set the lifetime.
     *
     * @param lifeTimeMillis the lifetime in milliseconds.
     * @return the old lifetime in milliseconds.
     */
    public long setLifeTimeMillis(long lifeTimeMillis) {
        long oldLifeTimeMillis = this.lifeTimeMillis;
        this.lifeTimeMillis = lifeTimeMillis;
        return oldLifeTimeMillis;
    }

    /**
     * This method allows retrieving the time since last use.
     *
     * @return the time since last use in milliseconds.
     */
    public long getLastTimeUsedMillis() {
        return lastTimeUsedMillis;
    }

    /**
     * This function allows to set ExtraLifeTimeAfterUseMillis
     *
     * @param extraLifeTimeAfterUseMillis the extra lifetime after use in milliseconds.
     * @return the old extra lifetime after use in milliseconds.
     */
    public long setExtraLifeTimeAfterUseMillis(long extraLifeTimeAfterUseMillis) {
        long oldExtraLifeTimeAfterUseMillis = this.extraLifeTimeAfterUseMillis;
        this.extraLifeTimeAfterUseMillis = extraLifeTimeAfterUseMillis;
        return oldExtraLifeTimeAfterUseMillis;
    }

    /**
     * This method determines if the entry is still valid.
     *
     * @return true if the entry is valid, otherwise false
     */
    public boolean isValid() {
        long currentTimeMillis = TimeHelper.currentTimeMillis();
        return currentTimeMillis - creationTimeMillis < lifeTimeMillis || currentTimeMillis - lastTimeUsedMillis < extraLifeTimeAfterUseMillis;
    }

    /**
     * This method allows to retrieve the value if it is valid, otherwise null.
     *
     * @return the value if it is valid, otherwise null
     */
    public V getValueIfValid() {
        if (isValid()) {
            return value;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ValueWithTime<?>) {
            return value.equals(((ValueWithTime<?>) o).getValue());
        }
        return value.equals(o);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
