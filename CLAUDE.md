# 토닥 백엔드 프로젝트 가이드

## 1. 프로젝트 개요

토닥 백엔드는 모바일 클라이언트를 위한 Spring Boot 4.0.6 + Kotlin 2.2.21 서버다.

- **인증 방식**: Google ID Token Flow (소셜 로그인만 지원, 자체 이메일/비번 가입 없음)
- **세션**: JWT(Access + Refresh) 회전 + 멀티 디바이스 + 탈취 감지, Stateless
- **DB**: PostgreSQL 단일 (로컬/운영 모두), Flyway 마이그레이션 (`ddl-auto=validate`)
- **아키텍처**: 헥사고날 (Ports & Adapters) — `domain` / `application` / `adapter` 3단 분리

## 2. 필수: 코드 작성/수정 전 skill 확인

코드를 작성하거나 수정하기 전에 **반드시** 해당 영역의 skill을 읽고 규칙을 따라야 한다. skill을 읽지 않고 코드를 작성하는 것은 금지한다.

| 작업 영역 | 반드시 읽을 skill |
|-----------|------------------|
| 모든 Kotlin 코드 (네이밍·Boolean·ktlint) | `code-convention` |
| domain 패키지 (model, vo, enum) | `domain-model` |
| application 패키지 (service, port, dto) | `usecase` |
| adapter/restIn (controller, dto, mapper) | `rest-controller` |
| adapter/jpaOut (entity, repository, adapter) | `jpa-entity` |
| 매퍼 파일 (REST/JPA/Application 모두) | `mapper` |
| 테스트 코드 (Kotest BehaviorSpec) | `testing` |
| 새 기능 추가 · 리팩토링 · 아키텍처 검토 | `architecture` |
| Gradle Convention Plugin · 의존성 추가 | `setup-gradle` |

## 3. 필수 테스트 대상

다음 레이어의 코드를 작성/수정할 때는 **반드시** 단위 테스트를 함께 작성해야 한다.

| 레이어 | 대상 | 테스트 방식 |
|--------|------|-------------|
| `application/service/` | UseCase | OutPort 모킹, BehaviorSpec |
| `**/utils/` | 유틸리티 | 순수 함수 테스트, BehaviorSpec |
| `domain/` | 도메인 모델, VO, enum | 순수 로직 테스트, BehaviorSpec |

테스트 없이 위 레이어의 코드를 완료 처리하지 않는다.

## 4. 커밋 메시지 규칙

- **형식**: `{type}({브랜치명}): 제목`
- **type 종류**: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`, `build`, `ci`, `perf`
- **예시**: 브랜치 `feature/google-login`이면 → `feat(feature/google-login): JWT 어댑터 구현`
