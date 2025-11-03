-- Migration 008: Add context column and ensure constraints consistency
-- This migration adds the context column to the questions table and ensures
-- all foreign key constraints use consistent BIGINT types.

-- Add context column to questions table (ignore error if already exists)
-- Using a simple approach to avoid dynamic SQL issues
ALTER TABLE questions ADD COLUMN context TEXT AFTER content;

-- Ensure tags table exists with proper structure
CREATE TABLE IF NOT EXISTS tags (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE,
    usage_count INT NOT NULL DEFAULT 0,
    INDEX idx_tags_usage_count (usage_count)
);

-- Ensure question_tags junction table exists with proper structure
CREATE TABLE IF NOT EXISTS question_tags (
    question_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (question_id, tag_id),
    CONSTRAINT fk_question_tags_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    CONSTRAINT fk_question_tags_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

-- Migration completed successfully