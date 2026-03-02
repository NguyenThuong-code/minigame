# Mini Game Backend Platform

Enterprise-grade Spring Boot backend system integrating **Keycloak
(IAM)**, **PostgreSQL**, and **Redis caching**.

------------------------------------------------------------------------

# 1. Executive Overview

Mini Game Backend is a secure, scalable RESTful API designed with
enterprise architecture principles:

-   Centralized Authentication via Keycloak
-   Stateless JWT-based Security
-   PostgreSQL Persistent Storage
-   Redis Distributed Caching
-   Transaction-safe Game Logic
-   Role-based Access Control (RBAC)

The system is containerized using Docker Compose for consistent local
and production-ready environments.

------------------------------------------------------------------------

# 2. Technology Stack

| Layer | Technology |
|--------|------------|
| Application | Spring Boot 3 |
| Security | Spring Security (OAuth2 Resource Server) |
| Identity Provider | Keycloak 22 |
| Database | PostgreSQL 16 |
| Cache | Redis 7 |
| Mapping | MapStruct |
| Containerization | Docker Compose |
| Authentication | OAuth2 / OIDC (JWT) |
| Payment Gateway | VNPAY Sandbox |

------------------------------------------------------------------------

# 3. System Architecture

Client → Keycloak → JWT → Spring Boot API → PostgreSQL\
↓\
Redis

Keycloak handles identity & credential storage.\
Spring Boot validates JWT and manages business logic.\
Redis caches leaderboard and profile data.

------------------------------------------------------------------------

# 4. Docker Infrastructure (docker-compose.yml)

Version: 3.8

Services:

-   PostgreSQL → 5432
-   Keycloak → 8083
-   pgAdmin → 5050
-   Redis → 6379

Start all services:

    docker compose up -d

------------------------------------------------------------------------

# 5. Keycloak Configuration (Detailed Setup)

Access Admin Console:

    http://localhost:8083

Login: - Username: admin - Password: admin

------------------------------------------------------------------------

## Create Realm

Create realm named:

    minigame

------------------------------------------------------------------------

## Create Client in Keycloak Admin Console

### Step 1: Go to Clients

http://localhost:8083 → login → select realm **minigame**\
Click **Clients** in left sidebar → click **Create client**

------------------------------------------------------------------------

### Step 2: General Settings

Client type: OpenID Connect\
Client ID: mini-game (must match application.yml client-id)\
Name: Mini Game\
Click Next

------------------------------------------------------------------------

### Step 3: Capability Configuration

Enable:

☑ Client authentication\
☐ Authorization

Authentication flow:

☐ Standard flow\
☑ Direct access grants

Click Next

------------------------------------------------------------------------

### Step 4: Login Settings

Root URL: http://localhost:8080\
Home URL: http://localhost:8080\
Valid redirect URIs: http://localhost:8080/\*\
Web origins: http://localhost:8080

Click Save

------------------------------------------------------------------------

### Step 5: Get Client Secret

After saving:

Click **Credentials tab**\
Copy the **Client Secret**

Update application.yml:

keycloak: client-secret: `<paste-secret-here>`{=html}

------------------------------------------------------------------------

### Step 6: Create Realm Roles

Left sidebar → Realm Roles → Create Role

Create:

Role 1: USER\
Role 2: ADMIN

------------------------------------------------------------------------

# 6. Application Configuration

## Step 1: Setup ngrok

Create account:  
https://dashboard.ngrok.com/signup

Add authtoken:

ngrok config add-authtoken YOUR_TOKEN

Start tunnel:

ngrok http 8080

Copy generated HTTPS URL and update application.yml.

---

## Step 2: Register Merchant Sandbox

Register:
https://sandbox.vnpayment.vn/devreg

Activate via email and receive:

- vnp_TmnCode
- vnp_HashSecret
- vnp_Url

---

## Step 3: Configure Merchant Portal

Login:
https://sandbox.vnpayment.vn/merchantv2/

Update:

IPN URL:
https://your-ngrok-url/payments/vnpay/ipn

Return URL:
https://your-ngrok-url/payments/vnpay/return

---

## Step 4: Sandbox Test Card

Bank: NCB  
Card Number: 9704198526191432198  
Cardholder: NGUYEN VAN A  
Issue Date: 07/15  
OTP: 123456

------------------------------------------------------------------------

# 7. Application Configuration

application.yml:

spring: datasource: url: jdbc:postgresql://localhost:5432/minigame
username: postgres password: postgres

security: oauth2: resourceserver: jwt: issuer-uri:
http://localhost:8083/realms/minigame

cache: type: redis

data: redis: host: localhost port: 6379

------------------------------------------------------------------------

# 8. Game Logic & Payment Flow & Caching Strategy

## Guess Game Rules

-   User guesses number between 1--5
-   5% win probability
-   Each guess consumes 1 turn
-   Win increases score
-   Row-level locking ensures correctness under concurrency


---

## Payment Flow

1. POST /payments/vnpay/buy-turns/create
2. Backend signs request using HMAC SHA512
3. User completes payment
4. VNPAY calls /payments/vnpay/ipn
5. Backend verifies signature and adds 5 turns

---

##  Security Notes

- Hash algorithm: HMAC SHA512
- Parameters sorted A–Z
- UTF-8 URL encoded
- Exclude vnp_SecureHash when signing
- Turns added only inside IPN handler

------------------------------------------------------------------------

## Redis Cache Design

Cache Names:

-   leaderboard → key: top10

Eviction Strategy:

-   Cache cleared AFTER transaction commit
-   Uses TransactionSynchronizationManager
-   Prevents stale data

------------------------------------------------------------------------

# 9. API Endpoints

| Endpoint | Method | Description | Authentication |
|----------|--------|------------|----------------|
| `/api/auth/register` | POST | Register new user | No |
| `/api/auth/login` | POST | Login user and receive JWT | No |
| `/api/users/me` | GET | Get current authenticated user profile | JWT Required |
| `/api/game/guess` | POST | Play guessing game (consume 1 turn) | JWT Required |
| `/payments/vnpay/buy-turns` | POST | Create VNPAY payment URL | JWT Required |
| `/payments/vnpay/ipn` | GET | VNPAY IPN callback (verify signature & add turns) | Public (Signature Verified) |
| `/api/users/leaderboard` | GET | Get Top 10 leaderboard | JWT Required |

------------------------------------------------------------------------

## Authentication Header (For Secured APIs)

All secured endpoints require:

------------------------------------------------------------------------

# 10. Database Design

Table: user_profiles

Fields:

-   id (UUID)
-   keycloak_id
-   username
-   email
-   first_name
-   last_name
-   phone_number
-   total_guesses
-   score
-   turns
-   role
-   updated_at

Table: payment_transaction

Fields:

- id

- txn_ref

- keycloak_id

- amount_vnd

- status (PENDING, SUCCESS, FAILED)

- created_at

- updated_at

------------------------------------------------------------------------
# 11. env
DB_PASSWORD=postgres;DB_USERNAME=postgres;KEYCLOAK_ADMIN_PASSWORD=admin;KEYCLOAK_ADMIN_USERNAME=admin;KEYCLOAK_CLIENT_ID=mini-game;KEYCLOAK_CLIENT_SECRET=VcrTaWllAXsMPQutNDU6XEKyAao7vnKB;KEYCLOAK_ISSUER_URI=http://localhost:8083/realms/minigame;KEYCLOAK_REALM=minigame;KEYCLOAK_SERVER_URL=http://localhost:8083;VNP_TMN_CODE=FEA2NGHF;VNP_HASH_SECRET=CID2JX6D6X5TXBLBNMFGSCE19YK3G3FP

------------------------------------------------------------------------

# 12. Security Architecture

-   Passwords stored only in Keycloak
-   Stateless JWT validation
-   Role-based endpoint authorization
-   Database row locking for transactional safety
-   Redis used for performance optimization

------------------------------------------------------------------------

# 13. Testing
- import minigame.postman_collection.json to postman to test api(change Bearer token in Authorization header)
- Result test for one user sent many requests /guess at the same time(Result test in stress_out) 
## Step1 
- click folder → Open Git Bash here

## Step2
- run:  ./stress_test.sh
------------------------------------------------------------------------

# 14. Author

Thuong deptrai

------------------------------------------------------------------------

Internal Enterprise Application -- Confidential
