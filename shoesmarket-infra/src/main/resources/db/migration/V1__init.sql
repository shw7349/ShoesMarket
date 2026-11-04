CREATE TABLE product (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    brand       VARCHAR(100) NOT NULL,
    price       BIGINT       NOT NULL,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL
);

CREATE TABLE drop_event (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id      BIGINT       NOT NULL,
    total_stock     INT          NOT NULL,
    per_user_limit  INT          NOT NULL DEFAULT 1,
    open_at         DATETIME(6)  NOT NULL,
    close_at        DATETIME(6)  NOT NULL,
    status          VARCHAR(20)  NOT NULL,
    created_at      DATETIME(6)  NOT NULL,
    updated_at      DATETIME(6)  NOT NULL,
    INDEX idx_drop_status_open_at (status, open_at)
);

CREATE TABLE reservation (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    drop_id     BIGINT       NOT NULL,
    user_id     BIGINT       NOT NULL,
    status      VARCHAR(20)  NOT NULL,
    expires_at  DATETIME(6)  NOT NULL,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,
    UNIQUE KEY uk_reservation_drop_user (drop_id, user_id),
    INDEX idx_reservation_status_expires (status, expires_at)
);

CREATE TABLE orders (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_id  BIGINT       NOT NULL,
    user_id         BIGINT       NOT NULL,
    drop_id         BIGINT       NOT NULL,
    amount          BIGINT       NOT NULL,
    status          VARCHAR(20)  NOT NULL,
    created_at      DATETIME(6)  NOT NULL,
    updated_at      DATETIME(6)  NOT NULL,
    UNIQUE KEY uk_orders_reservation (reservation_id)
);

CREATE TABLE payment (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id         BIGINT       NULL,
    reservation_id   BIGINT       NOT NULL,
    idempotency_key  VARCHAR(100) NOT NULL,
    pg_payment_key   VARCHAR(200) NULL,
    pg_order_id      VARCHAR(100) NOT NULL,
    amount           BIGINT       NOT NULL,
    status           VARCHAR(20)  NOT NULL,
    approved_at      DATETIME(6)  NULL,
    failure_reason   VARCHAR(500) NULL,
    created_at       DATETIME(6)  NOT NULL,
    updated_at       DATETIME(6)  NOT NULL,
    UNIQUE KEY uk_payment_idempotency (idempotency_key),
    UNIQUE KEY uk_payment_pg_key (pg_payment_key),
    INDEX idx_payment_status_created (status, created_at)
);
