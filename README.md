# 실시간 채팅 분석
- 실시간 채팅 수집 및 분석 파이프라인을 구현합니다. CHZZK 플랫폼의 WebSocket을 통해 채팅 메시지를 수신하여 버퍼에 저장하고, 비동기로 배치 처리하여 Ollama LLM을 이용한 감정 분석을 수행합니다.

## 아키텍처
<img width="600" height="400" alt="image" src="https://github.com/user-attachments/assets/50c2b637-c91e-467f-badf-4aeb860d11c2" />
<img width="1024" height="675" alt="1차mvp 다이어그램" src="https://github.com/user-attachments/assets/76a89b31-817f-41b1-a313-df451084b804" />


## 주요 기능:
- CHZZK 플랫폼 WebSocket 연결
- 실시간 채팅 메시지 수집 및 버퍼링
- 비동기 배치 처리
- Ollama LLM(exaone 3.5 7b)을 이용한 감정 분석
