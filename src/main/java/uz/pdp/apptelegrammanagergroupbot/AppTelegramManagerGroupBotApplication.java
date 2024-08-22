package uz.pdp.apptelegrammanagergroupbot;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Random;

@SpringBootApplication
public class AppTelegramManagerGroupBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppTelegramManagerGroupBotApplication.class, args);
    }

    @Bean
    public Random random(){
        return new Random();
    }
}
