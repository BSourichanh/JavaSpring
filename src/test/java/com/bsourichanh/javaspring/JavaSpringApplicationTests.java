package com.bsourichanh.javaspring;

import com.bsourichanh.javaspring.security.JwtService;
import com.bsourichanh.javaspring.service.UserValidationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.context.ActiveProfiles;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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

    @Autowired
    private JwtService jwtService;

    // Mock du service d'appel HTTP : isValidUser() retourne toujours true en test
    @MockitoBean
    private UserValidationService userValidationService;

    private static final String TEST_USER_ID = "00000000-0000-0000-0000-000000000001";

    private void setupValidUser() {
        when(userValidationService.isValidUser(anyString())).thenReturn(true);
    }

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
        setupValidUser();
        String json = """
                {
                    "gameType": "tictactoe"
                }
                """;

        mockMvc.perform(post("/games")
                        .contentType("application/json")
                        .header("X-UserId", TEST_USER_ID)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.factoryId").value("tictactoe"))
                .andExpect(jsonPath("$.boardSize").value(3));
    }

    @Test
    void testCreateGameEndpoint() throws Exception {
        setupValidUser();
        String json = """
                {
                    "gameType": "tictactoe",
                    "playerCount": 2,
                    "boardSize": 3
                }
                """;

        mockMvc.perform(post("/games")
                        .contentType("application/json")
                        .header("X-UserId", TEST_USER_ID)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.factoryId").value("tictactoe"))
                .andExpect(jsonPath("$.boardSize").value(3));
    }

    @Test
    void testCreateGameWithAlternativeFieldNames() throws Exception {
        setupValidUser();
        String json = """
                {
                    "type": "tictactoe",
                    "nbPlayers": 2,
                    "totalCell": 3
                }
                """;

        mockMvc.perform(post("/games")
                        .contentType("application/json")
                        .header("X-UserId", TEST_USER_ID)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.factoryId").value("tictactoe"))
                .andExpect(jsonPath("$.boardSize").value(3));
    }

    @Test
    void testCreateGameWithoutUserIdReturns401() throws Exception {
        when(userValidationService.isValidUser(null)).thenReturn(false);
        when(userValidationService.isValidUser("")).thenReturn(false);
        String json = """
                { "gameType": "tictactoe" }
                """;

        mockMvc.perform(post("/games")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetGameByIdSuccess() throws Exception {
        setupValidUser();
        String json = """
                {
                    "gameType": "tictactoe",
                    "playerCount": 2,
                    "boardSize": 3
                }
                """;

        String response = mockMvc.perform(post("/games")
                        .contentType("application/json")
                        .header("X-UserId", TEST_USER_ID)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String gameId = com.jayway.jsonpath.JsonPath.read(response, "$.id");

        mockMvc.perform(get("/games/" + gameId)
                        .header("X-UserId", TEST_USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(gameId))
                .andExpect(jsonPath("$.factoryId").value("tictactoe"))
                .andExpect(jsonPath("$.status").exists());
    }

    @Test
    void testGetGameByIdNotFound() throws Exception {
        setupValidUser();
        String randomId = java.util.UUID.randomUUID().toString();
        mockMvc.perform(get("/games/" + randomId)
                        .header("X-UserId", TEST_USER_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateTaquinGame() throws Exception {
        setupValidUser();
        String json = """
                {
                    "gameType": "taquin",
                    "playerCount": 1,
                    "boardSize": 4
                }
                """;

        mockMvc.perform(post("/games")
                        .contentType("application/json")
                        .header("X-UserId", TEST_USER_ID)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.factoryId").value("15 puzzle"))
                .andExpect(jsonPath("$.boardSize").value(4));
    }

    @Test
    void testCreateConnectFourGame() throws Exception {
        setupValidUser();
        String json = """
                {
                    "gameType": "connect4",
                    "playerCount": 2,
                    "boardSize": 7
                }
                """;

        mockMvc.perform(post("/games")
                        .contentType("application/json")
                        .header("X-UserId", TEST_USER_ID)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.factoryId").value("connect4"))
                .andExpect(jsonPath("$.boardSize").value(7));
    }

    @Test
    void testGetGamesFilteredForUser() throws Exception {
        setupValidUser();
        // Créer une partie pour TEST_USER_ID
        String json = """
                { "gameType": "tictactoe" }
                """;
        mockMvc.perform(post("/games")
                        .contentType("application/json")
                        .header("X-UserId", TEST_USER_ID)
                        .content(json))
                .andExpect(status().isOk());

        // Vérifier que GET /games avec TEST_USER_ID retourne au moins 1 partie
        mockMvc.perform(get("/games")
                        .header("X-UserId", TEST_USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThan(0)));

        // Vérifier que GET /games avec un autre utilisateur inconnu/sans partie retourne une liste vide
        String otherUserId = java.util.UUID.randomUUID().toString();
        mockMvc.perform(get("/games")
                        .header("X-UserId", otherUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testPlayMoveForbiddenWhenNotCurrentPlayer() throws Exception {
        setupValidUser();
        String json = """
                { "gameType": "tictactoe" }
                """;
        String response = mockMvc.perform(post("/games")
                        .contentType("application/json")
                        .header("X-UserId", TEST_USER_ID)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String gameId = com.jayway.jsonpath.JsonPath.read(response, "$.id");

        // Tenter de jouer un coup avec un autre ID que currentPlayerId -> 403 Forbidden
        String wrongPlayerId = java.util.UUID.randomUUID().toString();
        mockMvc.perform(post("/games/" + gameId + "/moves")
                        .contentType("application/json")
                        .header("X-UserId", wrongPlayerId)
                        .content("{\"x\": 0, \"y\": 0}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateGameWithBearerJwt() throws Exception {
        String token = jwtService.generateToken(TEST_USER_ID, "alice", "ROLE_USER", 3600000);
        String json = """
                { "gameType": "tictactoe" }
                """;

        mockMvc.perform(post("/games")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + token)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.factoryId").value("tictactoe"));

        // Vérifier que UserValidationService n'a JAMAIS été appelé (validation 100% locale sans réseau)
        verify(userValidationService, never()).isValidUser(anyString());
    }

    @Test
    void testCreateGameWithInvalidBearerJwtReturns401() throws Exception {
        String json = """
                { "gameType": "tictactoe" }
                """;

        mockMvc.perform(post("/games")
                        .contentType("application/json")
                        .header("Authorization", "Bearer invalid.token.signature")
                        .content(json))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCreateGameWithoutAuthReturns401() throws Exception {
        String json = """
                { "gameType": "tictactoe" }
                """;

        mockMvc.perform(post("/games")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testPlayMoveWithBearerJwt() throws Exception {
        String token = jwtService.generateToken(TEST_USER_ID, "alice", "ROLE_USER", 3600000);
        String json = """
                { "gameType": "tictactoe" }
                """;

        String createResponse = mockMvc.perform(post("/games")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + token)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String gameId = com.jayway.jsonpath.JsonPath.read(createResponse, "$.id");

        mockMvc.perform(post("/games/" + gameId + "/moves")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + token)
                        .content("{\"x\": 0, \"y\": 0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(gameId));
    }
}

