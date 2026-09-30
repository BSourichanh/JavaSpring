# 🎮 Square Games API & User Microservice

> Architecture microservices Spring Boot & Java 21 démontrant l'ensemble des concepts fondamentaux : Inversion de Contrôle, Plugins modulaires (Pattern Stratégie), Internationalisation (i18n), Persistance multi-sources (Pattern DAO : Mémoire, JDBC, JPA), Communication Inter-services (`RestClient`), Sécurisation Stateless (Spring Security, BCrypt, JJWT, RBAC), Gestion centralisée des exceptions (`@RestControllerAdvice`) et Frontend Web SPA complet.

---

## 🏛️ Architecture Globale

Le projet s'articule autour de deux microservices collaborant via HTTP REST et sécurisés par JSON Web Tokens, accompagnés d'une interface web moderne :

```text
┌────────────────────────────────────────────────────────┐
│                      Client HTTP                       │
│        (Frontend SPA Web, Bruno, Swagger UI)          │
└──────────────────────────┬─────────────────────────────┘
                           │
             ┌─────────────┴─────────────┐
             │                           │
  1. POST /auth/login                    │ 2. Requêtes de jeu avec
     (username, password)                │    Authorization: Bearer <jwt>
             │                           │
             ▼                           ▼
┌──────────────────────────┐   ┌──────────────────────────┐
│  User Service (:8081)    │   │ Square Games API (:8080) │
├──────────────────────────┤   ├──────────────────────────┤
│ - Inscription & Rôles    │   │ - Frontend SPA statique  │
│ - BCrypt Password Hash   │   │ - Moteur de jeux corrigé │
│ - Génération JWT HS256   │   │ - Morpion, Taquin, P4    │
│ - RBAC (@PreAuthorize)   │   │ - Pattern Stratégie      │
│ - Validation Jakarta     │   │ - Validation locale JWT  │
│ - GlobalExceptionHandler │   │ - DAO: Memory, JDBC, JPA │
│ - H2 Database            │   │ - GlobalExceptionHandler │
└──────────────────────────┘   └──────────────────────────┘
```

---

## 💻 Interface Web SPA (Single Page Application)

L'application intègre une interface web complète servie directement sur `http://localhost:8080/` :
- **Authentification & Gestion de profil** : Inscription et connexion avec stockage sécurisé du JWT dans le `localStorage`.
- **Catalogue & Création de parties** : Support multilingue (FR / EN) résolu dynamiquement via l'en-tête `Accept-Language`.
- **Arène de jeu temps réel** :
  - **Morpion (Tic-Tac-Toe)** : Placement dynamique des pions avec indicateur visuel de tour de rôle.
  - **Puissance 4 (Connect Four)** : Grille 7x6 avec moteur de gravité par colonne.
  - **Taquin (15-Puzzle)** : Moteur autonome résoluble (`FixedTaquinGame`), détection d'adjacence stricte et mise en valeur interactive des seules tuiles déplaçables (halo cyan pulsant).
- **Historique & Polling** : Rechargement automatique des coups et consultation des parties de l'utilisateur.

---

## 🚀 Les 5 Itérations & Évolutions d'Architecture

### 1. Itération 1 — Introduction & Inversion de Contrôle (IoC)
- Endpoint de santé `/heartbeat` via injection de dépendance par constructeur (`HeartbeatSensor` / `RandomHeartbeatSensor`).
- Aléatoire haute performance sans contention de verrou via `ThreadLocalRandom`.

### 2. Itération 2 — Architecture par Plugins (Pattern Stratégie) & Multilingue (i18n)
- Découplage du moteur de jeu tiers via l'interface `GamePlugin`.
- **Pattern Stratégie (GoF) & Open-Closed Principle (OCP)** : Sélection dynamique des plugins via `canHandle(gameType)` sans modifier le service.
- Plugins modulaires : **Morpion (Tic-Tac-Toe)**, **Taquin (Puzzle 15)**, **Puissance 4 (Connect Four)**.
- DTOs immuables (`record` Java 21) utilisant des wrappers d'objets (`Integer`) et des annotations de validation Jakarta (`@NotNull`).
- Internationalisation dynamique via `MessageSource` et l'en-tête `Accept-Language` (`fr`, `en`, fallback).

### 3. Itération 3 — Persistance & Pattern DAO
- Découplage de la persistance via l'interface `GameDao`.
- **3 implémentations étanches par profils Spring** :
  - `@Profile("memory")` : Cache en mémoire vive `ConcurrentHashMap`.
  - `@Profile("jdbc")` : Requêtes SQL natives avec `NamedParameterJdbcTemplate` et `schema.sql`.
  - `@Profile("jpa")` : Mapping relationnel Spring Data JPA (`GameEntity`, `GameTokenEntity`, relation `@OneToMany`) avec encapsulation complète (`private` et accesseurs).
- **Sources de données** : MySQL Docker (port `6603`) et base H2 mémoire.

### 4. Itération 4 — Microservices & Communication Inter-services
- Séparation du microservice autonome `JavaSpringUsers` (port `8081`).
- Client HTTP déclaratif `RestClient` avec interface de service dédiée `UserValidationService` (Inversion de dépendance).
- Identification du joueur via l'en-tête `X-UserId` et vérification distante de validité (`GET /users/{id}/valid`).
- Contrôle multi-joueurs strict sur `POST /games/{id}/moves` (seul `currentPlayerId` est autorisé à jouer).

### 5. Itération 5 — Sécurité Stateless, JWT, BCrypt, RBAC & Qualité
- **Service Utilisateurs (:8081)** :
  - Hash des mots de passe avec `BCryptPasswordEncoder`.
  - Émission de jetons cryptographiques signés HMAC-SHA256 (`POST /auth/login`).
  - Validation des payloads à l'inscription et au login (`@Valid`, `@NotBlank`, `@Email`, `@Size`).
  - Autorisations fines par rôles (`ROLE_USER`, `ROLE_ADMIN`) avec `@PreAuthorize` et `@EnableMethodSecurity`.
  - Protection anti-clickjacking sur la console H2 (`sameOrigin`).
- **Square Games API (:8080)** :
  - Filtre `JwtAuthenticationFilter` (`OncePerRequestFilter`) et `SecurityConfig`.
  - Validation 100% locale du jeton Bearer via secret partagé (0 appel réseau vers `:8081`).
  - **Cloisonnement BOLA (Broken Object Level Authorization)** sur `GET /games/{id}` : consultation strictement réservée aux participants inscrits dans la partie.
- **Gestionnaire Global d'Exceptions** :
  - Centralisation des erreurs via `@RestControllerAdvice` (`GlobalExceptionHandler`) pour un formatage JSON uniforme des erreurs (400, 401, 403, 404, 409).

---

## 🛠️ Stack Technique

- **Java** : OpenJDK 21+
- **Framework** : Spring Boot 4.x / Spring Framework 6.x
- **Frontend** : HTML5, CSS3 Moderne (Glassmorphism & animations CSS), Vanilla JavaScript SPA
- **Sécurité** : Spring Security 6.x, JJWT (`io.jsonwebtoken` 0.12.6), BCrypt
- **Persistance** : Spring Data JPA, Hibernate, Spring JDBC, MySQL Connector / H2
- **Validation** : Jakarta Bean Validation (`spring-boot-starter-validation`)
- **Documentation API** : SpringDoc OpenAPI 2.8.5 (Swagger UI)
- **Tests** : JUnit 5, Mockito, Spring Test (MockMvc)
- **Outils** : Maven Wrapper (`./mvnw`), Docker (MySQL), Bruno

---

## 🚦 Démarrage Rapide

### 1. Démarrer la base MySQL Docker (Itération 3)
```bash
docker start docker_mysql
# Port hôte : 6603 | User : root | Password : helloworld | Database : square_games
```

### 2. Démarrer le Service Utilisateurs (Port 8081)
```bash
cd ../JavaSpringUsers
./mvnw spring-boot:run
```

### 3. Démarrer l'API Square Games & le Frontend (Port 8080)
```bash
# Optionnel : configurer les variables d'environnement (JWT_SECRET)
cp .env.example .env

# Mode Mémoire (par défaut)
./mvnw spring-boot:run

# Mode JPA + MySQL Docker
./mvnw spring-boot:run -Dspring-boot.run.profiles=jpa,mysql

# Mode JPA + H2
./mvnw spring-boot:run -Dspring-boot.run.profiles=jpa,h2
```

L'application web est accessible directement sur : **`http://localhost:8080/`**

---

## 🧪 Tests Automatisés

```bash
# Tests de l'API de jeux (23 tests unitaires et d'intégration)
./mvnw clean test

# Tests du service utilisateurs (9 tests d'intégration)
cd ../JavaSpringUsers && ./mvnw clean test
```

---

## 📖 Documentation & Outils

- **Application Web SPA** : `http://localhost:8080/`
- **Swagger UI** : `http://localhost:8080/swagger-ui.html`
- **Collections Bruno** : Situées dans le dossier [`bruno/`](bruno/) (requêtes pas à pas des étapes 1 à 5).
