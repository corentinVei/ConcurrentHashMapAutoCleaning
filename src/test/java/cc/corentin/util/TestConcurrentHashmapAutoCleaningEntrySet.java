package cc.corentin.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestConcurrentHashmapAutoCleaningEntrySet {


    @Test
    public void testToArray(){
        ConcurrentHashMapAutoCleaning<String, String> map = new ConcurrentHashMapAutoCleaning<>(Long.MAX_VALUE);
        ConcurrentHashMap<String, String> mapReference = new ConcurrentHashMap<>();

        Set<Map.Entry<String,String>> set = map.entrySet();
        Set<Map.Entry<String,String>> setReference = mapReference.entrySet();

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
    public void testClear(){
        ConcurrentHashMapAutoCleaning<String, String> map = new ConcurrentHashMapAutoCleaning<>(Long.MAX_VALUE);
        ConcurrentHashMap<String, String> mapReference = new ConcurrentHashMap<>();

        Set<Map.Entry<String,String>> set = map.entrySet();
        Set<Map.Entry<String,String>> setReference = mapReference.entrySet();

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

        Set<Map.Entry<String,String>> set = map.entrySet();
        Set<Map.Entry<String,String>> setReference = mapReference.entrySet();

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

        Set<Map.Entry<String,String>> set = map.entrySet();
        Set<Map.Entry<String,String>> setReference = mapReference.entrySet();

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

        Set<Map.Entry<String,String>> set = map.entrySet();
        Set<Map.Entry<String,String>> setReference = mapReference.entrySet();

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

        Set<Map.Entry<String,String>> set = map.entrySet();
        Set<Map.Entry<String,String>> setReference = mapReference.entrySet();

        map.put("key1", "value1");
        mapReference.put("key1", "value1");

        assertEquals(setReference.isEmpty(), set.isEmpty());

        mapReference.remove("key1");
        map.setLifeTimeMillis("key1", 0);

        assertEquals(setReference.isEmpty(), set.isEmpty());

        map.close();
    }
}
