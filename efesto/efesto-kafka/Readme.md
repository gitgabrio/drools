# efesto-kafka

## General design
_efesto-kafka_ is exposed through a gateway-service design.
`efesto-kafka-runtime-gateway` represents the part of the code to be executed _locally_, i.e. inside the same JVM of the main Efesto processor.

[KafkaRuntimeServiceProvider](./efesto-kafka-runtime/efesto-kafka-runtime-gateway/src/main/java/org/kie/efesto/kafka/runtime/provider/service/KafkaRuntimeServiceProvider.java) is the kafka-specific implementation of `RuntimeServiceProvider`. It uses kafka-topics to discover 
remotely-deployed `KieRuntimeService` and return them as `KafkaKieRuntimeServiceGateway`, that will be stored in the `firstLevelCache` of the main Efesto processor.

[KafkaKieRuntimeServiceGateway](./efesto-kafka-runtime/efesto-kafka-runtime-gateway/src/main/java/org/kie/efesto/kafka/runtime/provider/service/KafkaKieRuntimeServiceGateway.java) is the kafka-specific implementation of `KieRuntimeService`. It uses kafka-topics to execute methods on
remotely-deployed `KieRuntimeService`.


`efesto-kafka-runtime-service` represents the part of the code to be executed _remotely_, i.e. outside the JVM of the main Efesto processor (the gateway).

[KafkaRuntimeLocalServiceProvider](./efesto-kafka-runtime/efesto-kafka-runtime-service/src/main/java/org/kie/efesto/kafka/runtime/services/service/KafkaRuntimeLocalServiceProvider.java) retrieves the locally discovered `KieServices` and provides them to the kafka topics.

[KafkaKieRuntimeService](./efesto-kafka-runtime/efesto-kafka-runtime-service/src/main/java/org/kie/efesto/kafka/runtime/services/service/KafkaKieRuntimeService.java) is the kafka _remote_ implementation of `KieService`:
1. it wraps the actual, local `KieService`
2. receive requests from `KafkaKieRuntimeServiceGateway` through Kie-Service-request-specific topic (`ModelLocalUriID`)
3. execute methods on wrapped `KieService`
4. send results back to `KafkaKieRuntimeServiceGateway` through Kie-Service-response-specific topic

## Topics
efesto-kafka exposes four of "public" topics to be used by external application, namely:

1. `compile-topic` (to request model compilation and deployment)
2. `compiled-topic` (to receive compilation result)
3. `evaluate-topic` (to request model evaluation)
4. `evaluated-topic` (to receive evaluation result)

Internally, efesto-kafka communication uses other topics, namely:

1. `runtimeservice_discover_topic` (used by `KafkaKieRuntimeServiceGateway` to discover distributed services)
2. `runtimeservice_notification_topic` (used by `KafkaKieRuntimeService`s to notify their availability)
3. `runtimeservice_evaluateinputrequest_topic` (used by `KafkaKieRuntimeServiceGateway` to forward evaluation requests to the distributed services)
4. `runtimeservice_evaluateinputresponse_topic` (used by `KafkaKieRuntimeService`s to publish evaluation output)

The two set of topics ("public" vs "interna") have been separated to enforce independence and avoid unwanted binding between the two different components (external API vs internal API).
The names of the topics are defined inside [KafkaConstants](./efesto-kafka-api/src/main/java/org/kie/efesto/kafka/api/KafkaConstants.java)


## Threads

The communication over kafka media is inherently asynchronous, and it depends heavily on always-on **Consumers**, wrapped inside `Threads` 
The [ThreadUtils](./efesto-kafka-api/src/main/java/org/kie/efesto/kafka/api/ThreadUtils.java) is provided to facilitate and unify the instantiation of such threads.
Each method of this class returns a `Thread` and accept a `Consumer` and one or more `FunctionalInterface`s.
Inside the returned thread, the consumer is polled and, when a record is received, the given function(s) are invoked.
Every "cvlient" code is responsible to implement and provide the function(s) to be executed inside the thread, when a record is received.
A particular thread is the one retrieved by the `getConsumeAndListenThread` method.
Inside this thread, every `EfestoKafkaMessageListener` provided will be notified with the message produced by the given function.
This thread provides the ability to register multiple listeners that will be informed when a given message is produced in response to a request.
This thread is currently used by: 
1. [KieServiceNotificationConsumer](./efesto-kafka-runtime/efesto-kafka-runtime-gateway/src/main/java/org/kie/efesto/kafka/runtime/provider/consumer/KieServiceNotificationConsumer.java) - the gateway-side listening for internal notification response
2. [EvaluateInputResponseConsumer](./efesto-kafka-runtime/efesto-kafka-runtime-gateway/src/main/java/org/kie/efesto/kafka/runtime/provider/consumer/EvaluateInputResponseConsumer.java) - the gateway-side listening for internal evaluation response
3. [EvaluateInputRequestConsumer](./efesto-kafka-runtime/efesto-kafka-runtime-service/src/main/java/org/kie/efesto/kafka/runtime/services/consumer/EvaluateInputRequestConsumer.java) - the service-side listening for internal evaluation request










## Kafka setup

1. download [zookeper](https://zookeeper.apache.org/releases.html)
2. extract it
3. make scripts executable
4. create a `zoo.cfg` config file under `../conf`
    ```
   tickTime=2000
   dataDir=/your/location/here/apache-zookeeper-3.8.0-bin/data
   clientPort=2181
   initLimit=5
   syncLimit=2
   ```
5. start zookeeper server: `./bin/zkServer.sh start`   
6. download [kafka](https://kafka.apache.org/downloads)
7. extract it
8. make scripts executable
9. start kafka server: `./bin/kafka-server-start.sh config/server.properties` (this will start the Kafka server and listen for connections on the default port (9092))