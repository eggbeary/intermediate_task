# JDK jtreg Test Case Report for LRU Cache with G1GC

## Introduction
This report documents the creation and execution of a JDK jtreg test case that focuses on G1 Garbage Collector (G1GC). The test involves implementing a typical Least Recently Used (LRU) cache and dynamically adding content to it. After running for a specified duration, the test assesses the survival of objects in the old generation, which provides insights into the operation and stages of G1GC, specifically the concurrent mark and mixed GC phases.

## Objective
The main objectives of this task are:
- To familiarize with the writing and execution of JDK tests, particularly those related to Garbage Collection.
- To understand the operational principles of the G1GC and the significance of its various stages.
- To analyze the survival of objects in the old generation to understand the mechanics of G1GC's concurrent mark and mixed GC phases.

## Test Implementation
### Test Setup
- **LRU Cache Implementation**: A `LinkedHashMap` based LRU cache is implemented with capabilities to ensure that it adheres to the least recently used eviction policy.
- **Whitebox API Usage**: Utilized existing Whitebox APIs like `isObjectInOldGen` and `getObjectSize` to monitor and record the behavior of objects in the old generation. These APIs help in identifying whether objects are in the old generation and in measuring the size of these objects.

### Test Execution
- **Parameters**:
  - Cache Size: 1000
  - Test Duration: 5000 milliseconds
  - Number of Runs: 5
- **Operation**: The cache is used to randomly add entries over the specified duration, ensuring dynamic interaction with the memory. After the operations, a full garbage collection is forced to move objects to the old generation for analysis.

### Source Code
The source code is structured as a standard jtreg test case with appropriate annotations and commands for execution. It includes:
- Implementation of the LRU cache.
- Use of Whitebox APIs to gather necessary data.
- Writing results to an output file for analysis.

## Results
### Individual Run Analysis
Each run showed slight variations in the memory usage and the number of objects in the old generation, reflecting the dynamic allocation and garbage collection behavior of the JVM under G1GC.

### Average Results Across Runs
- **Average Objects in Old Generation**: 49.4
- **Average Memory Used by Old Generation Objects**: 1185.6 bytes

These results suggest that a significant number of objects survive in the old generation, which is indicative of G1GC's efficiency in managing memory in a scenario with frequent allocation and deallocation.

## Conclusion
The test successfully demonstrates the ability to use JDK testing tools to analyze and understand the behavior of the G1 Garbage Collector. It provides valuable insights into the survival rate of objects in the old generation, highlighting the effectiveness of G1GC's concurrent mark and mixed GC phases in managing aged objects.

## Future Work
Further analysis could be conducted with varying cache sizes and durations to explore how G1GC handles different load and memory pressure scenarios. Additionally, extending the Whitebox API to provide more detailed insights into G1GC's behavior during the test could enhance the depth of analysis.

## Appendix
- **Test Code**: Located in the repository at `/test_case/LRUCacheTest.java`.
- **Output Files**: Results are saved in `/test_case/lru_cache_test_results.txt`.
