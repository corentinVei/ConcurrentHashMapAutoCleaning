# AutoCleanMap Class Documentation

This is the documentation for the `AutoCleanMap` class. This class implements the `ConcurrentMap` interface and allows the creation of a map that automatically cleans up entries that have exceeded a predefined lifespan.

## Important Notes

*   **This class must be closed to disable automatic cleaning and the thread.**
*   The methods `size()`,`equals()` and `isEmpty()` are much slower than the base map the reason for this slow performance is that it's impossible to know the number of invalid values in the map, and there is no solution that doesn't increase the complexity of the `put` function

## Overview

This class is designed to store a value along with its creation time and time of last use. It is important to understand the distinction between the `creationTimeMillis` and `lifeTimeMillis` variables. The former represents the date and time at which the value was created, while the latter represents the duration for which the value should remain valid. The same logic applies to the `lastTimeUsedMillis` and `extraLifeTimeAfterUseMillis` variables.

## Usage

To clean up the map of entries that have exceeded the predefined lifespan, the `setCleanPeriodMillis` method must be called with a value greater than 0 in the constructor or using the `setCleanPeriod(long cleanPeriodMillis)` method. A thread will be launched to clean up the map every `cleanPeriodMillis`milliseconds. If `cleanPeriodMillis` is set to 0, the thread will be stopped. The `cleanBlocking()` method can be used to clean up the map manually without using a thread. If you want to see usage examples and test cases, please check the [test](src/test/java/cc/corentin/util/ConcurrentHashmapAutoCleaningTest.java) folder in this project.

## Key Concepts

*   This class is designed to store a value along with its creation time and time of last use.
*   `creationTimeMillis`: the date and time at which the value was created.
*   `lifeTimeMillis`: the duration for which the value should remain valid.
*   `lastTimeUsedMillis`: the date and time at which the value was last used.
*   `extraLifeTimeAfterUseMillis`: the additional time that the value should remain valid after its last use.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.