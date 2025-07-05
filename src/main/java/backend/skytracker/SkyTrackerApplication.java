package backend.skytracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class SkyTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkyTrackerApplication.class, args);
    }

}
