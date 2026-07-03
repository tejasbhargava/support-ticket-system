````markdown
# SupportHub 🎫

A production-grade **Customer Support Ticketing System** backend built with Java Spring Boot. Features role-based access control, JWT authentication, a ticket lifecycle state machine, SLA-based overdue detection, and an auto-prioritization rule engine.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.x |
| Security | Spring Security + JWT (JJWT 0.12.7) |
| ORM | Spring Data JPA + Hibernate |
| Database | PostgreSQL |
| Mapping | ModelMapper + Manual Mappers |
| Validation | Jakarta Validation |
| Docs | Springdoc OpenAPI (Swagger UI) |
| Build | Maven |
| Containerization | Docker |

---

## Architecture

```
Client (Postman / Swagger UI)
           │
           ▼
    JWT Auth Filter
           │
           ▼
    REST Controllers
           │
           ▼
    Service Layer
    (Business Logic + State Machine + SLA)
           │
           ▼
    Priority Engine
    (Rule-based auto triage)
           │
           ▼
    Repository Layer
    (Spring Data JPA)
           │
           ▼
      PostgreSQL
```

---

## Project Structure

```
src/main/java/com/tejas/ticketingsystem/
├── config/
│   ├── SecurityConfig.java
│   ├── SwaggerConfig.java
│   └── DataSeeder.java
├── controller/
│   ├── AuthController.java
│   ├── TicketController.java
│   ├── CommentController.java
│   ├── TicketActivityController.java
│   └── DashboardController.java
├── dto/
│   ├── auth/
│   ├── ticket/
│   ├── comment/
│   └── dashboard/
├── entity/
│   ├── User.java
│   ├── Ticket.java
│   ├── Category.java
│   ├── Comment.java
│   └── TicketActivity.java
├── enums/
│   ├── Role.java
│   ├── Status.java
│   ├── Priority.java
│   ├── PrioritySource.java
│   └── ActivityType.java
├── mapper/
│   └── TicketMapper.java
├── repository/
│   ├── UserRepository.java
│   ├── TicketRepository.java
│   ├── CategoryRepository.java
│   ├── CommentRepository.java
│   └── TicketActivityRepository.java
├── rules/
│   └── PriorityEngine.java
├── security/
│   ├── JwtService.java
│   ├── JwtFilter.java
│   ├── CustomUserDetails.java
│   └── CustomUserDetailsService.java
├── service/
│   ├── AuthService.java
│   ├── TicketService.java
│   ├── CommentService.java
│   ├── TicketActivityService.java
│   └── DashboardService.java
└── util/
    └── TicketSpecification.java
```

---

## Core Features

### Authentication & Authorization
- JWT-based stateless authentication
- Three roles: `CUSTOMER`, `AGENT`, `ADMIN`
- Role-based endpoint protection via `@PreAuthorize`
- Query-level data scoping — customers see only their own tickets, agents see only assigned tickets

### Ticket Lifecycle — State Machine
Valid status transitions enforced at the service layer:
```
OPEN → IN_PROGRESS → WAITING_FOR_CUSTOMER → RESOLVED → CLOSED
                                          ↘
                                        REOPENED → IN_PROGRESS
```
Invalid transitions are rejected with a `400 Bad Request`.

### Priority Engine (Auto-Triage)
Incoming tickets are automatically prioritized based on category + keyword matching in the description — no manual triage needed by default. Agents/Admins can override manually, tracked via `PrioritySource` (AUTO / MANUAL).

| Category | Trigger Keywords | Auto Priority |
|---|---|---|
| ACCOUNT | locked, login, can't access | HIGH |
| BILLING | charged, refund, duplicate | HIGH |
| TECHNICAL | crash, down, error, broken | HIGH |
| GENERAL | urgent, critical, asap | HIGH |
| Any | no keyword match | MEDIUM |

### SLA / Overdue Detection
Each category has a default SLA window. Overdue status is computed on every response read — no background scheduler needed.

| Category | SLA Window |
|---|---|
| BILLING | 12 hours |
| ACCOUNT | 24 hours |
| TECHNICAL | 48 hours |
| GENERAL | 72 hours |

### Comments
- Full comment thread per ticket
- `isInternal` flag — agent-only notes hidden from customers
- Customers blocked from posting internal notes at the service layer

### Audit Log (TicketActivity)
Every significant event is automatically logged:
- `TICKET_CREATED`, `STATUS_CHANGED`, `ASSIGNED`, `PRIORITY_CHANGED`, `COMMENT_ADDED`, `REOPENED`

### Role-Based Dashboard
Single `GET /dashboard` endpoint returns different data per role:
- **Customer** — their open/resolved/waiting ticket counts + recent tickets
- **Agent** — assigned tickets, high priority count, overdue count, recent assignments
- **Admin** — system-wide stats, avg resolution time, tickets by category/status

---

## API Endpoints

### Auth
```
POST   /api/auth/register
POST   /api/auth/login
```

### Tickets
```
POST   /api/tickets                        CUSTOMER
GET    /api/tickets                        ALL (role-scoped, paginated)
GET    /api/tickets/{id}                   ALL (access-checked)
PATCH  /api/tickets/{id}/status            AGENT, ADMIN
PATCH  /api/tickets/{id}/assign            ADMIN
PATCH  /api/tickets/{id}/priority          ADMIN
GET    /api/tickets/overdue                AGENT, ADMIN
```

### Comments
```
POST   /api/tickets/{id}/comments          ALL
GET    /api/tickets/{id}/comments          ALL (isInternal filtered by role)
```

### Activity
```
GET    /api/tickets/{id}/activity          AGENT, ADMIN
```

### Dashboard
```
GET    /api/dashboard                      ALL (role-specific response)
```

## Getting Started

### Prerequisites

- Java 21
- Maven
- PostgreSQL (for local setup)
- Docker Desktop (for Docker setup)

---

### Option 1 — Run Locally

**1. Clone the repository**

```bash
git clone https://github.com/yourusername/supporthub.git
cd supporthub
```

**2. Create the database**

```sql
CREATE DATABASE ticsystemDB;
```

**3. Configure environment variables**

```text
DB_URL=jdbc:postgresql://localhost:5432/ticsystemDB
DB_USERNAME=postgres
DB_PASSWORD=your-password
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000
```

**4. Run the application**

```bash
./mvnw spring-boot:run
```

---

### Option 2 — Run with Docker

**1. Build the Docker image**

```bash
docker build -t supporthub .
```

**2. Run the container**

```bash
docker run -p 8080:8080 \
-e DB_URL="jdbc:postgresql://host.docker.internal:5432/ticsystemDB" \
-e DB_USERNAME="postgres" \
-e DB_PASSWORD="your-password" \
-e JWT_SECRET="your-secret-key" \
-e JWT_EXPIRATION="86400000" \
supporthub
```

---

### API Documentation

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

Categories (`ACCOUNT`, `BILLING`, `TECHNICAL`, `GENERAL`) are automatically seeded on first run.

## Performance

- Seeded PostgreSQL database with **10,000 tickets**
- Reduced paginated ticket retrieval latency by **78% (481 ms → 104 ms)**
- Reduced API payload size by **99.3% (310 KB → 2.14 KB)** using DTO projections and pagination
- Validated backend under **2,000 authenticated requests (0% failures)** using Apache JMeter

