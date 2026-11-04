CREATE TABLE incident (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    category          VARCHAR(50)   NOT NULL,
    source            VARCHAR(200)  NOT NULL,
    message           VARCHAR(1000) NOT NULL,
    probable_cause    VARCHAR(1000) NOT NULL,
    suggested_action  VARCHAR(1000) NOT NULL,
    context           VARCHAR(2000) NOT NULL,
    notion_page_url   VARCHAR(500)  NULL,
    occurred_at       DATETIME(6)   NOT NULL,
    created_at        DATETIME(6)   NOT NULL,
    updated_at        DATETIME(6)   NOT NULL,
    INDEX idx_incident_category_occurred (category, occurred_at)
);
