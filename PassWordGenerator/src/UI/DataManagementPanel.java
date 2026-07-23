package UI;

import crypto.CsvService;
import model.PasswordEntry;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;

public class DataManagementPanel extends JPanel {

    private PasswordManagerGUI mainFrame;
    private ImageIcon downloadIcon = UIUtils.loadAndScaleIcon("/icons/download.png", 32, 32);
    private ImageIcon uploadIcon = UIUtils.loadAndScaleIcon("/icons/upload.png", 32, 32);

    public DataManagementPanel(PasswordManagerGUI mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel titleLabel = new JLabel("Gestion des Données (Import / Export)", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 22));
        add(titleLabel, BorderLayout.NORTH);

        JPanel gridPanel = new JPanel(new GridLayout(1, 2, 20, 0));

        // --- CARD IMPORT ---
        JPanel importCard = createSectionCard("Importer un coffre-fort",
                "Importez vos identifiants depuis un fichier CSV.",
                new Color(40, 167, 69),
                downloadIcon);

        JButton btnImport = new JButton("Sélectionner un fichier CSV...");
        btnImport.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
        btnImport.addActionListener(e -> handleImport());
        importCard.add(btnImport, BorderLayout.SOUTH);

        // --- CARD EXPORT ---
        JPanel exportCard = createSectionCard("Exporter le coffre-fort",
                "⚠\uFE0F <b>Avertissement :</b> L'exportation génère un fichier CSV en texte clair non chiffré.",
                new Color(220, 53, 69),
                uploadIcon);

        JButton btnExport = new JButton("Exporter en CSV...");
        btnExport.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
        btnExport.addActionListener(e -> handleExport());
        exportCard.add(btnExport, BorderLayout.SOUTH);

        gridPanel.add(importCard);
        gridPanel.add(exportCard);

        add(gridPanel, BorderLayout.CENTER);
    }

    private JPanel createSectionCard(String title, String descriptionHtml, Color accentColor, ImageIcon icon) {
        JPanel card = new JPanel(new BorderLayout(10, 15));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel header = new JLabel(title);
        header.setIcon(icon);
        header.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        header.setForeground(accentColor);

        JLabel desc = new JLabel("<html><body style='width: 220px;'>" + descriptionHtml + "</body></html>");
        desc.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));

        JPanel topContent = new JPanel(new BorderLayout(5, 10));
        topContent.add(header, BorderLayout.NORTH);
        topContent.add(desc, BorderLayout.CENTER);

        card.add(topContent, BorderLayout.NORTH);
        return card;
    }

    private void handleImport() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Choisir un fichier CSV à importer");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Fichiers CSV (*.csv)", "csv"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                ArrayList<PasswordEntry> imported = CsvService.importFromCsv(file);
                if (imported.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Aucune entrée valide n'a été trouvée dans ce fichier.", "Importation", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int confirm = JOptionPane.showConfirmDialog(this,
                        "Détection de " + imported.size() + " mot(s) de passe.\nVoulez-vous les ajouter à votre coffre-fort ?",
                        "Confirmer l'importation", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    mainFrame.getFakeDatabase().addAll(imported);
                    mainFrame.saveVault();
                    mainFrame.refreshDashboardDisplay();
                    JOptionPane.showMessageDialog(this, imported.size() + " entrée(s) importée(s) avec succès !");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur lors de l'importation : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleExport() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "ATTENTION : Le fichier CSV généré contiendra vos mots de passe EN CLAIR.\nAssurez-vous de le stocker en lieu sûr puis de le supprimer après usage.\n\nContinuer ?",
                "Avertissement de Sécurité", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Enregistrer le fichier d'export CSV");
            fileChooser.setSelectedFile(new File("vault_export.csv"));
            fileChooser.setFileFilter(new FileNameExtensionFilter("Fichiers CSV (*.csv)", "csv"));

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (!file.getName().endsWith(".csv")) {
                    file = new File(file.getAbsolutePath() + ".csv");
                }

                try {
                    CsvService.exportToCsv(file, mainFrame.getFakeDatabase());
                    JOptionPane.showMessageDialog(this, "Exportation réussie dans :\n" + file.getAbsolutePath());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Erreur lors de l'exportation : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}