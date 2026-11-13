CREATE DATABASE IF NOT EXISTS employee_db;

CREATE USER IF NOT EXISTS 'employee_user'@'localhost'
IDENTIFIED BY 'REPLACE_WITH_A_STRONG_PASSWORD';

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX
ON employee_db.* TO 'employee_user'@'localhost';

USE employee_db;

CREATE TABLE IF NOT EXISTS employees (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE,
    department VARCHAR(100) NOT NULL,
    salary DECIMAL(12,2) NOT NULL,
    PRIMARY KEY (id)
);

INSERT INTO employees (name, email, department, salary)
VALUES
('Amit Sharma', 'amit@example.com', 'DevOps', 650000.00),
('Neha Verma', 'neha@example.com', 'Engineering', 720000.00);
