package io.github.hypecycle.chatpipeline.global.config;

import io.github.hypecycle.chatpipeline.connector.chzzk.ChannelIdReader;
import java.util.Scanner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public ChannelIdReader channelIdReader() {
        return new ChannelIdReader(new Scanner(System.in));
    }
}
