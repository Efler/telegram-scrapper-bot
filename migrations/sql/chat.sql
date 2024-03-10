-- liquibase formatted sql

-- changeset eflerrr:init_chat_table
CREATE TABLE Chat
(
    id         bigint                   NOT NULL,
    username   text                     NOT NULL,
    created_at timestamp with time zone NOT NULL,
    PRIMARY KEY (id)
);

-- rollback DROP TABLE Chat;
