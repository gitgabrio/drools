efesto-kafka
============

General design
--------------

`efesto-kafka-runtime-provider` represents the part of the code to be executed _server-side_, i.e. inside the same JVM of the main Efesto processor.
[KafkaRuntimeServiceProvider](./efesto-kafka-runtime/efesto-kafka-runtime-provider/src/main/java/org/kie/efesto/kafka/runtime/provider/service/KafkaRuntimeServiceProvider.java) is the kafka-specific implementation of `RuntimeServiceProvider`. It uses kafka-topics to discover 
remotely-deployed `KieRuntimeService` and return them as `KafkaKieRuntimeServiceGateway`, that will be stored in the `firstLevelCache` of the main Efesto processor.
[KafkaKieRuntimeServiceGateway](./efesto-kafka-runtime/efesto-kafka-runtime-provider/src/main/java/org/kie/efesto/kafka/runtime/provider/service/KafkaKieRuntimeServiceGateway.java) is the kafka-specific implementation of `KieRuntimeService`. It uses kafka-topics to execute methods on
remotely-deployed `KieRuntimeService`.


`efesto-kafka-runtime-services` represents the part of the code to be executed _remotely_, i.e. outside the JVM of the main Efesto processor.
[KafkaRuntimeLocalServiceProvider](./efesto-kafka-runtime/efesto-kafka-runtime-services/src/main/java/org/kie/efesto/kafka/runtime/services/service/KafkaRuntimeLocalServiceProvider.java) retrieves the locally discovered `KieServices` and provides them to the kafka topics.
[KafkaKieRuntimeService](./efesto-kafka-runtime/efesto-kafka-runtime-services/src/main/java/org/kie/efesto/kafka/runtime/services/service/KafkaKieRuntimeService.java) is the kafka _remote_ implementation of `KieService`:
1. it wraps the actual, local `KieService`
2. receive requests from `KafkaKieRuntimeServiceGateway` through Kie-Service-request-specific topic (`ModelLocalUriID`)
3. execute methods on wrapped `KieService`
4. send results back to `KafkaKieRuntimeServiceGateway` through Kie-Service-response-specific topic




Kafka setup
-----------

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