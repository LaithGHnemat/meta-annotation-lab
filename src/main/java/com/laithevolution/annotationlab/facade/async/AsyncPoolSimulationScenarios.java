package com.laithevolution.annotationlab.facade.async;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class AsyncPoolSimulationScenarios {

    @Lazy
    @Autowired
    public AsyncPoolSimulationScenarios self;

    @Autowired
    private ThreadPoolTaskExecutor asyncPoolSimulationExecutor;

    @Autowired
    private ThreadPoolTaskExecutor bigTaskExecutor;

    // Scenario 1: Async task using small executor

    @Async("asyncPoolSimulationExecutor")
    public CompletableFuture<String> performAsyncTask(int taskId, long delayMillis) {
        Thread current = Thread.currentThread();
        String threadName = current.getName();
        int activeCount = asyncPoolSimulationExecutor.getActiveCount();
        int queueSize = asyncPoolSimulationExecutor.getThreadPoolExecutor().getQueue().size();

        System.out.println("[SmallExecutor] Task-" + taskId + " started on " + threadName +
                " | ActiveThreads: " + activeCount + ", QueueSize: " + queueSize);

        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String msg = "Task-" + taskId + " done on " + threadName;
        System.out.println("[SmallExecutor] " + msg);
        return CompletableFuture.completedFuture(msg);
    }

    // Scenario 2: Async task using big executor

    @Async("bigTaskExecutor")
    public CompletableFuture<String> performBigAsyncTask(int taskId, long delayMillis) {
        Thread current = Thread.currentThread();
        String threadName = current.getName();
        int activeCount = bigTaskExecutor.getActiveCount();
        int queueSize = bigTaskExecutor.getThreadPoolExecutor().getQueue().size();

        System.out.println("[BigExecutor] BigTask-" + taskId + " started on " + threadName +
                " | ActiveThreads: " + activeCount + ", QueueSize: " + queueSize);

        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String msg = "BigTask-" + taskId + " done on " + threadName;
        System.out.println("[BigExecutor] " + msg);
        return CompletableFuture.completedFuture(msg);
    }


    // Scenario 3: Execute multiple tasks via async proxy
    public List<String> executeMultipleAsyncTasks(int numberOfTasks, long delayMillis) {
        List<CompletableFuture<String>> futures = new ArrayList<>();
        for (int i = 1; i <= numberOfTasks; i++) {
            futures.add(self.performAsyncTask(i, delayMillis));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        List<String> results = new ArrayList<>();
        for (CompletableFuture<String> future : futures) {
            results.add(future.join());
        }
        return results;
    }


    // Scenario 4: Task that can fail
    @Async("asyncPoolSimulationExecutor")
    public CompletableFuture<String> submitTaskWithFailure(int taskId, long delayMillis, boolean failTask) {
        Thread current = Thread.currentThread();
        String threadName = current.getName();
        int activeCount = asyncPoolSimulationExecutor.getActiveCount();
        int queueSize = asyncPoolSimulationExecutor.getThreadPoolExecutor().getQueue().size();

        System.out.println("[SmallExecutor] TaskWithFailure-" + taskId + " started on " + threadName +
                " | ActiveThreads: " + activeCount + ", QueueSize: " + queueSize);

        try {
            Thread.sleep(delayMillis);
            if (failTask) {
                throw new RuntimeException("Task-" + taskId + " failed intentionally");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
         // WARNING: Do not call supplyAsync(..., sameExecutor)
        // inside an @Async method using the same small pool; it can deadlock.

        String msg = "Task-" + taskId + " done";
        System.out.println("[SmallExecutor] " + msg);
        return CompletableFuture.completedFuture(msg);
    }
}
