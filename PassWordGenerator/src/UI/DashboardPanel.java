package UI;

import model.PasswordEntry;
import crypto.ClipboardUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class DashboardPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField searchField;
    private PasswordManagerGUI mainFrame;
    private ArrayList<PasswordEntry> currentDatabase;

    public DashboardPanel(PasswordManagerGUI mainFrame) {
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- TOP: Search bar ---
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.add(new JLabel("Rechercher un site : "), BorderLayout.WEST);

        searchField = new JTextField();
        topPanel.add(searchField, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // --- CENTER: The Table ---
        String[] columns = {"Nom du Site", "Identifiant / Login", "Mot de passe", "Date d'ajout"};

        // Table Template: Direct double-click edit is prohibited
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Cursor in the form of a hand when hovering over the table to indicate interactivity
        table.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Automatic sorting of columns
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // --- BOTTOM : Action Buttons ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAdd = new JButton("Ajouter");
        JButton btnEdit = new JButton("Modifier");
        JButton btnDelete = new JButton("Supprimer");

        bottomPanel.add(btnAdd);
        bottomPanel.add(btnEdit);
        bottomPanel.add(btnDelete);
        add(bottomPanel, BorderLayout.SOUTH);

        // --- CLICK MANAGEMENT / QUICK ONE-CLICK COPY ---
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = table.getSelectedRow();
                int col = table.getSelectedColumn();

                if (row != -1 && col != -1) {
                    // Convert the visual index into a real index of the model (essential if filtered/trié)
                    int modelRow = table.convertRowIndexToModel(row);

                    // Clic on the Identifier column (index 1)
                    if (col == 1) {
                        String username = (String) tableModel.getValueAt(modelRow, col);
                        ClipboardUtils.copyToClipboardWithTimeout(username, 30);
                        JOptionPane.showMessageDialog(DashboardPanel.this,
                                "Identifiant copié !",
                                "Presse-papier", JOptionPane.INFORMATION_MESSAGE);
                    }
                    // Clic sur la colonne Mot de passe (index 2)
                    else if (col == 2) {
                        String password = (String) tableModel.getValueAt(modelRow, col);
                        ClipboardUtils.copyToClipboardWithTimeout(password, 30);
                        JOptionPane.showMessageDialog(DashboardPanel.this,
                                "Mot de passe copié ! (Effacement automatique dans 30s)",
                                "Presse-papier", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });

        // --- SEARCH LOGIC ---
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String text = searchField.getText();
                if (text.trim().length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 0));
                }
            }
        });

        // Navigating to the add form
        btnAdd.addActionListener(e -> mainFrame.showFormScreen());

        // --- DELETE LOGIC ---
        btnDelete.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) { // Correction de la vérification de sélection
                int modelRow = table.convertRowIndexToModel(selectedRow);

                // 1. Deleting from View
                tableModel.removeRow(modelRow);

                // 2. Delete from Data List
                if (currentDatabase != null && modelRow < currentDatabase.size()) {
                    currentDatabase.remove(modelRow);
                }

                // 3. Backup the vault
                mainFrame.saveVault();

                JOptionPane.showMessageDialog(this, "Mot de passe supprimé avec succès !");
            } else {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner une ligne à supprimer.", "Erreur", JOptionPane.WARNING_MESSAGE);
            }
        });

        // --- EDIT LOGIC (POP-UP) ---
        btnEdit.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int modelRow = table.convertRowIndexToModel(selectedRow);

                String currentSite = (String) tableModel.getValueAt(modelRow, 0);
                String currentLogin = (String) tableModel.getValueAt(modelRow, 1);
                String currentPassword = (String) tableModel.getValueAt(modelRow, 2);

                JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
                UpdateDialog dialog = new UpdateDialog(topFrame, currentSite, currentLogin, currentPassword);
                dialog.setVisible(true);

                if (dialog.isValidated()) {
                    tableModel.setValueAt(dialog.getLogin(), modelRow, 1);
                    tableModel.setValueAt(dialog.getPassword(), modelRow, 2);

                    if (currentDatabase != null && modelRow < currentDatabase.size()) {
                        currentDatabase.set(modelRow, new PasswordEntry(currentSite, dialog.getLogin(), dialog.getPassword()));
                    }

                    mainFrame.saveVault();
                    JOptionPane.showMessageDialog(this, "Mot de passe modifié avec succès !");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner une ligne à modifier.", "Erreur", JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    // Adjust the width of columns from the Options tab
    public void updateColumnWidths(int webWidth, int userWidth) {
        if (table != null && table.getColumnModel().getColumnCount() >= 2) {
            table.getColumnModel().getColumn(0).setPreferredWidth(webWidth);
            table.getColumnModel().getColumn(1).setPreferredWidth(userWidth);
        }
    }

    // Table filling from the decrypted base
    public void loadDataIntoTable(ArrayList<PasswordEntry> database) {
        this.currentDatabase = database;
        tableModel.setRowCount(0);

        if (database != null) {
            for (PasswordEntry entry : database) {
                tableModel.addRow(new Object[]{entry.getWebsite(), entry.getUsername(), entry.getPassword()});
            }
        }
    }
}