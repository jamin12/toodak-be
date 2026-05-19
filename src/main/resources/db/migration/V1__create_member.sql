-- 회원 테이블.
--
-- 식별자는 UUID v7 (도메인에서 생성, JPA insert 전 확정).
-- 이메일은 부가 정보이므로 UNIQUE 제약을 두지 않는다 — 식별은 social_identity 의
-- (provider, provider_user_id) 조합으로 수행한다.
-- 상태(status): ACTIVE, WITHDRAWN. soft delete 정책 (탈퇴해도 row 보존).
CREATE TABLE IF NOT EXISTS member (
    id            UUID         NOT NULL PRIMARY KEY,
    email         VARCHAR(320) NOT NULL,
    status        VARCHAR(20)  NOT NULL,
    created_at    TIMESTAMP    NOT NULL,
    withdrawn_at  TIMESTAMP    NULL
);

COMMENT ON TABLE  member               IS '회원';
COMMENT ON COLUMN member.id            IS '회원 ID (UUID v7)';
COMMENT ON COLUMN member.email         IS '회원 이메일 (부가 정보, UNIQUE 아님)';
COMMENT ON COLUMN member.status        IS '회원 상태 (ACTIVE | WITHDRAWN)';
COMMENT ON COLUMN member.created_at    IS '가입 시각';
COMMENT ON COLUMN member.withdrawn_at  IS '탈퇴 시각 (NULL 이면 ACTIVE)';

-- email 은 조회용 보조 인덱스 (UNIQUE 아님)
CREATE INDEX IF NOT EXISTS ix_member_email ON member (email);
