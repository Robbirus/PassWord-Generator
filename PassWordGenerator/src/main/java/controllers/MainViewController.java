package controllers;

import app.AppContext;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;

/**
 * Remplace la JTabbedPane de PasswordManagerGUI : assemble les 6 onglets
 * de l'application dans un TabPane, en injectant le contexte partagé dans
 * chaque contrôleur enfant.
 */
public class MainViewController implements BaseController {

    @FXML private TabPane tabPane;

    private AppContext ctx;

    @Override
    public void setAppContext(AppContext ctx) {
        this.ctx = ctx;
        buildTabs();
    }

    private void buildTabs() {
        tabPane.getTabs().clear();

        addTab("Mon Coffre-fort", "/icons/vault.png", "/fxml/Dashboard.fxml");
        addTab("Ajouter", "/icons/add.png", "/fxml/AddEntry.fxml");
        Tab auditTab = addTab("Audit Sécurité", "/icons/shield.png", "/fxml/Audit.fxml");
        addTab("Générateur", "/icons/generator.png", "/fxml/Generator.fxml");
        addTab("Outils & Data", "/icons/data.png", "/fxml/DataManagement.fxml");
        addTab("Options", "/icons/settings.png", "/fxml/Settings.fxml");

        // Comme dans la version Swing : re-analyser le coffre quand on ouvre l'onglet Audit
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == auditTab) {
                ctx.refreshDashboard();
            }
        });
    }

    private Tab addTab(String title, String iconPath, String fxml) {
        try {
            Parent root = ctx.loadAndInject(fxml);

            Tab tab = new Tab(title, root);
            tab.setClosable(false);

            Image icon = new Image(getClass().getResourceAsStream(iconPath), 20, 20, true, true);
            tab.setGraphic(new ImageView(icon));

            tabPane.getTabs().add(tab);
            return tab;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
