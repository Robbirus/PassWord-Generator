package controllers;

import app.AppContext;
import crypto.PasswordGenerator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import model.PasswordEntry;

public class AddEntryController implements BaseController {

    private static final String[] CATEGORIES =
            {"Général", "Réseaux Sociaux", "Travail", "Banque", "Achats", "Personnel"};

    @FXML private TextField siteField;
    @FXML private TextField userField;
    @FXML private TextField passField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextField tagsField;
    @FXML private CheckBox favoriteCheck;

    private final PasswordGenerator passwordGenerator = new PasswordGenerator();
    private AppContext ctx;

    @FXML
    private void initialize() {
        categoryCombo.setItems(FXCollections.observableArrayList(CATEGORIES));
        categoryCombo.getSelectionModel().selectFirst();
    }

    @Override
    public void setAppContext(AppContext ctx) {
        this.ctx = ctx;
    }

    @FXML
    private void handleGenerate() {
        String generatedPass = passwordGenerator.generatePassword(16, true, true, true, true);
        passField.setText(generatedPass);
    }

    @FXML
    private void handleAdd() {
        String site = siteField.getText().trim();
        String user = userField.getText().trim();
        String pass = passField.getText().trim();
        String category = categoryCombo.getValue();
        String tags = tagsField.getText().trim();
        boolean isFavorite = favoriteCheck.isSelected();

        if (site.isEmpty() || user.isEmpty() || pass.isEmpty()) {
            ctx.showError("Veuillez remplir au moins le site, l'identifiant et le mot de passe.");
            return;
        }

        String currentDate = java.time.LocalDate.now().toString();
        PasswordEntry newEntry = new PasswordEntry(site, user, pass, currentDate, category, tags, isFavorite);

        ctx.getDatabase().add(newEntry);
        ctx.saveVault();
        ctx.refreshDashboard();

        siteField.clear();
        userField.clear();
        passField.clear();
        tagsField.clear();
        categoryCombo.getSelectionModel().selectFirst();
        favoriteCheck.setSelected(false);

        ctx.showInfo("Succès", "Mot de passe ajouté avec succès !");
    }
}
