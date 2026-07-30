package controllers;

import app.AppContext;
import crypto.ClipboardUtils;
import crypto.PasswordGenerator;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import model.AppSettings;
import model.PasswordEntry;

import java.util.ArrayList;
import java.util.Optional;

public class DashboardController implements BaseController {

    private static final String[] CATEGORIES =
            {"Toutes", "Général", "Réseaux Sociaux", "Travail", "Banque", "Achats", "Personnel"};
    private static final String[] FORM_CATEGORIES =
            {"Général", "Réseaux Sociaux", "Travail", "Banque", "Achats", "Personnel"};

    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilterCombo;
    @FXML private CheckBox favoritesFilterCheck;

    @FXML private TableView<PasswordEntry> table;
    @FXML private TableColumn<PasswordEntry, PasswordEntry> favColumn;
    @FXML private TableColumn<PasswordEntry, PasswordEntry> logoColumn;
    @FXML private TableColumn<PasswordEntry, String> siteColumn;
    @FXML private TableColumn<PasswordEntry, String> userColumn;
    @FXML private TableColumn<PasswordEntry, String> passColumn;
    @FXML private TableColumn<PasswordEntry, String> categoryColumn;
    @FXML private TableColumn<PasswordEntry, String> tagsColumn;
    @FXML private TableColumn<PasswordEntry, String> dateColumn;

    private AppContext ctx;
    private ArrayList<PasswordEntry> fullDatabase = new ArrayList<>();
    private final ObservableList<PasswordEntry> displayed = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        categoryFilterCombo.setItems(FXCollections.observableArrayList(CATEGORIES));
        categoryFilterCombo.getSelectionModel().selectFirst();

        table.setItems(displayed);
        table.setPlaceholder(new Label("Aucune entrée à afficher."));

        favColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
        favColumn.setCellFactory(col -> new TableCell<>() {
            private final ImageView starView = new ImageView();
            {
                starView.setFitWidth(16);
                starView.setFitHeight(16);
                setStyle("-fx-alignment: CENTER; -fx-cursor: hand;");
                setOnMouseClicked(e -> {
                    PasswordEntry entry = getItem();
                    if (entry != null) {
                        entry.setFavorite(!entry.isFavorite());
                        ctx.saveVault();
                        ctx.refreshDashboard();
                    }
                });
            }

            @Override
            protected void updateItem(PasswordEntry entry, boolean empty) {
                super.updateItem(entry, empty);
                if (empty || entry == null) {
                    setGraphic(null);
                } else {
                    String iconPath = entry.isFavorite() ? "/icons/star.png" : "/icons/star_outline.png";
                    starView.setImage(new Image(getClass().getResourceAsStream(iconPath), 16, 16, true, true));
                    setGraphic(starView);
                }
            }
        });

        logoColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
        logoColumn.setCellFactory(col -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            @Override
            protected void updateItem(PasswordEntry entry, boolean empty) {
                super.updateItem(entry, empty);
                if (empty || entry == null) {
                    setGraphic(null);
                } else {
                    imageView.setFitWidth(16);
                    imageView.setFitHeight(16);
                    Image favicon = entry.fetchFaviconImage();
                    imageView.setImage(favicon);
                    setGraphic(imageView);
                }
            }
        });

        siteColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getWebsite()));
        userColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));
        passColumn.setCellValueFactory(data -> {
            boolean show = AppSettings.isShowPasswordsByDefault();
            String pwd = show ? data.getValue().getPassword() : "••••••••";
            return new SimpleStringProperty(pwd);
        });
        categoryColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory()));
        tagsColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTags()));
        dateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDateAdded()));

        searchField.textProperty().addListener((obs, old, val) -> applyFiltersAndRefresh());
        categoryFilterCombo.valueProperty().addListener((obs, old, val) -> applyFiltersAndRefresh());
        favoritesFilterCheck.selectedProperty().addListener((obs, old, val) -> applyFiltersAndRefresh());
    }

    @Override
    public void setAppContext(AppContext ctx) {
        this.ctx = ctx;
        ctx.registerDashboard(this);
    }

    public void loadDataIntoTable(ArrayList<PasswordEntry> database) {
        this.fullDatabase = database;
        applyFiltersAndRefresh();
    }

    private void applyFiltersAndRefresh() {
        displayed.clear();
        if (fullDatabase == null) return;

        String query = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        String selectedCategory = categoryFilterCombo.getValue();
        boolean favoritesOnly = favoritesFilterCheck.isSelected();

        for (PasswordEntry entry : fullDatabase) {
            if (favoritesOnly && !entry.isFavorite()) continue;

            if (selectedCategory != null && !selectedCategory.equals("Toutes")
                    && !entry.getCategory().equalsIgnoreCase(selectedCategory)) continue;

            if (!query.isEmpty()) {
                boolean matchSite = entry.getWebsite() != null && entry.getWebsite().toLowerCase().contains(query);
                boolean matchUser = entry.getUsername() != null && entry.getUsername().toLowerCase().contains(query);
                boolean matchTags = entry.getTags() != null && entry.getTags().toLowerCase().contains(query);
                if (!matchSite && !matchUser && !matchTags) continue;
            }

            displayed.add(entry);
        }
    }

    @FXML
    private void handleCopy() {
        PasswordEntry entry = table.getSelectionModel().getSelectedItem();
        if (entry != null) {
            int delay = AppSettings.getClipboardClearDelay();
            ClipboardUtils.copyAndAutoClear(entry.getPassword(), delay);
            ctx.showInfo("Copié", "Mot de passe copié ! (Effacé du presse-papier dans " + delay + "s)");
        } else {
            ctx.showWarning("Veuillez sélectionner une ligne.");
        }
    }

    @FXML
    private void handleEdit() {
        PasswordEntry entry = table.getSelectionModel().getSelectedItem();
        if (entry == null) {
            ctx.showWarning("Veuillez sélectionner une ligne à modifier.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(ctx.getPrimaryStage());
        dialog.setTitle("Modifier le mot de passe");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField siteInput = new TextField(entry.getWebsite());
        TextField userInput = new TextField(entry.getUsername());
        TextField passInput = new TextField(entry.getPassword());

        Button btnGenerate = new Button("Générer");
        btnGenerate.setOnAction(e -> {
            PasswordGenerator generator = new PasswordGenerator();
            passInput.setText(generator.generatePassword(16, true, true, true, true));
        });
        HBox passRow = new HBox(5, passInput, btnGenerate);
        HBox.setHgrow(passInput, Priority.ALWAYS);

        ComboBox<String> categoryInput = new ComboBox<>(FXCollections.observableArrayList(FORM_CATEGORIES));
        categoryInput.setValue(entry.getCategory());

        TextField tagsInput = new TextField(entry.getTags());
        CheckBox favoriteInput = new CheckBox("Marquer comme favori");
        favoriteInput.setSelected(entry.isFavorite());

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        int r = 0;
        form.addRow(r++, new Label("Site Web / Application :"), siteInput);
        form.addRow(r++, new Label("Identifiant / Email :"), userInput);
        form.addRow(r++, new Label("Mot de Passe :"), passRow);
        form.addRow(r++, new Label("Catégorie :"), categoryInput);
        form.addRow(r++, new Label("Tags (séparés par des virgules) :"), tagsInput);
        form.addRow(r, favoriteInput);

        dialog.getDialogPane().setContent(form);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            entry.setWebsite(siteInput.getText().trim());
            entry.setUsername(userInput.getText().trim());
            entry.setPassword(passInput.getText().trim());
            entry.setCategory(categoryInput.getValue());
            entry.setTags(tagsInput.getText().trim());
            entry.setFavorite(favoriteInput.isSelected());

            ctx.saveVault();
            ctx.refreshDashboard();
            ctx.showInfo("Succès", "Entrée modifiée avec succès !");
        }
    }

    @FXML
    private void handleDelete() {
        PasswordEntry entry = table.getSelectionModel().getSelectedItem();
        if (entry == null) {
            ctx.showWarning("Veuillez sélectionner une ligne à supprimer.");
            return;
        }

        boolean confirm = ctx.showConfirm("Confirmation de suppression",
                "Êtes-vous sûr de vouloir supprimer \"" + entry.getWebsite() + "\" ?");
        if (confirm) {
            ctx.getDatabase().remove(entry);
            ctx.saveVault();
            ctx.refreshDashboard();
            ctx.showInfo("Succès", "Mot de passe supprimé !");
        }
    }
}