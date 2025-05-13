package io.spoud.intertopic;

import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerializer;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroDeserializer;
import io.spoud.avro.Person;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.test.TestRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static io.spoud.intertopic.InterTopicMigrationApplication.TOPIC_V1;
import static io.spoud.intertopic.InterTopicMigrationApplication.TOPIC_V2;
import static org.assertj.core.api.Assertions.assertThat;

class InterTopicMigrationApplicationTests {

    private Topology topology;
    private TopologyTestDriver testDriver;
    private TestInputTopic<String, GenericRecord> topic1;
    private TestOutputTopic<String, Person> topic2;

    @BeforeEach
    void setUp() {
        String schemaRegistryUrl = "mock://schemaregistry";
        topology = new KafkaStreamsConfig(schemaRegistryUrl).topology(new StreamsBuilder());
        Properties props = new Properties();
        props.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "inter-topic-test");
        props.setProperty("schema.registry.url", "mock://schema-registry");
        props.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        props.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());
        props.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, GenericAvroSerde.class.getName());
        Map<String, String> schemaRegistryUrlConfig = Map.of(SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);

        testDriver = new TopologyTestDriver(topology, props);

        GenericAvroSerializer serializer = new GenericAvroSerializer();
        serializer.configure(schemaRegistryUrlConfig, false);
        topic1 = testDriver.createInputTopic(TOPIC_V1, new StringSerializer(), serializer);

        SpecificAvroDeserializer<Person> deserializer = new SpecificAvroDeserializer<>();
        deserializer.configure(schemaRegistryUrlConfig, false);
        topic2 = testDriver.createOutputTopic(TOPIC_V2, new StringDeserializer(), deserializer);
    }

    @Test
    void testMigration() throws IOException {
        // given
        Schema.Parser parser = new Schema.Parser();
        Schema s = parser.parse(new File("src/main/resources/v1.avsc"));
        GenericRecordBuilder recordBuilder = new GenericRecordBuilder(s);
        var v1Record = recordBuilder
                .set(s.getField("first_name"), "John")
                .set(s.getField("last_name"), "Doe")
                .set(s.getField("company_domain"), "example.com")
                .build();

        // when
        topic1.pipeInput(new TestRecord<>("p1", v1Record));
        // then
        var expected = Person.newBuilder().setFirstName("John").setLastName("Doe").setEmail("john.doe@example.com").build();
        Person v2 = topic2.readValue();
        assertThat(v2).isEqualTo(expected);

    }
}
