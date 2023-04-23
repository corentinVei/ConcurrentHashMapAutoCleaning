package cc.corentin.util;

import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestConcurrentHashmapAutoCleaningCollectionIterator {

    @Test
    public void testHasNext() {
        ConcurrentHashMapAutoCleaning<String, String> map = new ConcurrentHashMapAutoCleaning<>(Long.MAX_VALUE);
        ConcurrentHashMap<String, String> mapReference = new ConcurrentHashMap<>();

        map.put("key1", "value1");
        mapReference.put("key1", "value1");

        Iterator<String> iterator = map.values().iterator();
        Iterator<String> iteratorReference = mapReference.values().iterator();

        assertEquals(iterator.hasNext(), iteratorReference.hasNext());

        map.put("key2", "value2");
        mapReference.put("key2", "value2");
        assertEquals(iterator.hasNext(), iteratorReference.hasNext());

        map.remove("key1");
        mapReference.remove("key1");

        assertEquals(iterator.hasNext(), iteratorReference.hasNext());

        map.remove("key2");
        mapReference.remove("key2");

        assertEquals(iterator.hasNext(), iteratorReference.hasNext());

        assertEquals(iterator.next(), iteratorReference.next());

        assertEquals(iterator.hasNext(), iteratorReference.hasNext());

        assertThrows(NoSuchElementException.class, iterator::next);
        assertThrows(NoSuchElementException.class, iteratorReference::next);

        map.put("key3", "value3");
        mapReference.put("key3", "value3");
        assertEquals(iterator.hasNext(), iteratorReference.hasNext());

        map.close();
    }

    @Test
    public void testNext(){
        ConcurrentHashMapAutoCleaning<String, String> map = new ConcurrentHashMapAutoCleaning<>(Long.MAX_VALUE);
        ConcurrentHashMap<String, String> mapReference = new ConcurrentHashMap<>();

        for (int i = 0; i < 1_000_000; i++) {
            map.put("key" + i, "value" + i);
            mapReference.put("key" + i, "value" + i);
        }

        Iterator<String> iterator = map.values().iterator();
        Iterator<String> iteratorReference = mapReference.values().iterator();

        while (iteratorReference.hasNext()){
            if (Math.random()>0.2){
                assertEquals(iterator.hasNext(),iteratorReference.hasNext()); // We are making a random call to test that the 'next' function does not modify the next value
            }
            assertEquals(iterator.next(), iteratorReference.next());
        }
        map.close();
    }

    @Test
    public void testRemove(){
        ConcurrentHashMapAutoCleaning<String, String> map = new ConcurrentHashMapAutoCleaning<>(Long.MAX_VALUE);
        ConcurrentHashMap<String, String> mapReference = new ConcurrentHashMap<>();

        for (int i = 0; i < 100_000; i++) {
            map.put("key" + i, "value" + i);
            mapReference.put("key" + i, "value" + i);
        }

        Iterator<String> iterator = map.values().iterator();
        Iterator<String> iteratorReference = mapReference.values().iterator();

        while (iterator.hasNext()){
            if (Math.random()>0.2){
                iterator.next();
                iteratorReference.next();
                iterator.remove();
                iteratorReference.remove();
            }
        }
        assertEquals(map, mapReference);

        map.close();
    }

    @Test
    public void testInvalid(){
        ConcurrentHashMapAutoCleaning<String, String> map = new ConcurrentHashMapAutoCleaning<>(Long.MAX_VALUE);
        ConcurrentHashMap<String, String> mapReference = new ConcurrentHashMap<>();

        map.put("key1", "value1");
        map.put("key2", "value2");
        mapReference.put("key1", "value1");
        mapReference.put("key2", "value2");

        Iterator<String> iterator = map.values().iterator();
        Iterator<String> iteratorReference = mapReference.values().iterator();

        map.setLifeTimeMillis("key1", 0);
        mapReference.remove("key1");

        assertEquals(iteratorReference.hasNext(), iterator.hasNext());
        iteratorReference.next();// When we pass the first element of the Reference care map into the concurrent HashMap, it is not valid, so it is not returned
        assertEquals(iteratorReference.next(),iterator.next());


        assertEquals(iteratorReference.hasNext(), iterator.hasNext());

        map.close();
    }

    @Test
    public void testInvalidAfterHasNext(){
        ConcurrentHashMapAutoCleaning<String, String> map = new ConcurrentHashMapAutoCleaning<>(Long.MAX_VALUE);
        ConcurrentHashMap<String, String> mapReference = new ConcurrentHashMap<>();

        map.put("key1", "value1");
        mapReference.put("key1", "value1");

        Iterator<String> iterator = map.values().iterator();
        Iterator<String> iteratorReference = mapReference.values().iterator();

        assertEquals(iteratorReference.hasNext(), iterator.hasNext());

        map.setLifeTimeMillis("key1", 0);

        assertNull(iterator.next());

        map.close();
    }
}
