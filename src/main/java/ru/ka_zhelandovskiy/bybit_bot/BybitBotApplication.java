package ru.ka_zhelandovskiy.bybit_bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@EnableCaching
@EnableScheduling
@SpringBootApplication
@EnableAutoConfiguration
public class BybitBotApplication {

    public static void main(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(BybitBotApplication.class);
        builder.headless(false);
        builder.run(args);
    }

}
