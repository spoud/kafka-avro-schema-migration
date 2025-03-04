package io.spoud.intertopic;

import io.spoud.avro.Person;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static io.spoud.intertopic.InterTopicMigrationApplication.TOPIC_V2;

@Component
public class DemoConsumer {

    private final List<Person> persons = new ArrayList<>();

    public DemoConsumer() {
    }

    @KafkaListener(topics = TOPIC_V2)
    private void consumeV2(ConsumerRecord<String, Person> record) {
        try {
            System.out.printf("topic: %s, email: %s%n", record.topic(), record.value().getEmail());
            this.persons.add(record.value());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Person> getPersons() {
        return persons;
    }
}
