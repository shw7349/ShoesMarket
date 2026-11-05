CREATE TABLE consistency_report (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    drop_id          BIGINT        NOT NULL,
    total_stock      INT           NOT NULL,
    confirmed_count  BIGINT        NOT NULL,
    reserved_count   BIGINT        NOT NULL,
    redis_remaining  INT           NOT NULL,
    consistent       BOOLEAN       NOT NULL,
    detail           VARCHAR(1000) NOT NULL,
    checked_at       DATETIME(6)   NOT NULL,
    created_at       DATETIME(6)   NOT NULL,
    updated_at       DATETIME(6)   NOT NULL,
    INDEX idx_consistency_drop (drop_id)
);
