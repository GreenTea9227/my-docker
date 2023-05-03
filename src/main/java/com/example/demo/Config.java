package com.example.demo;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Slf4j
@Configuration
public class Config {

    @Value("${my.name:yohan}")
    private String name;

    @Bean(name = "asyncExecutor")
    public Executor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(20);
        executor.setThreadNamePrefix("custom");
        executor.initialize();
        return executor;
    }

    @PostConstruct
    public void name() {
        System.out.println(name);
    }

    @Profile("dev")
    @Bean
    public void dev() {
        log.info("dev");
    }

    @Profile("hello")
    @Bean
    public void hello() {
        log.info("hello");
    }
}
