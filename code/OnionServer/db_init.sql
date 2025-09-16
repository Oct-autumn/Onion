CREATE TABLE IF NOT EXISTS user
(
    id        BIGINT AUTO_INCREMENT NOT NULL,
    username  VARCHAR(50)           NOT NULL,
    email     VARCHAR(100)          NULL,
    `role`    INT                   NOT NULL,
    create_at datetime              NULL,
    update_at datetime              NULL,
    CONSTRAINT pk_user PRIMARY KEY (id)
);

CREATE UNIQUE INDEX IF NOT EXISTS user_email_u_index ON user (email);