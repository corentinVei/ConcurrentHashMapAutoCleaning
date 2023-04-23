package cc.corentin.util;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

public class TestConcurrentHashmapAutoCleaning {
    @Test
    public void testEquals() {
        // Create 2 maps, one with the class to be tested and one with a reference map
        // We don't test the removal, so we use MAX_VALUE
        ConcurrentHashMapAutoCleaning<String, String> mapToTest = new ConcurrentHashMapAutoCleaning<>(Long.MAX_VALUE);
        Map<String, String> mapRef = new HashMap<>();
        // Add values to both maps
        mapToTest.put("key1", "value1");
        mapRef.put("key1", "value1");
        mapToTest.put("key2", "value2");
        mapRef.put("key2", "value2");
        mapToTest.put("key3", "value3");
        mapRef.put("key3", "value3");
        mapToTest.put("key4", "value4");
        mapRef.put("key4", "value4");
        // Test that the 2 maps are equal
        assertEquals(mapRef, mapToTest);
        assertEquals(mapToTest, mapRef);
        // Add an extra value to the map to be tested
        mapToTest.put("key5", "value5");
        // Test that the 2 maps are no longer equal
        assertNotEquals(mapRef, mapToTest);
        assertNotEquals(mapToTest, mapRef);
        mapToTest.close();
    }

    @Test
    public void testEqualsWithExpiredEntry() {
        // We create a map with a lifetime of 24 hours
        ConcurrentHashMapAutoCleaning<String, String> mapToTest = new ConcurrentHashMapAutoCleaning<>(24*60*60*1000);
        HashMap<String, String> mapRef = new HashMap<>();

        mapToTest.put("key1", "value1");
        mapRef.put("key1", "value1");

        long entryCreationTime = mapToTest.getCreationTimeMillis("key1");
        long newCurrentTime = entryCreationTime + 24 * 60 * 60 * 1000;
        // We simulate the passage of time
        try (MockedStatic<TimeHelper> theMock = Mockito.mockStatic(TimeHelper.class)) {
            theMock.when(TimeHelper::currentTimeMillis).thenReturn(newCurrentTime);
            assertNotEquals(mapRef, mapToTest);
            assertNotEquals(mapToTest, mapRef);
            // We add an extra key-value pair to the map being tested
            mapToTest.put("key1", "value1");
            // We test that the reference map and the map being tested are now equal
            assertEquals(mapRef, mapToTest);
            assertEquals(mapToTest, mapRef);
        }
        mapToTest.close();
    }

    @Test
    public void testPut() {
        // We create 2 maps, one with the class to be tested and one with a reference map
        // We don't test deletion, so we use MAX_VALUE
        ConcurrentHashMapAutoCleaning<String, String> mapToTest = new ConcurrentHashMapAutoCleaning<>(Long.MAX_VALUE);
        Map<String, String> mapRef = new HashMap<>();
        // We add values to both maps
        // since the map is empty, we test that the returned value is null
        assertNull(mapToTest.put("key1", "value1"));
        assertNull(mapRef.put("key1", "value1"));
        assertEquals(mapRef, mapToTest);
        // We replace the value with a new one and verify that we get the old value
        assertEquals("value1", mapToTest.put("key1", "value2"));
        assertEquals("value1", mapRef.put("key1", "value2"));
        // We test that the 2 maps are equal
        assertEquals(mapRef, mapToTest);
    }

    @Test
    public void testPutWithExpiredEntry() {
        // create a map with a lifespan of 24 hours
        ConcurrentHashMapAutoCleaning<String, String> mapToTest = new ConcurrentHashMapAutoCleaning<>(24*60*60*1000);
        HashMap<String, String> mapRef = new HashMap<>();

        mapToTest.put("key1", "value1");
        mapRef.put("key1", "value1");

        long entryCreationTime = mapToTest.getCreationTimeMillis("key1");
        long newCurrentTime = entryCreationTime + 24 * 60 * 60 * 1000;
        // We simulate the passage of time
        try (MockedStatic<TimeHelper> theMock = Mockito.mockStatic(TimeHelper.class)) {
            theMock.when(TimeHelper::currentTimeMillis).thenReturn(newCurrentTime);
            // add a new key-value pair to both maps
            assertNotEquals(mapRef, mapToTest);
            assertNull(mapToTest.put("key1", "value1"));

            assertEquals("value1", mapRef.put("key1", "value1"));
            assertEquals(mapRef, mapToTest);
        }
        mapToTest.close();
    }

    @Test
    public void testGet() {
        // We create 2 maps, one with the class to be tested and one with a reference map
        ConcurrentHashMapAutoCleaning<String, String> mapToTest = new ConcurrentHashMapAutoCleaning<>(24 * 60 * 60 * 1000);
        Map<String, String> mapRef = new HashMap<>();

        mapToTest.put("key1", "value1");
        mapRef.put("key1", "value1");


        assertEquals("value1", mapToTest.get("key1"));
        assertEquals("value1", mapRef.get("key1"));

        assertNull(mapToTest.get("key2"));
        assertNull(mapRef.get("key2"));

        long entryCreationTime = mapToTest.getCreationTimeMillis("key1");
        long newCurrentTime = entryCreationTime + 24 * 60 * 60 * 1000;
        // We simulate the passage of time
        try (MockedStatic<TimeHelper> theMock = Mockito.mockStatic(TimeHelper.class)) {
            theMock.when(TimeHelper::currentTimeMillis).thenReturn(newCurrentTime - 1);
            assertEquals("value1", mapToTest.get("key1"));
            theMock.when(TimeHelper::currentTimeMillis).thenReturn(newCurrentTime);
            assertNull(mapToTest.get("key1"));
        }
        mapToTest.close();
    }

    @Test
    public void testContainsKey() {
        // We create 2 maps, one with the class to be tested and one with a reference map
        // We don't test deletion, so we use MAX_VALUE
        ConcurrentHashMapAutoCleaning<String, String> mapToTest = new ConcurrentHashMapAutoCleaning<>(Long.MAX_VALUE);
        Map<String, String> mapRef = new HashMap<>();

        mapToTest.put("key1", "value1");
        mapRef.put("key1", "value1");

        assertTrue(mapToTest.containsKey("key1"));
        assertTrue(mapRef.containsKey("key1"));

        assertFalse(mapToTest.containsKey("key2"));
        assertFalse(mapRef.containsKey("key2"));


        mapToTest.close();
    }
    @Test
    public void testContainsKeyWithExpiredEntry() {

        ConcurrentHashMapAutoCleaning<String, String> mapToTest = new ConcurrentHashMapAutoCleaning<>(24*60*60*1000);

        mapToTest.put("key1", "value1");

        long entryCreationTime = mapToTest.getCreationTimeMillis("key1");
        long newCurrentTime = entryCreationTime + 24 * 60 * 60 * 1000;

        try (MockedStatic<TimeHelper> theMock = Mockito.mockStatic(TimeHelper.class)) {
            theMock.when(TimeHelper::currentTimeMillis).thenReturn(newCurrentTime);
            assertFalse(mapToTest.containsKey("key1"));
        }
        mapToTest.close();
    }

    @Test
    public void testSize() {

        ConcurrentHashMapAutoCleaning<String, String> mapToTest = new ConcurrentHashMapAutoCleaning<>(Long.MAX_VALUE);
        Map<String, String> mapRef = new HashMap<>();

        mapToTest.put("key1", "value1");
        mapRef.put("key1", "value1");
        assertEquals(1, mapToTest.size());
        assertEquals(1, mapRef.size());
        mapToTest.put("key2", "value2");
        mapRef.put("key2", "value2");
        assertEquals(2, mapToTest.size());
        assertEquals(2, mapRef.size());
        mapToTest.put("key3", "value3");
        mapRef.put("key3", "value3");
        assertEquals(3, mapToTest.size());
        assertEquals(3, mapRef.size());
        mapToTest.close();
    }
    @Test
    public void testSizeWithExpiredEntry() {

        ConcurrentHashMapAutoCleaning<String, String> mapToTest = new ConcurrentHashMapAutoCleaning<>(24 * 60 * 60 * 1000);

        mapToTest.put("key1", "value1");

        long entryCreationTime = mapToTest.getCreationTimeMillis("key1");
        long newCurrentTime = entryCreationTime + 24 * 60 * 60 * 1000;

        try (MockedStatic<TimeHelper> theMock = Mockito.mockStatic(TimeHelper.class)) {
            theMock.when(TimeHelper::currentTimeMillis).thenReturn(newCurrentTime);
            assertEquals(0, mapToTest.size());
        }
        mapToTest.close();
    }

    @Test
    public void testIsEmpty() {

        ConcurrentHashMapAutoCleaning<String, String> mapToTest = new ConcurrentHashMapAutoCleaning<>(Long.MAX_VALUE);
        Map<String, String> mapRef = new HashMap<>();

        assertTrue(mapToTest.isEmpty());
        assertTrue(mapRef.isEmpty());
        mapToTest.put("key1", "value1");
        mapRef.put("key1", "value1");
        assertFalse(mapToTest.isEmpty());
        assertFalse(mapRef.isEmpty());
        mapToTest.close();
    }

    @Test
    public void testIsEmptyWithExpiredEntry() {

        ConcurrentHashMapAutoCleaning<String, String> mapToTest = new ConcurrentHashMapAutoCleaning<>(24 * 60 * 60 * 1000);

        mapToTest.put("key1", "value1");

        long entryCreationTime = mapToTest.getCreationTimeMillis("key1");
        long newCurrentTime = entryCreationTime + 24 * 60 * 60 * 1000;

        try (MockedStatic<TimeHelper> theMock = Mockito.mockStatic(TimeHelper.class)) {
            theMock.when(TimeHelper::currentTimeMillis).thenReturn(newCurrentTime);
            assertTrue(mapToTest.isEmpty());
        }
        mapToTest.close();
    }

    @Test
    public void testRemove() {

        ConcurrentHashMapAutoCleaning<String, String> mapToTest = new ConcurrentHashMapAutoCleaning<>(Long.MAX_VALUE);
        Map<String, String> mapRef = new HashMap<>();

        mapToTest.put("key1", "value1");
        mapRef.put("key1", "value1");

        assertEquals("value1", mapToTest.remove("key1"));
        assertEquals("value1", mapRef.remove("key1"));

        assertNull(mapToTest.remove("key2"));
        assertNull(mapRef.remove("key2"));
        mapToTest.close();
    }

    @Test
    public void testRemoveWithExpiredEntry() {

        ConcurrentHashMapAutoCleaning<String, String> mapToTest = new ConcurrentHashMapAutoCleaning<>(24 * 60 * 60 * 1000);

        mapToTest.put("key1", "value1");

        long entryCreationTime = mapToTest.getCreationTimeMillis("key1");
        long newCurrentTime = entryCreationTime + 24 * 60 * 60 * 1000;

        try (MockedStatic<TimeHelper> theMock = Mockito.mockStatic(TimeHelper.class)) {
            theMock.when(TimeHelper::currentTimeMillis).thenReturn(newCurrentTime);
            assertNull(mapToTest.remove("key1"));
        }
        mapToTest.close();
    }

    @Test
    public void testPutAll() {

        ConcurrentHashMapAutoCleaning<String, String> mapToTest = new ConcurrentHashMapAutoCleaning<>(Long.MAX_VALUE);
        Map<String, String> mapRef = new HashMap<>();

        mapToTest.put("key1", "value1");
        mapRef.put("key1", "value1");
        mapToTest.put("key2", "value2");
        mapRef.put("key2", "value2");
        mapToTest.put("key3", "value3");
        mapRef.put("key3", "value3");

        Map<String, String> mapToAdd = new HashMap<>();
        mapToAdd.put("key4", "value4");
        mapToAdd.put("key5", "value5");

        mapRef.putAll(mapToAdd);
        mapToTest.putAll(mapToAdd);

        assertEquals(mapRef, mapToTest);
        mapToTest.close();
    }

    @Test
    public void testPutAllWithExpiredEntry() {

        ConcurrentHashMapAutoCleaning<String, String> mapToTest = new ConcurrentHashMapAutoCleaning<>(24 * 60 * 60 * 1000);
        Map<String, String> mapRef = new HashMap<>();

        mapToTest.put("key1", "value1");
        mapRef.put("key1", "value1");

        long entryCreationTime = mapToTest.getCreationTimeMillis("key1");
        long newCurrentTime = entryCreationTime + 24 * 60 * 60 * 1000;

        try (MockedStatic<TimeHelper> theMock = Mockito.mockStatic(TimeHelper.class)) {
            theMock.when(TimeHelper::currentTimeMillis).thenReturn(newCurrentTime);


            Map<String, String> mapToAdd = new HashMap<>();
            mapToAdd.put("key2", "value2");

            mapRef.putAll(mapToAdd);
            mapToTest.putAll(mapToAdd);
            assertEquals(mapToAdd, mapToTest);

            assertNotEquals(mapRef, mapToTest);
            theMock.when(TimeHelper::currentTimeMillis).thenReturn(newCurrentTime + 24 * 60 * 60 * 1000-1);

            assertEquals(mapToAdd, mapToTest);
            theMock.when(TimeHelper::currentTimeMillis).thenReturn(newCurrentTime + 24 * 60 * 60 * 1000);

            assertTrue(mapToTest.isEmpty());
        }
        mapToTest.close();
    }

    @Test
    public void testClear() {

        ConcurrentHashMapAutoCleaning<String, String> mapToTest = new ConcurrentHashMapAutoCleaning<>(Long.MAX_VALUE);
        Map<String, String> mapRef = new HashMap<>();

        mapToTest.put("key1", "value1");
        mapRef.put("key1", "value1");
        mapToTest.put("key2", "value2");
        mapRef.put("key2", "value2");
        mapToTest.put("key3", "value3");
        mapRef.put("key3", "value3");

        assertEquals(mapRef, mapToTest);

        mapToTest.clear();
        mapRef.clear();

        assertEquals(mapRef, mapToTest);
        mapToTest.close();
    }

    @Test
    public void testKeySet() {

        ConcurrentHashMapAutoCleaning<String, String> mapToTest = new ConcurrentHashMapAutoCleaning<>(Long.MAX_VALUE);
        Map<String, String> mapRef = new HashMap<>();

        for (int i = 0; i < 1000; i++) {
            mapToTest.put("key" + i, "value" + i);
            mapRef.put("key" + i, "value" + i);
        }

        Set<String> keySetRef = mapRef.keySet();
        Set<String> keySetToTest = mapToTest.keySet();
        assertTrue(keySetRef.containsAll(keySetToTest));
        assertTrue(keySetToTest.containsAll(keySetRef));
        assertEquals(keySetRef, keySetToTest);
        mapToTest.close();



        mapToTest = new ConcurrentHashMapAutoCleaning<>(24 * 60 * 60 * 1000);
        mapToTest.put("key1", "value1");

        long entryCreationTime = mapToTest.getCreationTimeMillis("key1");
        long newCurrentTime = entryCreationTime + 24 * 60 * 60 * 1000;

        try (MockedStatic<TimeHelper> theMock = Mockito.mockStatic(TimeHelper.class)) {
            theMock.when(TimeHelper::currentTimeMillis).thenReturn(newCurrentTime - 1);
            assertTrue(mapToTest.keySet().contains("key1"));
            theMock.when(TimeHelper::currentTimeMillis).thenReturn(newCurrentTime);
            assertFalse(mapToTest.keySet().contains("key1"));
        }
        mapToTest.close();



    }

    @Test
    public void testValues() {

        ConcurrentHashMapAutoCleaning<String, String> mapToTest = new ConcurrentHashMapAutoCleaning<>(Long.MAX_VALUE);
        Map<String, String> mapRef = new HashMap<>();

        for (int i = 0; i < 1000; i++) {
            mapToTest.put("key" + i, "value" + i);
            mapRef.put("key" + i, "value" + i);
        }

        assertTrue(mapRef.values().containsAll(mapToTest.values()));
        assertTrue(mapToTest.values().containsAll(mapRef.values()));
        mapToTest.close();
    }

    @Test
    public void testEntrySet() {

        ConcurrentHashMapAutoCleaning<String, String> mapToTest = new ConcurrentHashMapAutoCleaning<>(Long.MAX_VALUE);
        Map<String, String> mapRef = new HashMap<>();

        for (int i = 0; i < 10; i++) {
            mapToTest.put("key" + i, "value" + i);
            mapRef.put("key" + i, "value" + i);
        }


        Map.Entry<String, String> entry = mapRef.entrySet().iterator().next();
        Map.Entry<String, String> entry1;
        Iterator<Map.Entry<String, String>> it = mapToTest.entrySet().iterator();
        do {
            entry1 = it.next();
        } while (!entry1.equals(entry) && it.hasNext());
        assertEquals(entry, entry1);
        assertEquals(entry.hashCode(), entry1.hashCode());

        assertTrue(mapRef.entrySet().containsAll(mapToTest.entrySet()));
        assertTrue(mapToTest.entrySet().containsAll(mapRef.entrySet()));


        assertThrows(UnsupportedOperationException.class, () -> mapToTest.entrySet().iterator().next().setValue("value"));

        mapToTest.close();
    }

    @Test
    public void testPutIfAbsent() {

        ConcurrentHashMapAutoCleaning<String, String> mapToTest = new ConcurrentHashMapAutoCleaning<>(Long.MAX_VALUE);
        Map<String, String> mapRef = new HashMap<>();

        for (int i = 0; i < 10; i++) {
            mapToTest.put("key" + i, "value" + i);
            mapRef.put("key" + i, "value" + i);
        }

        assertNull(mapRef.putIfAbsent("key11", "value11"));

        assertNull(mapToTest.putIfAbsent("key11", "value11"));

        assertEquals(mapRef, mapToTest);

        assertEquals("value11", mapRef.putIfAbsent("key11", "value12"));

        assertEquals("value11", mapToTest.putIfAbsent("key11", "value12"));

        assertEquals(mapRef, mapToTest);
        mapToTest.close();
    }

    @Test
    public void testReplace() {

        ConcurrentHashMapAutoCleaning<String, String> mapToTest = new ConcurrentHashMapAutoCleaning<>(Long.MAX_VALUE);
        Map<String, String> mapRef = new HashMap<>();

        for (int i = 0; i < 10; i++) {
            mapToTest.put("key" + i, "value" + i);
            mapRef.put("key" + i, "value" + i);
        }

        assertNull(mapRef.replace("key10", "value10"));

        assertNull(mapToTest.replace("key10", "value10"));

        assertEquals(mapRef, mapToTest);

        assertEquals("value9", mapRef.replace("key9", "value10"));

        assertEquals("value9", mapToTest.replace("key9", "value10"));

        assertEquals(mapRef, mapToTest);

        assertFalse(mapRef.replace("key1", "value9", "value11"));

        assertFalse(mapToTest.replace("key1", "value9", "value10"));

        assertEquals(mapRef, mapToTest);

        assertTrue(mapRef.replace("key1", "value1", "value11"));

        assertTrue(mapToTest.replace("key1", "value1", "value11"));

        assertEquals(mapRef, mapToTest);
        mapToTest.close();
    }
    @Test
    public void testReplaceWithExpiredEntry() {

        ConcurrentHashMapAutoCleaning<String, String> mapToTest = new ConcurrentHashMapAutoCleaning<>(24*60*60*1000);
        HashMap<String, String> mapRef = new HashMap<>();

        mapToTest.put("key1", "value1");
        mapRef.put("key1", "value1");

        long entryCreationTime = mapToTest.getCreationTimeMillis("key1");
        long newCurrentTime = entryCreationTime + 24 * 60 * 60 * 1000;

        try (MockedStatic<TimeHelper> theMock = Mockito.mockStatic(TimeHelper.class)) {
            theMock.when(TimeHelper::currentTimeMillis).thenReturn(newCurrentTime);
            assertNull(mapToTest.replace("key1", "value2"));
            assertEquals("value1", mapRef.replace("key1", "value1"));
            assertEquals(0, mapToTest.size());
            assertFalse(mapToTest.replace("key1", "value1", "value2"));
            assertTrue(mapRef.replace("key1", "value1", "value2"));
            assertEquals(0, mapToTest.size());
        }
        mapToTest.close();
    }

    @Test
    public void testConcurrent() {
        int nbThreads = 1000;
        int nbIterations = 1000;

        ConcurrentHashMapAutoCleaning<String, String> mapToTest = new ConcurrentHashMapAutoCleaning<>(Long.MAX_VALUE);
        Map<String, String> mapRef = new ConcurrentHashMap<>();

        ArrayList<Thread> threads = new ArrayList<>(2000);
        class treadRunner implements Runnable {
            private final Map<String, String> mapT;
            private final int threadNumberT;

            treadRunner(Map<String, String> map, int threadNumber) {
                this.mapT = map;
                this.threadNumberT = threadNumber;
            }

            @Override
            public void run() {
                for (int i = 0; i < nbIterations; i++) {
                    mapT.put("threadNumberT : " + threadNumberT + "key" + i, "value" + i);
                }
            }
        }


        for (int i = 0; i < nbThreads; i++) {
            threads.add(new Thread(new treadRunner(mapRef, i)));
            threads.get(i * 2).start();
            threads.add(new Thread(new treadRunner(mapToTest, i)));
            threads.get(i * 2 + 1).start();

        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        assertEquals(mapRef, mapToTest);
        mapToTest.close();
    }

    @Test
    public void testLifetime() {

        ConcurrentHashMapAutoCleaning<String, String> mapToTest = new ConcurrentHashMapAutoCleaning<>(24 * 60 * 60 * 1000);

        mapToTest.put("key1", "value1");

        assertTrue(mapToTest.containsKey("key1"));

        long entryCreationTime = mapToTest.getCreationTimeMillis("key1");
        long newCurrentTime = entryCreationTime + 24 * 60 * 60 * 1000;

        try (MockedStatic<TimeHelper> theMock = Mockito.mockStatic(TimeHelper.class)) {
            theMock.when(TimeHelper::currentTimeMillis).thenReturn(newCurrentTime - 1);
            assertTrue(mapToTest.containsKey("key1"));
            theMock.when(TimeHelper::currentTimeMillis).thenReturn(newCurrentTime);
            assertFalse(mapToTest.containsKey("key1"));
        }
        mapToTest.close();
    }

    @Test
    public void testLifetimeAfterLastUse() {

        ConcurrentHashMapAutoCleaning<String, String> mapToTestWithoutCall = new ConcurrentHashMapAutoCleaning<>(24 * 60 * 60 * 1000, 60 * 60 * 1000);
        ConcurrentHashMapAutoCleaning<String, String> mapToTestWithCall = new ConcurrentHashMapAutoCleaning<>(24 * 60 * 60 * 1000, 60 * 60 * 1000);

        mapToTestWithCall.put("key1", "value1");
        mapToTestWithoutCall.put("key1", "value1");

        long entryCreationTimeWiveCall = mapToTestWithCall.getCreationTimeMillis("key1");
        long newCurrentTimeWiveCall = entryCreationTimeWiveCall + 24 * 60 * 60 * 1000;

        long entryCreationTimeWithoutCall = mapToTestWithoutCall.getCreationTimeMillis("key1");
        long newCurrentTimeWithoutCall = entryCreationTimeWithoutCall + 24 * 60 * 60 * 1000;


        try (MockedStatic<TimeHelper> theMock = Mockito.mockStatic(TimeHelper.class)) {

            long lastUseTimeWiveCall = newCurrentTimeWiveCall - 1;
            theMock.when(TimeHelper::currentTimeMillis).thenReturn(lastUseTimeWiveCall);
            assertTrue(mapToTestWithCall.containsKey("key1"));

            long lastUseTimeWithoutCall = newCurrentTimeWithoutCall - 1;
            theMock.when(TimeHelper::currentTimeMillis).thenReturn(lastUseTimeWithoutCall);
            assertTrue(mapToTestWithoutCall.containsKey("key1"));
            theMock.when(TimeHelper::currentTimeMillis).thenReturn(lastUseTimeWiveCall);
            assertEquals("value1", mapToTestWithCall.getAndUpdateTimeSinceLastUse("key1"));// Update the last usage on a map

            theMock.when(TimeHelper::currentTimeMillis).thenReturn(lastUseTimeWiveCall + 1);//We simulate time for the value to be deleted
            assertTrue(mapToTestWithCall.containsKey("key1"));// The value is not deleted because it has been used recently.

            theMock.when(TimeHelper::currentTimeMillis).thenReturn(lastUseTimeWithoutCall + 1);//We simulate time for the value to be deleted.
            assertFalse(mapToTestWithoutCall.containsKey("key1"));

            //We verify that the value is deleted after 1 hour
            theMock.when(TimeHelper::currentTimeMillis).thenReturn(lastUseTimeWiveCall + 60 * 60 * 1000 - 1);
            assertTrue(mapToTestWithCall.containsKey("key1"));
            theMock.when(TimeHelper::currentTimeMillis).thenReturn(lastUseTimeWiveCall + 60 * 60 * 1000);
            assertFalse(mapToTestWithCall.containsKey("key1"));
        }
        mapToTestWithoutCall.close();
        mapToTestWithCall.close();

    }

    @Test
    public void testMapCleaning() {

        ConcurrentHashMapAutoCleaning<String, String> map = new ConcurrentHashMapAutoCleaning<>(24 * 60 * 60 * 1000);

        map.put("key1", "value1");

        assertTrue(map.containsKey("key1"));

        long entryCreationTime = map.getCreationTimeMillis("key1");
        long newCurrentTime = entryCreationTime + 24 * 60 * 60 * 1000;

        try (MockedStatic<TimeHelper> theMock = Mockito.mockStatic(TimeHelper.class)) {
            theMock.when(TimeHelper::currentTimeMillis).thenReturn(newCurrentTime - 1);
            assertEquals(1, map.sizeWithExpired());
            map.cleanBlocking();
            assertEquals(1, map.sizeWithExpired());
            theMock.when(TimeHelper::currentTimeMillis).thenReturn(newCurrentTime);
            assertEquals(1, map.sizeWithExpired());
            map.cleanBlocking();
            assertEquals(0, map.sizeWithExpired());
        }
        map.close();
    }

    @Test
    public void testAutomaticCleaning() {
        // We use a lifespan of 0s and a cleaning period of 100ms
        ConcurrentHashMapAutoCleaning<String, String> map = new ConcurrentHashMapAutoCleaning<>(0, 0, 100);
        // We add a value to the map to be tested
        map.put("key1", "value1");
        // We wait for 200ms for the value to be deleted
        await().atMost(200, TimeUnit.MILLISECONDS).until(() -> map.sizeWithExpired() == 0);
        // We verify that the value has been deleted
        assertEquals(0, map.sizeWithExpired());
        map.close();
    }

    @Test
    public void testSetCleanPeriodTo0() {
        // We use a lifespan of 0s and a cleaning period of 100ms
        ConcurrentHashMapAutoCleaning<String, String> map = new ConcurrentHashMapAutoCleaning<>(0, 0, 100);

        map.setCleanPeriod(0);
        // We add a value to the map to be tested
        map.put("key1", "value1");

        // We wait 200ms to verify that the value is not deleted (if the cleaning period is still 100ms, it should be deleted)
        await().atMost(1, TimeUnit.SECONDS).until(() -> map.sizeWithExpired() == 1);

        // We verify that the value is not deleted
        assertEquals(1, map.sizeWithExpired());
        map.close();
    }

    @Test
    public void testChangeCleaningPeriod() {
        // We use a lifespan of 0s and a cleaning period of 100s
        ConcurrentHashMapAutoCleaning<String, String> map = new ConcurrentHashMapAutoCleaning<>(0, 0, 100_000);
        // We change the cleaning period
        map.setCleanPeriod(100);
        // We add a value to the map to be tested
        map.put("key1", "value1");
        // We wait for 200ms for the value to be deleted
        await().atMost(200, TimeUnit.MILLISECONDS).until(() -> map.sizeWithExpired() == 0);

        // We verify that the value is deleted
        assertEquals(0, map.sizeWithExpired());
        map.close();
    }

    @Test
    public void testThreadClean() {
        String uuid = UUID.randomUUID().toString();
        ConcurrentHashMapAutoCleaning<String, String> map = new ConcurrentHashMapAutoCleaning<>(0, 0, 100, uuid);
        // Assert that a thread With the name uuid is launched
        assertTrue(Thread.getAllStackTraces().keySet().stream().anyMatch(thread -> thread.getName().equals(uuid)));
        map.close();

        await().atMost(1, TimeUnit.SECONDS).until(() -> Thread.getAllStackTraces().keySet().stream().noneMatch(thread -> thread.getName().equals(uuid)));

        assertFalse(Thread.getAllStackTraces().keySet().stream().anyMatch(thread -> thread.getName().equals(uuid)));
    }

    @Test
    public void testContainsValue() {
        ConcurrentHashMapAutoCleaning<String, String> map = new ConcurrentHashMapAutoCleaning<>(24 * 60 * 60 * 1000);
        HashMap<String, String> hashMap = new HashMap<>();
        map.put("key1", "value1");
        hashMap.put("key1", "value1");
        assertTrue(map.containsValue("value1"));
        assertTrue(hashMap.containsValue("value1"));
        assertFalse(map.containsValue("value2"));
        assertFalse(hashMap.containsValue("value2"));
        map.close();
    }

    @Test
    public void testValueWithTime(){
        ValueWithTime<String> valueWithTime = new ValueWithTime<>("value1",0,0);
        ValueWithTime<String> valueWithTime2 = new ValueWithTime<>("value1",0,0);

        assertEquals(valueWithTime, valueWithTime2);
        assertEquals("value1", valueWithTime.getValue());
        assertEquals("value1".hashCode(), valueWithTime.hashCode());


    }
    @Test
    public void testRemoveKeyValue() {

        ConcurrentHashMapAutoCleaning<String, String> map = new ConcurrentHashMapAutoCleaning<>(24 * 60 * 60 * 1000);
        HashMap<String, String> hashMap = new HashMap<>();
        map.put("key1", "value1");
        hashMap.put("key1", "value1");
        assertTrue(hashMap.remove("key1", "value1"));
        assertTrue(map.remove("key1", "value1"));
        assertFalse(hashMap.remove("key1", "value1"));
        assertFalse(map.remove("key1", "value1"));
        map.close();
    }

    @Test
    public void testRemoveKayValueWithExpiredEntry() {

        ConcurrentHashMapAutoCleaning<String, String> map = new ConcurrentHashMapAutoCleaning<>(0);
        map.put("key1", "value1");
        assertFalse(map.remove("key1", "value1"));
        map.close();
    }


    @Test
    public void testIsEmptyWithInvalid() {
        ConcurrentHashMapAutoCleaning<String, String> map = new ConcurrentHashMapAutoCleaning<>(24 * 60 * 60 * 1000);
        HashMap<String, String> hashMap = new HashMap<>();
        assertTrue(map.isEmptyWithInvalid());
        assertTrue(hashMap.isEmpty());
        map.put("key1", "value1");
        hashMap.put("key1", "value1");
        assertFalse(map.isEmptyWithInvalid());
        assertFalse(hashMap.isEmpty());
        long entryCreationTime = map.getCreationTimeMillis("key1");
        long newCurrentTime = entryCreationTime + 24 * 60 * 60 * 1000;

        try (MockedStatic<TimeHelper> theMock = Mockito.mockStatic(TimeHelper.class)) {
            theMock.when(TimeHelper::currentTimeMillis).thenReturn(newCurrentTime);
            assertFalse(map.isEmptyWithInvalid());
            assertTrue(map.isEmpty());
        }
        map.close();

    }
    @Test
    public void testGetSinceLastUseMillis() {
        ConcurrentHashMapAutoCleaning<String, String> map = new ConcurrentHashMapAutoCleaning<>(24 * 60 * 60 * 1000);
        map.put("key1", "value1");
        long newCurrentTime = System.currentTimeMillis();
        try (MockedStatic<TimeHelper> theMock = Mockito.mockStatic(TimeHelper.class)) {
            theMock.when(TimeHelper::currentTimeMillis).thenReturn(newCurrentTime +100);
            map.getAndUpdateTimeSinceLastUse("key1");
            assertEquals(newCurrentTime +100, map.getLastTimeUsedMillis("key1"));
        }
        map.close();
    }
    @Test
    public void testForEach() {
        ConcurrentHashMapAutoCleaning<String, String> map = new ConcurrentHashMapAutoCleaning<>(Long.MAX_VALUE);
        HashMap<String, String> hashMap = new HashMap<>();
        for (int i = 0; i < 100; i++) {
            map.put("key" + i, "value" + i);
            hashMap.put("key" + i, "value" + i);
        }
        map.forEach((key, value) -> map.put(key, "value2"));
        hashMap.forEach((key, value) -> hashMap.put(key, "value2"));
        map.close();

        long newCurrentTime = System.currentTimeMillis();

        try (MockedStatic<TimeHelper> theMock = Mockito.mockStatic(TimeHelper.class)) {
            theMock.when(TimeHelper::currentTimeMillis).thenReturn(newCurrentTime);
            ConcurrentHashMapAutoCleaning<String, String> map2 = new ConcurrentHashMapAutoCleaning<>(24*60*60*1000);
            HashMap<String, String> hashMap2 = new HashMap<>();
            for (int i = 0; i < 100; i++) {
                map2.put("key" + i, "value" + i);
                hashMap2.put("key" + i, "value" + i);
            }
            assertEquals(map2, hashMap2);

            newCurrentTime =newCurrentTime+ 24 * 60 * 60 * 1000 + 1;
            theMock.when(TimeHelper::currentTimeMillis).thenReturn(newCurrentTime);
            AtomicInteger i = new AtomicInteger();
            map2.forEach((key, value) -> i.getAndIncrement());
            AtomicInteger j = new AtomicInteger();
            hashMap2.forEach((key, value) -> j.getAndIncrement());
            assertEquals(0, i.get());
            assertEquals(100, j.get());
            assertNotEquals(map2, hashMap2);

        }
    }
    @Test
    public void testOnEntryRemovedListener() {
        ConcurrentHashMapAutoCleaning<String, String> map = new ConcurrentHashMapAutoCleaning<>(0);
        map.setCleanPeriod(1);
        AtomicInteger i = new AtomicInteger();
        map.addListener((key, value) -> i.getAndIncrement());
        for (int j = 0; j < 100; j++) {
            map.put("key" + j, "value" + j);
        }
        await().atMost(1, TimeUnit.SECONDS).until(() -> i.get() == 100);
        map.close();
        assertEquals(100, i.get());
    }

    @Test
    public void testSetLifeTimeMillis(){
        ConcurrentHashMapAutoCleaning<String, String> map = new ConcurrentHashMapAutoCleaning<>(Long.MAX_VALUE);
        map.put("key1", "value1");
        assertEquals(Long.MAX_VALUE,map.setLifeTimeMillis("key1", 1000));
        long newCurrentTime = map.getCreationTimeMillis("key1");
        try (MockedStatic<TimeHelper> theMock = Mockito.mockStatic(TimeHelper.class)) {
            theMock.when(TimeHelper::currentTimeMillis).thenReturn(newCurrentTime + 1000 -1);
            assertTrue(map.containsKey("key1"));
            theMock.when(TimeHelper::currentTimeMillis).thenReturn(newCurrentTime + 1000);
            assertFalse(map.containsKey("key1"));
        }
        map.close();
    }
    @Test
    public void testSetLifeTimeMillisWithInvalid(){
        ConcurrentHashMapAutoCleaning<String, String> map = new ConcurrentHashMapAutoCleaning<>(1000);
        map.put("key1", "value1");
        long newCurrentTime = map.getCreationTimeMillis("key1");
        try (MockedStatic<TimeHelper> theMock = Mockito.mockStatic(TimeHelper.class)) {
            theMock.when(TimeHelper::currentTimeMillis).thenReturn(newCurrentTime + 1000);
            assertEquals(-1,map.setLifeTimeMillis("key1", Long.MAX_VALUE));
            assertFalse(map.containsKey("key1"));
        }
        map.close();
    }

    @Test
    public void testSetLifeTimeSinceLastUseMillis(){
        ConcurrentHashMapAutoCleaning<String, String> map = new ConcurrentHashMapAutoCleaning<>(1000);
        map.put("key1", "value1");
        assertEquals(0,map.setExtraLifeTimeAfterUseMillis("key1", 1000));
        ConcurrentHashMapAutoCleaning<String, String> map2 = new ConcurrentHashMapAutoCleaning<>(Long.MAX_VALUE, 1000);
        map2.put("key1", "value1");
        assertEquals(1000,map2.setExtraLifeTimeAfterUseMillis("key1", 0));
        long newCurrentTime = map.getCreationTimeMillis("key1");
        try (MockedStatic<TimeHelper> theMock = Mockito.mockStatic(TimeHelper.class)) {
            theMock.when(TimeHelper::currentTimeMillis).thenReturn(newCurrentTime + 1000 -1);
            assertTrue(map.containsKey("key1"));
            theMock.when(TimeHelper::currentTimeMillis).thenReturn(newCurrentTime + 1000);
            assertFalse(map.containsKey("key1"));
            theMock.when(TimeHelper::currentTimeMillis).thenReturn(newCurrentTime + 1000-1);
            map.getAndUpdateTimeSinceLastUse("key1");
            theMock.when(TimeHelper::currentTimeMillis).thenReturn(newCurrentTime + 1000);
            assertTrue(map.containsKey("key1"));
            theMock.when(TimeHelper::currentTimeMillis).thenReturn(newCurrentTime + 1000+1000-2);// We used the key for 1000-1, which is valid for 1000. Therefore, it is valid for 1000+1000-2, but not for 1000+1000-1
            assertTrue(map.containsKey("key1"));
            theMock.when(TimeHelper::currentTimeMillis).thenReturn(newCurrentTime + 1000+1000-1);
            assertFalse(map.containsKey("key1"));
        }
        map.close();
        map2.close();
    }

    @Test
    public void testSetCreationTimeMillis(){
        ConcurrentHashMapAutoCleaning<String, String> map = new ConcurrentHashMapAutoCleaning<>(1000);
        map.put("key1", "value1");
        long newCurrentTime = map.getCreationTimeMillis("key1");
        try (MockedStatic<TimeHelper> theMock = Mockito.mockStatic(TimeHelper.class)) {
            theMock.when(TimeHelper::currentTimeMillis).thenReturn(newCurrentTime + 1000 -1);
            map.setCreationTimeMillis("key1", newCurrentTime+1000);
            theMock.when(TimeHelper::currentTimeMillis).thenReturn(newCurrentTime + 1000); // should be invalid
            assertTrue(map.containsKey("key1"));//still valid because we haven't changed the creation date
            assertEquals(newCurrentTime+1000, map.getCreationTimeMillis("key1"));
            theMock.when(TimeHelper::currentTimeMillis).thenReturn(newCurrentTime + 1000+1000-1);
            assertTrue(map.containsKey("key1"));
            theMock.when(TimeHelper::currentTimeMillis).thenReturn(newCurrentTime + 1000+1000);
            assertFalse(map.containsKey("key1"));
        }
        map.close();
    }
}