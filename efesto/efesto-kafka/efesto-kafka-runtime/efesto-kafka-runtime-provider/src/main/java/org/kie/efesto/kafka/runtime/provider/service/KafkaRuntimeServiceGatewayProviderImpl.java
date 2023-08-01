package org.kie.efesto.kafka.runtime.provider.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;
import org.kie.efesto.kafka.api.listeners.EfestoKafkaMessageListener;
import org.kie.efesto.kafka.api.messages.AbstractEfestoKafkaMessage;
import org.kie.efesto.kafka.api.service.KafkaKieRuntimeService;
import org.kie.efesto.kafka.api.service.KafkaRuntimeServiceProvider;
import org.kie.efesto.kafka.runtime.provider.consumer.KieServiceNotificationConsumer;
import org.kie.efesto.kafka.runtime.provider.messages.EfestoKafkaRuntimeServiceNotificationMessage;
import org.kie.efesto.kafka.runtime.provider.producer.KieServicesDiscoverProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * This is the <b>kafka-specific</b> <code>RuntimeServiceProvider</code> that query and keep tracks of
 * <b>kafka-embedded</b> <code>KieRuntimeService</code>s
 */
public class KafkaRuntimeServiceGatewayProviderImpl implements KafkaRuntimeServiceProvider, EfestoKafkaMessageListener {

    private static final Logger logger = LoggerFactory.getLogger(KafkaRuntimeServiceGatewayProviderImpl.class);
    private List<KafkaKieRuntimeService> kieRuntimeServices = new ArrayList<>();


    public KafkaRuntimeServiceGatewayProviderImpl() {
    }


    public KafkaRuntimeServiceGatewayProviderImpl(final Consumer<Long, JsonNode> consumer, final Producer<Long, JsonNode> producer) {
        logger.info("Starting listening for AbstractEfestoKafkaMessage info on Kafka channel");
        Collection<EfestoKafkaMessageListener> listeners = new ArrayList<>();
        listeners.add(this);
        KieServiceNotificationConsumer.startEvaluateConsumer(consumer, listeners);
        searchServices(producer);
    }

    public void searchServices() {
        logger.info("Starting listening for AbstractEfestoKafkaMessage info on Kafka channel");
        KieServiceNotificationConsumer.startEvaluateConsumer(this);
        KieServicesDiscoverProducer.runProducer();
    }

    public void searchServices(final Producer<Long, JsonNode> producer) {
        logger.info("Requesting KieRuntimeServices info on Kafka channel with {}", producer);
        KieServicesDiscoverProducer.runProducer(producer);
    }

    @Override
    public List<KafkaKieRuntimeService> getKieRuntimeServices() {
        return Collections.unmodifiableList(kieRuntimeServices);
    }

    @Override
    public void onMessageReceived(AbstractEfestoKafkaMessage received) {
        if (!(received instanceof EfestoKafkaRuntimeServiceNotificationMessage)) {
            logger.warn("Unexpected message {}", received);
        } else {
            EfestoKafkaRuntimeServiceNotificationMessage notificationMessage = (EfestoKafkaRuntimeServiceNotificationMessage) received;
            KafkaKieRuntimeService toAdd = new KafkaKieRuntimeServiceGateway(notificationMessage.getModel(), notificationMessage.getEfestoClassKey());
            if (!kieRuntimeServices.contains(toAdd)) {
                logger.info("Adding newly discovered KieRuntimeService {}", toAdd);
                kieRuntimeServices.add(toAdd);
                KafkaRuntimeManagerUtils.addKieRuntimeServiceToFirstLevelCache(toAdd);
            } else {
                logger.warn("KieRuntimeService already discovered {}", toAdd);
            }
        }
    }
}
