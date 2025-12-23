package io.github.hypecycle.chatpipeline.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hypecycle.chatpipeline.connector.chzzk.api.ChannelIdReader;
import java.util.Scanner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ThreadConfig {

    @Bean
    public ChannelIdReader channelIdReader() {
        return new ChannelIdReader(new Scanner(System.in));
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public ThreadPoolTaskExecutor chatManagerThreadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(150);
        executor.setThreadNamePrefix("chat-manager");
        executor.initialize();
        return executor;
    }

    @Bean
    public ThreadPoolTaskExecutor chatWorkerThreadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(150);
        executor.setThreadNamePrefix("chat-worker");
        executor.initialize();
        return executor;
    }
}
