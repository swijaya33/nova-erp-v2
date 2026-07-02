# Nova ERP v2 — Phase 0 Plan: Foundation Layer

**Date:** 2026-07-02  
**Status:** In Progress  
**Approvals:** ✅ Java Spring Cloud path confirmed, SAK compliance scope confirmed

## Objective

Get the microservices infrastructure running and build the first two foundation services (Auth + GL) with SAK-compliant accounting logic. This is the "plumbing + core engine" phase — without it, nothing else can communicate or function correctly.

## Scope

### Deliverables
1. **Infrastructure Services** — Eureka Server + Config Server running via Docker Compose
2. **nova-common library** — hardened with SAK-aware base entities, multi-currency support, tenant abstraction interface
3. **Auth Service** — JWT auth with RBAC, OAuth2/OIDC ready, service discovery registered to Eureka
4. **GL Service (SAK Core)** — Chart of Accounts (5-category Indonesian standard), double-entry journal engine, period management, trial balance validation

### Out of Scope (deferred)
- Products, Orders, Invoices services (Phase 1+)
- SvelteKit frontend (Phase 2+)
- Event bus / Kafka (Phase 3+)
- Multi-currency exchange rate feeds (planned but not built in Phase 0 — base currency only)

## Architecture Decisions for Phase 0

### Multi-Tenancy: Option A (Shared DB, Shared Schema with `tenant_id`)
**Decision:** Start with shared database + `tenant_id` on every table. Design abstraction layer so migration to per-schema is possible later.

Rationale:
- Simplest path to first working version
- All 14 services share the same infrastructure in dev (limited hardware)
- Can add schema-per-tenant isolation later via Flyway migration + tenant routing filter
- Every entity gets `String tenantId` field; nova-common provides `TenantContext` for automatic injection

### SAK Compliance Scope
Per existing GL BRD/PRD:
- **Chart of Accounts:** 5 categories (Harta/Liability/Equity/Pendapatan/Beban) with standard codes (1xxx, 2xxx, 3xxx, 4xxx, 5xxx+)
- **Double-entry engine:** Σ Debit = Σ Credit enforced on every journal entry
- **Period management:** Monthly closing with lock mechanism
- **Financial statements:** Trial Balance, Income Statement (Laba Rugi), Balance Sheet (Neraca) — generated from ledger data
- **Pre-seeded CoA:** ≥30 accounts following Indonesian SMB conventions

### Database Strategy for Phase 0
- Dev: H2 in-memory per service (fastest iteration)
- Staging/Prod target: PostgreSQL via Spring profiles (`--spring.profiles.active=postgres`)
- Each service has its own schema — no shared tables between services

## Task Breakdown

### T0.1: Harden nova-common Library (~1h)
- Add `TenantContext` (ThreadLocal-based tenant resolution from JWT)
- Add SAK-aware base entity with account code validation
- Add multi-currency value type (`Currency`, `Money`)
- Add Resilience4j circuit breaker annotations for inter-service calls
- Build and verify: `mvn clean install -pl nova-common`

### T0.2: Infrastructure — Eureka + Config Server (~1h)
- Verify Eureka server builds and starts on port 8761
- Configure Config Server with shared config files
- Create docker-compose.yml for infra services (Eureka, Config Server, PostgreSQL, Redis)
- Verify all infra services start together

### T0.3: Auth Service — Full Implementation (~2h)
- JWT token generation/validation with RS256 keys
- User entity + role/permission model (RBAC)
- Password hashing with BCrypt
- Spring Security filter chain with Eureka client registration
- OpenAPI/Swagger docs at `/api-docs/openapi.yaml`
- Health check at `/actuator/health`

### T0.4: GL Service — SAK Core (~3h)
- Chart of Accounts entity + seed data (≥30 Indonesian standard accounts)
- Journal Entry entity with double-entry validation
- Ledger service with running balance calculation
- Period management (open/close/month lock)
- Trial Balance report endpoint
- Financial statement generation (Laba Rugi, Neraca)

### T0.5: Integration — Auth → GL via Eureka (~1h)
- GL service registers to Eureka
- Auth service calls GL health via Eureka discovery
- Shared JWT context propagation between services
- End-to-end test: create user → authenticate → access GL endpoints

## Success Criteria for Phase 0
- [ ] All infra services start via `docker-compose up` (Eureka, Config Server, PostgreSQL)
- [ ] `mvn clean install` passes on parent POM with all modules compiling
- [ ] Auth service starts, registers to Eureka, accepts `/auth/login` returning JWT
- [ ] GL service starts, registers to Eureka, seeds ≥30 SAK-standard accounts
- [ ] Journal entry creation enforces Σ Debit = Σ Credit (rejects unbalanced entries)
- [ ] Trial Balance report returns balanced totals for seeded data
- [ ] Auth token from service A is accepted by service B via shared JWT validation

## Estimated Effort: ~8 hours of focused work
