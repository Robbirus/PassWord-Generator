import UI.PasswordManagerGUI;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;


public class App {
    public static void main(String[] args) {
        if (model.AppSettings.isDarkMode()) {
            com.formdev.flatlaf.FlatDarkLaf.setup();
        } else {
            com.formdev.flatlaf.FlatLightLaf.setup();
        }

        // Ajoute ça APRÈS FlatLaf setup :
        java.awt.EventQueue.invokeLater(() -> {
            try {
                Class.forName("com.formdev.flatlaf.FlatLaf").getMethod("updateUI").invoke(null);
            } catch (Exception e) { e.printStackTrace(); }
        });
        
            new PasswordManagerGUI().setVisible(true);

    }
}