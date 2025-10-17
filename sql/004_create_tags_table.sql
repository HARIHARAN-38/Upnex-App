CREATE TABLE IF NOT EXISTS tags (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE,
    usage_count INT NOT NULL DEFAULT 0,
    INDEX idx_tags_usage_count (usage_count)
);
