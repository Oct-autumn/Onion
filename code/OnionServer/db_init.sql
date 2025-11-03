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
                         id INTEGER PRIMARY KEY AUTOINCREMENT,
                         name TEXT NOT NULL,
                         description TEXT,
                         expected_completion TEXT,
                         owner_id INTEGER NOT NULL,
                         create_at TEXT DEFAULT CURRENT_TIMESTAMP,
                         update_at TEXT DEFAULT CURRENT_TIMESTAMP,
                         FOREIGN KEY (owner_id) REFERENCES user(id)
);


-- project member table
CREATE TABLE IF NOT EXISTS project_member (
                                id INTEGER PRIMARY KEY AUTOINCREMENT,
                                project_id INTEGER NOT NULL,
                                user_id INTEGER NOT NULL,
                                role TEXT NOT NULL,       -- 开发者/测试员/项目经理
                                status TEXT,              -- To-Do / In-Process
                                working_hour TEXT,        -- 预估工时
                                FOREIGN KEY (project_id) REFERENCES project(id),
                                FOREIGN KEY (user_id) REFERENCES user(id)
);

