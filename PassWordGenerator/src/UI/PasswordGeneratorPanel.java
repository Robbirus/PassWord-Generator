package UI;

import crypto.ClipboardUtils;
import crypto.PasswordGenerator;
import model.AppSettings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class PasswordGeneratorPanel extends JPanel {

    private JTextField resultField;
    private JSlider lengthSlider;
    private JLabel lengthLabel;
    private JProgressBar strengthBar;
    private JLabel strengthLabel;
    private JLabel crackTimeLabel;

    private JCheckBox upperCheck;
    private JCheckBox lowerCheck;
    private JCheckBox digitsCheck;
    private JCheckBox symbolsCheck;

    private boolean isSelfGenerating = false;

    ImageIcon generateIcon = UIUtils.loadAndScaleIcon("/icons/dice.png", 24, 24);
    ImageIcon copyIcon = UIUtils.loadAndScaleIcon("/icons/copy.png", 24, 24);
    ImageIcon refreshIcon = UIUtils.loadAndScaleIcon("/icons/refresh.png", 24, 24);
    ImageIcon hourglassIcon = UIUtils.loadAndScaleIcon("/icons/hourglass.png", 24, 24);

    public PasswordGeneratorPanel() {
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel titleLabel = new JLabel("Générateur & Testeur de Robustesse", SwingConstants.CENTER);
        titleLabel.setIcon(generateIcon);
        titleLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 22));
        add(titleLabel, BorderLayout.NORTH);

        // --- CENTRAL PANEL ---
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // Input field / result (Editable by the user)
        resultField = new JTextField(25);
        resultField.setFont(new Font("Monospaced", Font.BOLD, 18));
        resultField.setHorizontalAlignment(JTextField.CENTER);

        // Allows you to directly evaluate what the user types ("admin", etc.)
        resultField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { onUserTyping(); }
            public void removeUpdate(DocumentEvent e) { onUserTyping(); }
            public void changedUpdate(DocumentEvent e) { onUserTyping(); }
        });

        JButton btnCopy = new JButton("Copier");
        btnCopy.setIcon(copyIcon);
        JButton btnRegenerate = new JButton("Régénérer");
        btnRegenerate.setIcon(refreshIcon);

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        topRow.add(resultField);
        topRow.add(btnRegenerate);
        topRow.add(btnCopy);
        mainPanel.add(topRow);

        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // --- STRENGTH BAR AND CRACKING TIME ---
        strengthBar = new JProgressBar(0, 100);
        strengthBar.setPreferredSize(new Dimension(400, 20));
        strengthBar.setStringPainted(true);

        strengthLabel = new JLabel("Robustesse : Faible", SwingConstants.CENTER);
        strengthLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));

        crackTimeLabel = new JLabel("Temps estimé pour le cracker : Instantané", SwingConstants.CENTER);
        crackTimeLabel.setIcon(hourglassIcon);
        crackTimeLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));

        JPanel strengthPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        strengthPanel.add(strengthBar);
        strengthPanel.add(strengthLabel);
        strengthPanel.add(crackTimeLabel);
        mainPanel.add(strengthPanel);

        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // --- GENERATOR OPTIONS ---
        JPanel optionsPanel = new JPanel(new GridLayout(5, 1, 5, 5));
        optionsPanel.setBorder(BorderFactory.createTitledBorder("Options de génération automatique"));

        lengthLabel = new JLabel("Longueur du mot de passe : 16");
        lengthSlider = new JSlider(4, 32, 16);
        lengthSlider.setMajorTickSpacing(4);
        lengthSlider.setPaintTicks(true);

        upperCheck = new JCheckBox("Majuscules (A-Z)", true);
        lowerCheck = new JCheckBox("Minuscules (a-z)", true);
        digitsCheck = new JCheckBox("Chiffres (0-9)", true);
        symbolsCheck = new JCheckBox("Symboles (!@#$%...)", true);

        optionsPanel.add(lengthLabel);
        optionsPanel.add(lengthSlider);
        optionsPanel.add(upperCheck);
        optionsPanel.add(lowerCheck);
        optionsPanel.add(digitsCheck);
        optionsPanel.add(symbolsCheck);

        mainPanel.add(optionsPanel);
        add(mainPanel, BorderLayout.CENTER);

        // --- EVENT LISTENERS ---
        lengthSlider.addChangeListener(e -> {
            lengthLabel.setText("Longueur du mot de passe : " + lengthSlider.getValue());
            generateAndEvaluate();
        });

        upperCheck.addActionListener(e -> generateAndEvaluate());
        lowerCheck.addActionListener(e -> generateAndEvaluate());
        digitsCheck.addActionListener(e -> generateAndEvaluate());
        symbolsCheck.addActionListener(e -> generateAndEvaluate());

        btnRegenerate.addActionListener(e -> generateAndEvaluate());
        btnCopy.addActionListener(e -> {
            if (!resultField.getText().isEmpty()) {
                int delay = AppSettings.getClipboardClearDelay();
                ClipboardUtils.copyAndAutoClear(resultField.getText(), delay);
                JOptionPane.showMessageDialog(this, "Mot de passe copié ! (Effacé du presse-papier dans " + delay + "s)");
            }
        });

        // Initial generation
        generateAndEvaluate();
    }

    private void onUserTyping() {
        if (isSelfGenerating) return;
        evaluatePassword(resultField.getText());
    }

    private void generateAndEvaluate() {
        int length = lengthSlider.getValue();
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
            strengthBar.setValue(0);
            strengthLabel.setText("Veuillez saisir ou générer un mot de passe");
            crackTimeLabel.setText("Temps estimé : N/A");
            crackTimeLabel.setIcon(hourglassIcon);
            return;
        }

        // 1. Determine the size of the alphabet used
        int poolSize = 0;
        if (pwd.matches(".*[a-z].*")) poolSize += 26;
        if (pwd.matches(".*[A-Z].*")) poolSize += 26;
        if (pwd.matches(".*[0-9].*")) poolSize += 10;
        if (pwd.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) poolSize += 32;
        if (poolSize == 0) poolSize = 26; // Default Safety

        // 2. Total combination name calculation: PoolSize  Length
        double combinations = Math.pow(poolSize, pwd.length());

        // 3. Attack speed
        double guessesPerSecond = 10_000_000_000.0;
        double secondsToCrack = combinations / guessesPerSecond;

        // 4. Entropy calculation (out of 100 score)
        double entropy = pwd.length() * (Math.log(poolSize) / Math.log(2));
        int score = (int) Math.min(100, (entropy / 100.0) * 100);

        // Known extremely simple passwords (e.g. admin, 123456, password)
        if (pwd.equalsIgnoreCase("admin") || pwd.equalsIgnoreCase("123456") || pwd.equalsIgnoreCase("password")) {
            score = 0;
            secondsToCrack = 0;
        }

        // Updating the bar and messages
        strengthBar.setValue(score);
        String formattedTime = formatCrackTime(secondsToCrack);
        crackTimeLabel.setText("Temps estimé pour le cracker : " + formattedTime);
        crackTimeLabel.setIcon(hourglassIcon);

        if (score >= 80) {
            strengthBar.setForeground(new Color(40, 167, 69));
            strengthLabel.setText("Robustesse : Indéchiffrable (" + score + "%)");
            crackTimeLabel.setForeground(new Color(40, 167, 69));
        } else if (score >= 50) {
            strengthBar.setForeground(new Color(255, 193, 7));
            strengthLabel.setText("Robustesse : Moyenne (" + score + "%)");
            crackTimeLabel.setForeground(new Color(220, 120, 0));
        } else {
            strengthBar.setForeground(new Color(220, 53, 69));
            strengthLabel.setText("Robustesse : Très Faible (" + score + "%)");
            crackTimeLabel.setForeground(new Color(220, 53, 69));
        }
    }

    /**
     * Convert a number of seconds into readable text (seconds, hours, years, centuries...)
     */
    private String formatCrackTime(double seconds) {
        if (seconds <= 0.001) return "Instantané ! ⚡";
        if (seconds < 1) return "Moins d'une seconde ⚠️";
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
        if (years < 1_000_000) return String.format("%.1f millier(s) d'années 🛡️", years / 1000.0);
        if (years < 1_000_000_000) return String.format("%.1f million(s) d'années 🛡️", years / 1_000_000.0);

        return "Plusieurs milliards d'années 🔒";
    }
}