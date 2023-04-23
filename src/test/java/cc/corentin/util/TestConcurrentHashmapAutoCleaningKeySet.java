package cc.corentin.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

public class TestConcurrentHashmapAutoCleaningKeySet {

    @Test
    public void testContains(){
        ConcurrentHashMapAutoCleaning<String, String> map = new ConcurrentHashMapAutoCleaning<>(Long.MAX_VALUE);
        ConcurrentHashMap<String, String> mapReference = new ConcurrentHashMap<>();

        Set<String> set = map.keySet();
        Set<String> setReference = mapReference.keySet();

        assertEquals(setReference.contains("key1"),set.contains("key1") );

        map.put("key1", "value1");
        mapReference.put("key1", "value1");

        assertEquals(setReference.contains("key1"),set.contains("key1") );

        map.remove("key1");
        mapReference.remove("key1");

        assertEquals(setReference.contains("key1"),set.contains("key1") );

        map.put("key1", "value1");
        map.setLifeTimeMillis("key1", 0);

        assertEquals(setReference.contains("key1"),set.contains("key1") );
        map.close();
    }

    @Test
    public void testToArray(){
        ConcurrentHashMapAutoCleaning<String, String> map = new ConcurrentHashMapAutoCleaning<>(Long.MAX_VALUE);
        ConcurrentHashMap<String, String> mapReference = new ConcurrentHashMap<>();

        Set<String> set = map.keySet();
        Set<String> setReference = mapReference.keySet();

        assertArrayEquals(setReference.toArray(), set.toArray());

        map.put("key1", "value1");
        mapReference.put("key1", "value1");

        assertArrayEquals(setReference.toArray(), set.toArray());

        map.put("key2", "value2");
        assertFalse(Arrays.equals(setReference.toArray(), set.toArray()));

        map.setLifeTimeMillis("key2", 0);

        assertArrayEquals(setReference.toArray(), set.toArray());
        map.close();

    }


    @Test
    public void testAddAllTest(){
        ConcurrentHashMapAutoCleaning<String, String> map = new ConcurrentHashMapAutoCleaning<>(Long.MAX_VALUE);
        ConcurrentHashMap<String, String> mapReference = new ConcurrentHashMap<>();

        Set<String> set = map.keySet();
        Set<String> setReference = mapReference.keySet();

        assertThrows(UnsupportedOperationException.class, () -> set.addAll(Arrays.asList("key1", "key2")));
        assertThrows(UnsupportedOperationException.class, () -> setReference.addAll(Arrays.asList("key1", "key2")));

        map.close();
    }



    @Test
    public void testClear(){
        ConcurrentHashMapAutoCleaning<String, String> map = new ConcurrentHashMapAutoCleaning<>(Long.MAX_VALUE);
        ConcurrentHashMap<String, String> mapReference = new ConcurrentHashMap<>();

        Set<String> set = map.keySet();
        Set<String> setReference = mapReference.keySet();

        map.put("key1", "value1");
        mapReference.put("key1", "value1");
        map.put("key2", "value2");
        mapReference.put("key2", "value2");

        set.clear();
        setReference.clear();

        assertEquals(setReference.size(), set.size());

        map.close();
    }

    @Test
    public void testEquals(){
        ConcurrentHashMapAutoCleaning<String, String> map = new ConcurrentHashMapAutoCleaning<>(Long.MAX_VALUE);
        ConcurrentHashMap<String, String> mapReference = new ConcurrentHashMap<>();

        Set<String> set = map.keySet();
        Set<String> setReference = mapReference.keySet();

        map.put("key1", "value1");
        mapReference.put("key1", "value1");

        assertEquals(setReference, set);

        map.put("key2", "value2");

        assertNotEquals(setReference, set);

        map.setLifeTimeMillis("key2", 0);

        assertEquals(setReference, set);

        map.close();
    }

    @Test
    public void testHashCode(){
        ConcurrentHashMapAutoCleaning<String, String> map = new ConcurrentHashMapAutoCleaning<>(Long.MAX_VALUE);
        ConcurrentHashMap<String, String> mapReference = new ConcurrentHashMap<>();

        Set<String> set = map.keySet();
        Set<String> setReference = mapReference.keySet();

        map.put("key1", "value1");
        mapReference.put("key1", "value1");


        assertEquals(setReference.hashCode(), set.hashCode());

        map.put("key2", "value2");

        assertNotEquals(setReference.hashCode(), set.hashCode());

        map.close();
    }

    @Test
    public void testSize(){
        ConcurrentHashMapAutoCleaning<String, String> map = new ConcurrentHashMapAutoCleaning<>(Long.MAX_VALUE);
        ConcurrentHashMap<String, String> mapReference = new ConcurrentHashMap<>();

        Set<String> set = map.keySet();
        Set<String> setReference = mapReference.keySet();

        map.put("key1", "value1");
        mapReference.put("key1", "value1");

        assertEquals(setReference.size(), set.size());

        map.put("key2", "value2");

        assertNotEquals(setReference.size(), set.size());

        map.setLifeTimeMillis("key2", 0);

        assertEquals(setReference.size(), set.size());

        map.close();
    }

    @Test
    public void testIsEmpty(){
        ConcurrentHashMapAutoCleaning<String, String> map = new ConcurrentHashMapAutoCleaning<>(Long.MAX_VALUE);
        ConcurrentHashMap<String, String> mapReference = new ConcurrentHashMap<>();

        Set<String> set = map.keySet();
        Set<String> setReference = mapReference.keySet();

        map.put("key1", "value1");
        mapReference.put("key1", "value1");

        assertEquals(setReference.isEmpty(), set.isEmpty());

        mapReference.remove("key1");
        map.setLifeTimeMillis("key1", 0);

        assertEquals(setReference.isEmpty(), set.isEmpty());

        map.close();
    }


}
