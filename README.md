# Hotel Booking Service

A Spring Boot backend for a hotel booking platform. Owners onboard one or more properties, guests search for
available rooms, book them, pay with card, UPI or wallet, and cancel with a refund based on the property's
cancellation policy.

## Build and run

You need Java 17 or later (the project was built and tested on JDK 21). The Maven wrapper downloads Maven, so you
do not need it installed.

```bash
./mvnw test                 # 101 unit, integration and architecture tests
./mvnw spring-boot:run      # starts on http://localhost:8080
# or
./mvnw package && java -jar target/hotel-booking-0.1.0-SNAPSHOT.jar
```

| What | Where |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI spec | http://localhost:8080/v3/api-docs |
| H2 console | http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:hotelbooking`, user `sa`, no password) |

On startup the service seeds demo data through the normal onboarding flow. Turn it off with
`hotel.demo-data.enabled=false`.

- **Lemon Leaf Hotels** is a chain with 3 properties: Premier (Koramangala) and Express (Indiranagar) in
  Bengaluru, and Andheri in Mumbai.
- **Sea Breeze Homestay** is a standalone owner with 1 non-refundable property in Goa.

## Try the API

Use check-in and check-out dates in the future. Copy the ids from each response into the next request.

```bash
# 1. Search. Returns only room types free on every night of the stay, cheapest property first.
curl "localhost:8080/api/v1/properties/search?city=Bengaluru&checkIn=2026-12-10&checkOut=2026-12-12&guests=2&amenities=WIFI,PARKING&maxPrice=5000"

# 2. Book. The rooms are held for 15 minutes and the booking is PENDING_PAYMENT.
curl -X POST localhost:8080/api/v1/bookings -H 'Content-Type: application/json' -d '{
  "propertyId": "<propertyId>", "roomTypeId": "<roomTypeId>",
  "checkIn": "2026-12-10", "checkOut": "2026-12-12", "guests": 3, "rooms": 2,
  "guest": {"fullName": "Asha Rao", "email": "asha@example.com", "phone": "+919812345678"}}'

# 3. Pay. A success confirms the booking. Sending the same Idempotency-Key again returns the first result.
curl -X POST localhost:8080/api/v1/bookings/<bookingId>/payments -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: 7f9c2b1e-0d4a-4c55-9a51-2f6f0f3c1a10' -d '{"method": "UPI", "vpa": "asha@okicici"}'

# 4. Cancel. The rooms are released and the refund follows the policy.
curl -X POST localhost:8080/api/v1/bookings/<bookingId>/cancellation
```

Payment bodies by method:

```json
{"method": "CARD", "cardNumber": "4242424242424242", "holderName": "Asha Rao", "expiryMonth": 12, "expiryYear": 2030}
{"method": "UPI", "vpa": "asha@okicici"}
{"method": "WALLET", "provider": "PAYTM", "walletId": "9812345678"}
```

The mock gateway declines card `4000000000000002`, UPI `fail@upi` and wallet `PAYTM` / `declined-wallet`
(configured in `hotel.payment.mock.declined-instruments`). Use them to try the failure and retry path.

### Endpoints

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/v1/owners` | Onboard an owner with 1 or more properties |
| POST | `/api/v1/owners/{ownerId}/properties` | Add a property to an existing owner |
| GET | `/api/v1/owners/{ownerId}` | Owner and all its properties |
| GET | `/api/v1/properties/{propertyId}` | Property and its room types |
| GET | `/api/v1/properties/search` | Discovery (parameters below) |
| POST | `/api/v1/bookings` | Book a room type; holds the rooms |
| GET | `/api/v1/bookings/{bookingId}` | Booking and its status |
| POST | `/api/v1/bookings/{bookingId}/payments` | Pay; `Idempotency-Key` header required |
| GET | `/api/v1/bookings/{bookingId}/payments` | All payment attempts for a booking |
| POST | `/api/v1/bookings/{bookingId}/cancellation` | Cancel, release rooms, refund |

Search parameters: `city`, `checkIn`, `checkOut` and `guests` are required. The optional ones are `rooms`
(default 1), `locality`, `minPrice` and `maxPrice` (average nightly rate per room), `amenities` (comma-separated)
and `minRating` (1 to 5).

Errors are [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) problem details with a stable `code`, for example
`{"status": 409, "code": "ROOMS_UNAVAILABLE", "detail": "Only 0 room(s) left on 2026-12-10, requested 1"}`.

| Status | Meaning | Example codes |
|---|---|---|
| 400 | Invalid input | `VALIDATION_FAILED`, `CHECK_IN_IN_PAST`, `OCCUPANCY_EXCEEDED`, `CARD_EXPIRED` |
| 404 | Unknown id | `BOOKING_NOT_FOUND`, `PROPERTY_NOT_FOUND`, `OWNER_NOT_FOUND` |
| 409 | Conflicts with current state | `ROOMS_UNAVAILABLE`, `INVALID_STATE_TRANSITION`, `PAYMENT_IN_PROGRESS`, `BOOKING_HOLD_EXPIRED` |
| 422 | A business rule forbids it | `CANCELLATION_WINDOW_CLOSED`, `IDEMPOTENCY_KEY_REUSED`, `DATES_NOT_OPEN` |
| 500 | Unexpected error (logged with the request id) | `INTERNAL_ERROR` |

## Design

### Modules

The code is packaged by feature. Each feature has the same four layers, and dependencies point inward:

```
com.hotelbooking
├── common        Money, DateRange, Occupancy, exceptions, StateTransitions, StrategyRegistry, error handler
├── property      owners, properties, room types; onboarding
├── inventory     rooms sold per room type per night; reserve / release
├── pricing       PricingStrategy (extension point)
├── booking       Booking aggregate, lifecycle, hold expiry job
├── payment       Payment aggregate, processors per method, gateway port + mock, idempotency, refunds
├── cancellation  cancellation policies and the cancel flow
├── search        SearchFilter pipeline over room offers
└── bootstrap     demo data

<feature>
├── domain          plain Java: entities, value objects, repository and gateway interfaces
├── application     use cases, transactions, orchestration
├── infrastructure  JPA entities and adapters, mock gateway
└── api             controllers and DTOs
```

`ArchitectureTest` (ArchUnit) enforces these rules:

- Domain classes do not import Spring, JPA, validation or Jackson.
- Domain and application code never depend on `api` or `infrastructure`.
- JPA entities are package-private inside `infrastructure.persistence`.
- Feature modules have no dependency cycles.

Modules talk through application services, plus one event. The property module publishes
`PropertyOnboardedEvent` and the inventory module opens nights for it, so `property` does not depend on
`inventory`.

### Domain model

- **`PropertyOwner` → `Property` → `RoomType`.** An owner owns one or more properties. A standalone hotel is an
  owner with one property and goes through the same endpoint and code path as a chain. There is no flag or
  special case for it.
- **`RoomNightInventory`.** One row per room type per night stores `totalRooms` and `bookedRooms`. A stay is
  bookable only when every night has enough free rooms.
- **`Booking`.** Holds the stay (property, room type, dates, occupancy) and the guest. The total price and the
  cancellation policy are copied in at booking time, so later changes to the property do not change what the
  guest agreed to.
- **`Payment`.** One attempt to pay for a booking, plus its refund. The amount always comes from the booking,
  never from the client. Only the masked instrument is stored.
- **Value objects.** These are records that validate themselves: `Money` (BigDecimal plus currency, 2 decimal
  places), `DateRange` (check-out day excluded), `Occupancy` (guests and rooms), `Location`, `StarRating`,
  `IdempotencyKey`, and typed ids.

Each lifecycle is a transition table declared once on its status enum (`StateTransitions`). Aggregates reject an
illegal move with `InvalidStateTransitionException` (409).

```
Booking:  PENDING_PAYMENT ──payment succeeds──▶ CONFIRMED ──cancel──▶ CANCELLED
          PENDING_PAYMENT ──cancel──▶ CANCELLED
          PENDING_PAYMENT ──hold times out──▶ EXPIRED (rooms released)
          A declined payment leaves the booking in PENDING_PAYMENT, so the guest can retry until the hold expires.

Payment:  INITIATED ──approved──▶ SUCCEEDED ──refund──▶ REFUND_PENDING ──▶ REFUNDED | REFUND_FAILED
          INITIATED ──declined──▶ FAILED
```

### Extension points

| To add | Implement | Registered by | Existing implementations |
|---|---|---|---|
| A search filter | `SearchFilter` (+ a field in `SearchCriteria`) | any `@Component` | availability, occupancy, locality, price range, amenities, star rating |
| A payment method | `PaymentInstrument` + `GatewayPaymentProcessor<T>` (+ a request subtype) | any `@Component` | card, UPI, wallet |
| A payment provider | `PaymentGateway` | a bean | `MockPaymentGateway` |
| A cancellation policy | `CancellationPolicy`, or new tiers for `TieredCancellationPolicy` | `CancellationPolicyConfiguration` | flexible, moderate, non-refundable |
| A pricing rule | `PricingStrategy` | `PricingConfiguration` | base rate |
| A different database | the `*Repository` interfaces in each `domain` package | an adapter bean | JPA adapters on H2 |

Processors and policies are looked up through `StrategyRegistry`, which Spring fills with every implementation.
Two implementations for the same key stop the application at startup.

**Pricing.** As agreed, only the extension point is built. Search and booking depend only on `PricingStrategy`,
which returns a per-night `PriceQuote`. Dynamic pricing would be a decorator registered in
`PricingConfiguration`, with no change to search or booking. For example (this class is not implemented yet):

```java
@Bean
PricingStrategy pricingStrategy() {
    return new WeekendSurchargeStrategy(new BasePricingStrategy(), 20); // +20% on Friday and Saturday nights
}
```

### Availability and concurrency

- **No double booking.** `InventoryService.reserve` loads the stay's night rows with `SELECT ... FOR UPDATE`
  in date order, checks every night, and updates them all in the booking's transaction. Two bookings that share
  a night queue on the same rows. Because every booking locks in date order, two bookings cannot deadlock each
  other. A database `CHECK (booked_rooms <= total_rooms)` refuses an oversell even if the locking is bypassed.
  `BookingConcurrencyIntegrationTest` starts 20 bookings at once for 3 rooms and checks that exactly 3 succeed.
- **One change per booking at a time.** Payment, cancellation and hold expiry all lock the booking row first and
  the inventory rows second. A payment cannot confirm a booking that is being cancelled or expired.
- **No database lock while the gateway is called.** Payment runs in three steps:
  1. Lock the booking, check the idempotency key, record an `INITIATED` payment, and commit.
  2. Call the gateway.
  3. Lock the booking again and record the result.

  If the booking was cancelled or expired while the charge was in flight, step 3 refunds the charge in full
  instead of confirming the booking.
- **Hold expiry.** `BookingHoldExpiryJob` runs every 30 seconds. It expires unpaid bookings past their hold and
  releases their rooms. Each booking is processed in its own transaction.

### Payment idempotency

- The `Idempotency-Key` header is required on every payment request.
- The first request with a key creates the payment. The key has a unique constraint in the database.
- A repeat with the same key and the same request returns the stored result with `200` and
  `Idempotent-Replayed: true`. This applies to both successful and declined payments, and the guest is never
  charged twice.
- A repeat while the first request is still at the gateway returns `409 PAYMENT_IN_PROGRESS`.
- The same key with a different request returns `422 IDEMPOTENCY_KEY_REUSED`. The service detects this by
  storing a SHA-256 fingerprint of the booking id, method and instrument.
- `PaymentIntegrationTest` starts 8 requests at once, with the same key and with different keys, and checks that
  the gateway is charged exactly once.

### Logging

- **Request id.** `RequestLoggingFilter` gives every request an id and puts it on every log line, for example
  `[3f2a9c1e-...]`. It also returns the id in the `X-Request-Id` response header. A client can send its own
  `X-Request-Id`; anything other than 8 to 64 letters, digits, `-` or `_` is replaced. With the id you can follow
  one payment through all its steps, even when many requests run at the same time.
- **One line per request**, with the method, path, status and duration.
- **Business events at INFO**, logged by the application services:
  - onboarding of owners and properties
  - booking placed, confirmed, cancelled and expired
  - payment initiated, succeeded, failed and replayed
  - the refund decision and its outcome
- **WARN and ERROR** are for things someone should look at:
  - a charge that succeeded after its booking was cancelled (refunded automatically)
  - lock timeouts
  - gateway outages
  - failed refunds, at ERROR, because the money is still owed
  - unexpected exceptions
- **DEBUG** covers searches, inventory changes and business rejections (4xx). Turn it on with
  `logging.level.com.hotelbooking=DEBUG`.
- **No personal or card data.** Logs contain ids, amounts, statuses and payment methods only. Guest contact
  details and card numbers are never logged. The domain classes have no loggers; logging happens in the
  application and API layers.

## Tests

`./mvnw test` runs 101 tests in about 10 seconds. All integration tests share one Spring context, and each test
uses its own city, so the tests do not see each other's data. A controllable clock and a scriptable gateway
make time and gateway behaviour predictable.

| Test | What it covers |
|---|---|
| `BookingTest`, `PaymentTest` | Allowed and forbidden transitions, hold deadline, cancellation window, idempotency fingerprint |
| `MoneyTest`, `DateRangeTest`, `PropertyTest`, `RoomNightInventoryTest`, `PaymentInstrumentTest` | Value object rules: rounding, nights, occupancy, duplicate room types, overselling, Luhn check, masking |
| `CancellationPolicyTest`, `SearchFiltersTest`, `PaymentProcessorTest` | Refund tier boundaries, each filter, processor selection, expired cards, gateway outage |
| `BookingConcurrencyIntegrationTest` | 20 parallel bookings for 3 rooms; overlapping stays competing for shared nights |
| `PaymentIntegrationTest` | Decline then retry, replay, key reuse, parallel requests charge once, refund when the booking is cancelled while the charge is in flight |
| `CancellationIntegrationTest` | Refund per policy and notice, rooms released, cancellation after check-in, refund failure |
| `BookingHoldExpiryIntegrationTest` | Expiry releases rooms, paid bookings are never expired, expired bookings cannot be paid or cancelled |
| `PropertySearchIntegrationTest` | Availability per night, each filter, sorting by price, multi-room fit |
| `BookingJourneyApiTest` | The full REST flow over HTTP, single owner growing into a chain, the error format |
| `ArchitectureTest` | The module and layer rules above |
