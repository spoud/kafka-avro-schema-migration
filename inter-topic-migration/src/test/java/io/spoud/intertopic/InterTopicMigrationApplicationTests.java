package io.spoud.intertopic;

import io.spoud.avro.Person;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
class InterTopicMigrationApplicationTests {

	@Autowired
	private DemoConsumer consumer;

	@Autowired
	private KafkaTemplate<String, Person> producer1;


	@Test
	void testMigration() {

	}
}
