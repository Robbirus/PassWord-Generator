package UI;

import model.PasswordEntry;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SecurityAuditPanel extends JPanel {

    private JLabel scoreLabel;
    private JProgressBar progressBar;
    private JTextArea detailsArea;

    public SecurityAuditPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header: Overall Score
        JPanel topPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        scoreLabel = new JLabel("Score de Sécurité : -- %", SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Dialog", Font.BOLD, 18));

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setFont(new Font("Dialog", Font.BOLD, 14));

        topPanel.add(scoreLabel);
        topPanel.add(progressBar);
        add(topPanel, BorderLayout.NORTH);

        // Center: Audit Report
        detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        detailsArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(detailsArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Rapport d'analyse détaillé"));
        add(scrollPane, BorderLayout.CENTER);
    }

    public void analyzeVault(ArrayList<PasswordEntry> database) {
        detailsArea.setText("");

        if (database == null || database.isEmpty()) {
            scoreLabel.setText("Score de Sécurité : N/A");
            progressBar.setValue(0);
            progressBar.setForeground(Color.GRAY);
            detailsArea.setText("Aucun mot de passe dans le coffre-fort pour effectuer l'analyse.");
            return;
        }

        int totalEntries = database.size();
        int weakCount = 0;
        int duplicateCount = 0;

        StringBuilder report = new StringBuilder();
        Map<String, Integer> passFrequency = new HashMap<>();

        // Analysis 1 & 2: Weak passwords + Frequency
        for (PasswordEntry entry : database) {
            String pass = entry.getPassword();
            passFrequency.put(pass, passFrequency.getOrDefault(pass, 0) + 1);

            boolean isWeak = pass.length() < 8 || !pass.matches(".*[0-9].*") || !pass.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");
            if (isWeak) {
                weakCount++;
                report.append("⚠️ [FAIBLE] Site: ").append(entry.getWebsite())
                        .append(" (Mot de passe trop court ou manquant de complexité)\n");
            }
        }

        // Duplicate detection
        for (PasswordEntry entry : database) {
            if (passFrequency.get(entry.getPassword()) > 1) {
                duplicateCount++;
                report.append("🔁 [RÉUTILISÉ] Site: ").append(entry.getWebsite())
                        .append(" (Le mot de passe est utilisé sur plusieurs comptes)\n");
            }
        }

        // Calcul du Score (100 base)
        int penaltyWeak = (int) (((double) weakCount / totalEntries) * 50);
        int penaltyDuplicate = (int) (((double) duplicateCount / totalEntries) * 50);
        int finalScore = Math.max(0, 100 - penaltyWeak - penaltyDuplicate);

        // Affichage des métriques
        progressBar.setValue(finalScore);
        scoreLabel.setText("Score de Sécurité : " + finalScore + " / 100");

        if (finalScore >= 80) {
            progressBar.setForeground(new Color(46, 204, 113)); // Vert
        } else if (finalScore >= 50) {
            progressBar.setForeground(new Color(241, 196, 15)); // Orange
        } else {
            progressBar.setForeground(new Color(231, 76, 60));  // Rouge
        }

        if (report.length() == 0) {
            report.append("✅ Félicitations ! Tous vos mots de passe sont robustes et uniques.");
        } else {
            report.insert(0, "--- SYNTHÈSE DES RECOMMANDATIONS ---\n\n");
        }

        detailsArea.setText(report.toString());
    }
}