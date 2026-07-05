CREATE TABLE attendance (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id      UUID NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    attendance_date  DATE NOT NULL,
    check_in_time    TIMESTAMP,
    check_out_time   TIMESTAMP,
    status           VARCHAR(20) NOT NULL DEFAULT 'PRESENT',
    working_minutes  INTEGER DEFAULT 0,
    notes            TEXT,
    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(employee_id, attendance_date)
);

CREATE INDEX idx_attendance_employee   ON attendance(employee_id);
CREATE INDEX idx_attendance_date       ON attendance(attendance_date);
CREATE INDEX idx_attendance_status     ON attendance(status);
--CREATE INDEX idx_attendance_emp_month  ON attendance(employee_id, DATE_TRUNC('month', attendance_date));