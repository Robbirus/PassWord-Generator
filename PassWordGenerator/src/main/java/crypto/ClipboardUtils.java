package crypto;

import javafx.application.Platform;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.util.Timer;
import java.util.TimerTask;

public class ClipboardUtils {

    private static Timer timer;

    /**
     * Copies a text to the clipboard and automatically clears it after X seconds.
     * Clipboard JavaFX must be accessed on the application thread (Platform.runLater).
     */
    public static void copyAndAutoClear(String text, int delaySeconds) {
        Platform.runLater(() -> {
            ClipboardContent content = new ClipboardContent();
            content.putString(text);
            Clipboard.getSystemClipboard().setContent(content);
        });

        // Cancels the previous timer if there was one
        if (timer != null) {
            timer.cancel();
        }

        timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    try {
                        Clipboard clipboard = Clipboard.getSystemClipboard();
                        // We only empty if the current content is still the one we copied
                        if (clipboard.hasString() && text.equals(clipboard.getString())) {
                            clipboard.clear();
                        }
                    } catch (Exception ignored) {}
                });
            }
        }, delaySeconds * 1000L);
    }
}
