package com.maroodb.simpleevent.core;

public final class Observable<I> {

    private final String topic;
    private final Task<I> task;
    private final SimpleEvent<I> simpleEventInstance;

    Observable(SimpleEvent<I> simpleEventInstance, String topic, Task<I> task) {
        this.simpleEventInstance = simpleEventInstance;
        this.topic = topic;
        this.task = task;
    }

    public void unsubscribe() {
        simpleEventInstance.remove(topic, task);
    }
}
