package com.maroodb.simpleevent;

import com.maroodb.simpleevent.core.Observable;
import com.maroodb.simpleevent.core.SimpleEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.awaitility.Awaitility.with;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


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
        StringBuffer sb = new StringBuffer();
        Consumer<String> consumer = sb::append;

        Observable<String> observable = simpleEvent.subscribe(TOPIC, consumer);
        simpleEvent.publish(TOPIC, message);

        with().pollDelay(100, MILLISECONDS)
                .and()
                .pollInterval(200, MILLISECONDS)
                .await().until(simpleEvent::thereIsNoActiveTask);

        assertEquals(message, sb.toString());

    }

    @Test
    public void subscribeThenUnsubscribeFromAnEvent() {
        SimpleEvent<String> simpleEvent = new SimpleEvent<>();
        String message = "Hello World!";
        StringBuffer sb = new StringBuffer();
        Consumer<String> consumer = sb::append;

        Observable<String> observable = simpleEvent.subscribe(TOPIC, consumer);
        observable.unsubscribe();
        simpleEvent.publish(TOPIC, message);


        assertEquals(0, sb.length());
    }

    @Test
    public void handleMultipleConsumersOfSameTopic() {

        SimpleEvent<String> simpleEvent = new SimpleEvent<>();
        String message = "H";
        StringBuffer sb = new StringBuffer();
        Consumer<String> consumer1 = sb::append;
        Consumer<String> consumer2 = sb::append;
        String expectedResult = "HH";

        Observable<String> observable1 = simpleEvent.subscribe(TOPIC, consumer1);
        Observable<String> observable2 = simpleEvent.subscribe(TOPIC, consumer2);

        simpleEvent.publish(TOPIC, message);

        with().pollDelay(100, MILLISECONDS)
                .and()
                .pollInterval(200, MILLISECONDS)
                .await().until(simpleEvent::thereIsNoActiveTask);

        assertEquals(sb.toString(), expectedResult);
    }

   @Test
    public void handleMultipleConsumersOfSameTopicNonBlocking() {

        SimpleEvent<String> simpleEvent = new SimpleEvent<>();
        String message = "Hello World!";
        StringBuffer sb = new StringBuffer();
        String expectedResult = "21";

        Consumer<String> consumer1 = (s) -> {
            sb.append("2");
        };
        Consumer<String> consumer2 = (s) -> {
            try {
                Thread.sleep(500);
                sb.append("1");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        Observable<String> observable2 = simpleEvent.subscribe(TOPIC, consumer2);
        Observable<String> observable1 = simpleEvent.subscribe(TOPIC, consumer1);

        simpleEvent.publish(TOPIC, message);

       with().pollDelay(100, MILLISECONDS)
               .and()
               .pollInterval(200, MILLISECONDS)
               .await().until(simpleEvent::thereIsNoActiveTask);

        assertEquals(sb.toString(), expectedResult);
    }

    @Test
    public void defineThreadPoolSize() {
        SimpleEvent<String> simpleEvent = new SimpleEvent<>(1);
        String message = "Hello World!";
        StringBuffer sb = new StringBuffer();

        String expectedResult = "12";

        Consumer<String> consumer1 = (s) -> {
            sb.append("2");
        };
        Consumer<String> consumer2 = (s) -> {
            try {
                Thread.sleep(500);
                sb.append("1");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        Observable<String> observable2 = simpleEvent.subscribe(TOPIC, consumer2);
        Observable<String> observable1 = simpleEvent.subscribe(TOPIC, consumer1);

        simpleEvent.publish(TOPIC, message);

        with().pollDelay(100, MILLISECONDS)
                .and()
                .pollInterval(200, MILLISECONDS)
                .await().until(simpleEvent::thereIsNoActiveTask);
        // given a poolSize = 1, task 2 will wait for task 1 to finish
        assertEquals(sb.toString(), expectedResult);
    }

    @Test
    public void throwExceptionWhenGivenAZeroThreadPoolSize() {
        SimpleEvent<String> simpleEvent;
        Supplier<SimpleEvent<String>> supplier = () -> new SimpleEvent<>(0);
        assertThrows(IllegalArgumentException.class, supplier::get);
    }

    @Test
    public void throwExceptionWhenGivenANegativeThreadPoolSize() {
        SimpleEvent<String> simpleEvent;
        Supplier<SimpleEvent<String>> supplier = () -> new SimpleEvent<>(-10);
        assertThrows(IllegalArgumentException.class, supplier::get);
    }

    @Test
    public void createSimpleEventWithGivenExecutorService() {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        SimpleEvent<String> simpleEvent = new SimpleEvent<String>(executorService);
        String message = "Hello World!";
        StringBuffer sb = new StringBuffer();
        Consumer<String> consumer = sb::append;

        Observable<String> observable = simpleEvent.subscribe(TOPIC, consumer);
        simpleEvent.publish(TOPIC, message);

        with().pollDelay(100, MILLISECONDS)
                .and()
                .pollInterval(200, MILLISECONDS)
                .await().until(simpleEvent::thereIsNoActiveTask);

        assertEquals(message, sb.toString());
    }

    @Test
    public void throwExceptionWhenCreateSimpleEventWithGivenTerminatedExecutorService() {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        executorService.shutdown();
        Supplier<SimpleEvent<String>> supplier = () -> new SimpleEvent<>(executorService);
        assertThrows(IllegalArgumentException.class, supplier::get);
    }

}
