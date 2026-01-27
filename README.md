# EmotionSync (CapStone)
감정을 이해하고, 공감할 수 있는 콘텐츠와 사람을 연결하는 모바일 서비스

EmotionSync는 사용자의 감정/설문 데이터를 기반으로 영화·음악·영상 콘텐츠를 추천하고, 친구와 공유·채팅까지 이어지는 **감정 기반 소셜 추천 플랫폼**입니다.  
Android 앱, Spring Boot 백엔드, Python(Flask) 추천 서비스가 유기적으로 연결된 3인 팀 프로젝트입니다.

---

## 프로젝트 한눈에 보기
- **문제 정의**: 감정 상태에 맞는 콘텐츠를 빠르게 찾기 어렵고, 추천 경험이 개인화되어 있지 않음
- **해결 방식**: 감정 설문 → AI 감정 분류 → TMDB/YouTube 기반 콘텐츠 추천 → 친구와 공유/채팅
- **팀 규모**: 3인 팀 프로젝트 (리더: 본인)

---

## 핵심 기능
- **감정 설문/기록**: 설문 결과와 감정 기록 저장 및 조회
- **AI 감정 분류 + 콘텐츠 추천**
  - LSTM 기반 감정 분류 모델
  - TMDB(영화) + YouTube(음악/영상) 추천
  - 한국/영문 콘텐츠 동시 추천 및 중복 제거
- **소셜 기능**
  - 친구 추가/초대코드/요청 수락
  - 콘텐츠 공유, 좋아요/싫어요, 매칭률(취향 일치도)
- **실시간 채팅**
  - WebSocket 기반 1:1 채팅
  - 로컬(Room) 저장으로 채팅 기록 유지
- **계정/보안**
  - 회원가입/로그인, JWT 인증
  - Google/Kakao 소셜 로그인
  - 아이디/비밀번호 찾기 및 변경
- **딥링크**
  - 공유 콘텐츠를 앱 내 상세 화면으로 바로 연결

---

## 시스템 아키텍처 (요약)
```
[Android App]
   |  REST (Retrofit) / WebSocket
   v
[Spring Boot API]  <->  [MariaDB]
   |
   |  HTTP
   v
[Flask Recommendation Service]
   |  TensorFlow 모델 + 감정 데이터셋
   v
[TMDB / YouTube API]
```

---

## AI/추천 파이프라인
- **학습 데이터**: `emotion_dataset.csv`
- **모델 학습 스크립트**: `create_model_files.py`
- **모델 산출물**: `final_emotion_model.h5`, `tokenizer.pickle`, `label_encoder.pickle`
- **추천 로직**: `recommend_service.py`
  - 감정 코드 → 키워드/장르 매핑
  - TMDB/YouTube API 호출
  - 결과 필터링 및 중복 제거

---

## 기술 스택
**모바일(Android)**
- Java/Kotlin, ViewBinding
- Retrofit, OkHttp, Gson
- Room (로컬 저장)
- EncryptedSharedPreferences (JWT 보안 저장)
- Glide, Material Components
- Google Sign-In, Kakao SDK

**백엔드**
- Spring Boot 3.2.4, Java 21
- Spring Security, JWT
- Spring WebSocket
- Spring Data JPA
- MariaDB

**AI/추천 서비스**
- Python, Flask
- TensorFlow/Keras, scikit-learn, pandas
- TMDB API, YouTube Data API

---

## 주요 도메인/데이터 모델
- **User, Friend, InviteCode**
- **EmotionRecord, EmotionSurveyRecord**
- **ChatMessage, ChatRoom**
- **Share, Notification, UserPreferenceMatch**

---

## 내 역할 (팀 리더 / 기여도 약 70%)
- **기획·설계 리딩**: 프로젝트 목표/요구사항 정의, 전체 사용자 흐름 설계
- **아키텍처 설계**: Android ↔ Spring ↔ Flask 연동 흐름 설계 및 구현 방향 제시
- **백엔드 개발 대부분 담당**
  - 회원/인증/JWT, 친구/공유/매칭/알림 API 설계 및 구현
  - DB 모델링 및 서비스 로직 구현
- **모바일 앱 개발 대부분 담당**
  - 주요 화면/UX 흐름 설계 및 구현
  - 추천/공유/친구/알림 기능 연동
- **협업 파트(팀원 담당)**
  - AI 모델 학습 파이프라인
  - WebSocket 기반 채팅 핵심 구현
  - Flask–Spring 연동부

---

## 프로젝트 구조
```
CapStoneRepo/
├─ EmotionSyncServer/        # Spring Boot 백엔드 + Python 추천 서비스
│  ├─ src/main/java/...       # API, 인증, 도메인 로직
│  ├─ app.py                  # Flask 추천 API
│  ├─ recommend_service.py    # 추천 로직
│  ├─ create_model_files.py   # 모델 학습 스크립트
│  └─ emotion_dataset.csv     # 감정 데이터셋
└─ capstone/                  # Android 앱
   └─ app/src/main/...        # 주요 UI 및 기능 구현
```

---

## 실행 방법 (요약)
1) **환경 변수 설정**
   - `EmotionSyncServer/.env.example` → `.env`
   - `EmotionSyncServer/src/main/resources/application.properties`
2) **Spring Boot 실행**  
3) **Flask 추천 서비스 실행** (`app.py`)  
4) **Android 앱 실행**

> 민감 정보(API Key/DB 계정 등)는 저장소에 포함되지 않습니다.

---

## 한 줄 요약
**감정 데이터를 기반으로 콘텐츠 추천과 소셜 커뮤니케이션을 연결한 AI 기반 모바일 서비스**
