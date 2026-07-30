package controllers;

import app.AppContext;
import crypto.CsvService;
import javafx.fxml.FXML;
import javafx.stage.FileChooser;
import model.PasswordEntry;

import java.io.File;
import java.util.ArrayList;

public class DataManagementController implements BaseController {

    private AppContext ctx;

    @Override
    public void setAppContext(AppContext ctx) {
        this.ctx = ctx;
    }

    @FXML
    private void handleImport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un fichier CSV à importer");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers CSV (*.csv)", "*.csv"));

        File file = fileChooser.showOpenDialog(ctx.getPrimaryStage());
        if (file != null) {
            try {
                ArrayList<PasswordEntry> imported = CsvService.importFromCsv(file);
                if (imported.isEmpty()) {
                    ctx.showWarning("Aucune entrée valide n'a été trouvée dans ce fichier.");
                    return;
                }

                boolean confirm = ctx.showConfirm("Confirmer l'importation",
                        "Détection de " + imported.size() + " mot(s) de passe.\nVoulez-vous les ajouter à votre coffre-fort ?");

                if (confirm) {
                    ctx.getDatabase().addAll(imported);
                    ctx.saveVault();
                    ctx.refreshDashboard();
                    ctx.showInfo("Succès", imported.size() + " entrée(s) importée(s) avec succès !");
                }
            } catch (Exception ex) {
                ctx.showError("Erreur lors de l'importation : " + ex.getMessage());
            }
        }
    }

    @FXML
    private void handleExport() {
        boolean confirm = ctx.showConfirm("Avertissement de Sécurité",
                "ATTENTION : Le fichier CSV généré contiendra vos mots de passe EN CLAIR.\n" +
                        "Assurez-vous de le stocker en lieu sûr puis de le supprimer après usage.\n\nContinuer ?");

        if (confirm) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le fichier d'export CSV");
            fileChooser.setInitialFileName("vault_export.csv");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers CSV (*.csv)", "*.csv"));

            File file = fileChooser.showSaveDialog(ctx.getPrimaryStage());
            if (file != null) {
                if (!file.getName().endsWith(".csv")) {
                    file = new File(file.getAbsolutePath() + ".csv");
                }
                try {
                    CsvService.exportToCsv(file, ctx.getDatabase());
                    ctx.showInfo("Succès", "Exportation réussie dans :\n" + file.getAbsolutePath());
                } catch (Exception ex) {
                    ctx.showError("Erreur lors de l'exportation : " + ex.getMessage());
                }
            }
        }
    }
}
