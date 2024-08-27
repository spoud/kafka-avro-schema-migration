package io.spoud.intratopic;

import io.spoud.avro.Person2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

@Component
public class DemoConsumer {

    private final Flux<Person2> persons;
    private FluxSink<Person2> sink;

    public DemoConsumer() {
        persons = Flux.create(objectFluxSink -> {
            this.sink = objectFluxSink;
        });
        persons.subscribe(person2 -> {
            System.out.printf("consumed %s\n", person2.getFirstName());
        });

    }

    @KafkaListener(topics = {"persons"})
    private void consumeMultipleVersions(ConsumerRecord<String, Person2> record) {
        try {
            this.sink.next(record.value());
        } catch (Exception e) {
            this.sink.error(e);
        }
    }

    public Flux<Person2> getPersons() {
        return persons;
    }
}
