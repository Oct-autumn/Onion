
PRAGMA foreign_keys = ON;

-- user 表
CREATE TABLE user (
                      id         INTEGER PRIMARY KEY AUTOINCREMENT,
                      username   TEXT NOT NULL,
                      email      TEXT NOT NULL UNIQUE,
                      password   TEXT NOT NULL,
                      role       INTEGER NOT NULL DEFAULT 0, -- 0=Normal,1=Admin
                      created_at TEXT NOT NULL DEFAULT (datetime('now')),
                      updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

-- user_auth 表

CREATE TABLE user_auth (
                            id INTEGER PRIMARY KEY,
                            hash VARCHAR(256) NOT NULL,
                            salt VARCHAR(32) NOT NULL,
                            FOREIGN KEY (id) REFERENCES user(id)
);

-- project 表（注意时间字段统一成 created_at / updated_at）
CREATE TABLE project (
                         id INTEGER PRIMARY KEY AUTOINCREMENT,
                         name TEXT NOT NULL,
                         description TEXT,
                         expected_completion TEXT,
                         owner_id INTEGER NOT NULL,
                         created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                         updated_at TEXT DEFAULT CURRENT_TIMESTAMP,
                         FOREIGN KEY (owner_id) REFERENCES user(id)
);

-- project_member 表
CREATE TABLE project_member (
                                id INTEGER PRIMARY KEY AUTOINCREMENT,
                                project_id INTEGER NOT NULL,
                                user_id INTEGER NOT NULL,
                                role TEXT NOT NULL,       -- 开发者/测试员/项目经理
                                status TEXT,              -- To-Do / In-Process
                                working_hour TEXT,        -- 预估工时
                                FOREIGN KEY (project_id) REFERENCES project(id),
                                FOREIGN KEY (user_id) REFERENCES user(id)
);
