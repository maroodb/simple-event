package com.maroodb.simpleevent.core;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class SimpleEvent <I> {

    private final static int DEFAULT_POOL_SIZE = 10;
    private final static AtomicInteger indexer = new AtomicInteger(1);
    private final Map<String, Set<Task<I>>> subscriptions = new HashMap<>();
    private final ExecutorService executorService;
    private final List<Future<?>> executingFutures = new LinkedList<>();

    public SimpleEvent() {
        executorService = Executors.newFixedThreadPool(DEFAULT_POOL_SIZE);
    }
    public SimpleEvent(int poolSize) {
        checkPoolSize(poolSize);
        executorService = Executors.newFixedThreadPool(poolSize);
    }

    public SimpleEvent(ExecutorService executorService) {
        checkExecutorService(executorService);
        this.executorService = executorService;
    }

    private void checkExecutorService(ExecutorService executorService) {
        if (executorService.isShutdown()) {
            throw new IllegalArgumentException();
        }
    }


    public Observable<I> subscribe(String topic, Consumer<I> consumer) {

        Set<Task<I>> topicConsumers = subscriptions.get(topic);

        if (topicConsumers == null) {
            topicConsumers = new HashSet<>();
        }

        Task<I> task = new Task<>(indexer.getAndIncrement(), consumer);
        topicConsumers.add(task);
        subscriptions.putIfAbsent(topic, topicConsumers);

        return new Observable<>(this, topic, task);
    }

    public void publish(String topic, I message) {

        Set<Task<I>> topicConsumers = subscriptions.get(topic);
        if (topicConsumers == null) {
            return;
        }
        for (Task<I> task : topicConsumers) {
            Future<?> future = executorService.submit(() -> {
                task.execute(message);
            });
            executingFutures.add(future);
        }

    }

    public void remove(String topic, Task<I> task) {
        Set<Task<I>> topicConsumers = subscriptions.get(topic);
        if (topicConsumers == null) {
            return;
        }
        topicConsumers.remove(task);
    }

    public boolean thereIsNoActiveTask() {
        removeTerminatedFutures();
        return executingFutures.size() == 0;
    }

    private void removeTerminatedFutures() {
        Predicate<Future<?>> isCancelled = Future::isCancelled;
        Predicate<Future<?>> isDone = Future::isDone;

        executingFutures.removeIf(isDone.or(isCancelled));
    }

    private void checkPoolSize(int poolSize) {
        if (poolSize < 1) {
            throw new IllegalArgumentException();
        }
    }
}
