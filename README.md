# SimpleEvent: A simple Java PubSub Library 
SimpleEvent is a simple event PubSub implementation library that makes asynchronous
messaging between objects more simple.

## Foretaste
```
var simpleEvent = new SimpleEvent<String>();

simpleEvent.subscribe("MyTopic", (message) -> {
    // do something with your message, or just print it!
    System.out.print(message);
});

simpleEvent.publish("MyTopic", "Hello World!");
```

## Installation

### Maven

```
<dependency>
  <groupId>io.github.maroodb</groupId>
  <artifactId>simple-event</artifactId>
  <version>1.0.1</version>
</dependency>
```
### Gradle

```
dependencies {
implementation group: 'io.github.maroodb', name: 'simple-event', version: '1.0.1'
    ...
}
```

## Example

```

var simpleEvent = new SimpleEvent<String>();

var observable = simpleEvent.subscribe("MyTopic", (message) -> {
    // do something with your message, or just print it!
    System.out.print(message);
});

simpleEvent.publish("MyTopic", "Hello World!");

// Unsubscribe from topic
observable.unsubscribe();

// nothing will happen
simpleEvent.publish("MyTopic", "Hello World!");


```