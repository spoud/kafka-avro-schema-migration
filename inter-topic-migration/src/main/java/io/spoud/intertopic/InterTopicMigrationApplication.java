package io.spoud.intertopic;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class InterTopicMigrationApplication {

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
