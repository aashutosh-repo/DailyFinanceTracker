# Stock Analysis Testing Guide

## 5-Minute Quick Start

1. Open PowerShell in `DailyFinanceTracker`.
2. Start PostgreSQL with pgvector:

   ```powershell
   docker compose up -d postgres
   ```

3. Start Ollama and pull the Python chat model:

   ```powershell
   ollama serve
   ollama pull qwen2.5-coder:7b
   ollama pull nomic-embed-text
   ```

4. Start the Spring Boot API in another terminal:

   ```powershell
   cd C:\Users\aashu\Projects\DailyFinanceTracker
   mvn spring-boot:run
   ```

5. Start the Python service in another terminal. The repository does not currently provide dependencies in `aiservices/requirement.txt`; see [Python AI service](#step-4-start-the-python-ai-service) before running this command.

   ```powershell
   cd C:\Users\aashu\Projects\DailyFinanceTracker\aiservices
   .\venv\Scripts\Activate.ps1
   uvicorn app.main:app --reload --port 8000
   ```

6. Start Angular:

   ```powershell
   cd C:\Users\aashu\Projects\personal-finance
   npm install
   npm start
   ```

7. Open `http://localhost:4200`, register/login, create company `TCS`, sync historical data, then open **Stock Analysis** at `http://localhost:4200/v1/stocks/analysis`.

8. Test the LangChain proxy with `POST http://localhost:8080/api/ai/chat` and the same question.

> The active market provider is `MOCK`. It does not call NSE or another live exchange. Historical mock data is generated for calendar days, and the current mock quote is deterministic for a symbol.

## 1. What This Application Does

The Stock Analysis flow combines company metadata, stored historical prices, deterministic backend calculations, a latest quote, and optional AI explanation.

```text
Company
   |
   v
Market Data Provider (currently MOCK)
   |
   +--> Historical Prices --> PostgreSQL --> Statistics / Technical Analysis
   |
   +--> Current Quote      --> PostgreSQL --> Angular REST polling
                                            |
                                            v
                                      Python AI service
                                            |
                          LangChain tools + optional RAG + Ollama
```

- **Company:** Stores a symbol, name, exchange, sector, and industry. A company must exist before market APIs can find it.
- **Market data provider:** Supplies historical OHLCV data and current quotes. The active implementation is `MockMarketDataProvider` and its source is `MOCK`.
- **Historical prices:** Stored in `market_prices`, one row per company/date. They are used for charts, statistics, and technical analysis.
- **Statistics:** Calculates start/end price, high/low, average price, absolute change, and percentage change for a requested date range.
- **Technical analysis:** Java code calculates existing indicators plus structured SMA, RSI, return, volatility, volume-trend, and trend fields. The LLM does not calculate these numbers.
- **Current quote:** Stored separately in `market_quotes`, one latest row per company. It contains price, daily change, OHLC, volume, timestamps, source, and market status.
- **Angular polling:** The Stock Analysis page requests a quote immediately and approximately every 30 seconds while the component is active.
- **LangChain tools:** The Python service calls Spring APIs and passes returned facts to the planner and answer-generation model.
- **RAG:** Optional document retrieval from `aiservices/knowledge`. It is disabled by default and currently has known import/configuration defects described later.

## 2. Prerequisites

### Required Software

| Software | Required version/configuration | Purpose |
|---|---|---|
| Java | 21 | Spring Boot backend |
| Spring Boot | 3.4.4 | REST API and persistence |
| Maven | 3.9+ recommended | Build and run the backend |
| Node.js | 18+ documented; Angular project uses modern Node/npm | Angular development server |
| Angular | 19.2.x; package `@angular/core` is `^19.2.0` | Frontend |
| npm | 9+ documented | Frontend dependencies and scripts |
| Python | Not pinned by the repository; use a current Python version compatible with installed FastAPI/LangChain packages | AI service |
| PostgreSQL | Docker image is PostgreSQL 16 with pgvector | Application and vector data |
| pgvector | `pgvector/pgvector:pg16` Docker image | Vector extension |
| Ollama | Running at `http://localhost:11434` | Local chat and embedding models |
| Docker Desktop | Required for the supplied PostgreSQL setup | Database container |

### Ollama models

The Python AI service defaults to:

- Chat: `qwen2.5-coder:7b` (`OLLAMA_CHAT_MODEL` can override it)
- Embeddings: `nomic-embed-text` (`OLLAMA_EMBEDDING_MODEL` can override it)

The Spring configuration also contains `phi3:mini` in `OllamaConfig`, but the Python workflow uses `qwen2.5-coder:7b` by default. Pull the Python defaults first.

```powershell
ollama list
ollama pull qwen2.5-coder:7b
ollama pull nomic-embed-text
```

### Repository configuration caveat

The active Spring profile is `local`. The active local database settings are:

```text
Host: localhost
Port: 5432
Database: finance-db
User: postgres
Password: postgres
```

The older `docs/QUICK_START.md` describes a different database (`daily_finance_tracker`) and a `dev` profile. Follow `application-local.yml` and `docker-compose.yml` for the current stock flow instead.

## 3. Application Architecture

| Component | Default URL | Responsibility |
|---|---|---|
| Angular | `http://localhost:4200` | Login, company setup, sync UI, charts, quote display, and polling |
| Spring Boot | `http://localhost:8080` | Company, market data, sync jobs, statistics, technical calculations, security |
| PostgreSQL/pgvector | `localhost:5432` | Companies, historical prices, sync jobs, quotes, and optional vector data |
| Python FastAPI | `http://localhost:8000` | LangChain planning, tool calls, answer generation, and optional asynchronous jobs |
| Ollama | `http://localhost:11434` | Local chat and embedding model runtime |

The supplied `docker-compose.yml` starts only the `postgres` service. It does **not** start Spring Boot, Angular, Python, Ollama, Redis, or Kafka.

## 4. Start Every Service

### Step 1 - Start PostgreSQL and pgvector

From the backend root:

```powershell
cd C:\Users\aashu\Projects\DailyFinanceTracker
docker compose up -d postgres
docker compose ps
```

The service is `postgres`, its container name is `postgres-db`, and it exposes `localhost:5432`.

Verify the database:

```powershell
docker exec postgres-db pg_isready -U postgres -d finance-db
```

Verify pgvector:

```powershell
docker exec -it postgres-db psql -U postgres -d finance-db -c "SELECT extname FROM pg_extension WHERE extname = 'vector';"
```

The compose initialization script enables the `vector` extension. Spring Hibernate uses `ddl-auto: update`; there is no required stock-specific Flyway migration for the current quote table.

### Step 2 - Start Ollama

Start Ollama using the normal Ollama desktop application or:

```powershell
ollama serve
```

In another terminal:

```powershell
curl.exe http://localhost:11434/api/tags
ollama list
```

Pull missing models:

```powershell
ollama pull qwen2.5-coder:7b
ollama pull nomic-embed-text
```

Expected result: the tags response is returned and both model names appear in `ollama list`.

### Step 3 - Start the Python AI service

The entrypoint is `aiservices/app/main.py` and the FastAPI object is named `app`.

```powershell
cd C:\Users\aashu\Projects\DailyFinanceTracker\aiservices
python -m venv venv
.\venv\Scripts\Activate.ps1
```

`aiservices/requirement.txt` is currently empty. The source imports FastAPI, Uvicorn, Pydantic, Requests, LangChain, LangGraph, LangChain Ollama, LangChain Postgres, Psycopg, and related packages. Install those packages in the virtual environment using your project-approved dependency process before starting the service. There is currently **no complete dependency lockfile or requirements list** in the repository.

Start the service:

```powershell
uvicorn app.main:app --reload --port 8000
```

Verify the process by opening `http://localhost:8000/docs` or:

```powershell
curl.exe http://localhost:8000/openapi.json
```

The Spring service calls the AI service through `AI_SERVICE_URL`, defaulting to `http://localhost:8000`.

### Step 4 - Start Spring Boot

Ensure PostgreSQL is running and the local settings are available. From the backend root:

```powershell
cd C:\Users\aashu\Projects\DailyFinanceTracker
mvn spring-boot:run
```

The application listens on `http://localhost:8080`.

Verify:

```powershell
curl.exe http://localhost:8080/actuator/health
curl.exe http://localhost:8080/ping
```

Build without starting:

```powershell
mvn clean install
```

The Dockerfile builds with Maven and Java 21, but the current Compose file does not define a backend service.

### Step 5 - Start Angular

From the frontend root:

```powershell
cd C:\Users\aashu\Projects\personal-finance
npm install
npm start
```

Open `http://localhost:4200`. The frontend calls Spring at `http://localhost:8080` and uses `withCredentials` on several requests.

Useful frontend checks:

```powershell
npm run build
npm test
```

The repository currently has an unrelated production prerender error for the parameterized route `v1/stocks/:symbol`; a development build is the most useful compile check for the Stock Analysis changes.

## 5. Verify Services Before Testing

- [ ] **PostgreSQL running:** `docker compose ps` shows `postgres` running.
- [ ] **pgvector available:** the `SELECT extname ...` command returns `vector`.
- [ ] **Ollama running:** `curl.exe http://localhost:11434/api/tags` succeeds.
- [ ] **Required models available:** `ollama list` shows `qwen2.5-coder:7b` and `nomic-embed-text`.
- [ ] **Python service running:** `http://localhost:8000/docs` opens.
- [ ] **Spring Boot running:** `/actuator/health` or `/ping` responds.
- [ ] **Angular running:** `http://localhost:4200` opens.
- [ ] **User registered/logged in:** login succeeds in the UI.
- [ ] **Company exists:** `GET /api/stocks/companies` contains the symbol being tested.

## 6. Authentication and User Setup

Stock endpoints are currently permitted by Spring Security under `/api/stocks/**`, but the Angular Stock Analysis routes are protected by `AuthGuard`. Register and log in through the UI before opening the analysis page.

### Register

```powershell
curl.exe -X POST http://localhost:8080/api/v1/auth/register `
  -H "Content-Type: application/json" `
  -d '{"username":"stocktester","email":"stocktester@example.com","password":"StrongPass123!","fullName":"Stock Tester","currency":"INR"}'
```

Actual required registration fields are `username`, `email`, `password`, and `fullName`. Optional fields are `phone`, `countryCode`, `currency`, `dateOfBirth`, and `bio`. Passwords must be at least eight characters and contain uppercase, lowercase, digit, and special character.

### Login

```powershell
curl.exe -i -c cookies.txt -X POST http://localhost:8080/api/v1/auth/login `
  -H "Content-Type: application/json" `
  -d '{"email":"stocktester@example.com","password":"StrongPass123!"}'
```

The backend sets `access_token` and `refresh_token` cookies. The cookies are marked `Secure`, so browser authentication over plain local HTTP may not work consistently. The Angular application uses credentialed requests; if the UI repeatedly returns to login, inspect cookie behavior in browser developer tools. Do not put real credentials in documentation or source control.

## 7. Test 1 - Create a Company

Run this before all stock tests because historical prices, statistics, technical analysis, and quotes look up an existing company.

```powershell
curl.exe -X POST http://localhost:8080/api/stocks/companies `
  -H "Content-Type: application/json" `
  -d '{"symbol":"TCS","name":"Tata Consultancy Services","exchange":"NSE","sector":"Information Technology","industry":"IT Services"}'
```

Actual request fields are `symbol`, `name`, `exchange`, `sector`, and `industry`. The endpoint returns HTTP `201` and a company response containing:

- `id`
- `symbol`
- `name`
- `exchange`
- `sector`
- `industry`

Expected result: a TCS company is created. Creating the same unique symbol again should fail rather than create a second company.

## 8. Test 2 - Retrieve the Company

```powershell
curl.exe http://localhost:8080/api/stocks/companies/TCS
```

Also list all companies:

```powershell
curl.exe http://localhost:8080/api/stocks/companies
```

Verify the symbol is uppercase as stored/returned and that the name, exchange, sector, and industry match the create request.

## 9. Test 3 - Sync Historical Market Data

Market sync asks the provider for historical OHLCV records, creates a sync job, stores or updates `MarketPrice` rows, and marks the job successful or failed.

The active synchronous-start endpoint is:

```powershell
curl.exe -X POST "http://localhost:8080/api/stocks/TCS/prices/sync?from=2025-01-01&to=2025-12-31"
```

Response shape:

```json
{"jobId":"...","status":"RUNNING","message":"Market data sync started"}
```

The endpoint starts an asynchronous worker and normally returns `RUNNING` immediately. There is also:

```powershell
curl.exe -X POST "http://localhost:8080/api/stocks/TCS/prices/sync/async?from=2025-01-01&to=2025-12-31"
```

That endpoint returns HTTP `202` with `jobId` and `status`, normally initially `QUEUED`. The Angular Sync Jobs page uses the first `/prices/sync` endpoint. Both endpoints use `from` and `to` query parameters and no JSON body.

The mock provider generates a record for every calendar date in the range, including weekends. Use a range such as `2025-01-01` to `2025-12-31` for enough records to exercise SMA 200.

## 10. Monitor a Sync Job

Fetch one job by ID:

```powershell
curl.exe http://localhost:8080/api/stocks/sync-jobs/{jobId}
```

List jobs:

```powershell
curl.exe "http://localhost:8080/api/stocks/sync-jobs?symbol=TCS&page=0&size=20&sort=startedAt,desc"
```

Supported status values are exactly:

- `QUEUED`
- `RUNNING`
- `SUCCESS`
- `FAILED`

The job response includes `jobId`, `symbol`, `status`, `fromDate`, `toDate`, `provider`, `totalRecords`, `insertedRecords`, `updatedRecords`, `errorMessage`, `startedAt`, and `completedAt`.

The Angular Sync Jobs page polls a selected job approximately every two seconds until `SUCCESS` or `FAILED`. A successful job has completed processing and historical prices can be queried. A failed job should expose an error message; do not treat `RUNNING` as completed.

Lifecycle:

```text
QUEUED -> RUNNING -> SUCCESS
                    |
                    +-> FAILED
```

## 11. Test 4 - Verify Historical Prices

```powershell
curl.exe "http://localhost:8080/api/stocks/TCS/prices?from=2025-01-01&to=2025-01-10"
```

Each returned record contains:

| Field | Meaning |
|---|---|
| `priceDate` | Historical record date |
| `open` | Opening price |
| `high` | Highest price in the record |
| `low` | Lowest price in the record |
| `close` | Closing price |
| `volume` | Volume value |
| `source` | Provider name; currently `MOCK` |

Expected result: a JSON array containing records stored by the sync. If the array is empty, confirm the sync completed successfully and that the requested dates are within its range.

## 12. Test 5 - Statistics

```powershell
curl.exe "http://localhost:8080/api/stocks/TCS/statistics?from=2025-01-01&to=2025-12-31"
```

The response contains:

- `symbol`
- `fromDate`
- `toDate`
- `totalRecords`
- `startPrice`
- `endPrice`
- `highestPrice`
- `lowestPrice`
- `averagePrice`
- `priceChange`
- `priceChangePercentage`

This endpoint calculates basic historical performance from stored `MarketPrice` rows. It is not the technical-analysis endpoint. No historical rows produces an error instead of invented values.

## 13. Test 6 - Technical Analysis

The technical endpoint is separate from basic statistics:

```powershell
curl.exe -X POST "http://localhost:8080/api/v1/stocks/TCS/technical?from=2025-01-01&to=2025-12-31&period=14"
```

`period` must be between `2` and `200`, and the requested history must contain at least that many records. The response includes the existing indicator list and structured fields such as:

- `currentPrice`
- `dailyReturn`
- `periodReturn`
- `sma20`
- `sma50`
- `sma200`
- `rsi14`
- `volatility`
- `volumeTrend`
- `trend`
- `fiftyTwoWeeksHigh`
- `fiftyTwoWeeksLow`

MACD and Bollinger Bands are also returned by the existing implementation when enough data exists. `sma200`, `sma50`, RSI 14, and other values may be `null` when the history is insufficient. The backend calculates these values; the LLM is not asked to calculate them.

Important: the existing `StockDetailsComponent` client has a method mismatch for this endpoint and calls it with `GET`, while Spring exposes `POST`. The Stock Analysis page uses `POST` correctly. Test the endpoint with the `POST` command above.

## 14. Test 7 - Current Quote

```powershell
curl.exe http://localhost:8080/api/stocks/TCS/quote
```

The response fields are:

| Field | Meaning |
|---|---|
| `symbol` | Company symbol |
| `companyName` | Company name |
| `exchange` | Company exchange |
| `price` | Latest quote price |
| `change` | Daily change |
| `changePercent` | Daily percentage change |
| `open` | Current-session open value |
| `high` | Current-session high value |
| `low` | Current-session low value |
| `volume` | Current quote volume |
| `marketTime` | Provider market timestamp |
| `fetchedAt` | Time the application fetched/returned the quote |
| `source` | Currently `MOCK` |
| `marketStatus` | `OPEN` or `CLOSED` using weekday/NSE session-hour logic |

A **historical price** is a dated OHLCV record stored in `market_prices` and used for chart/history calculations. A **current quote** is the latest separate snapshot stored in `market_quotes`; it is updated rather than inserting a new row for every poll.

The mock quote is deterministic by symbol, but its timestamps are current. It is not a real exchange quote and must not be treated as investment advice.

## 15. Near-Real-Time Polling Test

1. Log in at `http://localhost:4200`.
2. Open `http://localhost:4200/v1/stocks/analysis`.
3. Enter/select `TCS`.
4. Select a date range that was synchronized, such as `2025-01-01` through `2025-12-31`.
5. Click **Analyze**.
6. Confirm the historical chart and statistics appear.
7. Confirm the quote panel shows price, change, market status, source, and last fetched time.
8. Open browser developer tools and filter Network requests for `/api/stocks/TCS/quote`.
9. Confirm one request is made immediately after analysis loads.
10. Wait approximately 30 seconds and confirm another quote request and refreshed `fetchedAt` value.
11. During a request, the UI status can show `Updating`; after success it shows `Live`.
12. Temporarily stop Spring Boot or block the request to test `Update unavailable`. The last successful quote should remain visible.
13. Navigate to another page.
14. Confirm no further quote requests are made after the analysis component is destroyed.

The polling implementation uses RxJS `timer(0, 30000)`, `switchMap`, and teardown through `takeUntil` plus component cleanup. It does not use `setInterval`.

## 16. Complete Stock Analysis UI Test

1. Register and log in.
2. Open **Stocks > Companies** or call the create-company API.
3. Ensure `TCS` exists.
4. Open **Stocks > Sync Jobs** at `/v1/stocks/sync-jobs`.
5. Start a historical sync for a sufficiently large range.
6. Wait for `SUCCESS`.
7. Open **Stocks > Stock Analysis** at `/v1/stocks/analysis`.
8. Enter `TCS`, choose the synced date range, and click **Analyze**.
9. Verify the overview shows the selected company and date range.
10. Verify the current quote shows price, daily change, source `MOCK`, market status, and updated time.
11. Verify the closing-price Chart.js chart has points when historical data exists.
12. Verify statistics show start price, high, low, average, and latest volume.
13. Verify technical values. Values requiring more history should show `Unavailable`/`null`, not fabricated numbers.
14. Verify quote status changes between `Updating`, `Live`, and `Update unavailable` as appropriate.
15. Use the existing **Generate AI analysis** action only after the Python AI service and Ollama are available.

Correct behavior is a user-facing error such as `Unable to load stock information` or an unavailable state. Raw Java/Python stack traces should not be shown in the page.

## 17. LangChain Testing

The preferred application endpoint is Spring's proxy:

```http
POST http://localhost:8080/api/ai/chat
Content-Type: application/json

{"question":"How is TCS performing right now?"}
```

Spring forwards this request to the Python service at `AI_SERVICE_URL` plus `/api/ai/chat`. The direct Python endpoint is also available when testing the AI service in isolation:

```http
POST http://localhost:8000/api/ai/chat
Content-Type: application/json

{"question":"How is TCS performing right now?"}
```

The workflow asks Ollama for a structured tool plan, executes selected tools, then asks Ollama to explain the returned facts. The currently registered tools are:

| Tool | Purpose | Example question |
|---|---|---|
| `get_stock_prices` | Retrieve historical prices for `symbol`, `from_date`, and `to_date` | `Show TCS prices during August 2025.` |
| `get_current_quote` | Retrieve the Spring current quote endpoint | `What is TCS trading at right now?` |
| `get_stock_statistics` | Retrieve historical basic statistics | `How did TCS perform during August 2025?` |
| `get_company_info` | Retrieve company metadata | `What exchange is TCS listed on?` |
| `get_technical_analysis` | Intended to retrieve deterministic technical calculations | `What is TCS RSI?` |
| `search_stock_knowledge` | Search the optional knowledge base | `What services does TCS provide?` |

The planner prompt explicitly tells the model to use `get_current_quote` for latest/current questions, historical prices/statistics for past periods, and company/RAG tools for business-information questions. Tool selection is model-driven, so exact calls are not guaranteed.

Known limitation: `get_technical_analysis` currently has a malformed Python URL and uses `form` instead of `from`, so it is **Not implemented reliably** through the Python tool even though the Spring endpoint exists. Test technical analysis directly against Spring until this is fixed.

The Stock Analysis page's existing **Generate AI analysis** button is a separate path: its `ChatbotService` calls `/api/bot/chat` and is not the `/api/ai` LangChain research proxy. Test `/api/ai/chat` from the AI Research feature or with the API examples below when validating the tools documented here.

## 18. RAG Testing

Knowledge documents are stored in:

```text
DailyFinanceTracker/aiservices/knowledge/
  stock_analysis_guide.txt
  tcs_overview.txt
```

The ingestion script is `aiservices/app/rag/ingest.py`. It reads `knowledge/*.txt` relative to the current working directory, splits documents into 500-character chunks with 100-character overlap, adds metadata, and calls the vector store.

RAG is disabled by default. To attempt ingestion, configure a vector store connection and enable it:

```powershell
$env:VECTOR_STORE_ENABLED = "true"
$env:VECTOR_DATABASE_URL = "postgresql+psycopg://postgres:postgres@localhost:5432/finance-db"
cd C:\Users\aashu\Projects\DailyFinanceTracker\aiservices
.\venv\Scripts\Activate.ps1
python -m app.rag.ingest
```

The default Python vector-store connection is `postgresql+psycopg://postgres:postgres@localhost:5432/postgres`, which does not match the active Spring database `finance-db`; set `VECTOR_DATABASE_URL` explicitly for a deliberate test.

Expected ingestion output includes document count, chunk count, and `Documents indexed successfully`. Verify the vector collection using the configured PostgreSQL/vector-store tooling, then ask:

```json
{"question":"What services does TCS provide?"}
```

The answer should use `search_stock_knowledge` if the planner selects it and should contain grounded knowledge/source information when retrieval succeeds.

Known RAG limitations:

- RAG is disabled unless `VECTOR_STORE_ENABLED=true`.
- `ingest.py` imports `vector_store`, but the vector-store module currently exposes `_vector_store` and `get_vector_store`, so ingestion may fail before indexing.
- The retrieval module has related import/configuration defects.
- An unrelated question such as `What is the capital of France?` is not guaranteed to be rejected; depending on planner and retrieval behavior it may return no useful result or nearest-vector content. Treat any stock answer to it as a limitation, not proof of correct grounding.

## 19. Multi-Tool AI Test

Send:

```powershell
curl.exe -X POST http://localhost:8080/api/ai/chat `
  -H "Content-Type: application/json" `
  -d '{"question":"Tell me about TCS and explain how it performed during August 2025."}'
```

The intended high-level flow is:

```text
User question
   |
   v
Ollama planner
   |
   +--> get_company_info
   +--> get_stock_prices and/or get_stock_statistics
   +--> search_stock_knowledge when business knowledge is needed
   |
   v
Tool results
   |
   v
Ollama answer generation
   |
   v
Grounded response with tools_used and sources
```

Because the planner is an LLM, verify the actual `tools_used` field rather than assuming every intended tool was selected. For a current-performance question, inspect whether `get_current_quote` was selected.

## 20. Negative Tests

| Test | Input | Expected behavior | Must not happen |
|---|---|---|---|
| Unknown company | `GET /api/stocks/NO_SUCH_SYMBOL/quote` | Company-not-found error | A fabricated quote |
| Invalid symbol | Empty or malformed path symbol | Request fails or company lookup fails | A quote for another company |
| Invalid date range | `from=2025-12-31&to=2025-01-01` | Error or empty/invalid result according to current validation | Silent reversal of dates |
| Future date | Prices/statistics for dates after today | Likely empty data because no rows exist | Invented future market records |
| No historical data | Query before any successful sync | Empty prices or `No market price data found` for statistics | Invented statistics |
| Insufficient history | Technical analysis with fewer than required records | Error for requested base period; nullable extended indicators such as SMA 200 | Invented SMA 200/RSI values |
| Provider unavailable | Stop or replace the provider/backend | Quote/sync fails and UI shows an unavailable/error state | Old provider data presented as newly fetched without indication |
| Ollama unavailable | Stop Ollama, call `/api/ai/chat` | AI request fails or returns an unsuccessful result | AI claiming it used unavailable data |
| AI service unavailable | Stop Python service, use AI UI | AI error state; stock data remains usable | Stock screen crashing entirely |
| PostgreSQL unavailable | Stop `postgres-db`, call Spring API | Backend startup/request/database error | Successful persistence claim |
| Empty AI question | `{"question":""}` | Request should be rejected or fail to produce a useful grounded answer | Invented stock analysis |
| Unrelated AI question | `What is the capital of France?` | May return direct response, no tool result, or no useful RAG result; verify `grounded` and `tools_used` | Treating nearest vector text as verified finance fact |

## 21. End-to-End TCS Test

1. Start PostgreSQL/pgvector with `docker compose up -d postgres`.
2. Verify `vector` and `finance-db`.
3. Start Ollama and ensure `qwen2.5-coder:7b` and `nomic-embed-text` are available.
4. Start the Python service on port `8000` if its dependencies are installed.
5. Start Spring Boot on port `8080`.
6. Start Angular on port `4200`.
7. Register and log in.
8. Create TCS with the company API or Companies UI.
9. Retrieve TCS and verify its fields.
10. Start a sync for `2025-01-01` through `2025-12-31`.
11. Poll `/api/stocks/sync-jobs/{jobId}` until `SUCCESS`.
12. Query historical prices and confirm `source` is `MOCK`.
13. Query statistics and verify values are derived from returned historical records.
14. Query technical analysis with `POST /api/v1/stocks/TCS/technical` and verify insufficient indicators are null/unavailable.
15. Open `/v1/stocks/analysis`, select TCS and the synced date range, and click Analyze.
16. Verify the chart, statistics, technical section, and quote panel.
17. Wait about 30 seconds and verify another quote request.
18. Navigate away and verify polling stops.
19. Ask the AI: `How is TCS performing right now?` and inspect `tools_used`, `tools_results`, and `grounded`.
20. Ask: `What services does TCS provide?` and inspect whether company info/RAG tools are selected.
21. Ask: `Tell me about TCS and its recent performance.` and inspect the multi-tool result.

At every stage, confirm that a failure is reported as unavailable/error and that no price or technical number is invented.

## 22. API Quick Reference

| Feature | Method | Endpoint | Purpose |
|---|---|---|---|
| Register | POST | `/api/v1/auth/register` | Create a user |
| Login | POST | `/api/v1/auth/login` | Authenticate and set cookies |
| Create Company | POST | `/api/stocks/companies` | Create required company metadata |
| List Companies | GET | `/api/stocks/companies` | List companies |
| Get Company | GET | `/api/stocks/companies/{symbol}` | Retrieve one company |
| Start Sync | POST | `/api/stocks/{symbol}/prices/sync?from=YYYY-MM-DD&to=YYYY-MM-DD` | Start historical sync |
| Start Async Sync | POST | `/api/stocks/{symbol}/prices/sync/async?from=...&to=...` | Queue historical sync |
| List Sync Jobs | GET | `/api/stocks/sync-jobs` | Search/paginate jobs |
| Get Sync Job | GET | `/api/stocks/sync-jobs/{jobId}` | Monitor one job |
| Historical Prices | GET | `/api/stocks/{symbol}/prices?from=...&to=...` | Read stored OHLCV history |
| Statistics | GET | `/api/stocks/{symbol}/statistics?from=...&to=...` | Basic historical statistics |
| Current Quote | GET | `/api/stocks/{symbol}/quote` | Read latest quote |
| Technical Analysis | POST | `/api/v1/stocks/{symbol}/technical?from=...&to=...&period=14` | Backend technical calculations |
| AI Chat proxy | POST | `/api/ai/chat` | Spring proxy to the Python LangChain workflow |
| Direct AI Chat | POST | `http://localhost:8000/api/ai/chat` | Test Python LangChain service directly |
| Submit AI Job | POST | `/api/ai/chat/job` | Spring proxy for the currently unreliable async AI job |
| Get AI Job | GET | `/api/ai/chat/job/{jobId}` | Read the async AI job through Spring |

The stock endpoints are currently permitted by Spring Security. The Angular UI itself requires login because its routes are guarded.

## 23. Curl Examples

Create company:

```powershell
curl.exe -X POST http://localhost:8080/api/stocks/companies `
  -H "Content-Type: application/json" `
  -d '{"symbol":"TCS","name":"Tata Consultancy Services","exchange":"NSE","sector":"Information Technology","industry":"IT Services"}'
```

Start and monitor sync:

```powershell
$sync = curl.exe -s -X POST "http://localhost:8080/api/stocks/TCS/prices/sync?from=2025-01-01&to=2025-12-31" | ConvertFrom-Json
curl.exe "http://localhost:8080/api/stocks/sync-jobs/$($sync.jobId)"
```

Read prices, statistics, quote, and technical analysis:

```powershell
curl.exe "http://localhost:8080/api/stocks/TCS/prices?from=2025-01-01&to=2025-01-10"
curl.exe "http://localhost:8080/api/stocks/TCS/statistics?from=2025-01-01&to=2025-12-31"
curl.exe http://localhost:8080/api/stocks/TCS/quote
curl.exe -X POST "http://localhost:8080/api/v1/stocks/TCS/technical?from=2025-01-01&to=2025-12-31&period=14"
```

AI chat:

```powershell
curl.exe -X POST http://localhost:8080/api/ai/chat `
  -H "Content-Type: application/json" `
  -d '{"question":"How is TCS performing right now?"}'
```

To test the Python service without Spring, change the URL in that command to `http://localhost:8000/api/ai/chat`.

For authenticated endpoints outside the currently permitted stock paths, use the login response/cookies or a bearer token. Do not commit real tokens or credentials.

## 24. Postman Sequence

1. `POST /api/v1/auth/register` if a user does not exist.
2. `POST /api/v1/auth/login` and retain cookies if testing protected endpoints.
3. `POST /api/stocks/companies` to create TCS.
4. `GET /api/stocks/companies/TCS` to verify it.
5. `POST /api/stocks/TCS/prices/sync?from=2025-01-01&to=2025-12-31` and save `jobId`.
6. `GET /api/stocks/sync-jobs/{jobId}` until `status` is `SUCCESS`.
7. `GET /api/stocks/TCS/prices?from=2025-01-01&to=2025-12-31`.
8. `GET /api/stocks/TCS/statistics?from=2025-01-01&to=2025-12-31`.
9. `GET /api/stocks/TCS/quote`.
10. `POST /api/v1/stocks/TCS/technical?from=2025-01-01&to=2025-12-31&period=14`.
11. `POST /api/ai/chat` with a question body. This exercises Spring's proxy and then Python/Ollama.

The company must exist before sync, sync must succeed before historical statistics are useful, and the Python service plus Ollama must be running before AI tests.

## 25. Troubleshooting

| Problem | Likely cause | Solution |
|---|---|---|
| Company not found | Company was not created or symbol differs | Create TCS first and use the exact symbol |
| No market data | Sync has not reached `SUCCESS` | Poll the job endpoint and query the same date range |
| Statistics unavailable | No rows or invalid range | Sync a larger valid range and verify `from <= to` |
| SMA 200 unavailable | Fewer than 200 historical records | Sync a longer range; mock sync includes calendar days |
| Quote unavailable | Backend/provider/database issue | Check Spring logs, PostgreSQL, and `/api/stocks/TCS/quote` |
| Source is not real market data | Mock provider is active | This is expected; `source` is `MOCK` |
| Angular redirects to login | Auth guard or Secure cookie issue | Log in and inspect browser cookie/network behavior |
| Python import failure | `requirement.txt` is empty or dependencies missing | Install the imported FastAPI/LangChain packages in `aiservices/venv` |
| Ollama AI failure | Ollama stopped or model missing | Run `ollama serve`, `ollama list`, and pull the defaults |
| Vector search error | RAG disabled, wrong database, or known import defect | Set vector environment variables deliberately and inspect RAG modules |
| Technical AI tool fails | Malformed `get_technical_analysis` Python URL | Test the Spring POST endpoint directly; tool fix is pending |
| Async AI job fails | Current job constructor/API contract is incomplete | Use synchronous `POST /api/ai/chat`; async AI is not reliable |
| Spring cannot connect to database | Compose not running or database-name mismatch | Start `postgres`, use `finance-db`, and check `application-local.yml` |
| `mvn` is not recognized | Maven is not installed/on PATH | Install Maven 3.9+ or run through the project’s build environment |
| Angular production build fails | Existing prerender/budget configuration issues | Use `npx tsc -p tsconfig.app.json --noEmit` or development build for this flow |

## 26. Testing Checklist

### Basic

- [ ] PostgreSQL starts with Compose.
- [ ] pgvector extension is available.
- [ ] Spring Boot starts on port 8080.
- [ ] User registration/login works.
- [ ] Angular starts on port 4200.
- [ ] Company created.
- [ ] Company retrieved.
- [ ] Market sync starts.
- [ ] Sync reaches `SUCCESS`.
- [ ] Historical prices are available.
- [ ] Statistics are available.
- [ ] Technical endpoint returns backend-calculated values or null/unavailable values.

### Near Real-Time

- [ ] Current quote is available.
- [ ] Quote source is `MOCK`.
- [ ] Last fetched timestamp is shown.
- [ ] Immediate quote request occurs when analysis loads.
- [ ] Another request occurs approximately every 30 seconds.
- [ ] Last successful quote remains after a failed refresh.
- [ ] Polling stops after navigation away.
- [ ] Provider/backend failure is visible without a raw stack trace.

### AI and RAG

- [ ] Python `/api/ai/chat` responds.
- [ ] `get_stock_prices` works.
- [ ] `get_current_quote` works.
- [ ] `get_stock_statistics` works.
- [ ] `get_company_info` works.
- [ ] Direct Spring technical API works.
- [ ] RAG is tested only after enabling/configuring its vector store.
- [ ] Multi-tool question exposes the tools actually selected.
- [ ] Unrelated question behavior is recorded rather than assumed to be rejected.

## 27. Current Limitations

- The active provider is `MOCK`; there is no active real NSE/market API provider.
- Mock historical data is random per historical fetch and includes calendar days rather than exchange trading days.
- Current quote values are deterministic by symbol but are not live market data.
- No WebSocket, Kafka, Redis, or streaming infrastructure is implemented for quotes.
- RAG is disabled by default and has known import/database-configuration issues.
- `aiservices/requirement.txt` is empty, so Python setup is not reproducible from that file alone.
- The Python technical-analysis tool URL/parameter names are malformed.
- The asynchronous Python AI job endpoint is declared with `GET`, while Spring's proxy submits it with `GET` from a `POST /api/ai/chat/job` call and the Python job constructor currently omits its required status; use synchronous `/api/ai/chat` for testing.
- Some Angular stock-details client methods do not match the Spring technical endpoint method/path casing; the Stock Analysis page is the correct UI for this guide.
- The Angular production build has existing budget/prerender issues unrelated to the stock quote flow.
- The local login cookies are marked `Secure`, which can complicate authentication over plain `http://localhost`.
- There are no complete automated end-to-end tests for the entire stock flow.

## 28. Future Enhancements

These are future work, not current functionality:

- Replace the mock provider with a configured real market-data provider.
- Add exchange-calendar and holiday support.
- Add WebSocket or server-sent live updates.
- Add Kafka/Kafka Streams only if the project later needs event infrastructure.
- Fix and lock Python dependencies.
- Repair async AI job contracts and add reliable UI polling for them.
- Fix Python technical-analysis tool routing.
- Repair and enable RAG ingestion/retrieval against the intended database.
- Add news ingestion and sentiment analysis.
- Add more technical indicators and dedicated automated unit/integration tests.
- Resolve Angular prerender and bundle-budget configuration errors.

## 29. Source Files Used For This Guide

- Backend stock controller: `src/main/java/com/finance/tracker/stock/market/controller/StockMarketPriceController.java`
- Company controller: `src/main/java/com/finance/tracker/stock/market/controller/CompanyController.java`
- Sync controller: `src/main/java/com/finance/tracker/stock/market/controller/StockSyncJobController.java`
- Technical controller: `src/main/java/com/finance/tracker/stock/analysis/controller/AnalysisController.java`
- Security: `src/main/java/com/finance/tracker/config/SecurityConfig.java`
- Local configuration: `src/main/resources/application.yml` and `application-local.yml`
- Mock provider: `src/main/java/com/finance/tracker/stock/market/provider/MockMarketDataProvider.java`
- Angular routes: `personal-finance/src/app/app.routes.ts`
- Angular Stock Analysis page: `personal-finance/src/app/features/stocks/analysis/stock-analysis/`
- Python FastAPI entrypoint: `aiservices/app/main.py`
- Python tools/router/workflow: `aiservices/app/tools/stock_tools.py`, `aiservices/app/services/tool_router.py`, and `aiservices/app/services/analysis_workflow.py`
- RAG ingestion/vector store: `aiservices/app/rag/ingest.py` and `aiservices/app/rag/vector_store.py`
