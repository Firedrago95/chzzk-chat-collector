package io.github.hypecycle.chatpipeline.analyzer;

import io.github.hypecycle.chatpipeline.buffer.ChatBuffer;
import io.github.hypecycle.chatpipeline.domain.ChatMessage;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatAnalyzeService {

    private final ChatBuffer chatBuffer;
    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;

    public void analyze() throws InterruptedException {
        while (!Thread.interrupted()) {
            List<ChatMessage> batch = new ArrayList<>();
            ChatMessage chatMessage = chatBuffer.take();
            batch.add(chatMessage);
            chatBuffer.drainTo(batch, 30);
            List<ChatMessage> copied = List.copyOf(batch);
            threadPoolTaskExecutor.execute(() -> {});
        }
    }
}
