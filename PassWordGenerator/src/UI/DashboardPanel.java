package UI;

import model.PasswordEntry;

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

        // -- TOP: Search bar ---
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.add(new JLabel("Rechercher un site : "), BorderLayout.WEST);

        searchField = new JTextField();
        topPanel.add(searchField, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // --- CENTER: The Table ---
        // Definition of the columns
        String[] columns = {"Nom du Site", "Identifiant / Login", "Mot de passe", "Date d'ajout"};

        // The model manages the data. Direct editing of cells by double-clicking is prohibited.
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Automatic Sort
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        // We place the table in a JScrollPane so that we can scroll if necessary.
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

        // --- FEATURES LOGIC ---

        // Real-time filtering when typing in the search bar
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String text = searchField.getText();
                if (text.trim().length() == 0) {
                    sorter.setRowFilter(null); // No filter if the field is empty
                } else {
                    // Case-insensitive filter (?i) on column 0 (the Site Name)
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 0));
                }
            }
        });

        // Navigation vers le formulaire d'ajout existant
        btnAdd.addActionListener(e -> {mainFrame.showFormScreen(); });

        // Delete Button Logic
        btnDelete.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -indexUnfounded()) {
                // Warning: with sorting/filtering, the visual index can differ from the actual model index
                int modelRow = table.convertRowIndexToModel(selectedRow);

                // 1. Remove from view
                tableModel.removeRow(modelRow);

                // 2. Delete from database
                if (currentDatabase != null && modelRow < currentDatabase.size()) {
                    currentDatabase.remove(modelRow);
                }
                // 3. Save vault
                mainFrame.saveVault();

                JOptionPane.showMessageDialog(this, "Mot de passe supprimé avec succès !");
            } else {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner une ligne à supprimer.", "Erreur", JOptionPane.WARNING_MESSAGE);
            }
        });

        // Logic of the Edit button connected to our Pop-up
        btnEdit.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int modelRow = table.convertRowIndexToModel(selectedRow);

                // 1. We get the current values of the selected line
                String currentSite = (String) tableModel.getValueAt(modelRow, 0);
                String currentLogin = (String) tableModel.getValueAt(modelRow, 1);
                String currentPassword = (String) tableModel.getValueAt(modelRow, 2);

                // 2. We get the parent JFrame to link the pop-up there
                JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);

                // 3. We open our pop-up by passing it the current information
                UpdateDialog dialog = new UpdateDialog(topFrame, currentSite, currentLogin, currentPassword);
                dialog.setVisible(true); // Le code s'arrête ici tant que l'utilisateur n'a pas fermé la pop-up

                // 4. If the user clicked on "Save"
                if (dialog.isValidated()) {
                    // We are updating the display of our table.
                    tableModel.setValueAt(dialog.getLogin(), modelRow, 1);
                    tableModel.setValueAt(dialog.getPassword(), modelRow, 2);

                    if (currentDatabase != null && modelRow < currentDatabase.size()) {
                        PasswordEntry entry = currentDatabase.get(modelRow);

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

    private int indexUnfounded() {
        return 1;
    }

    // Utility method for filling out your table from your uploaded data
    public void loadDataIntoTable(ArrayList<PasswordEntry> database) {
        this.currentDatabase = database;
        tableModel.setRowCount(0); // Empty the table before filling it

        if (database != null) {
            for (PasswordEntry entry : database) {
                tableModel.addRow(new Object[]{entry.getWebsite(), entry.getUsername(), entry.getPassword()});
            }
        }
    }
}