# ForecastIQ — Retail Demand Forecasting Platform

A full-stack retail intelligence platform built with **Spring Boot 3.2** and a modern single-page dashboard UI. Provides demand forecasting, inventory optimization, and real-time KPI monitoring for retail operations.

---

## Features

- **Demand Forecasting** — Generate 7-day demand forecasts using Simple Moving Average (SMA) or Weighted Moving Average (WMA)
- **Inventory Optimization** — Calculate Safety Stock, Reorder Point, and Economic Order Quantity (EOQ)
- **Operations Dashboard** — Live KPIs: revenue, units sold, reorder alerts, top sellers, and demand by category
- **Product Catalog** — Full CRUD for product management with search and category filtering
- **Reorder Alerts** — Automatic detection of products at or below their reorder point with urgency levels
- **Interactive Charts** — Bar and line charts powered by Chart.js with gradient fills

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17 · Spring Boot 3.2 · Spring Data JPA |
| Database | H2 (in-memory) · Hibernate 6 |
| API Docs | SpringDoc OpenAPI 2 (Swagger UI) |
| Frontend | Vanilla JS · Tailwind CSS · Chart.js 4 |
| Build | Maven 3 |

---

## Getting Started

**Prerequisites:** Java 17+, Maven 3.8+

```bash
# Clone the repo
git clone https://github.com/Niteshsai117/retail-forecast-platform.git
cd retail-forecast-platform

# Run the app
mvn spring-boot:run
```

The app starts in ~2 seconds. Open your browser:

| URL | Description |
|---|---|
| `http://localhost:8080` | Dashboard UI |
| `http://localhost:8080/swagger-ui/index.html` | Swagger API docs |
| `http://localhost:8080/h2-console` | H2 database console |

**H2 console settings:** JDBC URL `jdbc:h2:mem:forecastdb` · Username `sa` · Password *(empty)*

---

## API Reference

### Dashboard
| Method | Endpoint | Description |
|---|---|---|
| GET | `/dashboard/summary` | KPIs for last 30 days |

### Products
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/products` | List all products |
| POST | `/api/products` | Create a product |
| GET | `/api/products/{id}` | Get by ID |
| GET | `/api/products/sku/{sku}` | Get by SKU |
| GET | `/api/products/category/{category}` | Filter by category |
| GET | `/api/products/categories` | List all categories |
| PUT | `/api/products/{id}` | Update a product |
| DELETE | `/api/products/{id}` | Delete a product |

### Forecasts
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/forecasts/generate` | Generate a demand forecast |
| GET | `/api/forecasts/product/{productId}/history` | Forecast history for a product |

**Forecast request body:**
```json
{
  "productId": 1,
  "algorithm": "SMA",
  "windowSize": 7,
  "serviceLevel": 0.95
}
```

### Inventory
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/inventory/optimize` | Compute Safety Stock, ROP, EOQ |

**Inventory request body:**
```json
{
  "productId": 1,
  "orderingCost": 50.0,
  "holdingCostPerUnit": 5.0,
  "serviceLevel": 0.95
}
```

---

## Seed Data

The app loads 5 products and 30 days of sales history on startup (`src/main/resources/data.sql`):

| Product | SKU | Category | Price | Stock |
|---|---|---|---|---|
| Wireless Headphones | ELEC-001 | Electronics | $79.99 | 150 |
| Bluetooth Speaker | ELEC-002 | Electronics | $59.99 | 28 |
| Running Shoes | CLTH-001 | Clothing | $89.99 | 200 |
| Yoga Mat | SPRT-001 | Sports | $34.99 | 45 |
| Organic Coffee Beans | FOOD-001 | Food & Beverage | $24.99 | 500 |

---

## Project Structure

```
src/main/java/com/retail/forecastiq/
├── controller/      # REST endpoints
├── service/         # Business logic (forecasting, optimization)
├── repository/      # Spring Data JPA repositories
├── entity/          # JPA entities
├── dto/             # Request/response DTOs
├── enums/           # ForecastAlgorithm enum
├── exception/       # Global error handling
├── scheduler/       # Scheduled forecast jobs
└── config/          # OpenAPI config

src/main/resources/
├── static/index.html    # Single-page dashboard UI
├── application.properties
└── data.sql             # Seed data
```

---

## Testing

Unit tests cover `ForecastService` and `InventoryService` using JUnit 5 + Mockito. Run them with:

```bash
mvn test
```

### ForecastService tests (`ForecastServiceTest`)

| Nested class | What's covered |
|---|---|
| `SmaTests` | Correct arithmetic mean, empty list → 0, null → 0, single value, all-identical values, zeros in window |
| `WmaTests` | Manual weighted calculation, WMA > SMA when recent values are high, WMA < SMA when recent values are low, single value, all-identical |
| `StdDevTests` | Known population std dev (2,4,4,4,5,5,7,9 → σ=2), single value → 0, null/empty → 0, identical values → 0 |
| `GenerateForecastTests` | `InsufficientDataException` on empty or single-record history, correct SMA daily demand, `needsReorder` flag when stock ≤ ROP, 7-day breakdown with correct start date, `dataPointsUsed` reflects window |

### InventoryService tests (`InventoryServiceTest`)

| Nested class | What's covered |
|---|---|
| `SafetyStockTests` | Formula verified at 90 / 95 / 99% service levels, zero std dev → 0, zero lead time → 0, monotone ordering (90 < 95 < 99) |
| `ReorderPointTests` | Typical case, zero lead time equals safety stock only, all zeros → 0, linear increase with lead time |
| `EoqTests` | Formula verified against √(2DS/H), zero holding cost → MAX\_VALUE, zero demand → 0, higher ordering cost → larger EOQ, higher holding cost → smaller EOQ |
| `ZScoreTests` | Lookup values at 90 / 95 / 99%, below-90 fallback |
| `OptimizeTests` | `needsReorder=true` when stock below ROP, recommendations always non-empty, empty sales history → graceful 0 demand without exception |

---

## Business Impact

| Problem | How ForecastIQ addresses it |
|---|---|
| **Stockouts** | Reorder Point alerts trigger before stock runs out, accounting for supplier lead time and demand variability |
| **Overstock costs** | EOQ calculates the mathematically optimal order quantity to minimise the sum of ordering and holding costs |
| **Reactive ordering** | Shifting from reactive to proactive replenishment: the 7-day forecast shows demand before it happens |
| **Urgency triage** | Reorder alerts are classified as CRITICAL / HIGH / MEDIUM so teams prioritise the right products first |
| **Demand uncertainty** | Safety stock is computed using real demand standard deviation, not a fixed buffer — so high-variance products get larger buffers automatically |
| **Category insight** | Avg daily demand by category surfaces which segments are growing, enabling better buying decisions |

---

## Forecasting Algorithms

**SMA (Simple Moving Average)** — Averages demand equally across the last N days.

**WMA (Weighted Moving Average)** — Assigns higher weights to more recent days, making it more responsive to recent trends.

Both algorithms feed into inventory metrics:
- **Safety Stock** = Z-score × σ(demand) × √(lead time)
- **Reorder Point** = (avg daily demand × lead time) + safety stock
- **EOQ** = √(2 × annual demand × ordering cost / holding cost)
