package io.github.hypecycle.chatpipeline.analyzer;

import io.github.hypecycle.chatpipeline.analyzer.dto.request.ChatEmotionAnalysisRequest;
import io.github.hypecycle.chatpipeline.analyzer.dto.response.ChatEmotionAnalysisResponse;
import io.github.hypecycle.chatpipeline.domain.ChatMessage;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatEmotionAnalyzer {

    private static final String prefix = """ 
            다음 채팅 리스트를 분석해서 JSON 형식으로 응답해줘. score는 각 감정의 단계롤 10점 만점에 몇점인지 점수를 매겨줘. 다른 설명은 하지마
            형식: {\\"sentiment\\": \\"string\\", \\"score\\": int, \\"summary\\": \\"string\\"}
            
            [채팅 리스트]
            """;

    private final RestClient restClient;

    @Value("${ollama.model-name}")
    private String modelName;

    @Async("chatWorkerThreadPoolTaskExecutor")
    public void analyze(List<ChatMessage> buffer) {
        String chatPrompt = buffer.stream()
                .map(ChatMessage::message)
                .map(m -> "- " + m)
                .collect(Collectors.joining("\n"));

        String prompt = prefix + chatPrompt;

        ChatEmotionAnalysisRequest request = ChatEmotionAnalysisRequest.from(modelName, prompt);

        ChatEmotionAnalysisResponse emotionResult = restClient.post()
                .uri("/api/generate")
                .body(request)
                .retrieve().body(ChatEmotionAnalysisResponse.class);

        log.info("{}", emotionResult.response());
    }
}
