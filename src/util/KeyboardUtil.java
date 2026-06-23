package util;

import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public final class KeyboardUtil {

    private KeyboardUtil() {
    }

    public static void configurarAtajos(Node nodo, Runnable onEnter, Runnable onEscape, Runnable onDelete) {
        nodo.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getTarget() instanceof TextArea) {
                return;
            }

            if (event.getCode() == KeyCode.ENTER && onEnter != null && !event.isControlDown() && !event.isShiftDown()) {
                onEnter.run();
                event.consume();
            } else if (event.getCode() == KeyCode.ESCAPE && onEscape != null) {
                onEscape.run();
                event.consume();
            } else if (event.getCode() == KeyCode.DELETE && onDelete != null) {
                onDelete.run();
                event.consume();
            }
        });
    }
}
