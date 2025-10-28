package com.laithevolution.annotationlab.facade.async;

import com.laithevolution.annotationlab.dto.AsyncResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class AsyncParallelScenarios {

    @Lazy
    @Autowired
    private AsyncParallelScenarios self;

    /**
     * Scenario 5a: Single async task simulation
     *  Simulates a delay to represent async work
     *  and returns thread info with timing data.
     */
    @Async
    public CompletableFuture<AsyncResult> performAsyncTask(int taskId, long delayMillis) {
        long start = System.currentTimeMillis();
        long threadId = Thread.currentThread().getId();
        String threadName = Thread.currentThread().getName();

        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        AsyncResult result = AsyncResult.builder()
                .threadId(threadId)
                .threadName(threadName)
                .startTimeMillis(start)
                .endTimeMillis(System.currentTimeMillis())
                .completed(true)
                .message("Task " + taskId + " completed")
                .build();

        return CompletableFuture.completedFuture(result);
    }

    /**
     * Scenario 5b: Run multiple async tasks in parallel
     *  Launches several async tasks concurrently and waits for all to finish.
     */
    public List<AsyncResult> executeMultipleAsyncTasks(int numberOfTasks, long delayMillis) {
        List<CompletableFuture<AsyncResult>> futures = IntStream.rangeClosed(1, numberOfTasks)
                .mapToObj(i -> self.performAsyncTask(i, delayMillis))
                .toList();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }
}
