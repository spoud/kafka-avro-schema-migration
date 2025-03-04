package io.spoud.intertopic;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@SpringBootApplication
@EnableKafkaStreams
public class InterTopicMigrationApplication {

    public static final String TOPIC_V1 = "persons";
    public static final String TOPIC_V2 = "persons.v2";

    public static void main(String[] args) {
        SpringApplication.run(InterTopicMigrationApplication.class, args);
    }

    @Bean
    NewTopic personsTopic() {
        return new NewTopic("persons", 4, (short) 1);
    }

    @Bean
    NewTopic personsV2Topic() {
        return new NewTopic("persons.v2", 4, (short) 1);
    }

}
