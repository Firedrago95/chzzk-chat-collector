package io.github.hypecycle.chatpipeline.buffer;

import io.github.hypecycle.chatpipeline.domain.ChatMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import org.springframework.stereotype.Component;

@Component
public class ChatBuffer {

    private final BlockingDeque<ChatMessage> queue = new LinkedBlockingDeque<>();

    public void produce(ChatMessage chatMessage) {
        queue.offer(chatMessage);
    }

    public List<ChatMessage> drainBatch(int maxSize, long timeoutMs) throws InterruptedException {
        List<ChatMessage> tempBatch = new ArrayList<>();
        tempBatch.add(queue.take());

        long deadLine = System.currentTimeMillis() + timeoutMs;
        while (tempBatch.size() < maxSize) {
            long remaining = deadLine - System.currentTimeMillis();
            if (remaining < 0) break;

            ChatMessage next = queue.poll();
            if (next == null) break;
            tempBatch.add(next);
            if (!queue.isEmpty()) {
                queue.drainTo(tempBatch, maxSize);
            }
        }
        return List.copyOf(tempBatch);
    }
}
