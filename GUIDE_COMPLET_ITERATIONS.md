# 📚 Guide Complet Pas à Pas — Toutes les Itérations Java Spring (Square Games)

> [!INFO] **Manuel Intégral du Module Spring Boot**  
> Ce document couvre l'intégralité des **Itérations 1 et 2** du cours du Campus Numérique. Chaque étape détaille les concepts théoriques, l'architecture, les fichiers à créer/modifier avec leur code source intégral, ainsi que les commandes de test et de validation.

---

## 🗺️ Sommaire Général

- [🧭 Vue d'ensemble du Projet & Architecture](#-vue-densemble-du-projet--architecture)
- [📦 ITÉRATION 1 : Introduction à Spring Boot](#-itération-1--introduction-à-spring-boot)
  - [1.1 — Découverte des Fondamentaux](#11--découverte-des-fondamentaux-1h30)
  - [1.2 — Exposition du premier Endpoint (Heartbeat)](#12--exposition-du-premier-endpoint-heartbeat-4h00)
  - [1.3 — Test et Validation de l'Endpoint](#13--test-et-validation-de-lendpoint-1h30)
- [🎮 ITÉRATION 2 : API REST Jeu de Plateau (Square Games)](#-itération-2--api-rest-jeu-de-plateau-square-games)
  - [2.1 — Configuration Maven & Découverte du Moteur de Jeux](#21--configuration-maven--découverte-du-moteur-de-jeux-2h00)
  - [2.2 — Conception de l'API REST](#22--conception-de-lapi-rest-3h00)
  - [2.3 — Implémentation de l'API (Controller, DTO & Service)](#23--implémentation-de-lapi-controller-dto--service-7h00)
  - [2.4 — Injection de Valeurs & Internationalisation (Plugins & i18n)](#24--injection-de-valeurs--internationalisation-plugins--i18n-7h00)
- [🛠️ Aide-mémoire des Commandes Utiles](#️-aide-mémoire-des-commandes-utiles)

---

## 🧭 Vue d'ensemble du Projet & Architecture

### Principes Directeurs
1. **Séparation par Couches (Package by Layer)** :
   - `config/` : Configuration Spring, Beans externes.
   - `controller/` : Réception HTTP, validation, routage.
   - `dto/` : Objets de transfert de données immuables (`record` Java 21).
   - `service/` : Règles métier et persistance en mémoire.
   - `plugin/` : Adaptateurs pour chaque jeu spécifique.
2. **Inversion de Contrôle (IoC) & Injection par Constructeur** :
   - Aucun `new Service()` dans le code applicatif.
   - Attributs déclarés `private final` pour garantir l'immuabilité et la testabilité.

```text
src/main/java/com/bsourichanh/javaspring/
├── JavaSpringApplication.java              <-- Point d'entrée (@SpringBootApplication)
├── config/
│   └── GameFactoryConfiguration.java       <-- Enregistrement des GameFactory externes
├── controller/
│   ├── HeartbeatController.java            <-- GET /heartbeat
│   ├── GameCatalogController.java          <-- GET /api/catalog/games
│   └── GameController.java                 <-- POST /games & GET /games/{id}
├── dto/
│   ├── GameCreationParams.java             <-- Paramètres de création reçus en JSON
│   └── GameDescriptionDto.java            <-- Informations d'un jeu pour le catalogue
├── plugin/
│   ├── GamePlugin.java                     <-- Contrat d'adaptation d'un jeu
│   ├── TicTacToePlugin.java                <-- Plugin Morpion
│   ├── TaquinPlugin.java                   <-- Plugin Taquin
│   └── ConnectFourPlugin.java              <-- Plugin Puissance 4
└── service/
    ├── HeartbeatSensor.java                <-- Interface du capteur de battements
    ├── RandomHeartbeatSensor.java          <-- Service de génération aléatoire
    ├── GameCatalog.java                    <-- Interface du catalogue
    ├── GameCatalogImpl.java                <-- Service du catalogue
    ├── GameService.java                    <-- Interface de gestion des parties
    └── GameServiceImpl.java                <-- Service de gestion multi-jeux
```

---

# 📦 ITÉRATION 1 : Introduction à Spring Boot

## 1.1 — Découverte des Fondamentaux (1h30)

### 🎯 Objectifs
Comprendre le paradigme Spring Boot avant d'écrire du code :
- **Spring Boot vs Spring Framework** : Spring Boot ajoute les *Starters* (dépendances regroupées), l'*Auto-Configuration* (détection automatique) et un *Serveur HTTP embarqué* (Tomcat intégré).
- **IoC & DI** : Les objets ne créent plus leurs dépendances ; c'est le conteneur Spring qui les instancie et les assemble.
- **Stéréotypes Spring** :
  - `@Component` : Marqueur générique de Bean.
  - `@Service` : Spécialisation pour la logique métier.
  - `@Repository` : Spécialisation pour l'accès aux données.
  - `@RestController` : Spécialisation web retournant directement du JSON.

---

## 1.2 — Exposition du premier Endpoint (Heartbeat) (4h00)

### 🎯 Objectif
Exposer un premier endpoint `GET /heartbeat` renvoyant une pulsation cardiaque entre 40 et 230, et appréhender les erreurs d'injection de dépendances.

### 📝 Étape 1.2.1 : Définir l'interface `HeartbeatSensor`
📁 `src/main/java/com/bsourichanh/javaspring/service/HeartbeatSensor.java`

```java
package com.bsourichanh.javaspring.service;

public interface HeartbeatSensor {
    int get();
}
```

### 📝 Étape 1.2.2 : Implémenter le service `RandomHeartbeatSensor`
📁 `src/main/java/com/bsourichanh/javaspring/service/RandomHeartbeatSensor.java`

```java
package com.bsourichanh.javaspring.service;

import org.springframework.stereotype.Service;
import java.util.Random;

@Service
public class RandomHeartbeatSensor implements HeartbeatSensor {

    private final Random random = new Random();

    @Override
    public int get() {
        // Retourne un entier aléatoire entre 40 et 230 inclus
        return random.nextInt(40, 231);
    }
}
```

### 📝 Étape 1.2.3 : Créer le contrôleur `HeartbeatController`
📁 `src/main/java/com/bsourichanh/javaspring/controller/HeartbeatController.java`

```java
package com.bsourichanh.javaspring.controller;

import com.bsourichanh.javaspring.service.HeartbeatSensor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HeartbeatController {

    private final HeartbeatSensor heartbeatSensor;

    // Injection recommandée par constructeur
    public HeartbeatController(HeartbeatSensor heartbeatSensor) {
        this.heartbeatSensor = heartbeatSensor;
    }

    @GetMapping("/heartbeat")
    public int heartbeat() {
        return heartbeatSensor.get();
    }
}
```

> [!TIP] **Le piège pédagogique du cours** :  
> Si vous annotiez `@Autowired private HeartbeatSensor sensor;` sans avoir créé `RandomHeartbeatSensor`, Spring échouait avec `NoSuchBeanDefinitionException`. Dès que vous ajoutez `@Service` sur `RandomHeartbeatSensor`, Spring trouve l'implémentation et démarre sans erreur !

---

## 1.3 — Test et Validation de l'Endpoint (1h30)

### 🎯 Objectif
Valider le fonctionnement de la sonde via cURL, Bruno/Postman et tests automatisés.

### 🧪 Test en ligne de commande (cURL)
```bash
curl -X GET http://localhost:8080/heartbeat
# Réponse attendue : un nombre entre 40 et 230 (ex: 78)
```

### 🧪 Test d'intégration automatisé (`MockMvc`)
📁 `src/test/java/com/bsourichanh/javaspring/JavaSpringApplicationTests.java`

```java
@Test
void testHeartbeatEndpoint() throws Exception {
    mockMvc.perform(get("/heartbeat"))
            .andExpect(status().isOk());
}
```

---

# 🎮 ITÉRATION 2 : API REST Jeu de Plateau (Square Games)

## 2.1 — Configuration Maven & Découverte du Moteur de Jeux (2h00)

### 🎯 Objectif
Intégrer le moteur `square-games:engine` via Maven et exposer le catalogue des jeux disponibles.

### 📝 Étape 2.1.1 : Ajout de la dépendance Maven
📁 `pom.xml`

```xml
<dependency>
    <groupId>fr.le-campus-numerique.square-games</groupId>
    <artifactId>engine</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### 📝 Étape 2.1.2 : Déclaration du Bean externe (`GameFactoryConfiguration`)
📁 `src/main/java/com/bsourichanh/javaspring/config/GameFactoryConfiguration.java`

Comme `TicTacToeGameFactory` provient d'un jar externe sans annotations Spring, nous la déclarons manuellement :

```java
package com.bsourichanh.javaspring.config;

import fr.le_campus_numerique.square_games.engine.tictactoe.TicTacToeGameFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GameFactoryConfiguration {

    @Bean
    public TicTacToeGameFactory ticTacToeGameFactory() {
        return new TicTacToeGameFactory();
    }
}
```

### 📝 Étape 2.1.3 : Interface et Service `GameCatalog`
📁 `src/main/java/com/bsourichanh/javaspring/service/GameCatalog.java`

```java
package com.bsourichanh.javaspring.service;

import java.util.Collection;

public interface GameCatalog {
    Collection<String> getAvailableGames();
}
```

📁 `src/main/java/com/bsourichanh/javaspring/service/GameCatalogImpl.java`

```java
package com.bsourichanh.javaspring.service;

import fr.le_campus_numerique.square_games.engine.tictactoe.TicTacToeGameFactory;
import org.springframework.stereotype.Service;
import java.util.Collection;
import java.util.List;

@Service
public class GameCatalogImpl implements GameCatalog {

    private final TicTacToeGameFactory ticTacToeFactory;

    public GameCatalogImpl(TicTacToeGameFactory ticTacToeFactory) {
        this.ticTacToeFactory = ticTacToeFactory;
    }

    @Override
    public Collection<String> getAvailableGames() {
        return List.of(ticTacToeFactory.getGameFactoryId());
    }
}
```

### 📝 Étape 2.1.4 : Contrôleur `GameCatalogController`
📁 `src/main/java/com/bsourichanh/javaspring/controller/GameCatalogController.java`

```java
package com.bsourichanh.javaspring.controller;

import com.bsourichanh.javaspring.service.GameCatalog;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Collection;

@RestController
@RequestMapping("/api/catalog")
public class GameCatalogController {

    private final GameCatalog gameCatalog;

    public GameCatalogController(GameCatalog gameCatalog) {
        this.gameCatalog = gameCatalog;
    }

    @GetMapping("/games")
    public Collection<String> listAvailableGames() {
        return gameCatalog.getAvailableGames();
    }
}
```

---

## 2.2 — Conception de l'API REST (3h00)

### 🎯 Matrice des Endpoints REST

| Verbe HTTP | URL | Rôle | Corps Requis | Code Succès | Code Erreur |
| :---: | :--- | :--- | :---: | :---: | :---: |
| **GET** | `/api/catalog/games` | Consulter les jeux disponibles | Aucun | `200 OK` | - |
| **POST** | `/games` | Créer une nouvelle partie | `GameCreationParams` (JSON) | `200 OK` (ou `201`) | `400 Bad Request` |
| **GET** | `/games/{gameId}` | Récupérer l'état d'une partie | Aucun | `200 OK` | `404 Not Found` |

---

## 2.3 — Implémentation de l'API (Controller, DTO & Service) (7h00)

### 🎯 Objectif
Implémenter `GameController`, le DTO `GameCreationParams`, et découpler la logique dans `GameService` en supportant plusieurs jeux.

### 📝 Étape 2.3.1 : DTO `GameCreationParams`
📁 `src/main/java/com/bsourichanh/javaspring/dto/GameCreationParams.java`

```java
package com.bsourichanh.javaspring.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public record GameCreationParams(
        @JsonProperty("gameType")
        @JsonAlias({"type", "game"})
        String gameType,

        @JsonProperty("playerCount")
        @JsonAlias({"nbPlayers", "players"})
        int playerCount,

        @JsonProperty("boardSize")
        @JsonAlias({"totalCell", "size"})
        int boardSize
) {
    public String getType() { return gameType; }
    public int getNbPlayers() { return playerCount; }
    public int getTotalCell() { return boardSize; }
}
```

### 📝 Étape 2.3.2 : Déclarer les autres GameFactory
📁 `src/main/java/com/bsourichanh/javaspring/config/GameFactoryConfiguration.java`

```java
package com.bsourichanh.javaspring.config;

import fr.le_campus_numerique.square_games.engine.connectfour.ConnectFourGameFactory;
import fr.le_campus_numerique.square_games.engine.taquin.TaquinGameFactory;
import fr.le_campus_numerique.square_games.engine.tictactoe.TicTacToeGameFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GameFactoryConfiguration {

    @Bean
    public TicTacToeGameFactory ticTacToeGameFactory() { return new TicTacToeGameFactory(); }

    @Bean
    public TaquinGameFactory taquinGameFactory() { return new TaquinGameFactory(); }

    @Bean
    public ConnectFourGameFactory connectFourGameFactory() { return new ConnectFourGameFactory(); }
}
```

### 📝 Étape 2.3.3 : Interface `GameService`
📁 `src/main/java/com/bsourichanh/javaspring/service/GameService.java`

```java
package com.bsourichanh.javaspring.service;

import com.bsourichanh.javaspring.dto.GameCreationParams;
import fr.le_campus_numerique.square_games.engine.Game;
import java.util.UUID;

public interface GameService {
    Game createGame(GameCreationParams params);
    Game getGame(UUID gameId);
}
```

### 📝 Étape 2.3.4 : Implémentation `GameServiceImpl`
📁 `src/main/java/com/bsourichanh/javaspring/service/GameServiceImpl.java`

```java
package com.bsourichanh.javaspring.service;

import com.bsourichanh.javaspring.dto.GameCreationParams;
import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.GameFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameServiceImpl implements GameService {

    private final Collection<GameFactory> gameFactories;
    private final Map<UUID, Game> games = new ConcurrentHashMap<>();

    public GameServiceImpl(Collection<GameFactory> gameFactories) {
        this.gameFactories = gameFactories;
    }

    @Override
    public Game createGame(GameCreationParams params) {
        GameFactory factory = getFactoryForType(params.gameType());
        Game game = factory.createGame(params.playerCount(), params.boardSize());
        games.put(game.getId(), game);
        return game;
    }

    @Override
    public Game getGame(UUID gameId) {
        return games.get(gameId);
    }

    private GameFactory getFactoryForType(String type) {
        if (type == null || type.isBlank()) {
            type = "tictactoe";
        }
        String normalized = type.toLowerCase().trim();
        for (GameFactory factory : gameFactories) {
            String id = factory.getGameFactoryId().toLowerCase();
            if (id.equals(normalized)
                    || (normalized.contains("tic") && id.contains("tictac"))
                    || (normalized.contains("taquin") && id.contains("puzzle"))
                    || (normalized.contains("connect") && id.contains("connect"))) {
                return factory;
            }
        }
        throw new IllegalArgumentException("Type de jeu non supporté : " + type);
    }
}
```

### 📝 Étape 2.3.5 : Contrôleur `GameController`
📁 `src/main/java/com/bsourichanh/javaspring/controller/GameController.java`

```java
package com.bsourichanh.javaspring.controller;

import com.bsourichanh.javaspring.dto.GameCreationParams;
import com.bsourichanh.javaspring.service.GameService;
import fr.le_campus_numerique.square_games.engine.Game;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/games")
    public Game createGame(@RequestBody GameCreationParams params) {
        return gameService.createGame(params);
    }

    @GetMapping("/games/{gameId}")
    public ResponseEntity<Game> getGame(@PathVariable("gameId") UUID gameId) {
        Game game = gameService.getGame(gameId);
        if (game == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(game);
    }
}
```

---

## 2.4 — Injection de Valeurs & Internationalisation (Plugins & i18n) (7h00)

### 🎯 Objectif
Permettre de créer des parties avec des valeurs par défaut sans tout renseigner, et traduire le nom des jeux selon l'entête HTTP `Accept-Language`.

### 📝 Étape 2.4.1 : Interface `GamePlugin`
📁 `src/main/java/com/bsourichanh/javaspring/plugin/GamePlugin.java`

```java
package com.bsourichanh.javaspring.plugin;

import fr.le_campus_numerique.square_games.engine.Game;
import java.util.Locale;

public interface GamePlugin {
    String getFactoryId();
    String getName(Locale locale);
    int getDefaultPlayerCount();
    int getDefaultBoardSize();
    Game createGame(Integer playerCount, Integer boardSize);
}
```

### 📝 Étape 2.4.2 : Propriétés par défaut (`application.properties`)
📁 `src/main/resources/application.properties`

```properties
spring.application.name=JavaSpring

# Paramètres par défaut des jeux
game.tictactoe.default-player-count=2
game.tictactoe.default-board-size=3

game.taquin.default-player-count=1
game.taquin.default-board-size=4

game.connectfour.default-player-count=2
game.connectfour.default-board-size=7
```

### 📝 Étape 2.4.3 : Fichiers i18n (`messages.properties`)
📁 `src/main/resources/messages.properties` (Anglais par défaut) :
```properties
game.tictactoe.name=Tic-Tac-Toe
game.taquin.name=15 Puzzle (Taquin)
game.connectfour.name=Connect Four
```

📁 `src/main/resources/messages_fr.properties` (Français) :
```properties
game.tictactoe.name=Morpion
game.taquin.name=Taquin
game.connectfour.name=Puissance 4
```

### 📝 Étape 2.4.4 : Création des composants Plugins
📁 `src/main/java/com/bsourichanh/javaspring/plugin/TicTacToePlugin.java`

```java
package com.bsourichanh.javaspring.plugin;

import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.tictactoe.TicTacToeGameFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class TicTacToePlugin implements GamePlugin {

    private final TicTacToeGameFactory factory;
    private final MessageSource messageSource;
    private final int defaultPlayerCount;
    private final int defaultBoardSize;

    public TicTacToePlugin(
            TicTacToeGameFactory factory,
            MessageSource messageSource,
            @Value("${game.tictactoe.default-player-count:2}") int defaultPlayerCount,
            @Value("${game.tictactoe.default-board-size:3}") int defaultBoardSize
    ) {
        this.factory = factory;
        this.messageSource = messageSource;
        this.defaultPlayerCount = defaultPlayerCount;
        this.defaultBoardSize = defaultBoardSize;
    }

    @Override
    public String getFactoryId() { return factory.getGameFactoryId(); }

    @Override
    public String getName(Locale locale) {
        return messageSource.getMessage("game.tictactoe.name", null, locale);
    }

    @Override
    public int getDefaultPlayerCount() { return defaultPlayerCount; }

    @Override
    public int getDefaultBoardSize() { return defaultBoardSize; }

    @Override
    public Game createGame(Integer playerCount, Integer boardSize) {
        int players = (playerCount != null && playerCount > 0) ? playerCount : defaultPlayerCount;
        int size = (boardSize != null && boardSize > 0) ? boardSize : defaultBoardSize;
        return factory.createGame(players, size);
    }
}
```

*(Créer de façon similaire `TaquinPlugin.java` et `ConnectFourPlugin.java`).*

### 📝 Étape 2.4.5 : Brancher `List<GamePlugin>` dans `GameServiceImpl`
Dans `GameServiceImpl.java`, remplacer `Collection<GameFactory>` par `List<GamePlugin>`. Spring injectera automatiquement la liste ordonnée de tous les plugins annotés `@Component` !

### 📝 Étape 2.4.6 : Internationalisation du Catalogue
Dans `GameCatalogController.java`, ajouter `Locale locale` en paramètre de méthode :
```java
@GetMapping("/games")
public Collection<GameDescriptionDto> listAvailableGames(Locale locale) {
    return gameCatalog.getAvailableGames(locale);
}
```

---

## 🛠️ Aide-mémoire des Commandes Utiles

### Compilation et Tests
```bash
# Compiler et lancer la suite de tests complète
./mvnw clean test

# Démarrer le serveur localement
./mvnw spring-boot:run
```

### Exemples de requêtes cURL
```bash
# 1. Vérifier la sonde de santé
curl -i -X GET http://localhost:8080/heartbeat

# 2. Consulter le catalogue de jeux
curl -i -X GET http://localhost:8080/api/catalog/games

# 3. Créer une partie de Morpion
curl -i -X POST http://localhost:8080/games \
  -H "Content-Type: application/json" \
  -d '{"gameType": "tictactoe", "playerCount": 2, "boardSize": 3}'

# 4. Consulter l'état de la partie créée
curl -i -X GET http://localhost:8080/games/<UUID_DE_LA_PARTIE>

# 5. Tester le catalogue avec la langue française
curl -i -X GET http://localhost:8080/api/catalog/games -H "Accept-Language: fr"
```
