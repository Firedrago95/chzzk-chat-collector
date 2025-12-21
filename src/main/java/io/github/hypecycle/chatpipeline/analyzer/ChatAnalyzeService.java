package io.github.hypecycle.chatpipeline.analyzer;

import io.github.hypecycle.chatpipeline.buffer.ChatBuffer;
import io.github.hypecycle.chatpipeline.domain.ChatMessage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatAnalyzeService implements CommandLineRunner {

    private final ChatBuffer chatBuffer;
    private final ChatEmotionAnalyzer chatEmotionAnalyzer;

    @Override
    public void run(String... args) throws Exception {
        analyze();
    }

    @Async("chatManagerThreadPoolTaskExecutor")
    public void analyze() throws InterruptedException {
        while (!Thread.interrupted()) {
            List<ChatMessage> chatMessages = chatBuffer.drainBatch(30, 1000);
            chatEmotionAnalyzer.analyze(chatMessages);
        }
    }
}
