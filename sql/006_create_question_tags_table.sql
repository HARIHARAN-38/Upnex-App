CREATE TABLE IF NOT EXISTS question_tags (
    question_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (question_id, tag_id),
    CONSTRAINT fk_question_tags_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    CONSTRAINT fk_question_tags_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);
