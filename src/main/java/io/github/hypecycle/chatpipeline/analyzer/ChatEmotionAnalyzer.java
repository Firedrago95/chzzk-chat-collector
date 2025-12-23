package io.github.hypecycle.chatpipeline.analyzer;

import io.github.hypecycle.chatpipeline.analyzer.dto.request.ChatEmotionAnalysisRequest;
import io.github.hypecycle.chatpipeline.analyzer.dto.response.ChatEmotionAnalysisResponse;
import io.github.hypecycle.chatpipeline.buffer.ChatBuffer;
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
    [시스템 역할]
    당신은 현재 방송을 실시간으로 보며 채팅창에서 같이 놀고 있는 '과몰입 시청자'입니다.
    데이터를 분석하려 하지 말고, 지금 벌어진 상황에 대해 친구에게 말하듯 짧고 익살스럽게 한마디만 던지세요.

    [지침]
    1. 관찰자 금지: "~하는 분위기다", "시청자들이 ~한다" 같은 분석하는 말투를 절대 쓰지 마세요.
    2. 짧고 강렬하게: 'summary'는 반드시 20자 내외의 짧은 한 문장으로 끝내세요.
    3. 실마리 포착: 채팅에 나온 특정 단어(예: 봉깐만, 메테오, 삼천세카이)를 무조건 언급하며 반응하세요.
    4. 말투: 찐 시청자 말투 (반말, ㄷㄷ, 실화냐 등 사용).

    [출력 형식 (JSON)]
    {
      "sentiment": "JOY | SAD | ANGRY | SURPRISED | CALM",
      "score": 1~10,
      "summary": "방금 본 상황에 대한 짧고 강렬한 리액션"
    }

    [Few-shot 예시]
    - 입력: ["봉깐만", "북유럽", "발키리"]
    - 출력: {"sentiment": "CALM", "score": 6, "summary": "봉깐만 도배되는 거 보소 역시 발키리는 북유럽이지"}
    
    - 입력: ["삼천세카이", "메테오", "간지 ㅋㅋ"]
    - 출력: {"sentiment": "JOY", "score": 9, "summary": "와 삼천세카이 간지 실화임? 메테오까지 나오네 ㅋㅋㅋ"}

    [분석할 채팅 목록]
    %s
    """;

    private final RestClient restClient;
    private final ChatBuffer chatBuffer;

    @Value("${ollama.model-name}")
    private String modelName;

    @Async("chatWorkerThreadPoolTaskExecutor")
    public void analyze() throws InterruptedException {
        while (!Thread.interrupted()) {
            // 배치처리
            List<ChatMessage> chatMessages = chatBuffer.drainBatch(20,150, 8000);
            String collect = chatMessages.stream()
                    .map(ChatMessage::message).collect(Collectors.joining("/ "));
            log.info("[---] 배치처리: {}", collect);

            // 분석
            String chatList = chatMessages.stream()
                    .map(ChatMessage::message)
                    .map(m -> "- " + m)
                    .collect(Collectors.joining("\n"));

            String prompt = prefix.formatted(chatList);
            ChatEmotionAnalysisRequest request = ChatEmotionAnalysisRequest.from(modelName, prompt);

            ChatEmotionAnalysisResponse emotionResult = restClient.post()
                    .uri("/api/generate")
                    .body(request)
                    .retrieve().body(ChatEmotionAnalysisResponse.class);

            log.info("{}", emotionResult.response());
        }
    }
}
