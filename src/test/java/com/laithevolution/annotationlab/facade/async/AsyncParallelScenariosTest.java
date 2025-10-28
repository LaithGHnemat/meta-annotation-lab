package com.laithevolution.annotationlab.facade.async;

import com.laithevolution.annotationlab.dto.AsyncResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AsyncParallelScenariosTest {

    @Autowired
    private AsyncParallelScenarios asyncParallelScenarios;

    /**
     * Scenario 5: Multiple async calls in parallel
     *  Verifies that multiple async tasks execute in parallel threads
     *  and do not block the main thread sequentially.
     */
    @Test
    void testExecuteMultipleAsyncTasks() {
        int numberOfTasks = 5;
        long taskDelayMillis = 500;

        long startTime = System.currentTimeMillis();

        List<AsyncResult> results = asyncParallelScenarios.executeMultipleAsyncTasks(numberOfTasks, taskDelayMillis);

        long totalDuration = System.currentTimeMillis() - startTime;

        System.out.println("Total execution time: " + totalDuration + "ms");

        assertTrue(results.stream()
                .allMatch(AsyncResult::isCompleted), "All tasks should complete successfully");


        Set<Long> threadIds = results.stream()
                .map(AsyncResult::getThreadId)
                .collect(Collectors.toSet());

        assertEquals(numberOfTasks, threadIds.size(), "Each async task should run in a separate thread");

        assertTrue(totalDuration < numberOfTasks * taskDelayMillis,
                "Tasks should run in parallel, total duration should be less than sum of delays");
        results.forEach(r ->
                System.out.println("Task executed in thread: " + r.getThreadName()
                        + " (ID: " + r.getThreadId() + ")"));
    }
}
