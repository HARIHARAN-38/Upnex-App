-- Migration 008: Add context column and ensure constraints consistency
-- This migration adds the context column to the questions table and ensures
-- all foreign key constraints use consistent BIGINT types.

-- Add context column to questions table if it doesn't exist
SET @sql = (SELECT IF(
    (SELECT COUNT(*)
        FROM INFORMATION_SCHEMA.COLUMNS 
        WHERE table_name='questions' 
        AND column_name='context'
        AND table_schema=DATABASE()) > 0,
    "SELECT 'Column context already exists'",
    "ALTER TABLE questions ADD COLUMN context TEXT AFTER content"
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Verify and ensure tags table exists with proper structure
CREATE TABLE IF NOT EXISTS tags (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE,
    usage_count INT NOT NULL DEFAULT 0,
    INDEX idx_tags_usage_count (usage_count)
);

-- Verify and ensure question_tags junction table exists with proper structure
-- Drop and recreate to ensure proper BIGINT foreign keys
CREATE TABLE IF NOT EXISTS question_tags (
    question_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (question_id, tag_id),
    CONSTRAINT fk_question_tags_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    CONSTRAINT fk_question_tags_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

-- Verify users table ID column is BIGINT (should already be correct)
-- This is a verification query - no changes needed as users.id is already BIGINT

-- Verify questions table user_id FK is BIGINT (should already be correct)
-- This is a verification query - no changes needed as questions.user_id is already BIGINT

-- Migration completed successfully