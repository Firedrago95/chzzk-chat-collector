package io.github.hypecycle.chatpipeline.analyzer;

import io.github.hypecycle.chatpipeline.buffer.ChatBuffer;
import io.github.hypecycle.chatpipeline.domain.ChatMessage;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Order(2)
@Service
@RequiredArgsConstructor
public class ChatAnalyzeService implements CommandLineRunner {

    private final ChatBuffer chatBuffer;
    private final ChatEmotionAnalyzer chatEmotionAnalyzer;

    @Override
    public void run(String... args) throws Exception {
        chatEmotionAnalyzer.analyze();
    }

//    @Async("chatManagerThreadPoolTaskExecutor")
//    public void analyze() throws InterruptedException {
//        while (!Thread.interrupted()) {
//            List<ChatMessage> chatMessages = chatBuffer.drainBatch(30, 1000);
//
//            String collects = chatMessages.stream()
//                    .map(ChatMessage::message)
//                    .collect(Collectors.joining("/"));
//            log.info("[ .. ] 배치 처리: {}", collects);
//            chatEmotionAnalyzer.analyze(chatMessages);
//        }
//    }
}
