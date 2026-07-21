package crypto;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.Timer;
import java.util.TimerTask;

public class ClipboardUtils {

    /**
     * Copy a text to the clipboard and automatically empty it after X seconds.
     */
    public static void copyToClipboardWithTimeout(String text, int timeoutSeconds) {
        if (text == null || text.isEmpty()) return;

        // 1. Copy to the system clipboard
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection selection = new StringSelection(text);
        clipboard.setContents(selection, selection);

        // 2. Programming the automatic emptying
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    // We check if the content is still the one we copied before emptying it.
                    String currentContent = (String) clipboard.getData(java.awt.datatransfer.DataFlavor.stringFlavor);
                    if (text.equals(currentContent)) {
                        clipboard.setContents(new StringSelection(""), null);
                    }
                } catch (Exception ignored) {
                    // Ignoré si le contenu a changé ou est inaccessible
                }
                timer.cancel();
            }
        }, timeoutSeconds * 1000L);
    }
}