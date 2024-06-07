package org.example.creww;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

//@SpringBootApplication
//@EnableJpaAuditing
@ConfigurationPropertiesScan
@SpringBootApplication
public class CrewwApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrewwApplication.class, args);
    }

}
