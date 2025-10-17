CREATE TABLE IF NOT EXISTS questions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    subject_id BIGINT,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    upvotes INT NOT NULL DEFAULT 0,
    downvotes INT NOT NULL DEFAULT 0,
    answer_count INT NOT NULL DEFAULT 0,
    is_solved BOOLEAN NOT NULL DEFAULT FALSE,
    view_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_questions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_questions_subject FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE SET NULL,
    INDEX idx_questions_user (user_id),
    INDEX idx_questions_subject (subject_id),
    INDEX idx_questions_created_at (created_at)
);
