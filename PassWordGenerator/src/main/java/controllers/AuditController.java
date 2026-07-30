package controllers;

import app.AppContext;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.PasswordEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AuditController implements BaseController {

    @FXML private Label overallScoreLabel;
    @FXML private ProgressBar overallProgressBar;
    @FXML private VBox detailsPanel;

    private AppContext ctx;

    @Override
    public void setAppContext(AppContext ctx) {
        this.ctx = ctx;
        ctx.registerAudit(this);
    }

    public void analyzeVault() {
        detailsPanel.getChildren().clear();
        ArrayList<PasswordEntry> database = ctx.getDatabase();

        if (database == null || database.isEmpty()) {
            overallScoreLabel.setText("Score global : Aucun mot de passe à analyser");
            overallProgressBar.setProgress(0);
            overallProgressBar.setStyle("-fx-accent: gray;");
            return;
        }

        // 1. Compter la fréquence de chaque mot de passe (pour détecter les réutilisations)
        Map<String, Integer> passwordCounts = new HashMap<>();
        for (PasswordEntry entry : database) {
            String pass = entry.getPassword();
            passwordCounts.put(pass, passwordCounts.getOrDefault(pass, 0) + 1);
        }

        int totalScore = 0;

        // 2. Analyser chaque entrée
        for (PasswordEntry entry : database) {
            int reuseCount = passwordCounts.get(entry.getPassword());
            int score = calculatePasswordScore(entry.getPassword(), reuseCount);
            totalScore += score;

            detailsPanel.getChildren().add(createAuditCard(entry, score, reuseCount));
        }

        // 3. Score moyen
        int averageScore = totalScore / database.size();
        overallScoreLabel.setText("Score global de santé : " + averageScore + "%");
        overallProgressBar.setProgress(averageScore / 100.0);

        if (averageScore >= 80) {
            overallProgressBar.setStyle("-fx-accent: #28a745;"); // Vert
        } else if (averageScore >= 50) {
            overallProgressBar.setStyle("-fx-accent: #ffc107;"); // Orange
        } else {
            overallProgressBar.setStyle("-fx-accent: #dc3545;"); // Rouge
        }
    }

    private int calculatePasswordScore(String password, int reuseCount) {
        if (password == null || password.isEmpty()) return 0;

        int score = 0;
        score += Math.min(password.length() * 4, 40);
        if (password.matches(".*[A-Z].*")) score += 15;
        if (password.matches(".*[a-z].*")) score += 15;
        if (password.matches(".*[0-9].*")) score += 15;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) score += 15;

        if (reuseCount > 1) {
            score = Math.max(0, score - 35);
        }

        return Math.min(100, score);
    }

    private BorderPane createAuditCard(PasswordEntry entry, int score, int reuseCount) {
        BorderPane card = new BorderPane();
        card.setPadding(new Insets(8, 12, 8, 12));
        card.setStyle("-fx-border-color: lightgray; -fx-border-radius: 4; -fx-background-radius: 4;");

        Label infoLabel = new Label(entry.getWebsite() + " (" + entry.getUsername() + ")");
        infoLabel.setStyle("-fx-font-weight: bold;");

        StringBuilder statusText = new StringBuilder();
        String color;
        boolean reused = reuseCount > 1;

        if (score >= 80) {
            statusText.append("Fort (").append(score).append("%)");
            color = "#28a745";
        } else if (score >= 50) {
            statusText.append("Moyen (").append(score).append("%)");
            color = "#dc7800";
        } else {
            statusText.append("Faible (").append(score).append("%)");
            color = "#dc3545";
        }

        if (reused) {
            statusText.append(" - Réutilisé sur ").append(reuseCount).append(" sites !");
            color = "#dc3545";
        }

        Label scoreLabel = new Label(statusText.toString());
        scoreLabel.setStyle("-fx-text-fill: " + color + ";");

        if (reused) {
            ImageView warningIcon = new ImageView(
                    new Image(getClass().getResourceAsStream("/icons/warning.png"), 16, 16, true, true));
            HBox statusBox = new HBox(6, warningIcon, scoreLabel);
            statusBox.setStyle("-fx-alignment: CENTER_RIGHT;");
            card.setRight(statusBox);
        } else {
            card.setRight(scoreLabel);
        }

        card.setLeft(infoLabel);
        return card;
    }
}