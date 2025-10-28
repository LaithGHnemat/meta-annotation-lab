package com.laithevolution.annotationlab.facade.async;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AsyncThreadPoolBehaviorTest {

    @Autowired
    private AsyncPoolSimulationScenarios asyncSim;

    /**
     * output Parallel execution with async tasks using small executor
     */
    @Test
    void testParallelTaskExecution() {
        List<String> results = asyncSim.executeMultipleAsyncTasks(3, 500);
        assertEquals(3, results.size(), "Should execute all 3 tasks");
        results.forEach(System.out::println);
    }

    /**
     * Test 2: Queue rejection with small executor
     */
    @Test
    void testQueueRejection() throws InterruptedException {
        int totalTasks = 5;
        int rejected = 0;

        for (int i = 0; i < totalTasks; i++) {
            final int taskId = i;
            try {
                asyncSim.performAsyncTask(taskId, 2000);
            } catch (RejectedExecutionException e) {
                rejected++;
            }
        }

        TimeUnit.SECONDS.sleep(5);

        System.out.println("Rejected tasks: " + rejected);
        assertTrue(rejected > 0, "Some tasks should have been rejected due to full queue");
    }

    /**
     * Test 3: Submit a failing task
     */
    @Test
    void testTaskFailure() {
        CompletableFuture<String> future = asyncSim.submitTaskWithFailure(1, 500, true);
        CompletionException exception = assertThrows(CompletionException.class, future::join);
        assertTrue(exception.getCause().getMessage().contains("Task-1 failed intentionally"));
    }

    /**
     * desc Test 4: Submit big tasks in parallel using big executor
     */

    @Test
    void testBigTasksExecution() throws InterruptedException {
        for (int i = 0; i < 5; i++)
            asyncSim.performBigAsyncTask(i, 1000);
        TimeUnit.SECONDS.sleep(3);
    }
}
