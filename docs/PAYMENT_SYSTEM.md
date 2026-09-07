# Payment Architecture and Dummy Provider

## 1. Payment architecture

The payment implementation follows a provider-based design so a real provider such as Razorpay, Stripe, or Adyen can be plugged in later without changing the controller or Angular page.

Key components:
- `PaymentController`: REST API surface
- `PaymentService`: orchestration and business validation
- `PaymentProvider`: abstraction for provider-specific integrations
- `DummyPaymentProvider`: deterministic test provider used for local learning
- `InMemoryPaymentRepository`: temporary in-memory storage for idempotency and payment lookups
- `Payment` entity: core domain model

## 2. Angular payment flow

The Angular flow is:
1. User opens `/v1/payments`
2. Payment form is validated with reactive forms
3. User submits payment
4. `PaymentService.createPayment()` calls `/api/payments`
5. UI shows processing state while waiting for the response
6. On success or failure, the screen renders a success/failure result

## 3. Backend payment flow

Backend flow:
1. `PaymentController` receives `POST /api/payments`
2. `PaymentService` validates request and idempotency key
3. Service creates a `Payment` in `CREATED` then transitions to `PROCESSING`
4. `DummyPaymentProvider` returns deterministic `SUCCESS` or `FAILED`
5. Service persists updated state and returns DTO response

## 4. API contract

### POST /api/payments
Request:
{
  "orderId": "ORD-10001",
  "amount": 1768.82,
  "currency": "INR",
  "paymentMethod": "CARD",
  "idempotencyKey": "unique-request-id"
}

Response example:
{
  "paymentId": "PAY-12345678",
  "orderId": "ORD-10001",
  "amount": 1768.82,
  "currency": "INR",
  "status": "SUCCESS",
  "provider": "DUMMY",
  "providerPaymentId": "DUMMY-987654",
  "createdAt": "2026-09-07T00:00:00",
  "message": "Payment successful"
}

### GET /api/payments/{paymentId}
Returns the current payment record and status.

## 5. Payment state machine

CREATED -> PROCESSING -> SUCCESS
              \-> FAILED
              \-> CANCELLED

State transitions are enforced in `Payment.transitionTo()` to prevent invalid updates.

## 6. Idempotency handling

The client sends an idempotency key. The backend stores a mapping of `idempotencyKey -> paymentId` in memory. If the same key is reused, the same payment result is returned instead of creating duplicates.

This is intentionally isolated inside the repository layer so it can later be replaced by Redis or a database table.

## 7. Dummy provider behavior

The dummy provider is deterministic:
- payment amount <= 0 => FAILED
- order ID containing `FAIL` or `DECLINE` => FAILED
- otherwise => SUCCESS

This ensures tests stay stable and repeatable.

## 8. How to test success

Use `ORD-SUCCESS-001` or any order ID without `FAIL` or `DECLINE`.

Example request:
{
  "orderId": "ORD-SUCCESS-001",
  "amount": 1768.82,
  "currency": "INR",
  "paymentMethod": "CARD",
  "idempotencyKey": "success-001"
}

## 9. How to test failure

Use `ORD-FAIL-001` or `ORD-DECLINE-001`.

Example request:
{
  "orderId": "ORD-FAIL-001",
  "amount": 1768.82,
  "currency": "INR",
  "paymentMethod": "CARD",
  "idempotencyKey": "fail-001"
}

## 10. How to integrate a real payment provider

Replace `DummyPaymentProvider` with a new implementation of `PaymentProvider`, while keeping the rest of the controller/service/UI unchanged. The provider-specific API call belongs only inside the provider implementation.

## 11. Security considerations

- No CVV or full card number is persisted.
- Sensitive card details are never logged.
- Payment data is masked in the UI and stored only in memory for a dummy flow.
- Real providers should only receive a provider tokenized payment object, not raw credentials.

## 12. Production improvements before real money

- Replace in-memory repository with DB or Redis
- Add user authentication/authorization checks
- Add persisted audit trail
- Add webhook reconciliation and retries
- Use encrypted secrets and provider credentials
- Add rate limiting and provider-specific idempotency storage
- Add outbox or saga pattern for event-driven workflows
