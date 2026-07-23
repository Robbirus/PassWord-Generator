package UI;

import model.PasswordEntry;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SecurityAuditPanel extends JPanel {

    private PasswordManagerGUI mainFrame;
    private JLabel overallScoreLabel;
    private JProgressBar overallProgressBar;
    private JPanel detailsPanel;

    public SecurityAuditPanel(PasswordManagerGUI mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // --- EN-TÊTE DU SCORE GLOBAL ---
        JPanel headerPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        JLabel titleLabel = new JLabel("Audit de Sécurité du Coffre-Fort", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 18));

        overallScoreLabel = new JLabel("Score global : 0%", SwingConstants.CENTER);
        overallScoreLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));

        overallProgressBar = new JProgressBar(0, 100);
        overallProgressBar.setStringPainted(true);
        overallProgressBar.setPreferredSize(new Dimension(300, 25));

        headerPanel.add(titleLabel);
        headerPanel.add(overallScoreLabel);
        headerPanel.add(overallProgressBar);

        add(headerPanel, BorderLayout.NORTH);

        // --- Details list ---
        detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(detailsPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void analyzeVault() {
        detailsPanel.removeAll();
        ArrayList<PasswordEntry> database = mainFrame.getFakeDatabase();

        if (database == null || database.isEmpty()) {
            overallScoreLabel.setText("Score global : Aucun mot de passe à analyser");
            overallProgressBar.setValue(0);
            overallProgressBar.setForeground(Color.GRAY);
            revalidate();
            repaint();
            return;
        }

        // 1. Count the frequency of each password (to detect reuses)
        Map<String, Integer> passwordCounts = new HashMap<>();
        for (PasswordEntry entry : database) {
            String pass = entry.getPassword();
            passwordCounts.put(pass, passwordCounts.getOrDefault(pass, 0) + 1);
        }

        int totalScore = 0;

        // 2. Analyze every entry
        for (PasswordEntry entry : database) {
            int reuseCount = passwordCounts.get(entry.getPassword());
            int score = calculatePasswordScore(entry.getPassword(), reuseCount);
            totalScore += score;

            JPanel entryCard = createAuditCard(entry, score, reuseCount);
            detailsPanel.add(entryCard);
            detailsPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        }

        // 3. Computes the average score
        int averageScore = totalScore / database.size();
        overallScoreLabel.setText("Score global de santé : " + averageScore + "%");
        overallProgressBar.setValue(averageScore);

        if (averageScore >= 80) {
            overallProgressBar.setForeground(new Color(40, 167, 69)); // Vert
        } else if (averageScore >= 50) {
            overallProgressBar.setForeground(new Color(255, 193, 7)); // Orange
        } else {
            overallProgressBar.setForeground(new Color(220, 53, 69)); // Rouge
        }

        revalidate();
        repaint();
    }

    private int calculatePasswordScore(String password, int reuseCount) {
        if (password == null || password.isEmpty()) return 0;

        int score = 0;

        // Length (max 40 pts)
        score += Math.min(password.length() * 4, 40);

        // Uppercase (+15 pts)
        if (password.matches(".*[A-Z].*")) score += 15;

        // Lowercase (+15 pts)
        if (password.matches(".*[a-z].*")) score += 15;

        // Numbers (+15 pts)
        if (password.matches(".*[0-9].*")) score += 15;

        // Symbols (+15 pts)
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) score += 15;

        // REUSE PENALTY
        if (reuseCount > 1) {
            // Remove 35 points if the password is reused elsewhere
            score = Math.max(0, score - 35);
        }

        return Math.min(100, score);
    }

    private JPanel createAuditCard(PasswordEntry entry, int score, int reuseCount) {
        JPanel card = new JPanel(new BorderLayout(10, 5));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        JLabel infoLabel = new JLabel("<html><b>" + entry.getWebsite() + "</b> (" + entry.getUsername() + ")</html>");

        StringBuilder statusText = new StringBuilder();
        Color statusColor;

        if (score >= 80) {
            statusText.append("Fort (").append(score).append("%)");
            statusColor = new Color(40, 167, 69);
        } else if (score >= 50) {
            statusText.append("Moyen (").append(score).append("%)");
            statusColor = new Color(220, 120, 0);
        } else {
            statusText.append("Faible (").append(score).append("%)");
            statusColor = new Color(220, 53, 69);
        }

        // Add the reuse warning if applicable
        if (reuseCount > 1) {
            statusText.append(" - ⚠️ <i>Réutilisé sur ").append(reuseCount).append(" sites !</i>");
            statusColor = new Color(220, 53, 69); // Force the red if reused
        }

        JLabel scoreLabel = new JLabel("<html>" + statusText + "</html>");
        scoreLabel.setForeground(statusColor);

        card.add(infoLabel, BorderLayout.WEST);
        card.add(scoreLabel, BorderLayout.EAST);

        return card;
    }
}