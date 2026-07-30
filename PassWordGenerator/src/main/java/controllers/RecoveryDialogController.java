package controllers;

import app.AppContext;
import crypto.RecoveryKeyManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class RecoveryDialogController implements BaseController {

    @FXML private TextField txtLogin;
    @FXML private TextField txtKey;

    private AppContext ctx;

    @Override
    public void setAppContext(AppContext ctx) {
        this.ctx = ctx;
    }

    @FXML
    private void handleImportKeyFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Charger le fichier de clé de secours");
        File file = fileChooser.showOpenDialog(currentStage());
        if (file != null) {
            String extractedKey = RecoveryKeyManager.readKeyFromFile(file);
            if (extractedKey != null) {
                txtKey.setText(extractedKey);
            } else {
                ctx.showError("Impossible de lire la clé depuis ce fichier.");
            }
        }
    }

    @FXML
    private void handleValidate() {
        String login = txtLogin.getText().trim();
        String key = txtKey.getText().trim();

        if (login.isEmpty() || key.isEmpty()) {
            ctx.showWarning("Veuillez remplir tous les champs.");
            return;
        }

        if (ctx.loadVaultWithKey(login, key)) {
            currentStage().close();
            // On diffère l'ouverture de la 2e fenêtre modale après la fermeture
            // complète de celle-ci, sinon la nouvelle fenêtre s'affiche mais
            // ne reçoit jamais le focus clavier (bug JavaFX connu lors d'un
            // enchaînement synchrone close() + showAndWait() sur des Stages
            // modales, notamment sous Linux).
            Platform.runLater(() -> openResetPasswordDialog(login));
        } else {
            ctx.showError("Identifiant ou clé de secours invalide !");
        }
    }

    private void openResetPasswordDialog(String login) {
        try {
            // 1. Create a new instance of FXMLLoader
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/ResetPassword.fxml"));

            // 2. Load the FXML component
            Parent root = loader.load();

            // 3. Inject the controller
            ResetPasswordController controller = loader.getController();
            controller.setAppContext(ctx);
            controller.setLogin(login);

            // 4. Create the new scene by passing explicit dimensions to it (e.g. 400x250)
            Scene scene = new Scene(root, 400, 250);

            // 5. Instantiate and display the modal window
            Stage dialog = new Stage();
            dialog.initOwner(ctx.getPrimaryStage());
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setTitle("Nouveau mot de passe maître");
            dialog.setScene(scene);

            // Display the window
            dialog.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Stage currentStage() {
        return (Stage) txtLogin.getScene().getWindow();
    }
}
