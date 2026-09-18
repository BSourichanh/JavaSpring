package com.bsourichanh.javaspring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.context.ActiveProfiles;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("memory")
class JavaSpringApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void testHeartbeatEndpoint() throws Exception {
        mockMvc.perform(get("/heartbeat"))
                .andExpect(status().isOk());
    }

    @Test
    void testGameCatalogEndpoint() throws Exception {
        mockMvc.perform(get("/api/catalog/games"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.id == 'tictactoe')]").exists())
                .andExpect(jsonPath("$[?(@.id == '15 puzzle')]").exists())
                .andExpect(jsonPath("$[?(@.id == 'connect4')]").exists());
    }

    @Test
    void testGameCatalogEndpointFrenchLocale() throws Exception {
        mockMvc.perform(get("/api/catalog/games")
                        .locale(java.util.Locale.FRENCH)
                        .header("Accept-Language", "fr"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == 'tictactoe')].name").value("Morpion"))
                .andExpect(jsonPath("$[?(@.id == '15 puzzle')].name").value("Taquin"))
                .andExpect(jsonPath("$[?(@.id == 'connect4')].name").value("Puissance 4"));
    }

    @Test
    void testGameCatalogEndpointEnglishLocale() throws Exception {
        mockMvc.perform(get("/api/catalog/games")
                        .locale(java.util.Locale.ENGLISH)
                        .header("Accept-Language", "en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == 'tictactoe')].name").value("Tic-Tac-Toe"))
                .andExpect(jsonPath("$[?(@.id == '15 puzzle')].name").value("15 Puzzle (Taquin)"))
                .andExpect(jsonPath("$[?(@.id == 'connect4')].name").value("Connect Four"));
    }

    @Test
    void testCreateGameWithDefaultParameters() throws Exception {
        String json = """
                {
                    "gameType": "tictactoe"
                }
                """;

        mockMvc.perform(post("/games")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.factoryId").value("tictactoe"))
                .andExpect(jsonPath("$.boardSize").value(3));
    }

    @Test
    void testCreateGameEndpoint() throws Exception {
        String json = """
                {
                    "gameType": "tictactoe",
                    "playerCount": 2,
                    "boardSize": 3
                }
                """;

        mockMvc.perform(post("/games")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.factoryId").value("tictactoe"))
                .andExpect(jsonPath("$.boardSize").value(3));
    }

    @Test
    void testCreateGameWithAlternativeFieldNames() throws Exception {
        String json = """
                {
                    "type": "tictactoe",
                    "nbPlayers": 2,
                    "totalCell": 3
                }
                """;

        mockMvc.perform(post("/games")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.factoryId").value("tictactoe"))
                .andExpect(jsonPath("$.boardSize").value(3));
    }

    @Test
    void testGetGameByIdSuccess() throws Exception {
        String json = """
                {
                    "gameType": "tictactoe",
                    "playerCount": 2,
                    "boardSize": 3
                }
                """;

        String response = mockMvc.perform(post("/games")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extraire l'ID du jeu
        String gameId = com.jayway.jsonpath.JsonPath.read(response, "$.id");

        mockMvc.perform(get("/games/" + gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(gameId))
                .andExpect(jsonPath("$.factoryId").value("tictactoe"))
                .andExpect(jsonPath("$.status").exists());
    }

    @Test
    void testGetGameByIdNotFound() throws Exception {
        String randomId = java.util.UUID.randomUUID().toString();
        mockMvc.perform(get("/games/" + randomId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateTaquinGame() throws Exception {
        String json = """
                {
                    "gameType": "taquin",
                    "playerCount": 1,
                    "boardSize": 4
                }
                """;

        mockMvc.perform(post("/games")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.factoryId").value("15 puzzle"))
                .andExpect(jsonPath("$.boardSize").value(4));
    }

    @Test
    void testCreateConnectFourGame() throws Exception {
        String json = """
                {
                    "gameType": "connect4",
                    "playerCount": 2,
                    "boardSize": 7
                }
                """;

        mockMvc.perform(post("/games")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.factoryId").value("connect4"))
                .andExpect(jsonPath("$.boardSize").value(7));
    }
}

