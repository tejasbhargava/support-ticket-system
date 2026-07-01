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

---

## Getting Started

### Prerequisites
- Java 21
- Maven
- PostgreSQL

### Setup

**1. Clone the repo**
```bash
git clone https://github.com/yourusername/supporthub.git
cd supporthub
```

**2. Create the database**
```sql
CREATE DATABASE ticsystemDB;
```

**3. Configure `application.properties`**
```properties
spring.datasource.url=your url of postgres
spring.datasource.username=postgres
spring.datasource.password=yourpassword

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

jwt.secret=your-secret-key-minimum-32-characters-long
jwt.expiration=86400000
```

**4. Run**
```bash
./mvnw spring-boot:run
```

**5. Access Swagger UI**
```
http://localhost:8080/swagger-ui/index.html
```

Categories are auto-seeded on first run (ACCOUNT, BILLING, TECHNICAL, GENERAL).

---

