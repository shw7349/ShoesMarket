CREATE TABLE drop_stock (
    drop_id    BIGINT  NOT NULL PRIMARY KEY,
    remaining  INT     NOT NULL,
    version    BIGINT  NOT NULL DEFAULT 0
);
