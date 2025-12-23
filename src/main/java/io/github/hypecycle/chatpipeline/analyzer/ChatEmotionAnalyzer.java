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
            너는 대한민국 실시간 스트리밍 플랫폼 '치지직'의 채팅 분석 전문가야.
            시청자들의 채팅을 보고 방송의 전체적인 분위기를 파악해야 해.
            
            [감정 분류 정의] 오직 다음 5가지 카테고리 중 하나만 선택해:
            - JOY: 웃음(ㅋㅋ), 환호, 즐거운 상황
            - HYPE: 와!, 캬, 지렸다, 대박, 슈퍼플레이에 대한 열광적인 반응.
            - SURPRISE: 놀람(헉, ㄷㄷ), 예상치 못한 전개
            - ANGER: 불만(아), 답답함, 비난
            - NEUTRAL: 단순 정보 공유, 인사, 감정이 없는 대화
            
            [제약 사항]
            - 출력은 반드시 JSON 형식이어야 함.
            - 출력형식은 '{' 로 시작하고 '}' 끝나되, 그 외의 정보는 어떠한 것도 없어야 함.
            - 입력된 채팅의 갯수와 상관없이 반드시 단 하나의 JSON 형식으로 나타내야 함.
            - 다른 설명이나 텍스트는 절대로 포함하지 마.
            - sentiment 값은 위 5가지 영문 대문자 중 하나여야 함.
            - score는 1~10 사이의 정수.
            - summary는 시청자들이 스트리머에게 할 감정표현으로 한국어 1문장으로 짧게 표현해줘.
            
            [분석할 채팅 목록]
            ${chatList}
            
            [Few-shot 예시] 
            - 입력: ["헉", "이게 뭐야", "시조의 거인 ㄷㄷ"] 
            - 출력: {"sentiment": "SURPRISE", "score": 9, "summary": "갑작스러운 시조의 거인 언급에 시청자들이 크게 놀란 상태입니다."}
            """;

    private final RestClient restClient;
    private final ChatBuffer chatBuffer;

    @Value("${ollama.model-name}")
    private String modelName;

    @Async("chatWorkerThreadPoolTaskExecutor")
    public void analyze() throws InterruptedException {
        while (!Thread.interrupted()) {
            // 배치처리
            List<ChatMessage> chatMessages = chatBuffer.drainBatch(50, 10000);
            String collect = chatMessages.stream()
                    .map(ChatMessage::message).collect(Collectors.joining("/ "));
            log.info("[---] 배치처리: {}", collect);

            // 분석
            String chatList = chatMessages.stream()
                    .map(ChatMessage::message)
                    .map(m -> "- " + m)
                    .collect(Collectors.joining("\n"));

            String prompt = prefix.replace("${chatList}", chatList);
            ChatEmotionAnalysisRequest request = ChatEmotionAnalysisRequest.from(modelName, prompt);

            ChatEmotionAnalysisResponse emotionResult = restClient.post()
                    .uri("/api/generate")
                    .body(request)
                    .retrieve().body(ChatEmotionAnalysisResponse.class);

            String response = emotionResult.response();
            int start = response.indexOf("{");
            int end = response.indexOf("}");
            String substring = response.substring(start, end + 1);
            log.info("{}", substring);
        }
    }
}
