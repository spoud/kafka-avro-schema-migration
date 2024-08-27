package io.spoud.intertopic;

import io.spoud.avro.Person;
import io.spoud.avro.PersonV1;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaStreamsConfig {

    @Bean
    public Topology topology() {
        var builder = new StreamsBuilder();
        // read from topic v1
        KStream<String, PersonV1> old = builder.stream("persons");
        old.mapValues((key, person) -> mapToV2(person)).to("persons.v2");
        return builder.build();
    }

    private static Person mapToV2(PersonV1 person) {
        return Person.newBuilder()
                .setEmail(String.format("%s.%s@%s", person.getFirstName(), person.getLastName(), person.getCompanyDomain()))
                .setFirstName(person.getFirstName())
                .setLastName(person.getLastName())
                .build();
    }
}
