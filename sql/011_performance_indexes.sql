-- Performance optimization indexes for question and answer queries
-- This file contains additional indexes to improve query performance

-- Index for question search optimization
CREATE INDEX IF NOT EXISTS idx_questions_search_composite ON questions (created_at DESC, is_solved, answer_count);

-- Index for question view count updates (frequently accessed)
CREATE INDEX IF NOT EXISTS idx_questions_view_count ON questions (view_count DESC);

-- Index for question user queries
CREATE INDEX IF NOT EXISTS idx_questions_user_created ON questions (user_id, created_at DESC);

-- Index for question subject queries  
CREATE INDEX IF NOT EXISTS idx_questions_subject_created ON questions (subject_id, created_at DESC);

-- Index for answer sorting by votes and acceptance
CREATE INDEX IF NOT EXISTS idx_answers_sorting ON answers (question_id, is_accepted DESC, upvotes DESC, created_at ASC);

-- Index for answer user queries
CREATE INDEX IF NOT EXISTS idx_answers_user ON answers (user_id, created_at DESC);

-- Index for tag search optimization
CREATE INDEX IF NOT EXISTS idx_tags_name_usage ON tags (name, usage_count DESC);

-- Index for question-tag relationship queries
CREATE INDEX IF NOT EXISTS idx_question_tags_question ON question_tags (question_id);
CREATE INDEX IF NOT EXISTS idx_question_tags_tag ON question_tags (tag_id);

-- Index for vote count aggregation
CREATE INDEX IF NOT EXISTS idx_answer_votes_answer_vote ON answer_votes (answer_id, is_upvote);

-- Add indexes for full-text search if not exists
CREATE INDEX IF NOT EXISTS idx_questions_title_fulltext ON questions (title);
CREATE INDEX IF NOT EXISTS idx_questions_content_search ON questions (content(255));