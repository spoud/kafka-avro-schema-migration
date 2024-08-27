package io.spoud.intratopic;

import io.spoud.avro.Person2;
import io.spoud.avro.Person;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

@EnableKafka
@Configuration
public class KafkaConsumerConfiguration {

    private static Person2 mapToV2(Person person) {
        return Person2.newBuilder()
                .setEmail(String.format("%s.%s@%s", person.getFirstName(), person.getLastName(), person.getCompanyDomain()))
                .setFirstName(person.getFirstName())
                .setLastName(person.getLastName())
                .build();
    }

    @Bean
    public ConsumerFactory<String, Person2> migratingConsumerFactory(KafkaProperties properties) {
        MultiSchemaDeserializer<Person, Person2> multiSchemaDeserializer = new MultiSchemaDeserializer<>(
                KafkaConsumerConfiguration::mapToV2
        );
        var migratingFactory = new DefaultKafkaConsumerFactory<String, Person2>(properties.buildConsumerProperties(null));
        migratingFactory.setValueDeserializer(multiSchemaDeserializer);
        migratingFactory.setKeyDeserializer(new StringDeserializer());
        return migratingFactory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Person2>
    kafkaListenerContainerFactory(KafkaProperties properties) {
        ConcurrentKafkaListenerContainerFactory<String, Person2> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(migratingConsumerFactory(properties));
        return factory;
    }
}
