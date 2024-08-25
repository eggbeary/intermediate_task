/*
 * @test
 * @summary Test LRU cache implementation and old region object statistics using WhiteBox API
 * @requires vm.gc=="G1" | vm.gc=="Parallel" | vm.gc=="Serial"
 * @library /test/lib
 * @modules java.base/jdk.internal.misc
 *          java.management
 * @run main/othervm -XX:+UnlockDiagnosticVMOptions -XX:+WhiteBoxAPI -Xbootclasspath/a:. LRUCacheTest
 */

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import jdk.test.whitebox.WhiteBox;
import java.io.FileWriter;
import java.io.PrintWriter;

public class LRUCacheTest {
    private static final WhiteBox WB = WhiteBox.getWhiteBox();
    private static final int CACHE_SIZE = 1000;
    private static final int TEST_DURATION_MS = 5000;
    private static final int SLEEP_INTERVAL_MS = 100;
    private static final int TEST_RUNS = 5;
    private static final String OUTPUT_FILE = "/home/eggbear/tencent/intermediate_task/lru_cache_test_results.txt";

    static class LRUCache<K, V> extends LinkedHashMap<K, V> {
        private final int capacity;

        public LRUCache(int capacity) {
            super(capacity, 0.75f, true);
            this.capacity = capacity;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > capacity;
        }
    }

    public static void main(String[] args) throws Exception {
        PrintWriter out = new PrintWriter(new FileWriter(OUTPUT_FILE));
        long totalOldGenObjects = 0;
        long totalOldGenMemory = 0;

        for (int run = 1; run <= TEST_RUNS; run++) {
            out.println("Run " + run + ":");
            LRUCache<Integer, String> cache = new LRUCache<>(CACHE_SIZE);
            Random random = new Random();

            // Randomly add content to LRU cache for a specified duration
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < TEST_DURATION_MS) {
                int key = random.nextInt(CACHE_SIZE * 2);
                cache.put(key, "Value" + key);
                Thread.sleep(SLEEP_INTERVAL_MS);
            }

            // Force a full GC to move objects to old generation
            WB.fullGC();

            // Get memory usage statistics
            long totalMemory = Runtime.getRuntime().totalMemory();
            long freeMemory = Runtime.getRuntime().freeMemory();
            long usedMemory = totalMemory - freeMemory;

            out.println("Total memory: " + totalMemory);
            out.println("Free memory: " + freeMemory);
            out.println("Used memory: " + usedMemory);

            // Count objects and memory in old generation
            long oldGenObjectCount = 0;
            long oldGenMemoryUsage = 0;
            for (Map.Entry<Integer, String> entry : cache.entrySet()) {
                if (WB.isObjectInOldGen(entry.getValue())) {
                    oldGenObjectCount++;
                    oldGenMemoryUsage += WB.getObjectSize(entry.getValue());
                }
            }

            out.println("Objects in old generation: " + oldGenObjectCount);
            out.println("Memory used by old generation objects: " + oldGenMemoryUsage);
            out.println();

            totalOldGenObjects += oldGenObjectCount;
            totalOldGenMemory += oldGenMemoryUsage;

            // Assertions
            assert oldGenObjectCount > 0 : "No objects found in the old generation";
            assert oldGenMemoryUsage > 0 : "No memory used by old generation objects";
            assert oldGenObjectCount <= CACHE_SIZE : "More old gen objects than cache size";
            assert oldGenMemoryUsage < usedMemory : "Old gen memory usage exceeds total used memory";
        }

        // Calculate and print averages
        double avgOldGenObjects = (double) totalOldGenObjects / TEST_RUNS;
        double avgOldGenMemory = (double) totalOldGenMemory / TEST_RUNS;

        out.println("Average Results:");
        out.println("Average objects in old generation: " + avgOldGenObjects);
        out.println("Average memory used by old generation objects: " + avgOldGenMemory);

        // Additional assertions for averages
        assert avgOldGenObjects > 0 : "Average old gen objects should be positive";
        assert avgOldGenMemory > 0 : "Average old gen memory usage should be positive";
        assert avgOldGenObjects <= CACHE_SIZE : "Average old gen objects should not exceed cache size";

        out.close();
        System.out.println("Test completed. Results written to " + OUTPUT_FILE);
    }
}

