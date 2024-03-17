-- liquibase formatted sql

-- changeset eflerrr:init_tracking_table
CREATE TABLE "Tracking"
(
    "id"      bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    "chat_id" bigint NOT NULL,
    "link_id" bigint NOT NULL,
    FOREIGN KEY ("chat_id") REFERENCES "Chat" ("id") ON DELETE CASCADE,
    FOREIGN KEY ("link_id") REFERENCES "Link" ("id") ON DELETE CASCADE
);

-- rollback DROP TABLE "Tracking";
