# WEX Purchase Transaction Manager

A RESTful API for storing purchase transactions in US dollars and retrieving them with currency conversion using the [Treasury Reporting Rates of Exchange](https://fiscaldata.treasury.gov/datasets/treasury-reporting-rates-exchange/treasury-reporting-rates-of-exchange).

## Tech Stack

- **Java 17** / **Spring Boot 3.2**
- **H2** embedded database (file-backed, no install needed)
- **Spring WebFlux** (WebClient) for Treasury API integration
- **SpringDoc OpenAPI** for interactive API docs
- **JUnit 5 + Mockito + MockWebServer** for testing

## Quick Start

### Prerequisites
- Java 17+ (tested with OpenJDK 21)

### Run the Application

```bash
# Unix/Mac
./gradlew bootRun

# Windows
gradlew.bat bootRun
```

The server starts at **http://localhost:8080**.

### Run Tests

```bash
./gradlew test
```

## API Reference

### Store a Purchase Transaction

```
POST /api/transactions
Content-Type: application/json
```

**Request body:**
```json
{
    "description": "Office supplies",
    "transactionDate": "2024-12-15",
    "purchaseAmount": 150.42
}
```

**Response (201 Created):**
```json
{
    "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
    "description": "Office supplies",
    "transactionDate": "2024-12-15",
    "purchaseAmount": 150.42
}
```

**Validation rules:**
| Field | Constraint |
|-------|-----------|
| `description` | Required, max 50 characters |
| `transactionDate` | Required, valid date (`YYYY-MM-DD`) |
| `purchaseAmount` | Required, positive, rounded to nearest cent |

### Retrieve with Currency Conversion

```
GET /api/transactions/{id}?currency={country-currency}
```

The `currency` parameter uses the Treasury API's `country_currency_desc` format (e.g., `Canada-Dollar`, `Japan-Yen`, `Euro Zone-Euro`).

**Example:**
```
GET /api/transactions/f47ac10b-...?currency=Canada-Dollar
```

**Response (200 OK):**
```json
{
    "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
    "description": "Office supplies",
    "transactionDate": "2024-12-15",
    "originalPurchaseAmount": 150.42,
    "exchangeRate": 1.362,
    "convertedAmount": 204.87,
    "targetCurrency": "Canada-Dollar"
}
```

**Error (422)** — if no exchange rate exists within 6 months of the transaction date:
```json
{
    "status": 422,
    "error": "Currency Conversion Failed",
    "message": "The purchase cannot be converted to the target currency..."
}
```

## Additional Endpoints

| URL | Description |
|-----|-------------|
| `/swagger-ui.html` | Interactive API documentation |
| `/h2-console` | Database admin console (JDBC URL: `jdbc:h2:file:./data/purchasedb`) |

## Design Decisions

- **UUID identifiers** — no sequential guessing, safe for distributed systems
- **BigDecimal for money** — avoids floating-point precision loss on financial calculations
- **File-backed H2** — data survives restarts without needing an external database
- **MockWebServer in tests** — the Treasury API is fully mocked so tests run offline and deterministically
- **HALF_UP rounding** — standard banker's rounding for the converted amount per the spec ("rounded to two decimal places")

## Project Structure

```
src/main/java/com/wex/purchasetransactions/
├── config/             WebClient bean
├── controller/         REST endpoints
├── dto/                Request/response objects
├── entity/             JPA entities
├── exception/          Custom exceptions + global handler
├── repository/         Spring Data JPA interfaces
└── service/            Business logic + Treasury API client
```