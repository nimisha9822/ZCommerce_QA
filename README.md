# ZCommerce QA Automation (Distributed System Testing)

## Overview

The application under test is the **Order Service** of the ZCommerce application.

During the course of this project:

- Configured and brought up the test bed to run API tests on the Order microservice.
- Performed automated tests on the MySQL database using JDBC.
- Implemented custom logging requirements for test logs.
- Used Docker to set up the test environment and mock servers.
- Performed API tests using RestAssured framework based on Swagger documentation.
- Integrated logs into the MySQL database using Log4j.

---

## Scope of Work

### Configure Test Bed for API Testing

1. Resolved inter-service dependency:
   - Used multiple Docker containers.
   - Configured mock servers with the Postman client.
2. Configured ports required for Docker networking to allow microservices to communicate.
3. Set up mock servers to avoid dependencies and reduce costs.
4. Executed test suite to verify functionality for ordering products and adding items to the cart.

**Skills Used:** Docker, Mock Servers, Swagger, Test Bed Configuration

---

### Automate Database Testing with JDBC

1. Brought up a test database seeded with sample data.
2. Wrote multiple queries spanning six tables in the Order database.
3. Automated database queries and ran tests using JDBC.

**Skills Used:** JDBC, SQL

---

### Implement Custom Logging with Log4j

1. Set up Log4j layouts and appenders.
2. Customized layout patterns for logs.
3. Configured Log4j to have dedicated files for storing logs for each environment.
4. Used Log4j JDBC appender to upload logs into the MySQL database.

**Skills Used:** Log4j, JDBC Appender

---

## Directory Structure

```
ZCommerce_QA_Automation/
├── src/
│   ├── api_tests/
│   │   ├── OrderServiceAPITest.java
│   ├── db_tests/
│   │   ├── OrderServiceDBTest.java
│   ├── logs/
│   │   ├── Log4jConfig.xml
├── docker/
│   ├── docker-compose.yml
├── reports/
│   ├── TestReport.html
├── README.md
```

---

## Setup Instructions

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd ZCommerce_QA_Automation
   ```

2. Install dependencies:
   - Ensure you have **Java JDK 11+** installed.
   - Install **Docker** and **Docker Compose**.
   - Set up MySQL database with seeded data.

3. Start the test bed using Docker:
   ```bash
   docker-compose up
   ```

4. Run API and database tests:
   ```bash
   mvn test
   ```

---

## Features

- **Mock Server Configuration:** Reduces inter-service dependency and testing costs.
- **Automated Database Testing:** Ensures data integrity and correctness in the Order database.
- **Custom Logging:** Provides detailed insights with dedicated log files and database-stored logs.
- **Dynamic Test Bed:** Leverages Docker to manage microservices and dependencies.
- **Integrated Testing:** Combines API and database tests for comprehensive validation.

---


