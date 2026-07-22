package UI;

import crypto.PasswordGenerator;
import model.AppSettings;
import model.PasswordEntry;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;

public class DashboardPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private PasswordManagerGUI mainFrame;

    private JTextField searchField;
    private JComboBox<String> categoryFilterCombo;
    private JCheckBox favoritesFilterCheck;

    private final String[] CATEGORIES = {"Toutes", "Général", "Réseaux Sociaux", "Travail", "Banque", "Achats", "Personnel"};

    public DashboardPanel(PasswordManagerGUI mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // --- HEADER ---
        JLabel titleLabel = new JLabel("Mon Coffre-fort de Mots de Passe", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 20));

        // --- Search bar and filters ---
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        // Search field
        filterPanel.add(new JLabel("🔍 Rechercher :"));
        searchField = new JTextField(15);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFiltersAndRefresh(); }
            public void removeUpdate(DocumentEvent e) { applyFiltersAndRefresh(); }
            public void changedUpdate(DocumentEvent e) { applyFiltersAndRefresh(); }
        });
        filterPanel.add(searchField);

        // Category
        filterPanel.add(new JLabel("Catégorie :"));
        categoryFilterCombo = new JComboBox<>(CATEGORIES);
        categoryFilterCombo.addActionListener(e -> applyFiltersAndRefresh());
        filterPanel.add(categoryFilterCombo);

        // Favorites
        favoritesFilterCheck = new JCheckBox("★ Favoris uniquement");
        favoritesFilterCheck.addActionListener(e -> applyFiltersAndRefresh());
        filterPanel.add(favoritesFilterCheck);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(titleLabel, BorderLayout.NORTH);
        topContainer.add(filterPanel, BorderLayout.SOUTH);
        add(topContainer, BorderLayout.NORTH);

        // --- TABLE ---
        String[] columns = {"★", "Logo", "Site Web", "Identifiant", "Mot de Passe", "Catégorie", "Tags", "Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 1) return ImageIcon.class; // Support de l'image Favicon
                return String.class;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(32);
        table.setFont(new Font("Dialog", Font.PLAIN, 13));
        table.setAutoCreateRowSorter(true);

        // Column Sizing
        table.getColumnModel().getColumn(0).setPreferredWidth(30);  // ★
        table.getColumnModel().getColumn(1).setPreferredWidth(40);  // Logo
        table.getColumnModel().getColumn(2).setPreferredWidth(140); // Site
        table.getColumnModel().getColumn(3).setPreferredWidth(120); // User
        table.getColumnModel().getColumn(4).setPreferredWidth(100); // Pass
        table.getColumnModel().getColumn(5).setPreferredWidth(100); // Catégorie
        table.getColumnModel().getColumn(6).setPreferredWidth(100); // Tags

        // Centering of the favorite star
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // --- ACTION BUTTONS ---
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        JButton btnCopy = new JButton("Copier");
        JButton btnEdit = new JButton("✏️ Éditer");
        JButton btnDelete = new JButton("Supprimer");
        btnDelete.setForeground(new Color(220, 53, 69));

        // COPY ACTION
        btnCopy.addActionListener(e -> {
            PasswordEntry entry = getSelectedEntry();
            if (entry != null) {
                StringSelection selection = new StringSelection(entry.getPassword());
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
                JOptionPane.showMessageDialog(this, "Mot de passe copié dans le presse-papier !");
            } else {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner une ligne.", "Attention", JOptionPane.WARNING_MESSAGE);
            }
        });

        // EDIT ACTION
        btnEdit.addActionListener(e -> {
            PasswordEntry entry = getSelectedEntry();
            if (entry != null) {
                JTextField siteInput = new JTextField(entry.getWebsite(), 20);
                JTextField userInput = new JTextField(entry.getUsername(), 20);
                JTextField passInput = new JTextField(entry.getPassword(), 20);

                String[] formCategories = {"Général", "Réseaux Sociaux", "Travail", "Banque", "Achats", "Personnel"};
                JComboBox<String> categoryInput = new JComboBox<>(formCategories);
                categoryInput.setSelectedItem(entry.getCategory());

                JTextField tagsInput = new JTextField(entry.getTags(), 20);
                JCheckBox favoriteInput = new JCheckBox("Marquer comme favori", entry.isFavorite());

                JButton btnGenerate = new JButton("🎲 Générer");
                btnGenerate.addActionListener(genEvent -> {
                    PasswordGenerator generator = new PasswordGenerator();
                    String newPass = generator.generatePassword(16, true, true, true, true);
                    passInput.setText(newPass);
                });

                JPanel passPanel = new JPanel(new BorderLayout(5, 0));
                passPanel.add(passInput, BorderLayout.CENTER);
                passPanel.add(btnGenerate, BorderLayout.EAST);

                JPanel editForm = new JPanel(new GridLayout(0, 1, 5, 5));
                editForm.add(new JLabel("Site Web / Application :"));
                editForm.add(siteInput);
                editForm.add(new JLabel("Identifiant / Email :"));
                editForm.add(userInput);
                editForm.add(new JLabel("Mot de Passe :"));
                editForm.add(passPanel);
                editForm.add(new JLabel("Catégorie :"));
                editForm.add(categoryInput);
                editForm.add(new JLabel("Tags (séparés par des virgules) :"));
                editForm.add(tagsInput);
                editForm.add(favoriteInput);

                int result = JOptionPane.showConfirmDialog(
                        this, editForm, "Modifier le mot de passe",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
                );

                if (result == JOptionPane.OK_OPTION) {
                    entry.setWebsite(siteInput.getText().trim());
                    entry.setUsername(userInput.getText().trim());
                    entry.setPassword(passInput.getText().trim());
                    entry.setCategory((String) categoryInput.getSelectedItem());
                    entry.setTags(tagsInput.getText().trim());
                    entry.setFavorite(favoriteInput.isSelected());

                    mainFrame.saveVault();
                    mainFrame.refreshDashboardDisplay();
                    JOptionPane.showMessageDialog(this, "Entrée modifiée avec succès !");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner une ligne à modifier.", "Attention", JOptionPane.WARNING_MESSAGE);
            }
        });

        // DELETE ACTION
        btnDelete.addActionListener(e -> {
            PasswordEntry entry = getSelectedEntry();

            if (entry != null) {
                int confirm = JOptionPane.showConfirmDialog(
                        this,
                        "Êtes-vous sûr de vouloir supprimer \"" + entry.getWebsite() + "\" ?",
                        "Confirmation de suppression",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    mainFrame.getFakeDatabase().remove(entry);
                    mainFrame.saveVault();
                    mainFrame.refreshDashboardDisplay();
                    JOptionPane.showMessageDialog(this, "Mot de passe supprimé !");
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

    private PasswordEntry getSelectedEntry() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int modelRow = table.convertRowIndexToModel(selectedRow);
            String website = (String) tableModel.getValueAt(modelRow, 2);
            String username = (String) tableModel.getValueAt(modelRow, 3);

            for (PasswordEntry entry : mainFrame.getFakeDatabase()) {
                if (entry.getWebsite().equals(website) && entry.getUsername().equals(username)) {
                    return entry;
                }
            }
        }
        return null;
    }

    private void applyFiltersAndRefresh() {
        loadDataIntoTable(mainFrame.getFakeDatabase());
    }

    public void loadDataIntoTable(ArrayList<PasswordEntry> database) {
        tableModel.setRowCount(0);
        boolean showPass = AppSettings.isShowPasswordsByDefault();

        if (database == null) return;

        String query = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        String selectedCategory = (String) categoryFilterCombo.getSelectedItem();
        boolean favoritesOnly = favoritesFilterCheck.isSelected();

        SwingUtilities.invokeLater(() -> {
            for (PasswordEntry entry : database) {
                // 1. Filter by favorites
                if (favoritesOnly && !entry.isFavorite()) continue;

                // 2. Filter by category
                if (selectedCategory != null && !selectedCategory.equals("Toutes")
                        && !entry.getCategory().equalsIgnoreCase(selectedCategory)) continue;

                // 3. Filter by text (search in site name, identifier and tags)
                if (!query.isEmpty()) {
                    boolean matchSite = entry.getWebsite() != null && entry.getWebsite().toLowerCase().contains(query);
                    boolean matchUser = entry.getUsername() != null && entry.getUsername().toLowerCase().contains(query);
                    boolean matchTags = entry.getTags() != null && entry.getTags().toLowerCase().contains(query);

                    if (!matchSite && !matchUser && !matchTags) {
                        continue;
                    }
                }

                String star = entry.isFavorite() ? "★" : "☆";
                String displayedPassword = showPass ? entry.getPassword() : "••••••••";
                ImageIcon favicon = entry.FetchFavicon();

                tableModel.addRow(new Object[]{
                        star,
                        favicon,
                        entry.getWebsite(),
                        entry.getUsername(),
                        displayedPassword,
                        entry.getCategory(),
                        entry.getTags(),
                        entry.getDateAdded()
                });
            }
        });
    }
}