# SecureBank India ATM

Java ATM with a console client and a JavaFX GUI. Shared core (`Bank`, `Account`, `Transaction`). Money is in **₹** with Indian digit grouping.

## Quick start (VS Code)

1. Install **Extension Pack for Java** (and Maven if prompted).
2. **File → Open Folder** → this project folder.
3. Wait for Java/Maven import to finish.
4. First time only, in terminal:

```bash
./scripts/prepare-javafx.sh
```

5. **Run and Debug** (`Ctrl+Shift+D`) → **SecureBank GUI** or **SecureBank Console** → Start.

> Use those launch configs. Plain “Run” above `main` skips the JavaFX module path and fails with `Module javafx.base not found`.

## Terminal

```bash
./scripts/prepare-javafx.sh   # downloads JavaFX jars via Maven → lib/javafx
./scripts/run-gui.sh          # GUI
./scripts/run-console.sh      # console
```

Or:

```bash
mvn -q compile
mvn javafx:run                # GUI
mvn -q exec:java              # console
```

## Demo accounts

| A/C  | PIN  | Name           | Balance      |
|------|------|----------------|--------------|
| 1001 | 1234 | Satya Kumar    | ₹52,500.00   |
| 1002 | 5678 | Priya Sharma   | ₹38,750.00   |
| 1003 | 9012 | Arjun Reddy    | ₹1,25,000.00 |
| 1004 | 2468 | Hitesh Patel   | ₹41,200.00   |
| 1005 | 1357 | Aman Verma     | ₹28,900.00   |
| 1006 | 8642 | Omkar Joshi    | ₹67,350.00   |
| 1007 | 9753 | Ritikesh Singh | ₹93,000.00   |
| 1008 | 1122 | Ram Yadav      | ₹15,500.00   |

Admin: `admin` / `admin123`

## Requirements

- JDK 21+
- Maven 3.8+
- Internet once (to fetch JavaFX)

## Layout

```
pom.xml
scripts/                 prepare-javafx, run-gui, run-console, build
src/main/java/com/atm/   core, gui, models, utils, main
src/main/resources/      style.css
.vscode/                 launch + tasks (JavaFX wired)
```

## Author

Satya Swaroop Satapathy
