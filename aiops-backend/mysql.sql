CREATE DATABASE IF NOT EXISTS aiops DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE aiops;

CREATE TABLE IF NOT EXISTS agents (
    agent_id VARCHAR(36) NOT NULL,
    hostname VARCHAR(255) NOT NULL,
    ip VARCHAR(255) NOT NULL,
    token VARCHAR(255) NOT NULL,
    registered_at DATETIME(6) NOT NULL,
    last_seen DATETIME(6) NULL,
    latest_metrics TEXT NULL,
    PRIMARY KEY (agent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS pairing_tokens (
    token VARCHAR(64) NOT NULL,
    hostname VARCHAR(255) NULL,
    ip VARCHAR(255) NULL,
    expires_at DATETIME(6) NOT NULL,
    used TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (token),
    KEY idx_pairing_tokens_expires_at (expires_at),
    KEY idx_pairing_tokens_used (used)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS agent_capabilities (
    agent_id VARCHAR(36) NOT NULL,
    capability VARCHAR(255) NOT NULL,
    KEY idx_agent_capabilities_agent_id (agent_id),
    CONSTRAINT fk_agent_capabilities_agent FOREIGN KEY (agent_id) REFERENCES agents (agent_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS approval_requests (
    approval_id VARCHAR(36) NOT NULL,
    agent_id VARCHAR(255) NOT NULL,
    command VARCHAR(255) NOT NULL,
    params TEXT NULL,
    reason TEXT NULL,
    status VARCHAR(255) NOT NULL,
    reviewer VARCHAR(255) NULL,
    created_at DATETIME(6) NOT NULL,
    reviewed_at DATETIME(6) NULL,
    PRIMARY KEY (approval_id),
    KEY idx_approval_requests_agent_id (agent_id),
    KEY idx_approval_requests_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS command_results (
    command_id VARCHAR(36) NOT NULL,
    agent_id VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    output TEXT NULL,
    timestamp DATETIME(6) NOT NULL,
    PRIMARY KEY (command_id),
    KEY idx_command_results_agent_id (agent_id),
    KEY idx_command_results_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS alerts (
    id VARCHAR(36) NOT NULL,
    hostname VARCHAR(255) NOT NULL,
    source VARCHAR(255) NOT NULL,
    metric VARCHAR(255) NOT NULL,
    severity VARCHAR(255) NOT NULL,
    current_value DOUBLE NOT NULL,
    threshold_value DOUBLE NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(255) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_alerts_hostname (hostname),
    KEY idx_alerts_status (status),
    KEY idx_alerts_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS knowledge_documents (
    id VARCHAR(36) NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    category VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_knowledge_documents_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS knowledge_keywords (
    knowledge_id VARCHAR(36) NOT NULL,
    keyword VARCHAR(255) NOT NULL,
    KEY idx_knowledge_keywords_knowledge_id (knowledge_id),
    KEY idx_knowledge_keywords_keyword (keyword),
    CONSTRAINT fk_knowledge_keywords_document FOREIGN KEY (knowledge_id) REFERENCES knowledge_documents (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS log_entries (
    id VARCHAR(36) NOT NULL,
    agent_id VARCHAR(255) NOT NULL,
    hostname VARCHAR(255) NOT NULL,
    level VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    metadata TEXT NULL,
    timestamp DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_log_entries_agent_id (agent_id),
    KEY idx_log_entries_hostname (hostname),
    KEY idx_log_entries_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO knowledge_documents (id, title, content, category)
SELECT 'doc-cpu-high', 'CPU 高负载排查', 'CPU 持续高于 90% 时优先检查高占用进程、最近发布和流量突增。', 'incident'
WHERE NOT EXISTS (SELECT 1 FROM knowledge_documents WHERE id = 'doc-cpu-high');

INSERT INTO knowledge_documents (id, title, content, category)
SELECT 'doc-nginx-restart', 'Nginx 重启案例', '历史案例表明 nginx worker 异常时重启服务可快速恢复。', 'playbook'
WHERE NOT EXISTS (SELECT 1 FROM knowledge_documents WHERE id = 'doc-nginx-restart');

INSERT INTO knowledge_documents (id, title, content, category)
SELECT 'doc-memory-alert', '内存告警处理', '内存超过 85% 时建议先抓取 top 和 GC 信息，再考虑扩容或重启。', 'playbook'
WHERE NOT EXISTS (SELECT 1 FROM knowledge_documents WHERE id = 'doc-memory-alert');

INSERT INTO knowledge_keywords (knowledge_id, keyword)
SELECT 'doc-cpu-high', 'cpu' WHERE NOT EXISTS (SELECT 1 FROM knowledge_keywords WHERE knowledge_id = 'doc-cpu-high' AND keyword = 'cpu');
INSERT INTO knowledge_keywords (knowledge_id, keyword)
SELECT 'doc-cpu-high', 'load' WHERE NOT EXISTS (SELECT 1 FROM knowledge_keywords WHERE knowledge_id = 'doc-cpu-high' AND keyword = 'load');
INSERT INTO knowledge_keywords (knowledge_id, keyword)
SELECT 'doc-cpu-high', 'process' WHERE NOT EXISTS (SELECT 1 FROM knowledge_keywords WHERE knowledge_id = 'doc-cpu-high' AND keyword = 'process');
INSERT INTO knowledge_keywords (knowledge_id, keyword)
SELECT 'doc-cpu-high', 'restart' WHERE NOT EXISTS (SELECT 1 FROM knowledge_keywords WHERE knowledge_id = 'doc-cpu-high' AND keyword = 'restart');

INSERT INTO knowledge_keywords (knowledge_id, keyword)
SELECT 'doc-nginx-restart', 'nginx' WHERE NOT EXISTS (SELECT 1 FROM knowledge_keywords WHERE knowledge_id = 'doc-nginx-restart' AND keyword = 'nginx');
INSERT INTO knowledge_keywords (knowledge_id, keyword)
SELECT 'doc-nginx-restart', 'restart' WHERE NOT EXISTS (SELECT 1 FROM knowledge_keywords WHERE knowledge_id = 'doc-nginx-restart' AND keyword = 'restart');
INSERT INTO knowledge_keywords (knowledge_id, keyword)
SELECT 'doc-nginx-restart', 'cpu' WHERE NOT EXISTS (SELECT 1 FROM knowledge_keywords WHERE knowledge_id = 'doc-nginx-restart' AND keyword = 'cpu');

INSERT INTO knowledge_keywords (knowledge_id, keyword)
SELECT 'doc-memory-alert', 'memory' WHERE NOT EXISTS (SELECT 1 FROM knowledge_keywords WHERE knowledge_id = 'doc-memory-alert' AND keyword = 'memory');
INSERT INTO knowledge_keywords (knowledge_id, keyword)
SELECT 'doc-memory-alert', 'oom' WHERE NOT EXISTS (SELECT 1 FROM knowledge_keywords WHERE knowledge_id = 'doc-memory-alert' AND keyword = 'oom');
INSERT INTO knowledge_keywords (knowledge_id, keyword)
SELECT 'doc-memory-alert', 'gc' WHERE NOT EXISTS (SELECT 1 FROM knowledge_keywords WHERE knowledge_id = 'doc-memory-alert' AND keyword = 'gc');
