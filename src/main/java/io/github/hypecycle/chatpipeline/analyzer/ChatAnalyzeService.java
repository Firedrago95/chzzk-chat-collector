package io.github.hypecycle.chatpipeline.analyzer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Slf4j
@Order(2)
@Service
@RequiredArgsConstructor
public class ChatAnalyzeService implements CommandLineRunner {

    private final ChatEmotionAnalyzer chatEmotionAnalyzer;

    @Override
    public void run(String... args) throws Exception {
        chatEmotionAnalyzer.analyze();
    }
}
