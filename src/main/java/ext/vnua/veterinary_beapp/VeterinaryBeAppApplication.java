package ext.vnua.veterinary_beapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VeterinaryBeAppApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(VeterinaryBeAppApplication.class, args);
    }

}
