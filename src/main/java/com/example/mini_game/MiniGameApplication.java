package com.example.mini_game;

import com.example.mini_game.dto.VnpayProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(VnpayProperties.class)
public class MiniGameApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiniGameApplication.class, args);
    }

}
