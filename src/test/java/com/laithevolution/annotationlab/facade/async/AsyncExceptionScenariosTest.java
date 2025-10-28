package com.laithevolution.annotationlab.facade.async;

import com.laithevolution.annotationlab.dto.AsyncExecutionResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AsyncExceptionScenariosTest {

    @Autowired
    private AsyncExceptionScenarios asyncExceptionScenarios;

    /**
     * Scenario 4a: RuntimeException in async method
     *  Ensures that a RuntimeException thrown in an async method
     *  does not block the main thread and can be captured asynchronously.
     */

    @Test
    void testAsyncRuntimeExceptionMethod() {
        CompletableFuture<Void> future = asyncExceptionScenarios.asyncRuntimeExceptionMethod();
        Awaitility.await()
                .atMost(1, TimeUnit.SECONDS)
                .until(future::isCompletedExceptionally);
        assertTrue(future.isCompletedExceptionally(), "Async RuntimeException should complete exceptionally");
    }

    /**
     * Scenario 4b: CheckedException in async method
     *  Ensures that a checked exception (wrapped in RuntimeException)
     *  thrown in an async method does not block the main thread.
     */

    @Test
    void testAsyncCheckedExceptionMethod() {
        CompletableFuture<Void> future = asyncExceptionScenarios.asyncCheckedExceptionMethod();
        Awaitility.await()
                .atMost(1, TimeUnit.SECONDS)
                .until(future::isCompletedExceptionally);
        assertTrue(future.isCompletedExceptionally(),
                "Async checked exception should complete exceptionally");
    }

    /**
     * Scenario 4c: Async method returning result with exception info
     *  Verifies that an exception inside the async method is captured
     *  in the AsyncResult DTO and does not block the main thread.
     */

    @Test
    void testTriggerAsyncMethodWithExceptionSync() {
        long start = System.currentTimeMillis();

        AsyncExecutionResult result = asyncExceptionScenarios.triggerAsyncMethodWithExceptionSync();

        long duration = System.currentTimeMillis() - start;
        assertTrue(duration >= 300, "Main thread should wait for async task");
        assertNotEquals(result.getMainThreadId(), result.getAsyncThreadId(),
                "Async method should run in a different thread than main thread");
        assertNotNull(result.getException(), "Exception should be captured");
        assertTrue(result.getException() instanceof RuntimeException,
                "Captured exception should be RuntimeException");
        assertFalse(result.isAsyncCompleted(), "Completed should be false due to exception");
        assertTrue(result.getAsyncThreadId() > 0, "Async thread ID should be valid");
        assertNotNull(result.getAsyncThreadName(), "Async thread name should not be null");
        assertTrue(result.getMainThreadId() > 0, "Main thread ID should be valid");
        assertTrue(result.getMainThreadEndTimeMillis() >= result.getMainThreadStartTimeMillis(),
                "Main thread end time should be after start time");
    }

}
