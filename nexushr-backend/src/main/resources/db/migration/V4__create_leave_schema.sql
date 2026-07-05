CREATE TABLE leave_types (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name           VARCHAR(100) NOT NULL UNIQUE,
    code           VARCHAR(20)  NOT NULL UNIQUE,
    annual_quota   INTEGER      NOT NULL DEFAULT 0,
    is_paid        BOOLEAN      NOT NULL DEFAULT TRUE,
    carry_forward  BOOLEAN      NOT NULL DEFAULT FALSE,
    description    TEXT,
    is_active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE leave_balances (
    id             UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id    UUID    NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    leave_type_id  UUID    NOT NULL REFERENCES leave_types(id) ON DELETE CASCADE,
    year           INTEGER NOT NULL,
    total_days     DECIMAL(5,1) NOT NULL DEFAULT 0,
    used_days      DECIMAL(5,1) NOT NULL DEFAULT 0,
    pending_days   DECIMAL(5,1) NOT NULL DEFAULT 0,
    created_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(employee_id, leave_type_id, year)
);

CREATE TABLE leave_requests (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id    UUID NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    leave_type_id  UUID NOT NULL REFERENCES leave_types(id),
    approved_by    UUID REFERENCES users(id) ON DELETE SET NULL,
    start_date     DATE NOT NULL,
    end_date       DATE NOT NULL,
    total_days     DECIMAL(5,1) NOT NULL,
    status         VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reason         TEXT NOT NULL,
    rejection_note TEXT,
    applied_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    decided_at     TIMESTAMP,
    created_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_leave_requests_employee   ON leave_requests(employee_id);
CREATE INDEX idx_leave_requests_status     ON leave_requests(status);
CREATE INDEX idx_leave_requests_dates      ON leave_requests(start_date, end_date);
CREATE INDEX idx_leave_balances_employee   ON leave_balances(employee_id);

INSERT INTO leave_types (name, code, annual_quota, is_paid, carry_forward) VALUES
    ('Casual Leave',   'CL',  12, TRUE,  FALSE),
    ('Sick Leave',     'SL',  10, TRUE,  FALSE),
    ('Earned Leave',   'EL',  15, TRUE,  TRUE),
    ('Maternity Leave','ML',  90, TRUE,  FALSE),
    ('Paternity Leave','PL',  15, TRUE,  FALSE),
    ('Loss of Pay',    'LOP',  0, FALSE, FALSE);