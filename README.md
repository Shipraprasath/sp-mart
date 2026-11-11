# SP MART — Complete Premium Electronics E-Commerce Web Application

**SP MART** is an enterprise-grade, full-stack electronics e-commerce web application engineered for a **B.Tech Artificial Intelligence and Data Science Capstone Project**. Built with **Java 21 LTS**, **Spring Boot 3.3.4**, **Spring Security 6**, **Spring Data JPA**, **Hibernate**, and **Thymeleaf**, backed by persistent **H2 Database** storage for local development and a production **MySQL 8** migration profile.

---

## 1. Project Overview & Architecture

- **Project Name:** SP MART
- **Academic Purpose:** B.Tech AI & Data Science Capstone Project
- **Architecture Pattern:** Multi-tier Layered Architecture (`Controller` → `Service` → `Repository` → `Entity` / `DTO`)
- **Design System:** Custom CSS Custom Properties (`--midnight-navy: #0B1220`, `--deep-blue: #142B4A`, `--electric-blue: #287BFF`, `--cyan-highlight: #22D3EE`, `--sale-coral: #F04452`)
- **Typography:** Space Grotesk (headings & brand logo) and Inter (body, buttons, prices)
- **Currency:** Indian Rupee (`₹`) throughout catalog, cart, checkout, seller, and admin modules
- **Sound System:** Melodic Web Audio API synthesizer for order celebrations with persistent user mute toggle
- **Security:** Spring Security 6 with BCrypt password hashing, CSRF tokens, Session Fixation protection, Remember-Me, and role-based URL guards

---

## 2. Technology Stack

| Layer | Technology |
|---|---|
| **Programming Language** | Java 21 LTS |
| **Backend Framework** | Spring Boot 3.3.4 |
| **Template Engine** | Thymeleaf + Thymeleaf Extras Spring Security 6 |
| **Frontend** | HTML5, CSS3 Custom Properties, JavaScript ES6+, Web Audio API |
| **Persistence & ORM** | Spring Data JPA, Hibernate 6.5 |
| **Database** | Persistent H2 Database (`./data/spmartdb`) + MySQL 8 Profile (`application-mysql.properties`) |
| **Security & Auth** | Spring Security 6, BCrypt Password Encoder, Remember-Me Services |
| **AI Engine** | Grounded Catalogue Knowledge Engine, Natural Language Budget Extractor & Device Comparison System |
| **Build Tool** | Apache Maven 3.9+ |
| **Testing** | JUnit 5, Mockito, Spring Boot Test, Spring Security Test (31 Automated Tests) |

---

## 3. User Roles & Multi-Role Permissions

### 👤 1. Buyer Account (`ROLE_CUSTOMER`)
- Split-screen registration and login with show/hide password and Remember-Me.
- Browse and search 12 electronics categories with brand, budget, and rating filters.
- Save electronics to Wishlist and move to Cart.
- Cart with quantity steppers and real-time Free Express Delivery tracker (over ₹500).
- "Almost Yours." checkout workflow with saved delivery address book.
- Payment options: UPI QR / VPA, Google Pay, Paytm, Debit/Credit Card, and Cash on Delivery (COD).
- Interactive payment sandbox with simulated success/failure testing.
- Order confirmation banner ("You're All Set!"), 3-5 days delivery estimate, and Web Audio API chime.
- Real-time 6-step visual tracking stepper and customer self-service cancellation.
- Post-delivery verified product reviews and ratings.
- Access to the floating AI Shopping Assistant on every consumer page.

### 🏪 2. Seller Account (`ROLE_SELLER`)
- Dedicated seller registration portal (`/seller/register`) with business details and GSTIN.
- Enforced administrator review workflow (`PENDING_APPROVAL` status redirecting to `/seller/pending`).
- SaaS Merchant Dashboard (`/seller/dashboard`) featuring 6 live metrics: Total Revenue, Total Orders, Active Listings, Low Stock Alerts, Pending Shipments, Customer Rating.
- Product CRUD (`/seller/products/**`) strictly isolated to the logged-in seller's catalog.
- Order fulfillment slicing (`/seller/orders/**`): update milestone fulfillment (`PROCESSING`, `SHIPPED`, `DELIVERED`).
- Revenue calculation and business analytics reporting (`/seller/reports`).

### 🛡️ 3. Administrator Account (`ROLE_ADMIN`)
- High-level executive dashboard (`/admin/dashboard`) with real-time analytics.
- **Seller Management (`/admin/sellers`):** Review merchant applications, approve, reject with reason, or suspend accounts.
- **Review Moderation (`/admin/reviews`):** Moderate customer feedback, approve verified ratings, hide, or delete inappropriate reviews.
- **Product & Category Management (`/admin/products`, `/admin/categories`):** Full control over global catalog and categories.
- **Warehouse Inventory Alerts (`/admin/inventory`):** Restock products and monitor low-stock items (≤ 10 units).
- **Customer Oversight (`/admin/customers`):** View registered buyers and manage access.

---

## 4. 12 Electronics Product Categories

1. **Smartphones** (Apple iPhone 15 Pro Max, Samsung Galaxy S24 Ultra, OnePlus 12, Google Pixel 8 Pro, etc.)
2. **Laptops & Computers** (Apple MacBook Pro M3, Dell XPS 15, Lenovo ThinkPad X1 Carbon, Asus ROG Zephyrus G14)
3. **Televisions** (Sony Bravia XR 65" 4K OLED, Samsung 55" Neo QLED 4K, LG C3 Series 55" OLED 4K)
4. **Air Conditioners** (Daikin 1.5 Ton 5-Star Inverter Split AC, Voltas 1.5 Ton 3-Star Split AC)
5. **Refrigerators** (LG 655L Side-by-Side Inverter Refrigerator, Samsung 253L Double Door Refrigerator)
6. **Washing Machines** (Bosch 8 kg 5-Star Inverter Front Load Washing Machine, IFB 7 kg Front Load)
7. **Speakers** (Sony HT-S20R 5.1ch Dolby Digital Soundbar, JBL Charge 5 Portable Bluetooth Speaker)
8. **Headphones & Earbuds** (Sony WH-1000XM5 Wireless Noise Cancelling, Apple AirPods Pro 2nd Gen)
9. **Kitchen Appliances** (Philips Digital Air Fryer HD9252, Dyson Pure Hot+Cool Air Purifier)
10. **Induction Stoves** (Prestige PIC 20 1200W Induction Cooktop, Philips Viva Collection Induction)
11. **Computer Accessories** (Logitech MX Master 3S Wireless Mouse, Keychron K2 Wireless Mechanical Keyboard)
12. **Smart-Home Devices** (Amazon Echo Dot 5th Gen with Alexa, Philips Hue Smart Bulb Starter Kit)

---

## 5. Intelligent AI Shopping Assistant

Available as a floating conversational widget on the bottom-right corner of all consumer pages (`POST /api/ai/assistant/chat`):

- **Catalogue Grounding:** Responses are strictly grounded in live database inventory to eliminate hallucinations.
- **Natural Language Budget Extractor:** Understands queries such as `"smartphones under 30000"`, `"laptops under 80k"`, `"soundbars below 15000"`.
- **Side-by-Side Device Comparison:** Triggered by `"compare iPhone vs Samsung"` or `"Sony vs LG"`. Produces a formatted markdown comparison table comparing effective price, discounts, customer ratings, stock status, and an AI purchasing verdict.
- **Tech Spec Explainer:** Explains technical concepts such as OLED vs QLED displays, Inverter compressors, RAM vs SSD, and Refresh rates.
- **Interactive Product Cards:** Renders rich thumbnail cards with pricing, discount %, and one-click "View Details" navigation.

---

## 6. Demonstration Accounts

The database is pre-seeded with ready-to-test credentials:

| Role | Email Address | Password | Permissions & Store Name |
|---|---|---|---|
| **Administrator** | `admin@spmart.com` | `Admin@123` | Full administrative control (`/admin/**`) |
| **Approved Seller 1** | `seller@spmart.com` | `Seller@123` | Apex Tech Official (`/seller/**`) |
| **Approved Seller 2** | `digitech@spmart.com` | `Seller@123` | DigiTech Home Appliances (`/seller/**`) |
| **Pending Seller** | `pending.seller@spmart.com` | `Seller@123` | FutureGadgets Hub (Awaiting Admin approval) |
| **Buyer 1** | `buyer@spmart.com` | `Buyer@123` | Verified customer with saved addresses |
| **Buyer 2** | `customer@spmart.com` | `Customer@123` | Active customer with previous order history |

---

## 7. Setup & Execution Instructions

### Prerequisites
- **Java 21 LTS** or newer (`java -version`)
- **Maven 3.8+** (`mvn -version`)
- Optional: **MySQL 8.0+** (if running the production profile)

### Local Execution (Default Persistent H2 Database)
```powershell
mvn spring-boot:run
```
- Storefront: [http://localhost:8080](http://localhost:8080)
- Seller Portal: [http://localhost:8080/seller/dashboard](http://localhost:8080/seller/dashboard)
- Admin Console: [http://localhost:8080/admin/dashboard](http://localhost:8080/admin/dashboard)
- H2 Web Console: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  *(JDBC URL: `jdbc:h2:file:./data/spmartdb`, User: `sa`, Password: blank)*

### Production Execution (MySQL 8 Migration Profile)
1. Ensure MySQL server is running.
2. Execute with the `mysql` profile:
```powershell
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```
Environment variable overrides (optional):
```powershell
$env:MYSQL_HOST="localhost"
$env:MYSQL_PORT="3306"
$env:MYSQL_DATABASE="spmartdb"
$env:MYSQL_USER="root"
$env:MYSQL_PASSWORD="yourpassword"
```

---

## 8. Test Suite Verification

Run the complete automated test suite of 31 tests:
```powershell
mvn test
```
- `AiAssistantServiceTest`: Verifies budget extraction, device comparison, and tech spec explanations.
- `SellerServiceTest`: Verifies seller registration, admin approval, and rejection workflows.
- `PasswordResetServiceTest`: Verifies token generation, expiry, single-use security, and password hash updates.
- `ProductServiceTest`: Verifies unique SKU constraints, catalog filtering, and inventory updates.
- `CartServiceTest`: Verifies cart additions, quantity steppers, and free delivery thresholds.
- `OrderServiceTest`: Verifies inventory deduction, idempotent submissions, cancellation restock, and status transitions.
- `AddressServiceTest`: Verifies user address management and IDOR security barriers.
- `UserServiceTest`: Verifies BCrypt encoding, duplicate protection, and customer profiles.
- `SecurityIntegrationTest`: Verifies public routes, authentication redirects, and role authorizations.
