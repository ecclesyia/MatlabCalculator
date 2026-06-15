# MatlabCalculator
A modern, desktop-class Android scientific calculator application built using **Kotlin** and **Jetpack Compose (Material 3)**. Under the hood, it combines a sleek, Windows-style calculator UI with a MATLAB-style mathematical expression parsing engine.
---
## 🌟 Key Features
### 1. Multiple Utility Modes
A slide-out Navigation Drawer allows you to switch between 5 distinct modes:
*   **Standard**: A clean and minimal keypad for daily arithmetic.
*   **Scientific**: Advanced scientific layout featuring expandable submenus for trigonometry and functional operations.
*   **Graphing**: An interactive coordinate plot canvas. Supports entering **multiple equations** (e.g. `y1 = sin(x)`, `y2 = x^2`) with custom colors and visibility toggles.
*   **Programmer**: Multi-base view displaying Hexadecimal, Decimal, Octal, and Binary conversions in real-time. Features dynamic keypad validation (A-F disabled in DEC, digits 2-9 disabled in BIN, etc.) and native bitwise operators (`AND`, `OR`, `XOR`).
*   **Date Calculation**: Calculates differences between dates, outputting detailed stats in Years, Months, and Days, alongside total days and weeks.
### 2. Custom Mathematical Parser (`Evaluator.kt`)
Instead of sequential button-by-button parsing, this application parses entire mathematical string inputs using a custom **Recursive Descent Parser**:
*   Supports standard operators: `+`, `-`, `*`, `/`, `^` (exponent), `%` (modulo).
*   Handles postfix factorials (e.g. `5! = 120`).
*   Built-in constants (`pi`, `e`) and functions (`sin`, `cos`, `tan`, `asin`, `acos`, `atan`, `abs`, `exp`, `log`, `ln`, `sqrt`).
### 3. Interactive History Panel
A slide-in drawer tracks past evaluations. Tapping any item in the history logs automatically restores the formula to your editing screen.
---
## 🛠️ Project Structure
```
MatlabCalculator/
├── settings.gradle.kts          # Project settings & dependency declarations
├── build.gradle.kts             # Root project build file
├── gradle.properties            # AndroidX configuration and JVM optimizations
├── app/
│   ├── build.gradle.kts         # App module compilation settings and Jetpack Compose dependencies
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml # Core Android system metadata
│           └── java/
│               └── com/
│                   └── example/
│                       └── matlabcalculator/
│                           ├── MainActivity.kt  # Compose UI, Mode structures, Navigation, and State
│                           └── Evaluator.kt     # Math parser and tokenization logic
```
---
## 🚀 How to Run the App
1.  **Clone the Repository**:
    ```bash
    git clone https://github.com/<your-username>/MatlabCalculator.git
    ```
2.  **Open in Android Studio**:
    *   Launch Android Studio, select **Open**, and navigate to the `MatlabCalculator/` directory.
    *   Gradle will synchronize and download all dependencies automatically.
3.  **Run the Project**:
    *   Connect your Android device (ensure "Install via USB" is toggled ON under Developer Options on Xiaomi/POCO devices) or start an emulator.
    *   Press the green **Run** button in the top toolbar of Android Studio.
---
## ⚙️ Tech Stack & Requirements
*   **Language**: Kotlin 2.0.20
*   **UI Framework**: Jetpack Compose (Material 3)
*   **Min SDK**: API 24 (Android 7.0)
*   **Target SDK**: API 34 (Android 14.0)
*   **Build Tool**: Gradle (Kotlin DSL)
