package com.laithevolution.annotationlab.facade.async;

import com.laithevolution.annotationlab.dto.AsyncExecutionResult;
import com.laithevolution.annotationlab.dto.AsyncResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Service
public class AsyncBehaviorScenarios {

    @Lazy
    @Autowired
    private AsyncBehaviorScenarios self;

    /*remark
     * Scenario 1: Async Execution Behavior
     * Executes a method asynchronously, simulates a delay,
     * and returns thread and timing info after completion.
     */
    @Async
    public CompletableFuture<AsyncResult> checkAsyncRunsInSeparateThread() {
        long startTime = System.currentTimeMillis();
        long threadId = Thread.currentThread().getId();
        String threadName = Thread.currentThread().getName();

        simulateDelay(500);

        AsyncResult result = AsyncResult.builder()
                .threadId(threadId)
                .threadName(threadName)
                .startTimeMillis(startTime)
                .completed(true)
                .build();

        return CompletableFuture.completedFuture(result);
    }

    /*remark
     * Scenario 2: Synchronous trigger for async method
     * Waits for async method completion, ensuring main thread
     * captures correct async thread and timing information.
     */
    public AsyncExecutionResult triggerAsyncMethodSync() {
        long mainThreadId = Thread.currentThread().getId();
        long mainThreadStart = System.currentTimeMillis();

        AsyncResult asyncResult;
        try {
            //remark: wait for async completion
            asyncResult = self.checkAsyncRunsInSeparateThread().get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute async method", e);
        }

        long mainThreadEnd = System.currentTimeMillis();

        return AsyncExecutionResult.builder()
                .mainThreadId(mainThreadId)
                .mainThreadStartTimeMillis(mainThreadStart)
                .mainThreadEndTimeMillis(mainThreadEnd)
                .asyncThreadId(asyncResult.getThreadId())
                .asyncThreadName(asyncResult.getThreadName())
                .asyncThreadStartTimeMillis(asyncResult.getStartTimeMillis())
                .asyncThreadEndTimeMillis(System.currentTimeMillis())
                .asyncCompleted(asyncResult.isCompleted())
                .build();
    }

    /*remark
     * Scenario 2b: Non-blocking async trigger
     *note
     * Returns immediately while async task completes independently
     * and updates AsyncExecutionResult once finished.
     */
    public AsyncExecutionResult triggerAsyncMethodNonBlocking() {
        long mainThreadId = Thread.currentThread().getId();
        long mainThreadStart = System.currentTimeMillis();

        CompletableFuture<AsyncResult> asyncFuture = self.checkAsyncRunsInSeparateThread();

        AsyncExecutionResult result = AsyncExecutionResult.builder()
                .mainThreadId(mainThreadId)
                .mainThreadStartTimeMillis(mainThreadStart)
                .mainThreadEndTimeMillis(System.currentTimeMillis())
                .asyncThreadId(0) // Will be updated after async completion
                .asyncThreadName("pending")
                .asyncThreadStartTimeMillis(0)
                .asyncThreadEndTimeMillis(0)
                .asyncCompleted(false)
                .build();

        //remark update AsyncExecutionResult when async task completes
        asyncFuture.thenAccept(asyncResult -> {
            result.setAsyncThreadId(asyncResult.getThreadId());
            result.setAsyncThreadName(asyncResult.getThreadName());
            result.setAsyncThreadStartTimeMillis(asyncResult.getStartTimeMillis());
            result.setAsyncThreadEndTimeMillis(System.currentTimeMillis());
            result.setAsyncCompleted(asyncResult.isCompleted());
            System.out.println("Async completed in thread: " + asyncResult.getThreadName());
        });

        return result;
    }

    /*note
     * Scenario 3a: Async method with void return type
     * Demonstrates that async methods with void return type execute asynchronously
     * without requiring a return value.
     */
    @Async
    public void asyncVoidMethod() {
        simulateDelay(300); // Simulate work
        System.out.println("asyncVoidMethod completed in thread: "
                + Thread.currentThread().getName());
    }

    /**
     * Scenario 3b: Async method with Future<T> return type
     *
     * Demonstrates that async methods returning Future<T> can be awaited using .get().
     */
    @Async
    public Future<String> asyncFutureMethod() {
        simulateDelay(400); // Simulate work
        String result = "Completed";
        System.out.println("asyncFutureMethod completed in thread: " + Thread.currentThread().getName());
        return CompletableFuture.completedFuture(result); // Return already completed Future
    }


    //remark Simulate processing delay
    private void simulateDelay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
