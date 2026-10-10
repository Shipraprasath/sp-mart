# SP MART — CAPSTONE PROJECT PRESENTATION SLIDE DECK (PPT)
## B.Tech Artificial Intelligence and Data Science Capstone Viva Presentation

---

### SLIDE 1: TITLE SLIDE
- **Project Title:** SP MART — Premium Electronics E-Commerce Web Application
- **Sub-Title:** Grounded AI Recommendation Architecture, Multi-Role Security & Progressive Web App Mobility
- **Academic Degree:** Bachelor of Technology (B.Tech) in Artificial Intelligence and Data Science
- **Technology Stack:** Java 21 LTS | Spring Boot 3.3.4 | Spring Security 6 | Spring Data JPA | Thymeleaf | PWA
- **Live Repository:** `https://github.com/Shipraprasath/sp-mart`

> **Speaker Notes:**  
> "Good morning, respected professors and external examiners. Today, I am proud to present SP MART, an enterprise-grade full-stack electronics e-commerce web application engineered with catalog-grounded Artificial Intelligence, multi-role security architecture, and modern mobile Progressive Web App standards."

---

### SLIDE 2: PROBLEM STATEMENT & MOTIVATION
- **Consumer Electronics Dilemma:**
  - High Average Order Value (AOV) requires high buyer trust and spec clarity.
  - Complex technical specifications (lithography, refresh rates, inverter ratings) confuse consumers.
- **Deficiencies in Existing E-Commerce Platforms:**
  1. *AI Hallucination Problem:* Generic bots recommend non-existent specs or out-of-stock items.
  2. *Privilege Bleed:* Weak role isolation allows unauthorized order state changes.
  3. *Monolithic Complexity:* Poor mobile adaptability and lack of native app download options.

> **Speaker Notes:**  
> "When buying electronics online, customers need exact specifications and honest comparisons. Existing chat assistants hallucinate because they are detached from the inventory database. SP MART solves this with direct database grounding and multi-role governance."

---

### SLIDE 3: OBJECTIVES OF SP MART
- **1. Grounded AI Shopping Engine:** Provide real-time budget parsing and side-by-side spec comparisons with zero hallucination.
- **2. Enterprise Multi-Role Architecture:** Enforce strict role boundaries across Buyers (`ROLE_CUSTOMER`), Verified Merchants (`ROLE_SELLER`), and System Administrators (`ROLE_ADMIN`).
- **3. Seamless Mobile-First PWA:** Deliver native-app installability without third-party app store gatekeepers.
- **4. Production-Ready Resilience:** Full test coverage (31 automated JUnit tests) and cloud PaaS readiness.

> **Speaker Notes:**  
> "Our core objective is to merge modern AI assistance with strict enterprise Java backend reliability, ensuring data integrity from browsing to delivery."

---

### SLIDE 4: SYSTEM ARCHITECTURE
- **Layered Multi-Tier Architecture:**
  - **Presentation Layer:** Thymeleaf 3, HTML5, CSS3 Custom Properties, JavaScript ES6+, Web Audio API.
  - **Security Layer:** Spring Security 6, Cookie-based CSRF Repository, BCrypt (Factor 12).
  - **Application / Controller Layer:** RESTful APIs & Spring MVC Controllers.
  - **Service / Business Layer:** Transactional domain logic & grounded AI engine.
  - **Persistence / Data Layer:** Spring Data JPA with Hibernate 6.5.
  - **Database Layer:** Persistent H2 file database (`./data/spmartdb`) + MySQL 8 production profile.

```
[ Client Browser / PWA Mobile ]
               │ HTTP / HTTPS
               ▼
[ Spring Security 6 Filter Chain (BCrypt + CSRF) ]
               │
               ▼
[ Controllers (MVC & REST APIs) ]
               │
               ▼
[ Service Layer (AI Engine + Business Logic) ]
               │
               ▼
[ Spring Data JPA & Hibernate 6.5 ]
               │
               ▼
[ H2 Database (Dev) / MySQL 8 (Prod) ]
```

> **Speaker Notes:**  
> "We implemented an industry-standard Multi-Tier Layered Architecture. Controllers never interact with database entities directly; all requests flow through transactional service contracts and DTOs."

---

### SLIDE 5: USER ROLES & PRIVILEGE MATRIX

| Feature / Permission | Buyer (`CUSTOMER`) | Seller (`SELLER`) | Admin (`ADMIN`) |
|---|:---:|:---:|:---:|
| Browse & Search Catalog | ✅ | ✅ | ✅ |
| Ask AI Assistant | ✅ | ✅ | ✅ |
| Shopping Cart & Checkout | ✅ | ❌ | ❌ |
| Order Cancellation | ✅ (Self-Service) | ❌ | ✅ (Override) |
| Seller Portal Access | ❌ | ✅ (Approved Only) | ❌ |
| Product CRUD Operations | ❌ | ✅ (Own Catalog) | ✅ (Global) |
| Order Shipment Slicing | ❌ | ✅ (Assigned Orders) | ✅ |
| Seller Account Approval | ❌ | ❌ | ✅ |
| Review Moderation | ❌ | ❌ | ✅ |

> **Speaker Notes:**  
> "Privileges are strictly enforced at the HTTP filter chain level and service boundaries. A seller can only manage their own products, and a buyer cannot access administrative dashboards."

---

### SLIDE 6: GROUNDED AI SHOPPING ASSISTANT
- **How It Works:**
  - **Natural Language Budget Extractor:** Parses phrases like `"laptops under 70k"`, `"phones below ₹30,000"`, `"soundbars under 15000"`.
  - **Brand & Category Token Extraction:** Matches brand and category keywords against JPA repositories.
  - **Side-by-Side Comparison Matrix:** Detects comparisons like `"MacBook vs Dell"` or `"iPhone vs Samsung"`, outputting a comparative spec table and AI verdict.
  - **Grounded Product Cards:** Emits rich UI cards with live pricing, stock status, and direct detail links.
  - **Zero Hallucination:** Every response is directly synthesized from active database records.

> **Speaker Notes:**  
> "Unlike general-purpose LLMs that invent product specs, our AI Assistant is strictly catalog-grounded. If a product is not in the database, the AI will never claim we have it."

---

### SLIDE 7: DATABASE DESIGN & RELATIONAL DATA MODEL
- **15 Structured Relational Tables:**
  - Identity: `users`, `seller_profiles`, `addresses`, `password_reset_tokens`.
  - Catalog: `categories` (12 categories), `products`, `reviews`.
  - Commerce: `carts`, `cart_items`, `wishlist_items`.
  - Orders: `customer_orders`, `order_items`, `order_status_histories`, `payments`.
- **Integrity Constraints:** Foreign key cascades, unique constraints on emails, slugs, and order numbers.

> **Speaker Notes:**  
> "Our database features 15 normalized tables. Orders maintain immutable historical item snapshots, meaning that if a merchant changes a product price tomorrow, past customer orders retain their original purchase price."

---

### SLIDE 8: CHECKOUT WORKFLOW & PAYMENT SANDBOX
- **Interactive Multi-Step Checkout:**
  - Saved Address Selector with new address modal.
  - Dynamic Free Delivery calculation (Free if $\ge$ ₹500, else ₹40).
- **Payment Sandbox Options:**
  - UPI QR Code & VPA simulation.
  - Google Pay & Paytm one-click flow.
  - Credit / Debit Card sandbox.
  - Cash on Delivery (COD) verification.
- **Web Audio API Celebration Chime:** Synthesizes a harmonic two-tone confirmation sound with persistent mute toggle.

> **Speaker Notes:**  
> "We built an interactive payment gateway sandbox supporting modern Indian payment rails including UPI and wallets, combined with Web Audio API chime synthesis for enhanced UX."

---

### SLIDE 9: SELLER BACK-OFFICE (SaaS MERCHANT PORTAL)
- **Merchant Onboarding:**
  - Self-service registration with GSTIN and business credentials.
  - Placed into `PENDING_APPROVAL` status until Administrator verification.
- **Merchant SaaS Dashboard:**
  - 6 Key Metrics: Total Revenue, Total Orders, Active Listings, Low Stock Alerts ($\le 10$ units), Pending Shipments, Store Rating.
- **Isolated Catalog Management:**
  - Full CRUD operations strictly restricted to the authenticated merchant's products.
  - Milestone shipment updater (`PROCESSING` → `SHIPPED` → `DELIVERED`).

> **Speaker Notes:**  
> "The seller back-office functions as a full SaaS portal where merchants manage their operations independently without interfering with other vendors."

---

### SLIDE 10: ADMINISTRATOR GOVERNANCE & OPERATIONS
- **Platform-Wide KPI Overview:**
  - Real-time aggregate revenue, order counts, active listings, and merchant count.
- **Seller Application Approval Queue:**
  - Review, approve, or reject merchant onboarding applications.
- **Review Moderation System:**
  - Audit customer feedback before publication to prevent abusive reviews.
- **Inventory Replenishment:**
  - One-click restock actions on low-stock warehouse products.

> **Speaker Notes:**  
> "The administrator panel provides high-level executive oversight, ensuring marketplace compliance and counterfeit prevention."

---

### SLIDE 11: CYBERSECURITY & DEFENSE MECHANISMS
- **BCrypt Cryptographic Hashing:** Strength factor 12 for all stored passwords.
- **Cookie-Based CSRF Mitigation:** `CookieCsrfTokenRepository.withHttpOnlyFalse()` with eager token pre-resolution to prevent response buffer commit exceptions.
- **SQL Injection Prevention:** 100% parameterized queries via Spring Data JPA and Hibernate ORM.
- **XSS Protection:** Context-aware automatic HTML escaping in Thymeleaf templates.
- **Session Security:** Session fixation defense and role-based URL guards.

> **Speaker Notes:**  
> "Security is implemented at defense-in-depth levels: passwords are non-reversible BCrypt hashes, CSRF tokens protect all state-modifying actions, and JPA prevents SQL injection."

---

### SLIDE 12: PROGRESSIVE WEB APP (PWA) MOBILITY
- **Web App Manifest (`/manifest.json`):**
  - Defines app name `SP MART`, theme color `#4F67EB`, and `standalone` display mode.
- **Service Worker (`/sw.js`):**
  - Caches critical application shells, enabling rapid repeat loading.
- **Native Install Experience:**
  - Detects mobile browser events and displays a 1-click **"Install SP MART App"** button.
  - Installs directly to Android and iOS home screens as a native application.

> **Speaker Notes:**  
> "SP MART is a verified Progressive Web App. Users opening the link on their smartphones can install it directly to their home screen without opening Google Play or the App Store."

---

### SLIDE 13: SOFTWARE TESTING & QUALITY ASSURANCE
- **Automated Test Results: 31 / 31 Passed (100% Success)**
  - *Security Tests:* Role URL restrictions, unauthorized redirects.
  - *Service Tests:* Product catalog search, cart price recalculation, order stock decrements, seller approval workflows.
  - *AI Assistant Tests:* Budget regex extraction, device comparison matrix validation.

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running SecurityIntegrationTest ... PASS
[INFO] Running ProductServiceTest ... PASS
[INFO] Running CartServiceTest ... PASS
[INFO] Running OrderServiceTest ... PASS
[INFO] Running SellerServiceTest ... PASS
[INFO] Running AiAssistantServiceTest ... PASS
[INFO] Running UserServiceTest ... PASS
[INFO] Results: 31 passed, 0 failed, 0 errors
[INFO] BUILD SUCCESS
```

> **Speaker Notes:**  
> "Every critical path is backed by automated tests, ensuring regression-free deployment and verifiable software quality."

---

### SLIDE 14: CLOUD DEPLOYMENT ARCHITECTURE
- **Continuous Deployment Pipeline:**
  - Source Code managed on GitHub: `https://github.com/Shipraprasath/sp-mart`.
  - Deployment Platform: Railway Cloud PaaS.
  - Dynamic Port Management: Configured via `server.port=${PORT:8080}`.
  - Java Runtime Configuration: `system.properties` declaring `java.runtime.version=21`.

> **Speaker Notes:**  
> "The application is fully cloud-native, ready for automatic deployment via Railway PaaS with dynamic port allocation."

---

### SLIDE 15: DEMONSTRATION WALKTHROUGH
- **1. Consumer Experience:**
  - Futuristic Cyber-Obsidian Login (`http://localhost:8080/login`) with 1-click role switcher.
  - Catalog browsing, multi-faceted filtering, and adding items to cart.
  - Conversational AI Shopping Assistant queries.
  - Checkout sandbox and Web Audio chime celebration.
- **2. Seller Experience:**
  - Login as `seller1@spmart.com` (`Seller@123`).
  - View real-time merchant revenue and update product inventory.
- **3. Admin Experience:**
  - Login as `admin@spmart.com` (`Admin@123`).
  - Approve merchant applications and moderate reviews.

> **Speaker Notes:**  
> "We will now walk through live demonstrations of each of the three user portals and the AI Assistant."

---

### SLIDE 16: CONCLUSION & FUTURE SCOPE
- **Key Achievements:**
  - Successfully developed and deployed an enterprise-grade electronics e-commerce web application.
  - Catalog-grounded AI eliminates shopping hallucinations.
  - Modern PWA provides smartphone installation.
- **Future Enhancements:**
  - Vector Embeddings (RAG) with pgvector for semantic search.
  - Real Razorpay / Stripe payment gateway webhooks.
  - Web Speech API integration for hands-free voice shopping.

> **Speaker Notes:**  
> "In conclusion, SP MART proves that enterprise Java backends and grounded AI can deliver a seamless, high-performance shopping experience. Thank you. We are now open for questions."

---

### SLIDE 17: VIVA VOCE — FREQUENT EXAMINER QUESTIONS & ANSWERS

**Q1: How does your AI Assistant prevent hallucinations?**  
*Answer:* "Generic LLMs generate text based on statistical token probabilities, which causes them to invent specs or products. SP MART uses deterministic database grounding: the query is parsed for budget, brand, and category, and then queried directly against our Spring Data JPA repository. If a product does not exist in our active inventory, the AI cannot suggest it."

**Q2: Why did you choose Spring Security 6 and BCrypt?**  
*Answer:* "Spring Security 6 provides a robust filter-chain architecture that separates role authorization (`ROLE_CUSTOMER`, `ROLE_SELLER`, `ROLE_ADMIN`) before requests reach controllers. We used BCrypt with factor 12 because it includes an adaptive work factor and automatic salt generation, making it resilient against rainbow table and brute-force attacks."

**Q3: How does the Progressive Web App (PWA) work without an App Store?**  
*Answer:* "A PWA relies on two browser standards: the Web App Manifest (`manifest.json`) which defines the app's name, icons, and standalone display mode; and the Service Worker (`sw.js`) which runs as a background script, caching assets and enabling mobile browsers to prompt users to install the web application directly onto their device home screen."
