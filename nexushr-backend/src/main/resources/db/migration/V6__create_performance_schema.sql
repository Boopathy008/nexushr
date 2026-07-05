CREATE TABLE performance_goals (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id  UUID NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    title        VARCHAR(200) NOT NULL,
    description  TEXT,
    target_date  DATE,
    status       VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    year         INTEGER NOT NULL,
    quarter      INTEGER NOT NULL CHECK (quarter BETWEEN 1 AND 4),
    created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE performance_reviews (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id  UUID NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    reviewer_id  UUID NOT NULL REFERENCES users(id),
    review_year  INTEGER NOT NULL,
    review_quarter INTEGER NOT NULL CHECK (review_quarter BETWEEN 1 AND 4),
    rating       DECIMAL(3,1) NOT NULL CHECK (rating BETWEEN 1.0 AND 5.0),
    feedback     TEXT NOT NULL,
    strengths    TEXT,
    improvements TEXT,
    status       VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    submitted_at TIMESTAMP,
    created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(employee_id, reviewer_id, review_year, review_quarter)
);

CREATE INDEX idx_perf_reviews_employee ON performance_reviews(employee_id);
CREATE INDEX idx_perf_reviews_period   ON performance_reviews(review_year, review_quarter);
CREATE INDEX idx_perf_goals_employee   ON performance_goals(employee_id);