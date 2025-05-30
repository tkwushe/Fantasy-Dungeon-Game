# Fantasy Dungeon Adventure

A text-based dungeon crawler game built in Java with a graphical user interface using Swing. Navigate through mysterious dungeons, solve puzzles, collect items, and battle your way to victory!

## Features

- **Interactive GUI**: Modern Swing-based interface with command input and visual map
- **Multiple Difficulty Levels**: Choose your challenge level (1-3)
- **Dynamic Gameplay**: Explore rooms, solve puzzles, and manage your inventory
- **Save/Load System**: SQLite database for persistent game state
- **Event-Driven Architecture**: Clean separation of concerns with event handlers
- **Comprehensive Testing**: JUnit test suite included

## Prerequisites

- Java 19 or higher
- Maven 3.6+ (for building)

## Getting Started

### Building the Project

```bash
mvn clean compile
```

### Running the Game

```bash
mvn exec:java -Dexec.mainClass="com.game.GameLauncher"
```

Or compile and run directly:

```bash
mvn clean package
java -jar target/fantasy-dungeon-adventure-1.0-SNAPSHOT.jar
```

### Running Tests

```bash
mvn test
```

## How to Play

1. **Start**: Launch the game and select a difficulty level (1-3)
2. **Navigate**: Use directional commands (`north`, `south`, `east`, `west`) or shortcuts (`n`, `s`, `e`, `w`)
3. **Interact**: Use commands like:
   - `look` - Examine your surroundings
   - `pickup <item>` - Collect items
   - `inventory` - Check your items
   - `use <item>` - Use items from inventory
   - `map` - View explored areas
   - `help` - Show available commands
4. **Save Progress**: Use `save` command or File menu
5. **Load Game**: Use `load` command or File menu

## Commands

| Command | Aliases | Description |
|---------|---------|-------------|
| `move <direction>` | `go`, `walk` | Move in a direction |
| `north/south/east/west` | `n`, `s`, `e`, `w` | Quick directional movement |
| `look` | `examine`, `inspect` | Look around current room |
| `pickup <item>` | `take`, `grab` | Pick up an item |
| `inventory` | `inv`, `items` | Show inventory |
| `use <item>` | | Use an item |
| `drop <item>` | | Drop an item |
| `map` | | Show explored map |
| `save` | | Save current game |
| `load` | | Load saved game |
| `help` | | Show help information |
| `quit` | `exit` | Exit the game |

## Project Structure

```
src/
├── main/java/com/game/
│   ├── GameLauncher.java          # Main entry point
│   ├── database/                  # Save/load functionality
│   ├── engine/                    # Core game engine
│   ├── event/                     # Event system
│   ├── gui/                       # Swing UI components
│   ├── item/                      # Item system and factories
│   ├── level/                     # Level management
│   ├── player/                    # Player character
│   ├── puzzle/                    # Puzzle mechanics
│   ├── room/                      # Room system
│   └── util/                      # Utilities and logging
├── resources/
│   └── database/                  # Database schemas
└── test/                          # JUnit tests
```

## Technologies Used

- **Java 19**: Core language
- **Maven**: Build and dependency management
- **Swing**: GUI framework
- **SQLite**: Database for save/load functionality
- **JUnit 5**: Testing framework
- **Observer Pattern**: Event-driven architecture
- **Factory Pattern**: Item creation
- **Singleton Pattern**: Game engine and UI management

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Development Setup

### IDE Configuration

**IntelliJ IDEA**: Project includes `.idea` configuration (add to .gitignore in production)
**VS Code**: Use Java Extension Pack for optimal development experience

### Running in Development

The main class is `com.game.GameLauncher`. Configure your IDE to run this class with JVM args if needed.

## License

This project is open source and available under the [MIT License](LICENSE).

## Known Issues

- Game requires Java 19+ (consider Java 11+ compatibility for wider adoption)
- Large save files may impact performance
- Some UI elements may need scaling on high-DPI displays

## Future Enhancements

- [ ] Multiplayer support
- [ ] Sound effects and background music
- [ ] More complex combat system
- [ ] Expanded item crafting
- [ ] Different character classes
- [ ] Randomized dungeon generation

---

*Built with ❤️ in Java* 