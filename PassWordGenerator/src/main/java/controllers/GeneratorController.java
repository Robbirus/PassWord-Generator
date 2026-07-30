package controllers;

import app.AppContext;
import crypto.ClipboardUtils;
import crypto.PasswordGenerator;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.AppSettings;

public class GeneratorController implements BaseController {

    @FXML private TextField resultField;
    @FXML private Slider lengthSlider;
    @FXML private Label lengthLabel;
    @FXML private ProgressBar strengthBar;
    @FXML private Label strengthLabel;
    @FXML private Label crackTimeLabel;

    @FXML private CheckBox upperCheck;
    @FXML private CheckBox lowerCheck;
    @FXML private CheckBox digitsCheck;
    @FXML private CheckBox symbolsCheck;

    private boolean isSelfGenerating = false;
    private AppContext ctx;

    @Override
    public void setAppContext(AppContext ctx) {
        this.ctx = ctx;
    }

    @FXML
    private void initialize() {
        resultField.textProperty().addListener((obs, oldVal, newVal) -> onUserTyping());

        lengthSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            lengthLabel.setText("Longueur du mot de passe : " + newVal.intValue());
            generateAndEvaluate();
        });

        upperCheck.selectedProperty().addListener((o, ov, nv) -> generateAndEvaluate());
        lowerCheck.selectedProperty().addListener((o, ov, nv) -> generateAndEvaluate());
        digitsCheck.selectedProperty().addListener((o, ov, nv) -> generateAndEvaluate());
        symbolsCheck.selectedProperty().addListener((o, ov, nv) -> generateAndEvaluate());

        // Génération initiale
        generateAndEvaluate();
    }

    @FXML
    private void handleRegenerate() {
        generateAndEvaluate();
    }

    @FXML
    private void handleCopy() {
        if (!resultField.getText().isEmpty()) {
            int delay = AppSettings.getClipboardClearDelay();
            ClipboardUtils.copyAndAutoClear(resultField.getText(), delay);
            if (ctx != null) {
                ctx.showInfo("Copié", "Mot de passe copié ! (Effacé du presse-papier dans " + delay + "s)");
            }
        }
    }

    private void onUserTyping() {
        if (isSelfGenerating) return;
        evaluatePassword(resultField.getText());
    }

    private void generateAndEvaluate() {
        int length = (int) lengthSlider.getValue();
        boolean useUpper = upperCheck.isSelected();
        boolean useLower = lowerCheck.isSelected();
        boolean useDigits = digitsCheck.isSelected();
        boolean useSymbols = symbolsCheck.isSelected();

        if (!useUpper && !useLower && !useDigits && !useSymbols) {
            resultField.setText("");
            evaluatePassword("");
            return;
        }

        isSelfGenerating = true;
        PasswordGenerator generator = new PasswordGenerator();
        String pwd = generator.generatePassword(length, useUpper, useLower, useDigits, useSymbols);
        resultField.setText(pwd);
        isSelfGenerating = false;

        evaluatePassword(pwd);
    }

    private void evaluatePassword(String pwd) {
        if (pwd == null || pwd.isEmpty()) {
            strengthBar.setProgress(0);
            strengthLabel.setText("Veuillez saisir ou générer un mot de passe");
            crackTimeLabel.setText("Temps estimé : N/A");
            return;
        }

        int poolSize = 0;
        if (pwd.matches(".*[a-z].*")) poolSize += 26;
        if (pwd.matches(".*[A-Z].*")) poolSize += 26;
        if (pwd.matches(".*[0-9].*")) poolSize += 10;
        if (pwd.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) poolSize += 32;
        if (poolSize == 0) poolSize = 26;

        double combinations = Math.pow(poolSize, pwd.length());

        double guessesPerSecond = 10_000_000_000.0;
        double secondsToCrack = combinations / guessesPerSecond;

        double entropy = pwd.length() * (Math.log(poolSize) / Math.log(2));
        int score = (int) Math.min(100, (entropy / 100.0) * 100);

        if (pwd.equalsIgnoreCase("admin") || pwd.equalsIgnoreCase("123456") || pwd.equalsIgnoreCase("password")) {
            score = 0;
            secondsToCrack = 0;
        }

        strengthBar.setProgress(score / 100.0);
        String formattedTime = formatCrackTime(secondsToCrack);
        crackTimeLabel.setText("Temps estimé pour le cracker : " + formattedTime);
        crackTimeLabel.setGraphic(crackTimeIcon(secondsToCrack));
        crackTimeLabel.setGraphicTextGap(6);

        String barColor;
        String textColor;
        if (score >= 80) {
            barColor = "#28a745";
            textColor = "#28a745";
            strengthLabel.setText("Robustesse : Indéchiffrable (" + score + "%)");
        } else if (score >= 50) {
            barColor = "#ffc107";
            textColor = "#dc7800";
            strengthLabel.setText("Robustesse : Moyenne (" + score + "%)");
        } else {
            barColor = "#dc3545";
            textColor = "#dc3545";
            strengthLabel.setText("Robustesse : Très Faible (" + score + "%)");
        }
        strengthBar.setStyle("-fx-accent: " + barColor + ";");
        crackTimeLabel.setStyle("-fx-text-fill: " + textColor + ";");
    }

    private String formatCrackTime(double seconds) {
        if (seconds <= 0.001) return "Instantané !";
        if (seconds < 1) return "Moins d'une seconde";
        if (seconds < 60) return (int) seconds + " seconde(s)";

        double minutes = seconds / 60;
        if (minutes < 60) return (int) minutes + " minute(s)";

        double hours = minutes / 60;
        if (hours < 24) return (int) hours + " heure(s)";

        double days = hours / 24;
        if (days < 30) return (int) days + " jour(s)";

        double months = days / 30.44;
        if (months < 12) return (int) months + " mois";

        double years = days / 365.25;
        if (years < 1000) return (int) years + " an(s)";
        if (years < 1_000_000) return String.format("%.1f millier(s) d'années", years / 1000.0);
        if (years < 1_000_000_000) return String.format("%.1f million(s) d'années", years / 1_000_000.0);

        return "Plusieurs milliards d'années";
    }

    // --- Crisp icons replacing the old emoji characters (⚡ ⚠️ 🛡️ 🔒) ---
    // Loaded once and cached to avoid re-reading the resource on every keystroke/slider tick.
    private Image boltIcon;
    private Image warningIcon;
    private Image shieldIcon;
    private Image lockIcon;

    private ImageView crackTimeIcon(double seconds) {
        Image icon;
        if (seconds <= 0.001) {
            if (boltIcon == null) boltIcon = loadIcon("/icons/bolt.png");
            icon = boltIcon;
        } else if (seconds < 1) {
            if (warningIcon == null) warningIcon = loadIcon("/icons/warning.png");
            icon = warningIcon;
        } else if (seconds < 365.25 * 24 * 3600 * 1000) {
            if (shieldIcon == null) shieldIcon = loadIcon("/icons/shield_check.png");
            icon = shieldIcon;
        } else {
            if (lockIcon == null) lockIcon = loadIcon("/icons/lock.png");
            icon = lockIcon;
        }

        ImageView view = new ImageView(icon);
        view.setFitWidth(16);
        view.setFitHeight(16);
        return view;
    }

    private Image loadIcon(String path) {
        return new Image(getClass().getResourceAsStream(path), 16, 16, true, true);
    }
}