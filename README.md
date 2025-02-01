# Customer Loan API

## Overview

Customer Loan API is a **Spring Boot** application designed to manage loans for customers in a banking system. The API allows administrators to create, list, and process loan payments securely.

## Features

- **Create Loan**: Allows admins to create a loan for a customer with defined limits and restrictions.
- **List Loans**: Fetches all loans for a given customer.
- **List Installments**: Retrieves all installments for a specific loan.
- **Pay Loan**: Processes installment payments for a loan, including partial and multiple payments.
- **Authentication & Authorization**:
  - **Admin** users can manage loans for all customers.
  - **Customer** users can only operate on their own loans.

## Tech Stack

- **Spring Boot** (Backend Framework)
- **Maven** (Build Tool)
- **H2 Database** (In-Memory DB for Testing)
- **Spring Security** (JWT Authentication)
- **JUnit & Mockito** (Unit Testing)

## Installation & Setup

### Prerequisites

- Java 17+
- Maven

### Clone the Repository

```sh
git clone https://github.com/emrebekar/ingcustomerloan.git
cd ingcustomerloan
```

### Build and Run

```sh
mvn clean install
mvn spring-boot:run
```

### Default Credentials

| Username | Password | Role           |
| -------- | -------- | -------------- |
| admin    | admin    | ROLE\_ADMIN    |
| customer | customer | ROLE\_CUSTOMER |

## API Endpoints

### Authentication

#### `POST /authenticate`

Generate token and send via "Authorization" header parameter. Set the value with "Bearer <generated_token>". Otherwise, the application cannot be used.

**Request Body:**

```json
{
  "username": "admin",
  "password": "admin"
}
```

**Response:**

```json
{
  "token": "<JWT_TOKEN>"
}
```

### Loan Management

#### `POST /api/loans/create` (Admin & Customer)

**Request Body:**

```json
{
  "customerId": 1,
  "amount": 5000,
  "interestRate": 0.2,
  "numberOfInstallments": 12
}
```

**Response:**

```json
None
```

#### `GET /api/loans/customer/{customerId}` (Admin & Customer)

Fetch all loans of a customer.

#### `GET /api/loans/{loanId}/installments` (Admin & Customer)

Retrieve all installments for a given loan.

### Loan Payment

#### `POST /api/loans/payment` (Admin & Customer)

**Request Body:**

```json
{
  "loanId": 1,
  "paymentAmount": 1000
}
```

**Response:**

```json
{
  "paidInstallmentCount": 2,
  "totalPaidAmount": 1000
}
```

## Security

- JWT-based authentication is used.
- `ROLE_ADMIN` can manage all loans.
- `ROLE_CUSTOMER` can only access their own loans.

## Testing

Run unit tests with:

```sh
mvn test
```

Swagger

http://localhost:8080/swagger-ui/index.html

Get token from /authenticate with admin or customer user and password. Then copy click Authorize button on the Swagger page and past the generated token to bearer area to use outher entpoins.

## License

[Apache 2.0](https://springdoc.org)

