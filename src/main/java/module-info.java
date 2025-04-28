module com.george.chess {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.base;
    requires java.logging;
    requires java.desktop; // Added to allow access to java.awt and javax.imageio

    // Allow JavaFX to access your window classes
    opens com.chess.window to javafx.graphics;
}
