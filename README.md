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

### Maven Repository

```
<dependency>
  <groupId>io.github.maroodb</groupId>
  <artifactId>simple-event</artifactId>
  <version>1.0.1</version>
</dependency>
```

