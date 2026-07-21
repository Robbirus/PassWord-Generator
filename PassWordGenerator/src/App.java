import UI.PasswordManagerGUI;

import javax.swing.*;

public class App {

    public static void main(String[] args) {
        if (model.AppSettings.isDarkMode()) {
            com.formdev.flatlaf.FlatDarkLaf.setup();
        } else {
            com.formdev.flatlaf.FlatLightLaf.setup();
        }

        SwingUtilities.invokeLater(() -> {
            new PasswordManagerGUI().setVisible(true);
        });
    }
}