package com.example.polymarket;

import com.example.polymarket.config.PolymarketProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(PolymarketProperties.class)
public class PolymarketApplication {

    public static void main(String[] args) {
        SpringApplication.run(PolymarketApplication.class, args);
    }

}
