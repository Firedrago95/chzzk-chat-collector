package io.github.hypecycle.chzzk.global.config;

import io.github.hypecycle.chzzk.connector.ChannelIdReader;
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
