package cc.corentin.util;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class TestConcurrentHashMapAutoCleaningEntry {


    @Test
    public void testEquals() {
        ConcurrentHashMapAutoCleaning<String, String> map = new ConcurrentHashMapAutoCleaning<>(Long.MAX_VALUE);
        ConcurrentHashMap<String, String> mapReference = new ConcurrentHashMap<>();
        ConcurrentHashMapAutoCleaning<String, String> map2 = new ConcurrentHashMapAutoCleaning<>(Long.MAX_VALUE);
        ConcurrentHashMap<String, String> mapReference2 = new ConcurrentHashMap<>();

        map.put("key1", "value1");
        mapReference.put("key1", "value1");

        Map.Entry<String, String> entry = map.entrySet().iterator().next();
        Map.Entry<String, String> entryReference = mapReference.entrySet().iterator().next();

        assertEquals(entry, entryReference);

        map2.put("key1", "value1");
        mapReference2.put("key2", "value2");

        Map.Entry<String, String> entry2 = map2.entrySet().iterator().next();
        Map.Entry<String, String> entryReference2 = mapReference2.entrySet().iterator().next();

        assertNotEquals(entry2, entryReference2);

        map.close();
    }

    @Test
    public void testHashCode() {
        ConcurrentHashMapAutoCleaning<String, String> map = new ConcurrentHashMapAutoCleaning<>(Long.MAX_VALUE);
        ConcurrentHashMap<String, String> mapReference = new ConcurrentHashMap<>();
        ConcurrentHashMapAutoCleaning<String, String> map2 = new ConcurrentHashMapAutoCleaning<>(Long.MAX_VALUE);
        ConcurrentHashMap<String, String> mapReference2 = new ConcurrentHashMap<>();

        map.put("key1", "value1");
        mapReference.put("key1", "value1");

        Map.Entry<String, String> entry = map.entrySet().iterator().next();
        Map.Entry<String, String> entryReference = mapReference.entrySet().iterator().next();

        assertEquals(entry.hashCode(), entryReference.hashCode());

        map2.put("key1", "value1");
        mapReference2.put("key1 hello world", "hello world");

        Map.Entry<String, String> entry2 = map2.entrySet().iterator().next();
        Map.Entry<String, String> entryReference2 = mapReference2.entrySet().iterator().next();

        assertNotEquals(entry2.hashCode(), entryReference2.hashCode());

        map.close();
    }

}
