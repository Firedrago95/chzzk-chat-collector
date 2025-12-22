package io.github.hypecycle.chatpipeline.global.config;

import java.net.http.HttpClient;
import java.net.http.HttpClient.Builder;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class AiConfig {

    @Value("${ollama.base-url}")
    private String ollamaUrl;

    @Value("${ollama.connect-timeout}")
    private Duration connectTimeout;

    @Value("${ollama.read-timeout}")
    private Duration readTimeout;

    @Bean
    public JdkClientHttpRequestFactory jdkClientHttpRequestFactory() {
        Builder builder = HttpClient.newBuilder();
        builder.connectTimeout(connectTimeout);
        HttpClient httpClient = builder.build();

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(readTimeout);
        return factory;
    }

    @Bean
    public RestClient restClient(JdkClientHttpRequestFactory jdkClientHttpRequestFactory) {
        return RestClient.builder()
                .requestFactory(jdkClientHttpRequestFactory)
                .baseUrl(ollamaUrl)
                .build();
    }
}
