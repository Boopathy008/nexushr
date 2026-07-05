INSERT INTO users (id, username, email, password_hash, role, is_active)
VALUES
  (gen_random_uuid(), 'admin',   'admin@nexushr.com',
   '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQyCjAfuMBYJMBqRXxNy9BfpC',
   'ADMIN', TRUE),
  (gen_random_uuid(), 'manager', 'manager@nexushr.com',
   '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQyCjAfuMBYJMBqRXxNy9BfpC',
   'MANAGER', TRUE);
-- Default password for both: Admin@1234