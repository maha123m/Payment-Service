# Payment Service API

A secure, scalable payment processing service built with Grails 5.3.6. Provides merchant account management, payment processing with state transitions (PENDING → SUCCESS → REFUNDED), and comprehensive filtering/pagination.

## Overview

**Technology Stack:**
- Framework: Grails 5.3.6
- Database: PostgreSQL (production/dev), H2 (testing)
- Testing: Spock framework for unit tests
- Build Tool: Gradle 8.0
- Java: JDK 17

**Port:** 8090

---

## Quick Start

### Prerequisites

- JDK 17 installed
- PostgreSQL 14+ (for development/production)
- Gradle 8.0+ (or use bundled gradlew)

### Setup

1. **Clone and navigate to project:**
   ```bash
   cd c:\Projects\payment-service
   ```

2. **Configure PostgreSQL (Development):**
   
   Create database:
   ```sql
   CREATE DATABASE payment_service;
   ```

   Update credentials in `grails-app/conf/application.yml`:
   - Host: `localhost:5432`
   - Username: `postgres`
   - Password: Set your own password

3. **Run the application:**
   ```bash
   ./gradlew bootRun
   ```
   
   Server starts at `http://localhost:8090`

4. **Run tests:**
   ```bash
   ./gradlew test
   ```

---

## Project Structure

```
payment-service/
├── grails-app/
│   ├── controllers/payment/service/        # API controllers
│   │   ├── MerchantController
│   │   ├── PaymentController
│   │   └── BaseApiController               # Base class with error handling
│   ├── services/payment/service/           # Business logic
│   │   ├── MerchantService
│   │   └── PaymentService
│   ├── domain/payment/service/             # GORM domain models
│   │   ├── Merchant
│   │   └── PaymentTransaction
│   └── conf/                               # Configuration files
│       ├── application.yml                 # Database & server config
│       └── UrlMappings.groovy             # API route definitions
│
├── src/main/groovy/payment/service/
│   ├── commands/                           # Request DTOs with validation
│   │   ├── CreateMerchantCommand
│   │   └── CreatePaymentCommand
│   ├── api/                                # Response mapping objects
│   │   ├── MerchantResponse
│   │   └── PaymentResponse
│   ├── enums/                              # PaymentStatus enum
│   └── exception/                          # Error codes and exceptions
│
└── src/test/groovy/payment/service/        # Unit tests
    ├── MerchantServiceSpec
    └── PaymentServiceSpec
```

---

## API Documentation

### Base URL
```
http://localhost:8090/api
```

### Response Format

**Success Response:**
```json
{
  "id": 1,
  "name": "Store Name",
  "email": "store@example.com",
  "apiKey": "merchant_a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6"
  
}
```

**Error Response:**
```json
{
  "errorCode": "10",
  "error": "Email 'test@test.com' is already in use. Please use a different email address."
}
```

---

## Endpoints

### 1. Merchant Management

#### Create Merchant
```http
POST /api/merchants
Content-Type: application/json

{
  "name": "My Store",
  "email": "store@example.com"
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "name": "My Store",
  "email": "store@example.com",
  "apiKey": "merchant_a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8090/api/merchants \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My Store",
    "email": "store@example.com"
  }'
```

---

### 2. Payment Processing

#### Create Payment (PENDING)
```http
POST /api/payments
X-API-KEY: merchant_a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6
X-SIGNATURE: <hmac-sha256-signature>
Content-Type: application/json

{
  "reference": "INV-12345",
  "amount": 150.50,
  "currency": "USD",
  "description": "Order #12345 payment"
}
```

**Response:** `201 Created`
```json
{
  "reference": "INV-12345",
  "amount": 150.50,
  "currency": "USD",
  "description": "Order #12345 payment",
  "status": "PENDING",
  "dateCreated": "2026-08-16T10:30:00Z"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8090/api/payments \
  -H "X-API-KEY: merchant_a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6" \
  -H "Content-Type: application/json" \
  -d '{
    "reference": "INV-12345",
    "amount": 150.50,
    "currency": "USD",
    "description": "Order #12345 payment"
  }'
```

---

#### Capture Payment (PENDING → SUCCESS)
```http
POST /api/payments/{reference}/capture
X-API-KEY: merchant_a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6
X-SIGNATURE: <hmac-sha256-signature>
```

**Response:** `200 OK`
```json
{
  "reference": "INV-12345",
  "amount": 150.50,
  "currency": "USD",
  "description": "Order #12345 payment",
  "status": "SUCCESS",
  "dateCreated": "2026-08-16T10:30:00Z"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8090/api/payments/INV-12345/capture \
  -H "X-API-KEY: merchant_a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6"
```

---

#### Refund Payment (SUCCESS → REFUNDED)
```http
POST /api/payments/{reference}/refund
X-API-KEY: merchant_a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6
X-SIGNATURE: <hmac-sha256-signature>
```

**Response:** `200 OK`
```json
{
  "reference": "INV-12345",
  "amount": 150.50,
  "currency": "USD",
  "description": "Order #12345 payment",
  "status": "REFUNDED",
  "dateCreated": "2026-08-16T10:30:00Z"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8090/api/payments/INV-12345/refund \
  -H "X-API-KEY: merchant_a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6"
```

---

#### Get Payment Details
```http
GET /api/payments/{reference}
X-API-KEY: merchant_a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6
```

**Response:** `200 OK`
```json
{
  "reference": "INV-12345",
  "amount": 150.50,
  "currency": "USD",
  "description": "Order #12345 payment",
  "status": "SUCCESS",
  "merchant": "My Store",
  "dateCreated": "2026-08-16T10:30:00Z",
  "lastUpdated": "2026-08-16T10:32:00Z"
}
```

**cURL Example:**
```bash
curl -X GET http://localhost:8090/api/payments/INV-12345 \
  -H "X-API-KEY: merchant_a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6"
```

---

#### List Merchant Payments (with Pagination & Filters)
```http
GET /api/payments?status=SUCCESS&fromDate=2026-08-01&toDate=2026-08-31&max=20&offset=0
X-API-KEY: merchant_a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6
```

**Query Parameters:**
| Parameter | Type | Required | Description | Example |
|-----------|------|----------|-------------|---------|
| `status` | String | No | Filter by status: `PENDING`, `SUCCESS`, `REFUNDED` | `SUCCESS` |
| `fromDate` | String | No | Start date (yyyy-MM-dd format) | `2026-08-01` |
| `toDate` | String | No | End date (yyyy-MM-dd format, inclusive) | `2026-08-31` |
| `max` | Integer | No | Items per page (1-500, default 20) | `50` |
| `offset` | Integer | No | Pagination offset (default 0) | `0` |

**Response:** `200 OK`
```json
{
  "total": 5,
  "count": 2,
  "max": 20,
  "offset": 0,
  "hasNext": false,
  "payments": [
    {
      "reference": "INV-12345",
      "amount": 150.50,
      "currency": "USD",
      "description": "Order #12345 payment",
      "status": "SUCCESS",
      "dateCreated": "2026-08-16T10:30:00Z"
    },
    {
      "reference": "INV-12346",
      "amount": 200.00,
      "currency": "USD",
      "description": "Order #12346 payment",
      "status": "SUCCESS",
      "dateCreated": "2026-08-16T11:00:00Z"
    }
  ]
}
```

**cURL Examples:**

List all SUCCESS payments:
```bash
curl -X GET "http://localhost:8090/api/payments?status=SUCCESS" \
  -H "X-API-KEY: merchant_a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6"
```

List payments from August 2026 with pagination:
```bash
curl -X GET "http://localhost:8090/api/payments?fromDate=2026-08-01&toDate=2026-08-31&max=10&offset=0" \
  -H "X-API-KEY: merchant_a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6"
```

Combine filters:
```bash
curl -X GET "http://localhost:8090/api/payments?status=PENDING&fromDate=2026-08-10&toDate=2026-08-20" \
  -H "X-API-KEY: merchant_a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6"
```

---

## Authentication & Request Signing

All payment endpoints require the following headers:

```
X-API-KEY: merchant_<unique-32-character-id>
X-SIGNATURE: <hmac-sha256-signature>
```

### API Key

The `X-API-KEY` header identifies the merchant making the request. It is automatically generated when a merchant account is created and is required for:
- Creating payments
- Capturing/refunding payments
- Listing payments
- Getting payment details

### Request Signature

The `X-SIGNATURE` header ensures request integrity by verifying that the request body was not tampered with in transit.

**How to generate the signature:**
```
signature = HMAC-SHA256(merchantSecretKey, rawRequestBody)
```

The signature is calculated using the merchant's secret key (provided when the merchant account is created) and the raw JSON request body. The result should be lowercase hexadecimal.

**Important:**
- Use the exact raw JSON body as it will be sent (do not re-serialize or reorder fields)
- The signature should be lowercase hexadecimal
- Never expose the secret key in requests or logs

**Example (JavaScript):**
```javascript
const crypto = require('crypto');
const secretKey = 'your_merchant_secret_key';
const body = '{"amount":100,"currency":"USD","reference":"PAY-123"}';
const signature = crypto.createHmac('sha256', secretKey).update(body).digest('hex');
```

For detailed implementation examples in JavaScript, Python, and Java, see `SIGNATURE_DOCUMENTATION.md`.

### Authentication Errors

If the API key is missing, invalid, or the merchant is inactive, the API returns:

**`401 Unauthorized` (Missing/Invalid Key):**
```json
{
  "errorCode": "30",
  "error": "Missing X-API-KEY header"
}
```

**`401 Unauthorized` (Missing/Invalid Signature):**
```json
{
  "errorCode": "33",
  "error": "Missing X-SIGNATURE header"
}
```

**`401 Unauthorized` (Invalid Signature):**
```json
{
  "errorCode": "34",
  "error": "Invalid request signature."
}
```

**`403 Forbidden` (Inactive Merchant):**
```json
{
  "errorCode": "32",
  "error": "Merchant is inactive"
}
```

---

## Error Handling

All errors follow a consistent format with error codes and HTTP status codes:

### Error Codes Reference

| Code | HTTP Status | Category | Error Message | When It Occurs |
|------|------------|----------|---------------|----------------|
| **10** | 400 | Merchant | `Email '...' is already in use. Please use a different email address.` | Attempting to create merchant with duplicate email |
| **10** | 400 | Merchant | `Email is required and cannot be empty.` | Email field is null or blank in create merchant request |
| **10** | 400 | Merchant | `Merchant name is required and cannot be empty.` | Name field is null or blank in create merchant request |
| **11** | 404 | Merchant | `Merchant not found` | Attempting to retrieve non-existent merchant |
| **20** | 400 | Payment | `Missing required fields: reference (unique payment ID), amount (greater than 0), and currency.` | Create payment request missing reference, amount, or currency |
| **21** | 400 | Payment | `Amount must be greater than 0. You provided: {amount}` | Payment amount is zero or negative |
| **22** | 400 | Payment | `Payment reference '{reference}' already exists. Please use a unique reference for this payment.` | Attempting to create payment with duplicate reference |
| **23** | 400 | Payment | `Payment validation failed: {details}` | Domain model validation fails (constraints violation) |
| **30** | 401 | Authentication | `Missing X-API-KEY header` | API request without X-API-KEY header |
| **31** | 401 | Authentication | `Invalid API key` | X-API-KEY header contains non-existent or incorrect key |
| **32** | 403 | Authentication | `Merchant is inactive` | Merchant account is deactivated (active=false) |
| **33** | 401 | Authentication | `Missing X-SIGNATURE header` | API request without X-SIGNATURE header |
| **34** | 401 | Authentication | `Invalid request signature.` | HMAC-SHA256 signature verification failed (wrong secret key, tampered body, or incorrect calculation) |
| **40** | 404 | Capture | `Payment not found` | Attempting to capture non-existent payment reference |
| **41** | 403 | Capture | `Payment does not belong to this merchant` | Merchant trying to capture payment created by different merchant |
| **42** | 400 | Capture | `Cannot capture payment. Current status is '{status}'. Payment must be in PENDING status to be captured.` | Attempting to capture payment that is already SUCCESS or REFUNDED |
| **43** | 500 | Capture | `Failed to update payment: {details}` | Database error while saving captured payment |
| **50** | 404 | Refund | `Payment not found` | Attempting to refund non-existent payment reference |
| **51** | 403 | Refund | `Payment does not belong to this merchant` | Merchant trying to refund payment created by different merchant |
| **52** | 400 | Refund | `Cannot refund payment. Current status is '{status}'. Only SUCCESS payments can be refunded.` | Attempting to refund PENDING or already REFUNDED payment |
| **53** | 500 | Refund | `Failed to update payment: {details}` | Database error while saving refunded payment |
| **60** | 404 | Payment Query | `Payment not found` | Attempting to get details of non-existent payment reference |
| **61** | 403 | Payment Query | `Payment does not belong to this merchant` | Merchant trying to view payment created by different merchant |
| **70** | 400 | List Filters | `Invalid status filter` | Status filter value is not PENDING, SUCCESS, or REFUNDED |
| **71** | 400 | List Filters | `Invalid {fromDate/toDate} format, expected yyyy-MM-dd` | Date filter not in yyyy-MM-dd format or invalid date (e.g., 2026-02-30) |
| **72** | 400 | List Filters | `max must be between 1 and 500` | Pagination `max` parameter outside valid range |
| **72** | 400 | List Filters | `offset must be >= 0` | Pagination `offset` parameter is negative |
| **99** | 500 | System | `Internal server error` | Unexpected server error (see application logs) |

---

### Error Categories Explained

#### **Merchant Errors (10-11)**
- **Code 10**: Validation errors when creating merchant accounts
  - Email already registered by another merchant
  - Missing or empty name field
  - Missing or invalid email format
  - Solution: Provide unique, valid email and non-empty name

- **Code 11**: Merchant lookup failure (not used in current API, reserved for future use)

#### **Payment Errors (20-23)**
- **Code 20**: Missing required payment fields
  - Solution: Ensure request includes `reference`, `amount`, and `currency`

- **Code 21**: Invalid payment amount
  - Solution: Amount must be greater than 0 (e.g., 0.01, 100.50, but not 0, -50)

- **Code 22**: Duplicate payment reference
  - Solution: Each payment reference must be unique per merchant. Use different reference for new payment.

- **Code 23**: Payment validation failed
  - Solution: Check database constraints (e.g., currency field length, merchant exists)

#### **Authentication Errors (30-32)**
- **Code 30**: Missing API key header
  - Solution: Include `X-API-KEY: <merchant_api_key>` in request headers

- **Code 31**: Invalid or unknown API key
  - Solution: Verify API key is correct (from merchant creation response)

- **Code 32**: Merchant account inactive
  - Solution: Contact support to reactivate merchant account (admin only)

#### **Capture Errors (40-43)**
- **Code 40-41**: Payment not found or not owned
  - Solution: Verify payment reference is correct and belongs to merchant

- **Code 42**: Invalid payment status for capture
  - Solution: Can only capture PENDING payments. Check current status first.
  - Flow: PENDING → (capture) → SUCCESS

- **Code 43**: Server error during capture
  - Solution: Retry operation. If persists, contact support with error details.

#### **Refund Errors (50-53)**
- **Code 50-51**: Payment not found or not owned
  - Solution: Verify payment reference is correct and belongs to merchant

- **Code 52**: Invalid payment status for refund
  - Solution: Can only refund SUCCESS payments. Capture first if PENDING.
  - Flow: PENDING → (capture) → SUCCESS → (refund) → REFUNDED

- **Code 53**: Server error during refund
  - Solution: Retry operation. If persists, contact support with error details.

#### **Payment Query Errors (60-61)**
- **Code 60**: Payment reference not found
  - Solution: Verify reference is spelled correctly

- **Code 61**: Payment belongs to different merchant
  - Solution: Each merchant can only view/manage their own payments. Use correct API key.

#### **List Filter Errors (70-72)**
- **Code 70**: Invalid status filter value
  - Valid values: `PENDING`, `SUCCESS`, `REFUNDED`
  - Example: `?status=SUCCESS` (not `?status=success` or `?status=Completed`)

- **Code 71**: Invalid date format
  - Required format: `yyyy-MM-dd` (e.g., `2026-08-16`, not `08/16/2026`)
  - Date must be valid (e.g., `2026-02-30` is invalid)

- **Code 72**: Pagination parameters out of range
  - `max` must be 1-500 (default 20)
  - `offset` must be >= 0 (default 0)

#### **System Errors (99)**
- **Code 99**: Unexpected server error
  - Solution: Check server logs for details. Retry request. If problem persists, contact support.

---

### Common Error Scenarios

**400 Bad Request - Missing Fields:**
```json
{
  "errorCode": "20",
  "error": "Missing required fields: reference (unique payment ID), amount (greater than 0), and currency."
}
```

**400 Bad Request - Duplicate Reference:**
```json
{
  "errorCode": "22",
  "error": "Payment reference 'INV-12345' already exists. Please use a unique reference for this payment."
}
```

**400 Bad Request - Invalid Amount:**
```json
{
  "errorCode": "21",
  "error": "Amount must be greater than 0. You provided: -50"
}
```

**400 Bad Request - Invalid Status for Operation:**
```json
{
  "errorCode": "42",
  "error": "Cannot capture payment. Current status is 'SUCCESS'. Payment must be in PENDING status to be captured."
}
```

**400 Bad Request - Invalid Merchant Email:**
```json
{
  "errorCode": "10",
  "error": "Email is required and cannot be empty."
}
```

**404 Not Found - Payment Not Found:**
```json
{
  "errorCode": "60",
  "error": "Payment not found"
}
```

**404 Not Found - Merchant Not Found:**
```json
{
  "errorCode": "11",
  "error": "Merchant not found"
}
```

**403 Forbidden - Payment Not Owned by Merchant:**
```json
{
  "errorCode": "61",
  "error": "Payment does not belong to this merchant"
}
```

**400 Bad Request - Invalid Date Format:**
```json
{
  "errorCode": "71",
  "error": "Invalid fromDate format, expected yyyy-MM-dd"
}
```

---

## Payment Status Flow

```
PENDING ──capture()──> SUCCESS ──refund()──> REFUNDED
```

- **PENDING**: Initial state after payment creation. Ready for capture.
- **SUCCESS**: Payment has been captured (charged). Can be refunded.
- **REFUNDED**: Payment has been refunded back to customer. Terminal state.

**State Transition Rules:**
- Only PENDING payments can be captured
- Only SUCCESS payments can be refunded
- Cannot capture an already captured payment
- Cannot refund a PENDING payment

---

## Testing

### Run All Tests
```bash
./gradlew test
```

### Run Specific Test Suite
```bash
./gradlew test --tests MerchantServiceSpec
./gradlew test --tests PaymentServiceSpec
```

### Tests Included

1. **MerchantServiceSpec** (`src/test/groovy/payment/service/MerchantServiceSpec.groovy`):
   - Merchant creation with auto-generated API key
   - Duplicate email rejection
   - API key validation (missing, invalid, inactive merchant)

2. **PaymentServiceSpec** (`src/test/groovy/payment/service/PaymentServiceSpec.groovy`):
   - Payment creation in PENDING status
   - Payment capture (PENDING → SUCCESS)
   - Payment refund (SUCCESS → REFUNDED)
   - Invalid state transition rejection

---

## Docker Deployment

### Build Docker Image
```bash
docker build -f Dockerfile -t payment-service:latest .
```

### Run Container
```bash
docker run -d \
  -p 8090:8090 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-host:5432/payment_service \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=your_password \
  payment-service:latest
```

### Docker Compose (with PostgreSQL)
Use `docker-compose.yml` to run both the app and database:
```bash
docker-compose up -d
```

Server will be available at `http://localhost:8090`

---

## Configuration

### Development Environment
Edit `grails-app/conf/application.yml`:
```yaml
spring:
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQL10Dialect
  datasource:
    url: jdbc:postgresql://localhost:5432/payment_service
    username: postgres
    password: your_password
```

### Production Environment
Set environment variables:
```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db:5432/payment_service
export SPRING_DATASOURCE_USERNAME=prod_user
export SPRING_DATASOURCE_PASSWORD=secure_password
```

### Database Creation
Tables are automatically created by Hibernate on first run (`dbCreate: update` in config).

Manual creation (optional):
```sql
CREATE TABLE merchant (
  id BIGSERIAL PRIMARY KEY,
  version BIGINT,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  api_key VARCHAR(255) UNIQUE,
  active BOOLEAN DEFAULT true,
  date_created TIMESTAMP,
  last_updated TIMESTAMP
);

CREATE TABLE payment_transaction (
  id BIGSERIAL PRIMARY KEY,
  version BIGINT,
  reference VARCHAR(255) NOT NULL UNIQUE,
  amount NUMERIC(19,2) NOT NULL,
  currency VARCHAR(10) NOT NULL,
  description TEXT,
  status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
  merchant_id BIGINT NOT NULL REFERENCES merchant(id),
  date_created TIMESTAMP,
  last_updated TIMESTAMP
);
```

---

## Postman Collection

**Quick Import:**
Use the provided Postman collection file included in the project root: `Payment-System.postman_collection.json`

**Or Import from URL:**
1. Open Postman
2. Click "Import" → "Link"
3. Paste the collection URL or upload the file
4. Collection includes all endpoints with pre-configured requests and examples

**Environment Setup:**
1. Create Postman environment: `Payment Service Dev`
2. Add variable: `api_key` = (your merchant API key)
3. Add variable: `base_url` = `http://localhost:8090`

---

## Troubleshooting

### Application Won't Start

**Error:** `Connection refused to PostgreSQL`
- **Solution:** Ensure PostgreSQL is running and accessible at `localhost:5432`
- **Command:** `psql -U postgres -h localhost` to test connection

**Error:** `Database 'payment_service' does not exist`
- **Solution:** Create the database: `CREATE DATABASE payment_service;`

### Tests Fail with WebDriver Error

**Error:** `Could not find method autoDownload()`
- **Solution:** Already fixed in `build.gradle`. Run: `./gradlew clean build`

### API Returns 401 Unauthorized

**Ensure:**
1. X-API-KEY header is present in request
2. API key value is correct (from merchant creation response)
3. Merchant account is active (check database: `SELECT * FROM merchant;`)

### API Returns 400 Bad Request

**Check:**
1. Request body is valid JSON
2. Required fields are present (reference, amount, currency for payments)
3. Amount is greater than 0
4. Dates are in yyyy-MM-dd format for filtering

---

## Architecture & Design

### Key Design Patterns

1. **Service Layer Pattern**: Business logic in services, controllers remain thin
2. **Command Object Pattern**: Request validation via command objects separate from domain
3. **Repository Pattern**: GORM provides transparent data access
4. **State Machine Pattern**: Payment status transitions (PENDING → SUCCESS → REFUNDED)
5. **API Key Authentication**: Merchant identification and isolation

### Security Features

- API key authentication on all payment endpoints
- Merchant isolation (payments scoped to API key owner)
- Input validation on all endpoints
- SQL injection prevention via GORM/Hibernate
- Request size limits via Spring Boot defaults

### Performance Optimizations

- Pagination on list endpoints (max 500 items per request)
- Indexed queries on reference and email fields
- Connection pooling (5 initial, 20 max connections)
- Transaction management to ensure data consistency
