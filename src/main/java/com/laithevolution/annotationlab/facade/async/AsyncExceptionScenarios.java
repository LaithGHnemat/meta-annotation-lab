package com.laithevolution.annotationlab.facade.async;

import com.laithevolution.annotationlab.dto.AsyncExecutionResult;
import com.laithevolution.annotationlab.dto.AsyncResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class AsyncExceptionScenarios {
    @Lazy
    @Autowired
    private AsyncExceptionScenarios self;

    /**
     * Scenario 4a: Async method throwing RuntimeException
     *
     * Demonstrates that RuntimeExceptions thrown inside an async method
     * do not block the main thread and are captured within the async thread.
     */
    @Async
    public CompletableFuture<Void> asyncRuntimeExceptionMethod() {
        return CompletableFuture.runAsync(() -> {
            throw new RuntimeException("Forced RuntimeException");
        });
    }

    /**
     * Scenario 4b: Async method throwing CheckedException
     *  Demonstrates that checked exceptions (e.g., IOException) in async methods
     *  must be wrapped (e.g., in RuntimeException) since async execution
     *  does not allow checked exceptions to propagate directly.
     */
    @Async
    public CompletableFuture<Void> asyncCheckedExceptionMethod() {
        return CompletableFuture.runAsync(() -> {
            try {
                throw new IOException("Forced CheckedException");
            } catch (IOException e) {
                throw new RuntimeException(e); //desc wrap checked exception
            }
        });
    }

    /**
     * Scenario 4c: Async method returning AsyncResult with exception info
     * Illustration capturing exception and thread info within a returned DTO,
     *  allowing main thread to continue without being blocked by the async exception.
     */

    @Async
    public CompletableFuture<AsyncResult> asyncMethodReturningResultWithExceptionInfoAsync() {
        long startTime = System.currentTimeMillis();
        long threadId = Thread.currentThread().getId();
        String threadName = Thread.currentThread().getName();

        Optional<Exception> capturedException;

        try {
            Thread.sleep(300);

            // Forced exception for demonstration
            throw new RuntimeException("Forced exception inside async thread");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            capturedException = Optional.of(e);
        } catch (RuntimeException e) {
            capturedException = Optional.of(e);
        }

        AsyncResult result = AsyncResult.builder()
                .threadId(threadId)
                .threadName(threadName)
                .startTimeMillis(startTime)
                .endTimeMillis(System.currentTimeMillis())
                .completed(capturedException.isEmpty())
                .exception(capturedException.orElse(null))
                .build();

        return CompletableFuture.completedFuture(result);
    }

    public AsyncExecutionResult triggerAsyncMethodWithExceptionSync() {
        long mainThreadId = Thread.currentThread().getId();
        long mainThreadStart = System.currentTimeMillis();

        AsyncResult asyncResult;
        try {
            // Wait for async method to complete
            asyncResult = self.asyncMethodReturningResultWithExceptionInfoAsync().get();
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
                .asyncThreadEndTimeMillis(asyncResult.getEndTimeMillis())
                .asyncCompleted(asyncResult.isCompleted())
                .exception(asyncResult.getException())
                .build();
    }


}
