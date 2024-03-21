package com.example.amazingmaze;

import com.example.amazingmaze.services.GameService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@Slf4j
@SpringBootApplication
public class AmazingMazeApplication {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(AmazingMazeApplication.class, args);
        GameService gameService = context.getBean(GameService.class);
        gameService.gatherInfo();
    }
}