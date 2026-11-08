CREATE TABLE review (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id   BIGINT        NOT NULL,
    member_id    BIGINT        NOT NULL,
    author_name  VARCHAR(100)  NOT NULL,
    rating       INT           NOT NULL,
    content      VARCHAR(2000) NOT NULL,
    image_url    VARCHAR(500)  NULL,
    hidden       BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at   DATETIME(6)   NOT NULL,
    updated_at   DATETIME(6)   NOT NULL,
    INDEX idx_review_product (product_id, hidden)
);
