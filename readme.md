# AdCRM — Real Estate Marketing Agency CRM

Internal dashboard for a real estate marketing agency to manage clients, track Meta ad campaign performance, generate invoices, and export reports.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 4.0.3 (Java 25) |
| Frontend | Thymeleaf + Tailwind CSS (CDN) + Chart.js (CDN) |
| Database | PostgreSQL 18 (`crm_db`, port `5432`) |
| ORM | Spring Data JPA / Hibernate (`ddl-auto=update`) |
| Security | Spring Security (currently open/permissive) |
| Build | Gradle |
| Package | `com.prototype` |

**Port:** `http://localhost:8081`  
**Admin login:** `admin@crm.com` / `admin123`

---

## Project Structure

```
src/main/
├── java/com/prototype/
│   ├── PrototypeApplication.java
│   ├── config/
│   │   ├── SecurityConfig.java          ← CSRF disabled, all routes open
│   │   └── DataInitializer.java         ← seeds admin user on startup
│   ├── controller/
│   │   ├── AuthController.java          ← GET/POST /login
│   │   ├── DashboardController.java     ← GET /dashboard
│   │   ├── ClientController.java        ← /clients CRUD
│   │   ├── NoteController.java          ← POST /clients/{id}/notes
│   │   ├── InvoiceController.java       ← /invoices CRUD + toggle status
│   │   ├── ReportsController.java       ← /reports + /reports/export (CSV)
│   │   └── MetaSyncController.java      ← POST /clients/{id}/sync-meta
│   ├── model/
│   │   ├── User.java                    ← roles: ADMIN, MANAGER, EMPLOYEE
│   │   ├── Client.java                  ← includes metaAdAccountId
│   │   ├── Invoice.java                 ← status: PAID, UNPAID
│   │   ├── LineItem.java                ← ManyToOne Invoice
│   │   ├── Note.java                    ← types: MEETING, STRATEGY, GENERAL
│   │   └── Campaign.java               ← includes metaCampaignId, impressions, clicks
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── ClientRepository.java
│   │   ├── InvoiceRepository.java
│   │   ├── LineItemRepository.java
│   │   ├── NoteRepository.java
│   │   └── CampaignRepository.java      ← includes findByMetaCampaignId()
│   └── service/
│       ├── UserDetailsServiceImpl.java
│       ├── ClientService.java           ← cascade delete (invoices, campaigns, notes)
│       └── MetaAdsService.java          ← calls Meta Marketing API v19.0
└── resources/
    ├── templates/
    │   ├── login.html
    │   ├── dashboard.html
    │   ├── clients.html                 ← includes Meta Ad Account ID field
    │   ├── client-detail.html           ← includes campaign table + sync button
    │   ├── invoice-form.html
    │   ├── invoices.html
    │   ├── reports.html
    │   └── fragments/layout.html        ← sidebar fragment
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

server.servlet.session.tracking-modes=cookie

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Meta Ads Integration
meta.access.token=YOUR_EXTENDED_TOKEN_HERE
```

---

## What Is Built ✅

### Authentication
- Login page with email + password
- Spring Security wired with BCrypt password encoding
- Admin user auto-seeded by `DataInitializer` on startup
- Session-based auth via `HttpSession`
- ⚠️ Authentication is **not enforced** — all routes are currently open for development

### Dashboard (`/dashboard`)
- Stat cards: Total Clients, Active Clients, Total Revenue (live), Pending Payments (live)
- Monthly Revenue line chart — real data from PAID invoices grouped by month
- Leads Generated bar chart — real data from campaigns grouped by month
- Collapsible sidebar

### Clients (`/clients`)
- Full table with all client fields including Meta Ad Account ID column
- Add Client modal with all fields including Meta Ad Account ID
- Edit Client modal — pre-fills all fields, saves changes
- Delete Client — cascade deletes invoices, line items, campaigns, notes safely
- Status badges: Active (green), Paused (yellow), Completed (grey)

### Client Detail (`/clients/{id}`)
- Full client info card
- Quick stat cards (Monthly Fee, Ad Budget, Contract End)
- **Meta Ad Campaigns table** — shows spend, impressions, clicks, leads, CPL, period per campaign
- **Sync Meta Ads button** — pulls last 30 days from Meta Marketing API, upserts into DB
- Notes section — add/view notes with type badges (Meeting, Strategy, General) and timestamps

### Invoices (`/invoices`)
- Invoice list with client name, invoice number, amount, GST, status, date
- New Invoice form — auto-generated invoice number, dynamic line items, 18% GST auto-calc
- Save invoice with full line items persisted
- Mark as Paid / Unpaid toggle

### Reports (`/reports`)
- Per-client summary table: Ad Spend, Leads, CPL, Total Invoiced, Total Paid, Pending
- Export to CSV (`/reports/export`)

### Meta Ads Integration
- `MetaAdsService` calls Meta Marketing API v19.0
- Pulls per-campaign: spend, impressions, clicks, leads, CPL, date range
- Upserts into `campaigns` table using `metaCampaignId` to prevent duplicates
- Access token stored in `application.properties`
- Ad Account ID stored per client in DB

---

## What Is Remaining ❌

### High Priority

| Feature | Details |
|---|---|
| **Invoice PDF Download** | Generate downloadable PDF per invoice. Requires adding Flying Saucer (`org.xhtmlrenderer:flying-saucer-pdf`) or iText to `build.gradle.kts`. Endpoint: `GET /invoices/{id}/pdf` |
| **Re-enable Spring Security** | Protect all routes except `/login`. Re-add CSRF tokens to all forms. Proper login/logout flow. |
| **Ad Spend graph on Dashboard** | Third chart on dashboard showing monthly ad spend from campaigns table |

### Medium Priority

| Feature | Details |
|---|---|
| **Client Payment Tracker** | Payment history per client, payment method (Bank/UPI/Cash), partial payments, overdue alerts, due date reminders |
| **Invoice history on Client Detail** | List of all invoices for the client inside their detail page |
| **Excel (.xlsx) Export** | Add Apache POI to `build.gradle.kts`, add `GET /reports/export/xlsx` endpoint |
| **Automated Monthly PDF Report** | One-click per-client PDF with leads, spend, CPL, graphs. Export for sharing with clients |
| **Meta token auto-refresh** | Current token expires in ~60 days. Add a refresh mechanism or reminder |

### Low Priority

| Feature | Details |
|---|---|
| **Team Management** | Add/manage users with roles (Admin, Manager, Employee). Role-based permissions on routes and UI actions |
| **Sidebar active state** | Currently hardcoded to Dashboard in `layout.html`. Needs dynamic active class per page |
| **Ad Spend graph on Dashboard** | Wire third chart to real campaign spend data grouped by month |
| **Google Sheets export** | Integration-ready CSV is done; full Sheets API push is optional |
| **Client portal** | Separate login for clients to view their own campaign reports — out of current scope, future paid feature |

---

## Known Issues & Gotchas

**`PrototypeApplication.java`** — `main` method is missing `public`. Change to `public static void main` or the app won't start.

**CSRF is disabled.** Do NOT add `_csrf` hidden inputs to any form. If you re-enable CSRF, add to every POST form:
```html
<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
```

**PostgreSQL reserved word.** `User` entity uses `@Table(name = "users")` — never change this.

**Meta token expires in ~60 days.** Refresh it at [developers.facebook.com/tools/explorer](https://developers.facebook.com/tools/explorer) → Generate Token → Extend. Replace in `application.properties`.

**Meta sync returns empty** if the ad account has no campaigns running in the last 30 days. Needs at least one active or recently run campaign.

**Sidebar active state** is hardcoded to Dashboard in `fragments/layout.html`. All pages show Dashboard as active in the sidebar — cosmetic bug only.

---

## How to Run

1. Make sure PostgreSQL is running (`crm_db` on port `5432`)
2. Set your DB password and Meta access token in `application.properties`
3. Run via IntelliJ → Gradle `bootRun` or green run button on `PrototypeApplication.java`
4. Open `http://localhost:8081`
5. Login: `admin@crm.com` / `admin123`