CREATE TABLE departments (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name         VARCHAR(100) NOT NULL UNIQUE,
    code         VARCHAR(20)  NOT NULL UNIQUE,
    manager_id   UUID REFERENCES users(id) ON DELETE SET NULL,
    description  TEXT,
    is_active    BOOLEAN NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE designations (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title          VARCHAR(100) NOT NULL,
    grade          VARCHAR(20),
    department_id  UUID NOT NULL REFERENCES departments(id) ON DELETE CASCADE,
    is_active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(title, department_id)
);

CREATE TABLE employees (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    department_id   UUID NOT NULL REFERENCES departments(id),
    designation_id  UUID NOT NULL REFERENCES designations(id),
    employee_code   VARCHAR(20) NOT NULL UNIQUE,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    phone           VARCHAR(20),
    gender          VARCHAR(10),
    date_of_birth   DATE,
    date_of_joining DATE NOT NULL,
    address         TEXT,
    emergency_contact_name  VARCHAR(100),
    emergency_contact_phone VARCHAR(20),
    profile_picture_url     TEXT,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_employees_department  ON employees(department_id);
CREATE INDEX idx_employees_designation ON employees(designation_id);
CREATE INDEX idx_employees_status      ON employees(status);
CREATE INDEX idx_employees_code        ON employees(employee_code);
CREATE INDEX idx_employees_name        ON employees(first_name, last_name);

INSERT INTO departments (name, code, description)
VALUES ('Human Resources', 'HR', 'HR and People Operations'),
       ('Engineering',     'ENG', 'Software Development'),
       ('Finance',         'FIN', 'Finance and Accounting'),
       ('Operations',      'OPS', 'Business Operations');