-- Refresh Token 테이블.
--
-- 정책 요약:
--   - 평문 토큰은 DB 에 저장하지 않는다 — SHA-256 해시(`token_hash`)만 저장하고 평문은 발급 시점에만 클라이언트에 전달.
--   - 발급 단위: (member_id, device_id). 같은 디바이스에서 재로그인 시 기존 활성 토큰은 회수(revoke) 후 신규 발급.
--   - 회전(rotate): 새 토큰의 `rotated_from_id` 가 직전 토큰을 가리킨다. 이미 revoke 된 토큰으로 refresh 요청이 들어오면 탈취로 간주.
--   - 폐기(revoke): `revoked_at` 단방향 채움 — 한 번 채워지면 비울 수 없다.
CREATE TABLE IF NOT EXISTS refresh_token (
    id                UUID         NOT NULL PRIMARY KEY,
    member_id         UUID         NOT NULL,
    token_hash        VARCHAR(128) NOT NULL,
    device_id         VARCHAR(255) NOT NULL,
    device_label      VARCHAR(255) NULL,
    issued_at         TIMESTAMP    NOT NULL,
    expires_at        TIMESTAMP    NOT NULL,
    revoked_at        TIMESTAMP    NULL,
    rotated_from_id   UUID         NULL,
    CONSTRAINT fk_refresh_token_member
        FOREIGN KEY (member_id) REFERENCES member (id),
    CONSTRAINT uq_refresh_token_token_hash
        UNIQUE (token_hash)
);

COMMENT ON TABLE  refresh_token                  IS 'Refresh Token (디바이스 단위 세션)';
COMMENT ON COLUMN refresh_token.id               IS 'Refresh Token ID (UUID v7)';
COMMENT ON COLUMN refresh_token.member_id        IS '회원 ID (FK -> member.id)';
COMMENT ON COLUMN refresh_token.token_hash       IS '평문 토큰의 SHA-256 해시 (HEX). 평문은 저장하지 않는다.';
COMMENT ON COLUMN refresh_token.device_id        IS '클라이언트 디바이스 식별자 (UUID 등)';
COMMENT ON COLUMN refresh_token.device_label     IS '사용자/관리자가 인식할 수 있는 디바이스 라벨';
COMMENT ON COLUMN refresh_token.issued_at        IS '발급 시각';
COMMENT ON COLUMN refresh_token.expires_at       IS '만료 시각 (now + refresh-ttl)';
COMMENT ON COLUMN refresh_token.revoked_at       IS '폐기 시각 (NULL 이면 활성). 한 번 채워지면 비울 수 없다.';
COMMENT ON COLUMN refresh_token.rotated_from_id  IS '회전 직전 토큰의 ID (자기 참조). 탈취 감지에 사용.';

-- (member_id, device_id) 로 같은 디바이스의 활성 토큰을 조회한다.
CREATE INDEX IF NOT EXISTS ix_refresh_token_member_device
    ON refresh_token (member_id, device_id);

-- member_id 로 한 회원의 모든 토큰을 일괄 회수할 때 사용.
CREATE INDEX IF NOT EXISTS ix_refresh_token_member_id
    ON refresh_token (member_id);
