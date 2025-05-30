IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'dungeon_adventure')
BEGIN
    CREATE DATABASE dungeon_adventure;
END
GO

USE dungeon_adventure;
GO

IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[game_saves]') AND type in (N'U'))
BEGIN
    CREATE TABLE game_saves (
        save_id INT IDENTITY(1,1) PRIMARY KEY,
        save_name VARCHAR(100) NOT NULL,
        player_data VARBINARY(MAX) NOT NULL,
        current_level INT NOT NULL,
        save_date DATETIME NOT NULL DEFAULT GETDATE(),
        CONSTRAINT UQ_save_name UNIQUE (save_name)
    );
END
GO

IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[player_statistics]') AND type in (N'U'))
BEGIN
    CREATE TABLE player_statistics (
        player_id INT IDENTITY(1,1) PRIMARY KEY,
        player_name VARCHAR(100) NOT NULL,
        rooms_explored INT DEFAULT 0,
        puzzles_solved INT DEFAULT 0,
        items_collected INT DEFAULT 0,
        play_time INT DEFAULT 0,
        last_played DATETIME DEFAULT GETDATE()
    );
END
GO 