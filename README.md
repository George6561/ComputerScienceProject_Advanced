## Features

### Core Functionality

#### **Game Mechanics**
- **Interactive Chessboard:**
  - Implemented using `ChessBoard`, `SharedBoard`, and `ChessWindow` classes.
  - Monte Carlo and Stockfish both operate on a **shared board** for consistent move tracking.

- **Move Generation & Validation:**
  - Moves are validated using Stockfish and custom logic in `ChessBoard`.
  - Support for special moves:
    - Castling
    - En passant
    - Pawn promotion

- **Monte Carlo Tree Search (Basic Version):**
  - **Random move simulation** over a **5-second window**.
  - Evaluates **8 half-moves ahead** using `MonteCarloTree`.
  - Picks the **highest-scoring move** for White (**lowest for Black**).
  - Uses `MoveRating` to assign heuristic values to positions.

---

### **Stockfish Integration**
- **UCI Engine Communication:**
  - The `StockfishConnector` class interacts with Stockfish.
  - Retrieves:
    - Optimal moves
    - Position evaluations
    - Legal moves

- **Configurable Analysis:**
  - Adjustable settings for:
    - Depth of analysis
    - Move time per turn
  - Allows for flexible gameplay and deeper AI-driven analysis.

---

### **Monte Carlo Move Selection**
- **New AI-based move selection for Black:**
  - Implemented in `MonteCarloMoves` and `MonteCarloTree`.
  - Searches possible random move paths before selecting the best-scoring one.
  - Makes decisions based on **position evaluations**.

- **Simulates Multiple Games:**
  - Runs **hundreds of random games** within a 5-second window.
  - Stores evaluation scores and picks the best move from the highest-ranked node.

---

### **User Interface**

#### **JavaFX-Based GUI**
- **Visual representation** of the chessboard and pieces (`ChessWindow`, `MainWindow`).
- **Drag-and-drop functionality** for piece movement.
- **Real-time board updates** and user feedback.

#### **Scalable Design**
- Modular architecture with separation of concerns:
  - **Game logic:** `ChessGame`
  - **GUI:** `ChessWindow`, `MainWindow`
  - **Engine communication:** `StockfishConnector`
  - **Monte Carlo Move Search:** `MonteCarloMoves`, `MonteCarloTree`
- Designed for **easy expansion** and integration of advanced AI techniques.
