PRAGMA foreign_keys = ON;

-- user table
CREATE TABLE IF NOT EXISTS user (
                                    id         INTEGER PRIMARY KEY AUTOINCREMENT,
                                    username   TEXT NOT NULL,
                                    email      TEXT NOT NULL UNIQUE,
                                    password   TEXT NOT NULL,
                                    role       INTEGER NOT NULL DEFAULT 0, -- 0=Normal,1=Admin
                                    created_at TEXT NOT NULL DEFAULT (datetime('now')),
                                    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

-- project table
CREATE TABLE IF NOT EXISTS project (
                                       id                  INTEGER PRIMARY KEY AUTOINCREMENT,
                                       name                TEXT NOT NULL,
                                       description         TEXT,
                                       owner_id            INTEGER NOT NULL,
                                       status              TEXT DEFAULT 'doing',
                                       expected_completion TEXT,
                                       created_at          TEXT NOT NULL DEFAULT (datetime('now')),
                                       updated_at          TEXT NOT NULL DEFAULT (datetime('now')),
                                       FOREIGN KEY (owner_id) REFERENCES user(id) ON DELETE CASCADE
);

-- project member table
CREATE TABLE IF NOT EXISTS project_member (
                                              id           INTEGER PRIMARY KEY AUTOINCREMENT,
                                              project_id   INTEGER NOT NULL,
                                              user_id      INTEGER NOT NULL,
                                              role         TEXT NOT NULL,
                                              status       TEXT DEFAULT 'To-Do',
                                              working_hour INTEGER DEFAULT 0,
                                              joined_at    TEXT NOT NULL DEFAULT (datetime('now')),
                                              FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
                                              FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
                                              UNIQUE (project_id, user_id)
);

-- requirement table
CREATE TABLE IF NOT EXISTS requirement (
                                           id           INTEGER PRIMARY KEY AUTOINCREMENT,
                                           project_id   INTEGER NOT NULL,
                                           title        TEXT NOT NULL,
                                           description  TEXT,
                                           status       TEXT DEFAULT 'backlog',
                                           assignee_id  INTEGER,
                                           created_at   TEXT NOT NULL DEFAULT (datetime('now')),
                                           updated_at   TEXT NOT NULL DEFAULT (datetime('now')),
                                           FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
                                           FOREIGN KEY (assignee_id) REFERENCES user(id)
);
