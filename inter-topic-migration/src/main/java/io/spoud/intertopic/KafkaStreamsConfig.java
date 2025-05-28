package io.spoud.intertopic;

import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import io.spoud.avro.Person;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static io.spoud.intertopic.InterTopicMigrationApplication.TOPIC_V1;
import static io.spoud.intertopic.InterTopicMigrationApplication.TOPIC_V2;
import static java.util.Objects.requireNonNull;

@Configuration
public class KafkaStreamsConfig {

    private final String schemaRegistryUrl;

    public KafkaStreamsConfig(@Value("${spring.kafka.streams.properties.schema.registry.url}")
                              String schemaRegistryUrl) {
        this.schemaRegistryUrl = requireNonNull(schemaRegistryUrl);
    }

    @Bean
    public Topology topology(StreamsBuilder builder) {
        Serde<GenericRecord> genericAvroSerde = new GenericAvroSerde();
        genericAvroSerde.configure(Map.of(SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl), false);

        // read from topic v1
        KStream<String, GenericRecord> old = builder.stream(TOPIC_V1, Consumed.with(new Serdes.StringSerde(), genericAvroSerde));
        KStream<String, Person> mapped = old.mapValues((key, person) -> mapToV2(person));
        // write to v2
        mapped.to(TOPIC_V2);
        return builder.build();

    }

    private static Person mapToV2(GenericRecord person) {
        var email = String.format("%s.%s@%s", person.get("first_name"), person.get("last_name"), person.get("company_domain"));
        return Person.newBuilder()
                .setEmail(email)
                .setFirstName(person.get("first_name").toString())
                .setLastName(person.get("last_name").toString())
                .build();
    }
}
