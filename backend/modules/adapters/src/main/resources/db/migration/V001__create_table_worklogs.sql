CREATE SCHEMA IF NOT EXISTS worklog;

CREATE TABLE IF NOT EXISTS worklog.worklogs (
    id               UUID                     NOT NULL PRIMARY KEY,
    issue_id         BIGINT                   NOT NULL,
    issue_key        TEXT                     NOT NULL,
    start_inclusive  TIMESTAMP                NOT NULL,
    end_inclusive    TIMESTAMP                NOT NULL,
    duration_seconds BIGINT                   NOT NULL,
    description      TEXT,
    is_exported      BOOLEAN                  NOT NULL DEFAULT FALSE,
    hash             TEXT                     NOT NULL,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL
);
