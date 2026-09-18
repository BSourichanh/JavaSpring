CREATE TABLE IF NOT EXISTS games (
    id VARCHAR(36) PRIMARY KEY,
    factory_id VARCHAR(50) NOT NULL,
    board_size INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    current_player_id VARCHAR(36),
    player_ids TEXT
);