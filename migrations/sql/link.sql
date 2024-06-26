-- liquibase formatted sql

-- changeset eflerrr:init_link_table
CREATE TABLE "Link"
(
    "id"         bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    "url"        text                     NOT NULL UNIQUE,
    "created_at" timestamp with time zone NOT NULL,
    "checked_at" timestamp with time zone NOT NULL,
    "updated_at" timestamp with time zone NOT NULL
);

-- rollback DROP TABLE "Link";
