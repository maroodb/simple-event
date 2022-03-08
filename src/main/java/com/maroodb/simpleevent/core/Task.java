package com.maroodb.simpleevent.core;

import java.util.Objects;
import java.util.function.Consumer;

class Task<I> {
    private final int id;
    private final Consumer<I> consumer;

    Task(int id, Consumer<I> consumer) {
        this.id = id;
        this.consumer = consumer;
    }

    void execute(I message) {
        consumer.accept(message);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task<?> task = (Task<?>) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
