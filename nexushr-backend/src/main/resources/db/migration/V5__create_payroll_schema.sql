CREATE TABLE salary_structures (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id     UUID NOT NULL UNIQUE REFERENCES employees(id) ON DELETE CASCADE,
    basic_salary    DECIMAL(12,2) NOT NULL,
    hra             DECIMAL(12,2) NOT NULL DEFAULT 0,
    transport_allowance DECIMAL(12,2) NOT NULL DEFAULT 0,
    medical_allowance   DECIMAL(12,2) NOT NULL DEFAULT 0,
    other_allowances    DECIMAL(12,2) NOT NULL DEFAULT 0,
    tax_rate        DECIMAL(5,2)  NOT NULL DEFAULT 0,
    pf_rate         DECIMAL(5,2)  NOT NULL DEFAULT 12.00,
    effective_from  DATE          NOT NULL,
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE TABLE payroll_runs (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id      UUID NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    pay_month        INTEGER NOT NULL,
    pay_year         INTEGER NOT NULL,
    working_days     INTEGER NOT NULL DEFAULT 0,
    present_days     DECIMAL(5,1) NOT NULL DEFAULT 0,
    leave_days       DECIMAL(5,1) NOT NULL DEFAULT 0,
    lop_days         DECIMAL(5,1) NOT NULL DEFAULT 0,
    basic_salary     DECIMAL(12,2) NOT NULL,
    hra              DECIMAL(12,2) NOT NULL DEFAULT 0,
    allowances       DECIMAL(12,2) NOT NULL DEFAULT 0,
    gross_salary     DECIMAL(12,2) NOT NULL,
    tax_deduction    DECIMAL(12,2) NOT NULL DEFAULT 0,
    pf_deduction     DECIMAL(12,2) NOT NULL DEFAULT 0,
    lop_deduction    DECIMAL(12,2) NOT NULL DEFAULT 0,
    other_deductions DECIMAL(12,2) NOT NULL DEFAULT 0,
    total_deductions DECIMAL(12,2) NOT NULL,
    net_salary       DECIMAL(12,2) NOT NULL,
    status           VARCHAR(20)   NOT NULL DEFAULT 'DRAFT',
    processed_at     TIMESTAMP,
    created_at       TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP     NOT NULL DEFAULT NOW(),
    UNIQUE(employee_id, pay_month, pay_year)
);

CREATE INDEX idx_payroll_employee  ON payroll_runs(employee_id);
CREATE INDEX idx_payroll_period    ON payroll_runs(pay_year, pay_month);
CREATE INDEX idx_payroll_status    ON payroll_runs(status);