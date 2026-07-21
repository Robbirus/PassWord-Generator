package UI;

import crypto.PasswordGenerator;
import model.AppSettings;
import model.PasswordEntry;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;

public class DashboardPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private PasswordManagerGUI mainFrame;

    public DashboardPanel(PasswordManagerGUI mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("Mon Coffre-fort de Mots de Passe", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 20));
        add(titleLabel, BorderLayout.NORTH);

        String[] columns = {"Site Web", "Identifiant", "Mot de Passe", "Date d'ajout"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setFont(new Font("Dialog", Font.PLAIN, 13));
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        JButton btnCopy = new JButton("Copier");
        JButton btnEdit = new JButton("✏️ Éditer");
        JButton btnDelete = new JButton("Supprimer");
        btnDelete.setForeground(new Color(220, 53, 69));

        // --- ACTION : COPIER ---
        btnCopy.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int modelRow = table.convertRowIndexToModel(selectedRow);
                PasswordEntry entry = mainFrame.getFakeDatabase().get(modelRow);

                StringSelection selection = new StringSelection(entry.getPassword());
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
                JOptionPane.showMessageDialog(this, "Mot de passe copié dans le presse-papier !");
            } else {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner une ligne.", "Attention", JOptionPane.WARNING_MESSAGE);
            }
        });

        // --- ACTION : ÉDITER (AVEC GÉNÉRATEUR DEDANS) ---
        btnEdit.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int modelRow = table.convertRowIndexToModel(selectedRow);
                PasswordEntry entry = mainFrame.getFakeDatabase().get(modelRow);

                // Champs de saisie pré-remplis
                JTextField siteInput = new JTextField(entry.getWebsite(), 20);
                JTextField userInput = new JTextField(entry.getUsername(), 20);
                JTextField passInput = new JTextField(entry.getPassword(), 20);

                // Bouton Générateur de mot de passe
                JButton btnGenerate = new JButton("🎲 Générer");
                btnGenerate.addActionListener(genEvent -> {
                    PasswordGenerator generator = new PasswordGenerator();
                    String newGeneratedPassword = generator.generatePassword(16, true, true, true, true);
                    passInput.setText(newGeneratedPassword);
                });

                // Ligne du mot de passe avec son bouton générateur à côté
                JPanel passPanel = new JPanel(new BorderLayout(5, 0));
                passPanel.add(passInput, BorderLayout.CENTER);
                passPanel.add(btnGenerate, BorderLayout.EAST);

                // Formulaire d'édition
                JPanel editForm = new JPanel(new GridLayout(0, 1, 5, 5));
                editForm.add(new JLabel("Site Web / Application :"));
                editForm.add(siteInput);
                editForm.add(new JLabel("Identifiant / Email :"));
                editForm.add(userInput);
                editForm.add(new JLabel("Mot de Passe :"));
                editForm.add(passPanel);

                int result = JOptionPane.showConfirmDialog(
                        this, editForm, "Modifier le mot de passe",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
                );

                if (result == JOptionPane.OK_OPTION) {
                    // Mettre à jour les données de l'objet
                    entry.setWebsite(siteInput.getText().trim());
                    entry.setUsername(userInput.getText().trim());
                    entry.setPassword(passInput.getText().trim());

                    // Sauvegarder sur disque et rafraîchir IHM + Audit
                    mainFrame.saveVault();
                    mainFrame.refreshDashboardDisplay();

                    JOptionPane.showMessageDialog(this, "Entrée modifiée avec succès !");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner une ligne à modifier.", "Attention", JOptionPane.WARNING_MESSAGE);
            }
        });

        // --- ACTION : SUPPRIMER ---
        btnDelete.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int modelRow = table.convertRowIndexToModel(selectedRow);
                ArrayList<PasswordEntry> db = mainFrame.getFakeDatabase();

                if (modelRow < db.size()) {
                    PasswordEntry entry = db.get(modelRow);

                    int confirm = JOptionPane.showConfirmDialog(
                            this,
                            "Êtes-vous sûr de vouloir supprimer \"" + entry.getWebsite() + "\" ?",
                            "Confirmation de suppression",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE
                    );

                    if (confirm == JOptionPane.YES_OPTION) {
                        db.remove(modelRow);
                        mainFrame.saveVault();
                        mainFrame.refreshDashboardDisplay();
                        JOptionPane.showMessageDialog(this, "Mot de passe supprimé !");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner une ligne à supprimer.", "Attention", JOptionPane.WARNING_MESSAGE);
            }
        });

        actionPanel.add(btnCopy);
        actionPanel.add(btnEdit);
        actionPanel.add(btnDelete);
        add(actionPanel, BorderLayout.SOUTH);
    }

    public void loadDataIntoTable(ArrayList<PasswordEntry> database) {
        tableModel.setRowCount(0);
        boolean showPass = AppSettings.isShowPasswordsByDefault();

        if (database != null) {
            for (PasswordEntry entry : database) {
                String displayedPassword = showPass ? entry.getPassword() : "••••••••";
                tableModel.addRow(new Object[]{
                        entry.getWebsite(),
                        entry.getUsername(),
                        displayedPassword,
                        entry.getDateAdded()
                });
            }
        }
    }
}