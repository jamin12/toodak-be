-- 소셜 제공자 연결 테이블 (Member 1:N).
--
-- (provider, provider_user_id) 조합이 시스템 전체에서 UNIQUE.
--   - Google: sub claim
--   - Kakao: 회원번호
--   - Apple: sub claim
-- email_verified_at: 제공자가 이메일 소유를 검증한 시각 (자동 계정 연결 정책 판단용).
CREATE TABLE IF NOT EXISTS social_identity (
    id                 UUID         NOT NULL PRIMARY KEY,
    member_id          UUID         NOT NULL,
    provider           VARCHAR(20)  NOT NULL,
    provider_user_id   VARCHAR(255) NOT NULL,
    email_verified_at  TIMESTAMP    NULL,
    linked_at          TIMESTAMP    NOT NULL,
    CONSTRAINT fk_social_identity_member
        FOREIGN KEY (member_id) REFERENCES member (id),
    CONSTRAINT uq_social_identity_provider_user
        UNIQUE (provider, provider_user_id)
);

COMMENT ON TABLE  social_identity                   IS '소셜 제공자 연결';
COMMENT ON COLUMN social_identity.id                IS '소셜 연결 ID (UUID v7)';
COMMENT ON COLUMN social_identity.member_id         IS '회원 ID (FK -> member.id)';
COMMENT ON COLUMN social_identity.provider          IS '소셜 제공자 (GOOGLE | KAKAO | APPLE)';
COMMENT ON COLUMN social_identity.provider_user_id  IS '제공자가 발급한 영구 사용자 식별자';
COMMENT ON COLUMN social_identity.email_verified_at IS '제공자가 이메일을 검증한 시각 (자동 연결 정책 판단용)';
COMMENT ON COLUMN social_identity.linked_at         IS '연결 시각';

-- member_id 로 한 회원의 모든 소셜 연결을 조회할 때 사용
CREATE INDEX IF NOT EXISTS ix_social_identity_member_id ON social_identity (member_id);
