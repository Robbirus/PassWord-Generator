package crypto;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.util.Timer;
import java.util.TimerTask;

public class ClipboardUtils {

    private static Timer timer;

    /**
     * Copy a text to the clipboard and delete it automatically after 30 seconds.
     */
    public static void copyAndAutoClear(String text, int delaySeconds) {
        StringSelection selection = new StringSelection(text);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);

        // Cancel the previous timer if there is one
        if (timer != null) {
            timer.cancel();
        }

        timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    // Only empty the clipboard if the current data is still the one you copied
                    var clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                        String currentContent = (String) clipboard.getData(DataFlavor.stringFlavor);
                        if (text.equals(currentContent)) {
                            clipboard.setContents(new StringSelection(""), null);
                        }
                    }
                } catch (Exception ignored) {}
            }
        }, delaySeconds * 1000L);
    }
}