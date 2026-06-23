package util;

import javafx.animation.PauseTransition;
import javafx.util.Duration;

public final class DebounceUtil {

    private DebounceUtil() {
    }

    public static PauseTransition crear(Runnable accion, int millis) {
        PauseTransition pause = new PauseTransition(Duration.millis(millis));
        pause.setOnFinished(event -> accion.run());
        return pause;
    }
}
