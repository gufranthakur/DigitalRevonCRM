# AdCRM — Project Context for Claude

This document contains everything needed to resume building the AdCRM dashboard. Read this fully before writing any code.

---

## What This Project Is

An internal CRM dashboard for a real estate marketing agency. It is used only by the agency team — there is no client-facing portal in the current scope. The purpose is to manage clients, track ad campaign performance, generate invoices, and export reports.

---

## Tech Stack

- **Backend:** Spring Boot 4.0.3 (Java 25, though Java 21 LTS is recommended)
- **Frontend:** Thymeleaf + Tailwind CSS (via CDN) + Chart.js (via CDN)
- **Database:** PostgreSQL 18 (database name: `crm_db`, port: `5432`)
- **ORM:** Spring Data JPA / Hibernate
- **Security:** Spring Security (currently disabled/permissive for development)
- **Build tool:** Gradle
- **Package:** `com.prototype`

---

## Project Structure

```
src/main/
├── java/com/prototype/
│   ├── PrototypeApplication.java         ← main entry point
│   ├── config/
│   │   ├── SecurityConfig.java           ← Spring Security (currently open/permissive)
│   │   └── DataInitializer.java          ← seeds admin user on startup
│   ├── controller/
│   │   ├── AuthController.java           ← GET /login
│   │   ├── DashboardController.java      ← GET /dashboard
│   │   └── ClientController.java         ← /clients CRUD
│   ├── model/
│   │   ├── User.java                     ← @Entity, has Role enum
│   │   ├── Client.java                   ← @Entity, has Status enum
│   │   ├── Invoice.java                  ← @Entity, ManyToOne Client
│   │   ├── LineItem.java                 ← @Entity, ManyToOne Invoice
│   │   ├── Note.java                     ← @Entity, ManyToOne Client + User
│   │   └── Campaign.java                 ← @Entity, ManyToOne Client
│   ├── repository/
│   │   ├── UserRepository.java           ← findByEmail()
│   │   ├── ClientRepository.java         ← findByStatus()
│   │   ├── InvoiceRepository.java        ← findByClient()
│   │   ├── LineItemRepository.java       ← findByInvoice()
│   │   ├── NoteRepository.java           ← findByClient()
│   │   └── CampaignRepository.java       ← findByClient()
│   └── service/
│       ├── UserDetailsServiceImpl.java   ← Spring Security user loading
│       └── ClientService.java            ← getAll(), save(), delete(), getById()
└── resources/
    ├── templates/
    │   ├── login.html                    ← ✅ done, premium dark design
    │   ├── dashboard.html                ← ✅ done, stat cards + charts
    │   ├── clients.html                  ← ✅ done, table + add modal + delete
    │   ├── client-detail.html            ← ✅ done, client info + notes placeholder
    │   └── fragments/
    │       └── layout.html               ← sidebar fragment (th:fragment="sidebar")
    └── application.properties
```

---

## application.properties

```properties
spring.application.name=prototype
server.port=8081

spring.datasource.url=jdbc:postgresql://localhost:5432/crm_db
spring.datasource.username=postgres
spring.datasource.password=YOUR_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

---

## Security Status

Spring Security is currently **fully open** (all requests permitted, CSRF disabled) for development convenience. The `SecurityConfig.java` looks like this:

```java
http
    .csrf(csrf -> csrf.disable())
    .authorizeHttpRequests(auth -> auth
        .anyRequest().permitAll()
    );
```

The login page and `AuthController` exist and work, but login is not enforced yet. Re-enabling proper authentication is a TODO item.

**Important:** Do NOT add `th:name="${_csrf.parameterName}"` or `th:value="${_csrf.token}"` hidden inputs to any forms while CSRF is disabled — it will throw a `SpelEvaluationException`.

**Admin credentials (seeded by DataInitializer):**
- Email: `admin@crm.com`
- Password: `admin123`

---

## Design System

The UI uses a consistent premium dark SaaS aesthetic. Every page must follow these rules:

**Colors:**
- Background: `#0a0a0a`
- Card/sidebar background: `#111111`
- Borders: `#1a1a1a`
- Primary text: `#e5e5e5`
- Muted text: `#525252`
- Accent (emerald): `#10b981`
- Accent hover: `#34d399`
- Error/danger: `#ef4444` / `#f87171`
- Warning (paused): `#eab308`

**Typography:** DM Sans (body) + DM Mono (monospace). Load via Google Fonts:
```html
<link href="https://fonts.googleapis.com/css2?family=DM+Sans:wght@300;400;500;600&family=DM+Mono:wght@400;500&display=swap" rel="stylesheet">
```

**Every page must include in `<head>`:**
```html
<script src="https://cdn.tailwindcss.com"></script>
<link href="https://fonts.googleapis.com/css2?family=DM+Sans:wght@300;400;500;600&family=DM+Mono:wght@400;500&display=swap" rel="stylesheet">
<style>* { font-family: 'DM Sans', sans-serif; } body { background-color: #0a0a0a; }</style>
```

**Cards:**
```css
background: #111111; border: 1px solid #1a1a1a; border-radius: 16px;
```

**Status badges:**
```css
.badge-active  { background: rgba(16,185,129,0.1); color: #10b981; }
.badge-paused  { background: rgba(234,179,8,0.1);  color: #eab308; }
.badge-completed { background: rgba(107,114,128,0.1); color: #6b7280; }
```

**Input fields:**
```css
background: #0a0a0a; border: 1px solid #242424; color: #e5e5e5;
focus: border-color #10b981, box-shadow 0 0 0 3px rgba(16,185,129,0.1);
```

**Primary button:**
```css
background: #10b981; color: #000; font-weight: 600;
hover: background #34d399;
```

---

## Sidebar Fragment

The sidebar lives in `fragments/layout.html` as `th:fragment="sidebar"`. Include it in every page with:

```html
<div th:replace="~{fragments/layout :: sidebar}"></div>
```

The sidebar has a collapsible toggle via JavaScript:
```javascript
function toggleSidebar() {
    document.getElementById('sidebar').classList.toggle('collapsed');
}
```

When collapsed, `.nav-label` and `.logo-text` elements are hidden via CSS.

The active nav item uses class `active` which applies `color: #10b981; background: rgba(16,185,129,0.08)`.

---

## Data Models

### User
Fields: `id` (Long), `name`, `email`, `password`, `role` (enum: ADMIN, MANAGER, EMPLOYEE). Table name: `users` (not `user` — reserved word in PostgreSQL).

### Client
Fields: `id`, `name`, `projectName`, `contactEmail`, `contactPhone`, `monthlyFee` (Double), `adBudget` (Double), `contractStart` (LocalDate), `contractEnd` (LocalDate), `status` (enum: ACTIVE, PAUSED, COMPLETED).

### Invoice
Fields: `id`, `invoiceNumber`, `issueDate` (LocalDate), `totalAmount` (Double), `gstAmount` (Double), `status` (enum: PAID, UNPAID), `client` (ManyToOne), `lineItems` (OneToMany, CascadeType.ALL).

### LineItem
Fields: `id`, `description`, `quantity` (Integer), `unitPrice` (Double), `invoice` (ManyToOne).

### Note
Fields: `id`, `content`, `type` (enum: MEETING, STRATEGY, GENERAL), `createdAt` (LocalDateTime), `client` (ManyToOne), `user` (ManyToOne).

### Campaign
Fields: `id`, `name`, `adSpend` (Double), `leadsGenerated` (Integer), `cpl` (Double), `startDate` (LocalDate), `endDate` (LocalDate), `client` (ManyToOne).

---

## What Is Done

The following pages and features are fully working:

**Login page** (`/login`) — premium dark design, email + password form, Spring Security wired but currently not enforced.

**Dashboard** (`/dashboard`) — stat cards for Total Clients, Active Clients, Total Revenue (static ₹0), Pending Payments (static ₹0). Line chart for Monthly Revenue and bar chart for Leads Generated using Chart.js with dummy data. Collapsible sidebar.

**Clients page** (`/clients`) — full table showing all clients with name, project, monthly fee, ad budget, status badge, contract end date. Add Client modal with full form. Delete button per row. Empty state message.

**Client Detail page** (`/clients/{id}`) — shows all client info in a card grid, quick stat cards on the right, notes section placeholder.

---

## What Is Remaining (40% Demo Scope)

The following features need to be built to complete the first 40% demo:

**Edit Client** — `@GetMapping("/clients/edit/{id}")` pre-populates the form with existing data, `@PostMapping("/clients/update/{id}")` saves changes. Add an Edit button to the clients table row.

**Invoice Generator** (`/invoices`) — list of all invoices with client name, invoice number, amount, status, date. Generate Invoice page with: auto-generated invoice number, add/remove line items dynamically, GST calculation (18%), agency details, client details dropdown. Mark as Paid/Unpaid toggle. PDF download (use Flying Saucer / iText library).

**Dashboard real data** — wire Total Revenue and Pending Payments to real invoice data. Wire Monthly Revenue chart to real invoice data grouped by month. Wire Leads chart to real campaign data.

**Notes on Client Detail** — add note form inside client detail page. Notes list showing type badge, content, date, author. `NoteController` with `@PostMapping("/clients/{id}/notes")`.

**Re-enable Spring Security** — once demo is stable, re-enable authentication with proper login/logout flow. Re-add CSRF tokens to all forms. Protect all routes except `/login`.

**Reports page** (`/reports`) — simple page showing per-client summary: ad spend, leads, CPL. Export to CSV button.

---

## Known Issues and Gotchas

**CSRF is disabled.** Do not add `_csrf` hidden inputs to any form. If you re-enable CSRF, add this to every form POST: `<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>` and restore the full SecurityConfig.

**PostgreSQL reserved word.** The `User` entity uses `@Table(name = "users")` — never change this to `user`.

**`ddl-auto=update`** is set, meaning Hibernate auto-creates/updates tables. This is fine for development. For production, switch to `validate` and use Flyway migrations.

**Thymeleaf fragment styling** — the sidebar fragment does not include `<style>` tags. Each page must define its own sidebar CSS (`.sidebar`, `.nav-item`, `.logo-dot`, etc.) in its own `<style>` block. This is intentional since Tailwind CDN handles most of it.

**Status badge Thymeleaf syntax** — use this exact pattern for conditional badge classes:
```html
th:classappend="${client.status == T(com.prototype.model.Client.Status).ACTIVE} ? 'badge-active' :
               (${client.status == T(com.prototype.model.Client.Status).PAUSED} ? 'badge-paused' : 'badge-completed')"
```

**Port is 8081**, not 8080. Always use `http://localhost:8081`.

---

## How to Run

Make sure PostgreSQL is running via Postgres.app on Mac, then run the project from IntelliJ via the Gradle `bootRun` task or the green run button on `PrototypeApplication.java`.