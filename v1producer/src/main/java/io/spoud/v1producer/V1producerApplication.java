package io.spoud.v1producer;

import io.spoud.avro.Person;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class V1producerApplication implements CommandLineRunner {

    private final KafkaTemplate<String, Person> kTemplate;

    public V1producerApplication(KafkaTemplate<String, Person> kTemplate) {
        this.kTemplate = kTemplate;
    }

    public static void main(String[] args) {
        SpringApplication.run(V1producerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        for (int i = 0; i < 10; i++) {
            kTemplate.send("persons", "p" + i, Person.newBuilder()
                    .setFirstName("First" + i)
                    .setLastName("Last")
                    .setCompanyDomain("example.com")
                    .build())
                    .get(2000, TimeUnit.MILLISECONDS);
        }

    }
}
