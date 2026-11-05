CREATE TABLE member (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    email         VARCHAR(200) NOT NULL,
    password      VARCHAR(200) NOT NULL,
    display_name  VARCHAR(100) NOT NULL,
    created_at    DATETIME(6)  NOT NULL,
    updated_at    DATETIME(6)  NOT NULL,
    UNIQUE KEY uk_member_email (email)
);
