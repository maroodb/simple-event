package com.maroodb.simpleevent.core;

/**
 * Observable it's the return type of subscribe method, its purpose is to handle subscription.
 * @author maroodb (Marouen Dbouba)
 * @param <I> the type of message handled by consumers.
 */
public final class Observable<I> {

    private final String topic;
    private final Task<I> task;
    private final SimpleEvent<I> simpleEventInstance;

    Observable(SimpleEvent<I> simpleEventInstance, String topic, Task<I> task) {
        this.simpleEventInstance = simpleEventInstance;
        this.topic = topic;
        this.task = task;
    }

    /**
     * Unsubscribe of the current observable
     */
    public void unsubscribe() {
        simpleEventInstance.remove(topic, task);
    }
}
