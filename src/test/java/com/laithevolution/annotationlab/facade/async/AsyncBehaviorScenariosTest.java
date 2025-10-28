package com.laithevolution.annotationlab.facade.async;

import com.laithevolution.annotationlab.dto.AsyncExecutionResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AsyncBehaviorScenariosTest {

    @Autowired
    private AsyncBehaviorScenarios asyncScenarios;

    /**
     * Scenario 1: Synchronous trigger of async method
     *
     * Verifies that the async method runs in a separate thread and
     * completes successfully when the main thread waits for it.
     */
    @Test
    void testAsyncRunsInSeparateThreadSyncTrigger() {
        AsyncExecutionResult result = asyncScenarios.triggerAsyncMethodSync();

        System.out.println("Main Thread ID: " + result.getMainThreadId());
        System.out.println("Async Thread ID: " + result.getAsyncThreadId());
        System.out.println("Async Completed: " + result.isAsyncCompleted());

        assertNotEquals(result.getMainThreadId(), result.getAsyncThreadId(),
                "Async method should run in a different thread than main thread");

        assertTrue(result.isAsyncCompleted(), "Async method should complete successfully");
        assertTrue(result.getAsyncThreadStartTimeMillis() >= 0, "Async start time should be recorded");
        assertTrue(result.getAsyncThreadEndTimeMillis() >= result.getAsyncThreadStartTimeMillis(),
                "Async end time should be after start time");
    }

    /**
     * Scenario 2: Non-blocking trigger of async method
     *
     * Verifies that the main thread is not blocked and the async
     * method completes independently in a separate thread.
     */
    @Test
    void testAsyncRunsInSeparateThreadNonBlockingTrigger() {
        AsyncExecutionResult result = asyncScenarios.triggerAsyncMethodNonBlocking();

        System.out.println("Main Thread ID: " + result.getMainThreadId());
        System.out.println("Async Thread ID (initial): " + result.getAsyncThreadId());
        System.out.println("Async Completed (initial): " + result.isAsyncCompleted());

        // Measure main thread execution duration
        long mainThreadDuration = result.getMainThreadEndTimeMillis() - result.getMainThreadStartTimeMillis();
        System.out.println("Main thread execution duration: " + mainThreadDuration + "ms");

        // Assert main thread did not block for async task
        assertTrue(mainThreadDuration < 100, "Main thread should not wait for async task completion");

        // Wait until the async task completes (max 1 second)
        Awaitility.await()
                .atMost(1, TimeUnit.SECONDS)
                .until(result::isAsyncCompleted);

        System.out.println("Async Thread ID (after completion): " + result.getAsyncThreadId());
        System.out.println("Async Completed: " + result.isAsyncCompleted());

        // Assertions for async task
        assertNotEquals(result.getMainThreadId(), result.getAsyncThreadId(),
                "Async method should run in a different thread than main thread");
        assertTrue(result.isAsyncCompleted(), "Async method should complete successfully");
        assertTrue(result.getAsyncThreadStartTimeMillis() >= 0, "Async start time should be recorded");
        assertTrue(result.getAsyncThreadEndTimeMillis() >= result.getAsyncThreadStartTimeMillis(),
                "Async end time should be after start time");
    }

    /**
     * Scenario 3a: Async method with void return type
     *
     * Verifies that async void methods execute independently without blocking the main thread.
     */
    @Test
    void testAsyncVoidMethod() {
        long start = System.currentTimeMillis();
        asyncScenarios.asyncVoidMethod();
        long duration = System.currentTimeMillis() - start;
        System.out.println("Main thread duration after calling asyncVoidMethod: " + duration + "ms");
        assertTrue(duration < 100,
                "Main thread should not wait for async void method");
    }

    /**
     * Scenario 3b: Async method with Future<T> return type
     *
     * Verifies that async methods returning Future<T> can be awaited and return expected result.
     */
    @Test
    void testAsyncFutureMethod() throws Exception {
        Future<String> future = asyncScenarios.asyncFutureMethod();
        String result = future.get(); // Waits for async completion
        System.out.println("asyncFutureMethod result: " + result);
        assertEquals("Completed", result,
                "Async Future<T> method should return expected result");
    }
}
