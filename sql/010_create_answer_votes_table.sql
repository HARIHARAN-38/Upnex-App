-- Migration 010: Create answer_votes table for tracking user votes on answers
-- This table prevents duplicate voting and tracks vote changes

CREATE TABLE IF NOT EXISTS answer_votes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    answer_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    is_upvote BOOLEAN NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_answer_votes_answer FOREIGN KEY (answer_id) REFERENCES answers(id) ON DELETE CASCADE,
    CONSTRAINT fk_answer_votes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- Unique constraint to prevent duplicate votes by same user on same answer
    UNIQUE KEY uk_answer_votes_user_answer (answer_id, user_id),
    
    -- Indexes for performance
    INDEX idx_answer_votes_answer (answer_id),
    INDEX idx_answer_votes_user (user_id),
    INDEX idx_answer_votes_created_at (created_at)
);

-- Add comment to document the table purpose
ALTER TABLE answer_votes COMMENT = 'Tracks user votes on answers to prevent duplicate voting and enable vote changes';