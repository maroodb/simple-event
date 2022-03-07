package com.maroodb.simpleevent.core;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public final class SimpleEvent <I> {

    private final static AtomicInteger indexer = new AtomicInteger(1);
    private final Map<String, Set<Task<I>>> subscriptions = new HashMap<>();

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
            task.execute(message);
        }
    }

    public void remove(String topic, Task<I> task) {
        Set<Task<I>> topicConsumers = subscriptions.get(topic);
        if (topicConsumers == null) {
            return;
        }
        topicConsumers.remove(task);
    }
}
