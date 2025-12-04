# Limits Microservice (spring-boot)

Implements:
- Per-user limit storage (default 100000.00)
- Reset to default daily at 00:00 (server time) via scheduler
- Reserve / confirm / cancel workflow
- Direct decrease and restore endpoints
- Auto-create user record on first access
- Liquibase changelog for tables

Endpoints:
GET  /api/limits/{userId} -> returns limit entity
POST /api/limits/{userId}/reserve {amount} -> reserves amount, returns reservationId
POST /api/limits/reservation/{reservationId}/confirm -> confirms reservation (applies decrease)
POST /api/limits/reservation/{reservationId}/cancel -> cancels reservation (releases reserved amount)
POST /api/limits/{userId}/decrease {amount} -> decrease without reservation
POST /api/limits/{userId}/restore {amount} -> restore amount

Notes:
- Configure PostgreSQL connection in application.yml
- Consider stronger concurrency control (optimistic locking or DB constraints) in production\n