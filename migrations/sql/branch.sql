-- liquibase formatted sql

-- changeset eflerrr:init_branch_table
CREATE TABLE "Branch"
(
    "id"               bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    "link_id"          bigint                   NOT NULL,
    "repository_owner" text                     NOT NULL,
    "repository_name"  text                     NOT NULL,
    "branch_name"      text                     NOT NULL,
    "last_commit_time" timestamp with time zone NOT NULL,
    FOREIGN KEY ("link_id") REFERENCES "Link" ("id") ON DELETE CASCADE
);

-- rollback DROP TABLE "Branch";
