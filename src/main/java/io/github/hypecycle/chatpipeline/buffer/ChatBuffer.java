package io.github.hypecycle.chatpipeline.buffer;

import io.github.hypecycle.chatpipeline.domain.ChatMessage;
import java.util.Collection;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import org.springframework.stereotype.Component;

@Component
public class ChatBuffer {

    private final BlockingDeque<ChatMessage> queue = new LinkedBlockingDeque<>();

    public void produce(ChatMessage chatMessage) {
        queue.offer(chatMessage);
    }

    public ChatMessage take() throws InterruptedException {
        return queue.take();
    }

    public void drainTo(Collection<? super ChatMessage> list, int maxSize) {
        queue.drainTo(list, maxSize);
    }
}
