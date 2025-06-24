-- Tabela pytań testu uzdolnień kierunkowych
CREATE TABLE academic_skills_test_questions (
    id BIGSERIAL PRIMARY KEY,
    question_text VARCHAR(1000) NOT NULL,
    category VARCHAR(50) NOT NULL,
    order_number INTEGER NOT NULL
);

-- Tabela odpowiedzi użytkowników na test uzdolnień kierunkowych
CREATE TABLE academic_skills_test_responses (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(255) NOT NULL UNIQUE,
    completed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tabela z poszczególnymi odpowiedziami użytkownika
CREATE TABLE academic_skills_test_answers (
    response_id BIGINT NOT NULL,
    answers INTEGER,
    FOREIGN KEY (response_id) REFERENCES academic_skills_test_responses(id) ON DELETE CASCADE
);

-- Index dla szybszego wyszukiwania po session_id
CREATE INDEX idx_academic_skills_test_responses_session_id ON academic_skills_test_responses(session_id);

-- Index dla szybszego sortowania pytań
CREATE INDEX idx_academic_skills_test_questions_order ON academic_skills_test_questions(order_number);

-- Index dla filtrowania pytań po kategorii
CREATE INDEX idx_academic_skills_test_questions_category ON academic_skills_test_questions(category); 