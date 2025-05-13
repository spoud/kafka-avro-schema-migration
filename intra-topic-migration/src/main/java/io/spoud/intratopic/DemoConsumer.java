package io.spoud.intratopic;

import io.spoud.avro.Person2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class DemoConsumer {

    private final List<Person2> persons = new ArrayList<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoConsumer.class);

    public DemoConsumer() {

    }

    @KafkaListener(topics = {"persons"})
    private void consumeMultipleVersions(ConsumerRecord<String, Person2> record) {
        try {
            this.persons.add(record.value());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public List<Person2> getPersons() {
        return persons;
    }

    public void clearPersons() {
        persons.clear();
    }

}
