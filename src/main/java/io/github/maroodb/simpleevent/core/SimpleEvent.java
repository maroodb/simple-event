package io.github.maroodb.simpleevent.core;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * SimpleEvent is a PubSub API, which handle subscriptions and publish asynchronously in-memory
 * using a pool of thread.
 * @author maroodb (Marouen Dbouba)
 * @param <I> the type of Message handled by consumers
 */
public final class SimpleEvent<I> {

    private final static int DEFAULT_POOL_SIZE = 10;
    private final static AtomicInteger indexer = new AtomicInteger(1);

    private final Map<String, Set<Task<I>>> subscriptions = new HashMap<>();
    private final ExecutorService executorService;
    private final List<Future<?>> executingFutures = new LinkedList<>();

    /**
     * Constructs a new <tt>SimpleEvent</tt>.
     * The <tt>SimpleEvent</tt> is created with a default thread pool size (10)
     */
    public SimpleEvent() {
        executorService = Executors.newFixedThreadPool(DEFAULT_POOL_SIZE);
    }

    /**
     * Constructs a new <tt>SimpleEvent</tt> with a specified thread pool size.
     * @param poolSize the required thread pool size.
     */
    public SimpleEvent(int poolSize) {
        checkPoolSize(poolSize);
        executorService = Executors.newFixedThreadPool(poolSize);
    }

    /**
     * Constructs a new <tt>SimpleEvent</tt> with a specified ExecutorService (externally managed)
     * @param executorService an external managed ExecutorService
     */
    public SimpleEvent(ExecutorService executorService) {
        checkExecutorService(executorService);
        this.executorService = executorService;
    }

    /**
     * Subscribe and register the specified consumer to the given topic.
     * @param topic    a String topic name on which the client want to subscribe.
     * @param consumer a Consumer lambda function that il will be executed every time a message published to the topic.
     * @return return an Object of type observable
     */
    public Observable<I> subscribe(String topic, Consumer<I> consumer) {

        checkTopic(topic);
        checkConsumer(consumer);

        Set<Task<I>> topicConsumers = subscriptions.get(topic);

        if (topicConsumers == null) {
            topicConsumers = new HashSet<>();
        }

        int taskId = indexer.getAndIncrement();
        Task<I> task = new Task<>(taskId, consumer);
        topicConsumers.add(task);
        subscriptions.putIfAbsent(topic, topicConsumers);

        return new Observable<>(this, topic, task);
    }

    /**
     * Publish a message to a specific topic.
     * @param topic   a String topic name on which the client want to publish
     * @param message a parameterized message which will be consumed by topic subscribers.
     */
    public void publish(String topic, I message) {
        runCleanFutureTask();
        Set<Task<I>> topicConsumers = subscriptions.get(topic);
        if (topicConsumers == null) {
            return;
        }
        for (Task<I> task : topicConsumers) {
            Future<?> future = executorService.submit(() -> task.execute(message));
            executingFutures.add(future);
        }

    }


    void remove(String topic, Task<I> task) {
        Set<Task<I>> topicConsumers = subscriptions.get(topic);
        if (topicConsumers == null) {
            return;
        }
        topicConsumers.remove(task);
    }

    /**
     * Check if there is active thread.
     * @return a boolean false/true indicates if there is an active running consumer task.
     */
    public boolean thereIsNoActiveTask() {
        removeTerminatedFutures();
        return executingFutures.size() == 0;
    }

    private void runCleanFutureTask() {
        executorService.submit(this::removeTerminatedFutures);
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

    private void checkExecutorService(ExecutorService executorService) {
        if (executorService.isShutdown()) {
            throw new IllegalArgumentException();
        }
    }

    private void checkTopic(String topic) {
        if (topic == null || topic.isEmpty()) {
            throw new IllegalArgumentException();
        }
    }

    private void checkConsumer(Consumer<I> consumer) {
        if (consumer == null) {
            throw new IllegalArgumentException();
        }
    }
}
