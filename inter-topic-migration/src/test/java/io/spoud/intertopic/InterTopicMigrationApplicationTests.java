package io.spoud.intertopic;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerializer;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import io.spoud.avro.Person;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.kafka.common.serialization.Deserializer;
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
import static org.apache.kafka.streams.StreamsConfig.*;
import static org.assertj.core.api.Assertions.assertThat;

class InterTopicMigrationApplicationTests {

    private TestInputTopic<String, GenericRecord> topic1;
    private TestOutputTopic<String, Person> topic2;

    @BeforeEach
    void setUp() {
        String schemaRegistryUrl = "mock://schemaregistry";
        Topology topology = new KafkaStreamsConfig(schemaRegistryUrl).topology(new StreamsBuilder());
        Properties props = new Properties();
        props.setProperty(APPLICATION_ID_CONFIG, "inter-topic-test");
        props.setProperty(SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        props.setProperty(BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        props.setProperty(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());
        props.setProperty(DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class.getName());
        Map<String, String> schemaRegistryUrlConfig = Map.of(SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);

        TopologyTestDriver testDriver = new TopologyTestDriver(topology, props);

        Deserializer<Person> v2Deserializer = new SpecificAvroSerde<Person>().deserializer();
        var d = Map.of(SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        KafkaAvroDeserializer genericDeserializer = new KafkaAvroDeserializer();
        genericDeserializer.configure(d, false);
        v2Deserializer.configure(d, false);
        topic2 = testDriver.createOutputTopic(TOPIC_V2, new StringDeserializer(), v2Deserializer);

        GenericAvroSerializer v1Serializer = new GenericAvroSerializer();
        v1Serializer.configure(schemaRegistryUrlConfig, false);
        topic1 = testDriver.createInputTopic(TOPIC_V1, new StringSerializer(), v1Serializer);
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
        assertThat(topic2.getQueueSize()).isEqualTo(1);
        var expected = Person.newBuilder().setFirstName("John").setLastName("Doe").setEmail("John.Doe@example.com").build();
        Person out = topic2.readValue();
        assertThat(out.getSchema()).isEqualTo(expected.getSchema());
        assertThat(out).isEqualTo(expected);
    }
}
