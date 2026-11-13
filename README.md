# Employee Management REST API — Java + MySQL on AWS EC2

A beginner-friendly DevOps practice project. A Java 17 Spring Boot REST API performs CRUD operations on a MySQL database hosted on the same Ubuntu EC2 instance.

## Architecture

Client / browser / curl → EC2 public IP:8080 → Spring Boot (Java) → MySQL (localhost:3306)

This is a learning setup. For production, use a private database such as Amazon RDS, HTTPS, a reverse proxy, backups, and a secrets manager.

## Requirements

- AWS account
- Ubuntu 24.04 LTS EC2 instance (t3.micro can be used for a small test, subject to current free-tier eligibility and resource limits)
- Security group inbound: SSH TCP 22 from **your IP only**; custom TCP 8080 from **your IP only** for testing
- Java 17, Maven, MySQL 8
- Git

Do not open MySQL port 3306 to the internet.

## 1. Create the EC2 instance

1. Open AWS Console → EC2 → Launch instance.
2. Name: `java-mysql-employee-app`.
3. Choose Ubuntu Server 24.04 LTS.
4. Choose a small instance type appropriate for your account and workload.
5. Create/select a key pair and download the `.pem` file safely.
6. Configure the security group as described above.
7. Launch and note the public IPv4 address.

Connect from your computer (replace the key and IP):

```bash
ssh -i your-key.pem ubuntu@EC2_PUBLIC_IP
```

## 2. Install Java, Maven, MySQL and Git

Run on EC2:

```bash
sudo apt update
sudo apt install -y openjdk-17-jdk maven mysql-server git
java -version
mvn -version
mysql --version
```

## 3. Configure the database

Generate a strong database password and keep it private. Do not commit it to GitHub.

```bash
sudo mysql
```

At the MySQL prompt, run these commands, replacing `STRONG_DB_PASSWORD` with your own password:

```sql
CREATE DATABASE employee_db;
CREATE USER 'employee_user'@'localhost' IDENTIFIED BY 'STRONG_DB_PASSWORD';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX ON employee_db.* TO 'employee_user'@'localhost';
USE employee_db;

CREATE TABLE employees (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE,
    department VARCHAR(100) NOT NULL,
    salary DECIMAL(12,2) NOT NULL,
    PRIMARY KEY (id)
);

INSERT INTO employees (name, email, department, salary)
VALUES ('Amit Sharma', 'amit@example.com', 'DevOps', 650000.00),
       ('Neha Verma', 'neha@example.com', 'Engineering', 720000.00);

SELECT * FROM employees;
EXIT;
```

The SQL file in `database/schema.sql` documents the schema. The commands above create a separate database user with limited permissions.

## 4. Put this project in your GitHub repository

Create an empty GitHub repository named `ec2-java-mysql-employee-app`.

On EC2, after downloading/cloning the project files (or uploading them to the instance), run:

```bash
git init
git add .
git commit -m "Initial Java MySQL employee management application"
git branch -M main
git remote add origin https://github.com/YOUR_GITHUB_USERNAME/ec2-java-mysql-employee-app.git
git push -u origin main
```

If you create the repository on your own computer first, you can push from your computer and then clone it on EC2:

```bash
git clone https://github.com/YOUR_GITHUB_USERNAME/ec2-java-mysql-employee-app.git
cd ec2-java-mysql-employee-app
```

## 5. Build and run the Java application

From the project root, set the database password in the current shell. Use the exact password you assigned in MySQL:

```bash
export DB_USER=employee_user
export DB_PASSWORD='STRONG_DB_PASSWORD'
mvn clean package
java -jar target/employee-app-1.0.0.jar
```

Keep this terminal open while testing. The application starts on port 8080.

## 6. Test the API

From your own computer, replace `EC2_PUBLIC_IP` with the instance public IP. The security group must allow port 8080 from your current public IP.

Get all employees:

```bash
curl http://EC2_PUBLIC_IP:8080/api/employees
```

Create an employee:

```bash
curl -X POST http://EC2_PUBLIC_IP:8080/api/employees \
  -H "Content-Type: application/json" \
  -d '{"name":"Faizan Sheikh","email":"faizan@example.com","department":"DevOps","salary":800000}'
```

Get employee with ID 1:

```bash
curl http://EC2_PUBLIC_IP:8080/api/employees/1
```

Update employee with ID 1:

```bash
curl -X PUT http://EC2_PUBLIC_IP:8080/api/employees/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Amit Sharma","email":"amit@example.com","department":"Cloud DevOps","salary":700000}'
```

Delete employee with ID 1:

```bash
curl -X DELETE http://EC2_PUBLIC_IP:8080/api/employees/1
```

You can also open `http://EC2_PUBLIC_IP:8080/api/employees` in a browser.

## 7. Run it in the background with systemd (optional)

First stop the foreground Java process with Ctrl+C. Create a dedicated application directory and copy the jar:

```bash
sudo mkdir -p /opt/employee-app
sudo cp target/employee-app-1.0.0.jar /opt/employee-app/employee-app.jar
sudo useradd --system --home /opt/employee-app --shell /usr/sbin/nologin employeeapp || true
sudo chown -R employeeapp:employeeapp /opt/employee-app
```

Create the service file:

```bash
sudo nano /etc/systemd/system/employee-app.service
```

Paste this, replacing the password:

```ini
[Unit]
Description=Java Employee Management API
After=network.target mysql.service
Requires=mysql.service

[Service]
User=employeeapp
WorkingDirectory=/opt/employee-app
Environment="DB_USER=employee_user"
Environment="DB_PASSWORD=STRONG_DB_PASSWORD"
ExecStart=/usr/bin/java -jar /opt/employee-app/employee-app.jar
Restart=on-failure
RestartSec=5

[Install]
WantedBy=multi-user.target
```

Save, then run:

```bash
sudo systemctl daemon-reload
sudo systemctl enable --now employee-app
sudo systemctl status employee-app
sudo journalctl -u employee-app -f
```

## 8. Useful SQL practice

```sql
USE employee_db;
SELECT * FROM employees;
SELECT name, salary FROM employees WHERE department = 'DevOps';
SELECT department, COUNT(*) AS employee_count FROM employees GROUP BY department;
UPDATE employees SET salary = 850000 WHERE email = 'faizan@example.com';
DELETE FROM employees WHERE email = 'faizan@example.com';
```

## 9. Troubleshooting

- `Access denied for user`: verify `DB_USER`, `DB_PASSWORD`, and MySQL user host (`localhost`).
- `Communications link failure`: check `sudo systemctl status mysql`; the app connects to MySQL on localhost.
- `Connection refused` from your PC: check `sudo systemctl status employee-app` and EC2 security group TCP 8080 source rule.
- Port 8080 already in use: stop the other process or change `server.port` in `application.properties` and the security group.
- Build fails: check `java -version` (must be 17+) and `mvn -version`.

## What you practise

- Linux commands and EC2 administration
- Java 17 and Maven build
- Spring Boot REST APIs
- MySQL database, users, tables and SQL CRUD queries
- Git and GitHub
- Linux systemd service management
- AWS security groups and basic troubleshooting
