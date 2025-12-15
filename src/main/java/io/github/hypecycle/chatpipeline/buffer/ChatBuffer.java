package io.github.hypecycle.chatpipeline.buffer;

import io.github.hypecycle.chatpipeline.domain.ChatMessage;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import org.springframework.stereotype.Component;

@Component
public class ChatBuffer {

    private final BlockingDeque<ChatMessage> queue = new LinkedBlockingDeque<>();

    public void produce(ChatMessage chatMessage) {
        queue.offer(chatMessage);
    }

    public ChatMessage poll() {
        return queue.poll();
    }
}
