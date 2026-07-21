package UI;

import javax.swing.*;
import java.awt.*;

public class UpdateDialog extends JDialog {
    private JTextField loginField;
    private JPasswordField passwordField;
    private boolean validated = false;

    // Le constructeur prend en paramètre la fenêtre parente et les données actuelles
    public UpdateDialog(JFrame parent, String site, String currentLogin, String currentPassword) {
        super(parent, "Modifier - " + site, true); // true rend la pop-up modale (bloque le fond)
        setSize(350, 200);
        setLocationRelativeTo(parent); // Centre la pop-up par rapport à la fenêtre principale
        setLayout(new BorderLayout(10, 10));

        // Panneau de saisie (Formulaire)
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        formPanel.add(new JLabel("Identifiant :"));
        loginField = new JTextField(currentLogin);
        formPanel.add(loginField);

        formPanel.add(new JLabel("Mot de passe :"));
        passwordField = new JPasswordField(currentPassword);
        formPanel.add(passwordField);

        // Bouton pour générer un mot de passe fort à la volée
        JButton btnGenerate = new JButton("Générer");
        formPanel.add(new JLabel("")); // Case vide pour aligner le bouton
        formPanel.add(btnGenerate);

        add(formPanel, BorderLayout.CENTER);

        // Barre de boutons en bas
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnOk = new JButton("Enregistrer");
        JButton btnCancel = new JButton("Annuler");
        actionPanel.add(btnOk);
        actionPanel.add(btnCancel);
        add(actionPanel, BorderLayout.SOUTH);

        // --- LOGIQUE ---

        // Générateur automatique (réutilise ta logique existante si tu veux)
        btnGenerate.addActionListener(e -> {
            // Exemple simple, remplace par ton générateur si nécessaire
            String generated = Long.toHexString(Double.doubleToLongBits(Math.random())).substring(0, 12);
            passwordField.setText(generated);
        });

        // Validation
        btnOk.addActionListener(e -> {
            if (loginField.getText().trim().isEmpty() || new String(passwordField.getPassword()).trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Les champs ne peuvent pas être vides.", "Erreur", JOptionPane.ERROR_MESSAGE);
            } else {
                validated = true;
                dispose(); // Ferme la pop-up
            }
        });

        // Annulation
        btnCancel.addActionListener(e -> dispose());
    }

    // Getters pour récupérer les valeurs après la fermeture
    public boolean isValidated() { return validated; }
    public String getLogin() { return loginField.getText(); }
    public String getPassword() { return new String(passwordField.getPassword()); }
}