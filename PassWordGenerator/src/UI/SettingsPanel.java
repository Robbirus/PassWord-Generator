package UI;

import model.AppSettings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SettingsPanel extends JPanel {

    private JCheckBox showPasswordsCheck;
    private JSpinner clipboardSpinner;
    private JSpinner autoLockSpinner;

    ImageIcon settingsIcon = UIUtils.loadAndScaleIcon("/icons/settings.png", 32, 32);
    ImageIcon saveIcon = UIUtils.loadAndScaleIcon("/icons/save.png", 32, 32);

    public SettingsPanel(PasswordManagerGUI mainFrame) {
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel titleLabel = new JLabel("Paramètres de l'Application", SwingConstants.LEFT);
        titleLabel.setIcon(settingsIcon);
        titleLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 20));
        add(titleLabel, BorderLayout.NORTH);

        JPanel optionsPanel = new JPanel(new GridLayout(0, 1, 10, 15));
        optionsPanel.setBorder(BorderFactory.createTitledBorder("Sécurité & Affichage"));

        // 1. Show Default Passwords
        showPasswordsCheck = new JCheckBox("Afficher les mots de passe en clair dans le tableau par défaut", AppSettings.isShowPasswordsByDefault());

        // 2. Clipboard Erase Timeout
        JPanel clipboardPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        clipboardPanel.add(new JLabel("Effacer le presse-papier après :"));
        clipboardSpinner = new JSpinner(new SpinnerNumberModel(AppSettings.getClipboardClearDelay(), 5, 300, 5));
        clipboardPanel.add(clipboardSpinner);
        clipboardPanel.add(new JLabel("secondes"));

        // 3. Auto-Lock Timeout by Inactivity
        JPanel autoLockPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        autoLockPanel.add(new JLabel("Verrouiller automatiquement après :"));
        autoLockSpinner = new JSpinner(new SpinnerNumberModel(AppSettings.getAutoLockDelay(), 0, 60, 1));
        autoLockPanel.add(autoLockSpinner);
        autoLockPanel.add(new JLabel("minutes (0 = Désactivé)"));

        optionsPanel.add(showPasswordsCheck);
        optionsPanel.add(clipboardPanel);
        optionsPanel.add(autoLockPanel);

        add(optionsPanel, BorderLayout.CENTER);

        // Save Button
        JButton btnSave = new JButton("Enregistrer les préférences");
        btnSave.setIcon(saveIcon);
        btnSave.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
        btnSave.addActionListener(e -> {
            AppSettings.setShowPasswordsByDefault(showPasswordsCheck.isSelected());
            AppSettings.setClipboardClearDelay((Integer) clipboardSpinner.getValue());
            AppSettings.setAutoLockDelay((Integer) autoLockSpinner.getValue());

            mainFrame.refreshDashboardDisplay();
            mainFrame.resetInactivityTimer(); // Resets the timer with the new duration
            JOptionPane.showMessageDialog(this, "Paramètres sauvegardés avec succès !");
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(btnSave);
        add(bottomPanel, BorderLayout.SOUTH);
    }
}