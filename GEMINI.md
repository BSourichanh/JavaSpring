# Instructions de Projet & Optimisation de Tokens — Gemini / Antigravity

Ce fichier définit le contexte technique, les règles d'architecture et les consignes d'optimisation pour minimiser la consommation de tokens lors des échanges.

---

## ⚡ Directives d'Optimisation de Tokens (Mode Économe)

1. **Concision Maximale** :
   - Pas de politesses superflues ni d'introductions verbeuses ("Bonjour", "J'espère que vous allez bien...").
   - Réponses directes, denses, orientées action (code ciblé, explications courtes en puces).
   - Ne jamais réécrire un fichier complet si seules quelques lignes changent (fournir uniquement les blocs modifiés).

2. **Économie de Contexte & Outils** :
   - Limiter les lectures de fichiers aux lignes strictement nécessaires (`StartLine` / `EndLine`).
   - Éviter d'exécuter des commandes verbeuses sans filtrage (privilégier les flags silencieux ou filtrer la sortie).
   - Pas de résumés verbeux après l'exécution d'un outil : indiquer le résultat (Succès / Erreur) et le point clé.

3. **Langue** : Français technique, précis et direct.

---

## 🏛️ Architecture & Stack Technique

- **Projet** : Square Games API (Formation Le Campus Numérique)
- **Langage** : Java 21+
- **Framework** : Spring Boot 4.x
- **Build tool** : Maven Wrapper (`./mvnw`)
- **Bibliothèque externe** : `fr.le_campus_numerique.square_games:engine:1.0-SNAPSHOT`

### Organisation des packages (`com.bsourichanh.javaspring`) :
- `controller/` : Contrôleurs REST (`@RestController`, `@RequestMapping`).
- `dto/` : Objets de transfert de données (Java 21 `record` uniquement, annotations Jackson `@JsonProperty`, `@JsonAlias`).
- `service/` : Interfaces et implémentations métier (`@Service`).
- `dao/` : Couche d'accès aux données (`GameDao`, `InMemoryGameDao`, `JdbcGameDao`, `JpaGameDao`).
- `plugin/` : Plugins de jeux (`GamePlugin`, `TicTacToePlugin`, `TaquinPlugin`, `ConnectFourPlugin`).
- `config/` : Configurations de beans Spring (`@Configuration`, `@Bean`).

---

## 📐 Règles de Conception & Bonnes Pratiques

1. **Injection de Dépendances** :
   - Uniquement par **constructeur** (pas de `@Autowired` sur les champs).
   - Champs `private final`.
   - Utilisation d'interfaces dans les services (`GameDao`, `GamePlugin`, `GameCatalog`, `GameService`).

2. **Types & Nullabilité** :
   - Préférer les wrappers d'objets (`Integer`) aux types primitifs (`int`) dans les DTOs pour autoriser les valeurs par défaut lorsqu'un champ est omis en JSON.

3. **Internationalisation (i18n)** :
   - `MessageSource` injecté dans les composants nécessitant une traduction.
   - Les contrôleurs acceptent `Locale locale` résolu via l'en-tête `Accept-Language`.
   - Fichiers de ressources : `messages.properties` (fallback), `messages_en.properties`, `messages_fr.properties`.
   - Propriété obligatoire : `spring.messages.fallback-to-system-locale=false`.

4. **Persistance & Pattern DAO (Itération 3)** :
   - Le service `GameServiceImpl` ne stocke rien en local : tout passe par `GameDao`.
   - Utilisation de profils Spring (`@Profile("memory")`, `@Profile("jdbc")`, `@Profile("jpa")`) pour basculer d'implémentation sans conflit.

---

## 🧪 Commandes de Référence Rapide

```bash
# Compilation & Tests
./mvnw clean test

# Démarrage de l'application
./mvnw spring-boot:run

# Vérification état du port 8080
lsof -i :8080
```
