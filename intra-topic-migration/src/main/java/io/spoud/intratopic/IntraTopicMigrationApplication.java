package io.spoud.intratopic;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class IntraTopicMigrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntraTopicMigrationApplication.class, args);
    }

    @Bean
    NewTopic personsTopic() {
        return new NewTopic("persons", 4, (short) 1);
    }

}
