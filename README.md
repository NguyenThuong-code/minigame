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

  Layer               Technology
  ------------------- ------------------------------------------
  Application         Spring Boot 3
  Security            Spring Security (OAuth2 Resource Server)
  Identity Provider   Keycloak 22
  Database            PostgreSQL 16
  Cache               Redis 7
  Mapping             MapStruct
  Containerization    Docker Compose
  Authentication      OAuth2 / OIDC (JWT)

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

application.yml:

spring: datasource: url: jdbc:postgresql://localhost:5432/minigame
username: postgres password: postgres

security: oauth2: resourceserver: jwt: issuer-uri:
http://localhost:8083/realms/minigame

cache: type: redis

data: redis: host: localhost port: 6379

------------------------------------------------------------------------

# 7. Game Logic & Caching Strategy

## Guess Game Rules

-   User guesses number between 1--5
-   5% win probability
-   Each guess consumes 1 turn
-   Win increases score
-   Row-level locking ensures correctness under concurrency

## Buy Turns

-   Adds 5 turns per purchase
-   Uses atomic DB update
-   Evicts Redis cache after commit

------------------------------------------------------------------------

## Redis Cache Design

Cache Names:

-   leaderboard → key: top10
-   me → key: keycloakId

Eviction Strategy:

-   Cache cleared AFTER transaction commit
-   Uses TransactionSynchronizationManager
-   Prevents stale data

------------------------------------------------------------------------

# 8. API Endpoints

## Register

POST /api/auth/register

## Login

POST /api/auth/login

## Guess

POST /api/game/guess

## Buy Turns

POST /api/game/buy-turns

## Leaderboard

GET /api/users/leaderboard

------------------------------------------------------------------------

# 9. Database Design

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

------------------------------------------------------------------------
# 10. env
  DB_PASSWORD=postgres;DB_USERNAME=postgres;KEYCLOAK_ADMIN_PASSWORD=admin;KEYCLOAK_ADMIN_USERNAME=admin;KEYCLOAK_CLIENT_ID=mini-game;KEYCLOAK_CLIENT_SECRET=VcrTaWllAXsMPQutNDU6XEKyAao7vnKB;KEYCLOAK_ISSUER_URI=http://localhost:8083/realms/minigame;KEYCLOAK_REALM=minigame;KEYCLOAK_SERVER_URL=http://localhost:8083
------------------------------------------------------------------------

# 11. Security Architecture

-   Passwords stored only in Keycloak
-   Stateless JWT validation
-   Role-based endpoint authorization
-   Database row locking for transactional safety
-   Redis used for performance optimization

------------------------------------------------------------------------

# 12. Testing
- import minigame.postman_collection.json to postman to test api(change Bearer token in Authorization header)
- Result test for one user sent many requests /guess at the same time(Result test in stress_out) 
## Step1 
- click folder → Open Git Bash here

## Step2
- run:  ./stress_test.sh
------------------------------------------------------------------------

# 13. Author

Thuong deptrai

------------------------------------------------------------------------

Internal Enterprise Application -- Confidential
