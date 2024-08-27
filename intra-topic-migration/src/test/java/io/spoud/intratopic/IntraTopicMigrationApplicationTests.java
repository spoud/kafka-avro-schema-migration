package io.spoud.intratopic;

import io.spoud.avro.Person2;
import io.spoud.avro.Person3;
import io.spoud.avro.Person;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
class IntraTopicMigrationApplicationTests {

    @Autowired
    private DemoConsumer consumer;

    @Autowired
    private KafkaTemplate<String, Person> producer1;

    @Autowired
    private KafkaTemplate<String, Person2> producer2;

    @Autowired
    private KafkaTemplate<String, Person3> producer3;

    @Test
    @DisplayName("should map v1 records to v2 records transparently")
    void testV1toV2Mapping() {
        this.producer1.send("persons", "p1", Person.newBuilder()
                .setFirstName("John")
                .setLastName("Doe")
                .setCompanyDomain("example.com")
                .build());
        Person2 p2 = Person2.newBuilder()
                .setFirstName("Jane")
                .setLastName("Foo")
                .setEmail("jane.foo@example.com")
                .build();
        this.producer2.send("persons", "p2", p2);

        assertThat(consumer.getPersons().take(2).collectList().block()).containsExactlyInAnyOrder(
                Person2.newBuilder()
                        .setFirstName("John")
                        .setLastName("Doe")
                        .setEmail("John.Doe@example.com")
                        .build(),
                p2
        );
    }

    @Test
    @DisplayName("should fail for for unrecognized record/schema types")
    void testUnsupportedSchema() {
        Person3 p3 = Person3.newBuilder()
                .setUid(UUID.randomUUID().toString())
                .build();
        this.producer3.send("persons", "p3", p3);
        assertThatThrownBy(() -> consumer.getPersons()
                .blockFirst(Duration.of(500, ChronoUnit.MILLIS))
        ).isInstanceOf(IllegalStateException.class);
    }
}
