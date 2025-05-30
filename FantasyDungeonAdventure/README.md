# Fantasy Dungeon Adventure

A text-based dungeon crawler game built in Java with a graphical user interface using Swing. Navigate through mysterious dungeons, solve puzzles, collect items, and battle your way to victory!

## 🎮 Play Now!

**Want to try the game immediately?** 

### 🌐 Play in Browser (No Downloads!)
👉 **[Play Online](https://YOUR_USERNAME.github.io/YOUR_REPO_NAME/)** 👈

- **Full Java Version**: Runs your actual Java game in the browser using CheerpJ
- **HTML5 Version**: Lightweight browser-compatible version at `/simple.html`
- **No Java Installation Required**: Works on any modern browser!

### 💾 Download & Play
👉 **[Download Latest Release](../../releases/latest)** 👈

- **Windows**: Download `fantasy-dungeon-adventure-windows.zip`, extract, and double-click `run-game.bat`
- **Mac/Linux**: Download `fantasy-dungeon-adventure-unix.tar.gz`, extract, and run `./run-game.sh`
- **Any Platform**: Download the standalone JAR and run `java -jar fantasy-dungeon-adventure-standalone.jar`

**Requirements**: Java 19+ ([Download here](https://adoptium.net/))

---

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

### Playing the Game (No Development Setup Needed)

#### Web Browser (Easiest!)
1. **Visit**: [https://YOUR_USERNAME.github.io/YOUR_REPO_NAME/](https://YOUR_USERNAME.github.io/YOUR_REPO_NAME/)
2. **Choose**: 
   - Main page: Full Java version (may take a moment to load)
   - `/simple.html`: Instant HTML5 version
3. **Play**: Works on any device with a modern browser!

#### Download Version
1. **Download**: Get the latest release from the [releases page](../../releases)
2. **Extract**: Unzip/untar the downloaded file
3. **Run**: 
   - Windows: Double-click `run-game.bat`
   - Mac/Linux: Run `./run-game.sh` in terminal
   - Manual: `java -jar fantasy-dungeon-adventure-standalone.jar`

### Building from Source

```bash
mvn clean package
java -jar target/fantasy-dungeon-adventure-1.0-SNAPSHOT-standalone.jar
```

### Running in Development

```bash
mvn exec:java -Dexec.mainClass="com.game.GameLauncher"
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

## Documentation

### 📚 [Complete Case Study](CASE_STUDY.md)
An in-depth technical analysis covering:
- **Architecture & Design Patterns**: Singleton, Factory, Observer, Strategy patterns
- **Performance Analysis**: Memory usage, optimization strategies
- **Code Quality Assessment**: SOLID principles, maintainability metrics
- **Technical Implementation**: Database design, GUI architecture, procedural generation
- **Testing Strategy**: Coverage analysis and recommendations
- **Lessons Learned**: Best practices and improvement opportunities

*Perfect for students, educators, and developers studying Java application architecture.*

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

## 🚀 Deployment Options

This project supports multiple deployment methods:

### 1. Web Deployment (GitHub Pages)
- **Auto-deploys** on every push to main branch
- **CheerpJ Version**: Runs actual Java code in browser
- **HTML5 Version**: Lightweight, instant-load version
- **No installation** required for players

### 2. Release Packages (GitHub Actions)
- **Automatic builds** when you create version tags
- **Windows/Mac/Linux** packages with run scripts
- **Standalone JARs** for universal compatibility

### For Repository Owners

#### Web Deployment:
1. **Enable GitHub Pages** in repository settings
2. **Push to main branch** - web version auto-deploys!
3. **Share**: `https://YOUR_USERNAME.github.io/YOUR_REPO_NAME/`

#### Release Deployment:
1. **Tag and Release**:
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```
2. **Automatic Build**: GitHub Actions creates downloadable packages
3. **Share**: Link to your releases page

### What Gets Created

#### Web Version:
- `index.html` - Full Java game in browser via CheerpJ  
- `simple.html` - HTML5/JavaScript version
- Works on any device with a modern browser

#### Download Packages:
- `fantasy-dungeon-adventure-windows.zip` - Windows package with batch file
- `fantasy-dungeon-adventure-unix.tar.gz` - Mac/Linux package with shell script  
- `fantasy-dungeon-adventure-standalone.jar` - Universal JAR file

### Manual Build for Distribution

```bash
# Build standalone JAR
mvn clean package

# The fat JAR will be at:
# target/fantasy-dungeon-adventure-1.0-SNAPSHOT-standalone.jar

# Create run scripts
echo 'java -jar fantasy-dungeon-adventure-1.0-SNAPSHOT-standalone.jar' > run.sh
chmod +x run.sh
```

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