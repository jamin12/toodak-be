# 기술 흐름 문서 (toodak-be)

이 폴더는 **"코드만 봐서는 한눈에 안 잡히는 기술 흐름"** 을 정리한다.
비즈니스/기획 결정은 [`../plans/`](../plans/) 에, 코딩 컨벤션은 [`../../.claude/skills/`](../../.claude/skills/) 에.

여기에 들어갈 후보:
- 외부 프로토콜 (OIDC/OAuth, Webhook, 결제 PG 등) 의 시퀀스 다이어그램
- 비동기 패턴 (Outbox, Saga, Idempotency Key, At-least-once 처리)
- 보안 정책 (JWT 회전, 탈취 감지, 권한 모델, Rate limit)
- 트랜잭션/일관성 경계 (한 트랜잭션 범위, 멱등성, 동시성 race 처리)
- 운영/관측 (로깅 컨벤션, 메트릭, 장애 대응 절차)

## 인덱스

| 문서 | 다루는 내용 |
|---|---|
| [google-oidc-login.md](google-oidc-login.md) | Google ID Token Flow 로그인/가입/자동 연결 전체 시퀀스 + 분기 정책 |
| (예정) refresh-token-rotation.md | RefreshToken 회전 + 탈취 감지 시퀀스 (Phase 4) |
| (예정) jwt-auth-filter.md | Spring Security 필터 체인 + JwtAuthenticationFilter 동작 |

## 작성 원칙

- **다이어그램 먼저**: mermaid 시퀀스/플로우 다이어그램으로 전체 흐름을 먼저 보여준다.
- **코드 좌표 명시**: 각 단계마다 `file:line` 또는 `ClassName.method` 로 코드와 연결한다.
- **분기/예외 정책**: 정상 경로뿐 아니라 거부/오류 경로도 함께 적는다.
- **트랜잭션/멱등성 경계**: "어디까지 한 트랜잭션인가", "재시도 시 어떻게 되는가" 를 명시한다.
- **시간이 지나도 의미 있도록**: "지금 무엇을 했다" 가 아니라 "왜 이렇게 흐른다" 를 적는다.
