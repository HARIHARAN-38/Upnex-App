-- Create question_votes table to track user votes on questions
-- This implements Reddit-like voting where each user can vote only once per question
CREATE TABLE question_votes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    vote_type ENUM('upvote', 'downvote') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    
    -- Ensure one vote per user per question
    UNIQUE KEY unique_user_question_vote (user_id, question_id),
    
    -- Indexes for performance
    INDEX idx_user_votes (user_id),
    INDEX idx_question_votes (question_id),
    INDEX idx_vote_type (vote_type)
);