package com.maroodb.simpleevent;

import com.maroodb.simpleevent.core.Observable;
import com.maroodb.simpleevent.core.SimpleEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class SimpleEventTest {

    private static String TOPIC;

    @BeforeAll
    static void setup() {
         TOPIC = "SIMPLE_TOPIC";
    }

    @Test
    public void subscribeToAnEventByTopic() {
        SimpleEvent<String> simpleEvent = new SimpleEvent<>();
        simpleEvent.subscribe(TOPIC, System.out::println);
    }

    @Test
    public void publishToAnEventByTopic() {
        String message = "Hello World!";
        SimpleEvent<String> simpleEvent = new SimpleEvent<>();
        simpleEvent.publish(TOPIC, message);
    }

    @Test
    public void subscribeAndExecuteConsumerWhenPublishMessage() {
        SimpleEvent<String> simpleEvent = new SimpleEvent<>();
        String message = "Hello World!";
        StringBuilder sb = new StringBuilder();
        Consumer<String> consumer = sb::append;

        Observable<String> observable = simpleEvent.subscribe(TOPIC, consumer);
        simpleEvent.publish(TOPIC, message);

        assertEquals(message, sb.toString());

    }

    @Test
    public void subscribeThenUnsubscribeFromAnEvent() {
        SimpleEvent<String> simpleEvent = new SimpleEvent<>();
        String message = "Hello World!";
        StringBuilder sb = new StringBuilder();
        Consumer<String> consumer = sb::append;

        Observable<String> observable = simpleEvent.subscribe(TOPIC, consumer);
        observable.unsubscribe();
        simpleEvent.publish(TOPIC, message);

        assertEquals(0, sb.length());
    }
}
