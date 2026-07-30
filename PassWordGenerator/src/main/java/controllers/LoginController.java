package controllers;

import app.AppContext;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController implements BaseController {

    @FXML private TextField userInput;
    @FXML private PasswordField passInput;

    private AppContext ctx;

    @Override
    public void setAppContext(AppContext ctx) {
        this.ctx = ctx;
    }

    @FXML
    private void handleLogin() {
        String login = userInput.getText().trim();
        String pass = passInput.getText();

        if (login.isEmpty() || pass.isEmpty()) {
            ctx.showWarning("Veuillez remplir tous les champs.");
            return;
        }

        if (ctx.loadVault(login, pass)) {
            ctx.setCurrentLogin(login);
            ctx.setCurrentMasterPassword(pass);
            ctx.showMainView();
        } else {
            ctx.showError("Identifiant ou mot de passe maître incorrect !");
        }
    }

    @FXML
    private void handleRecovery() {
        try {
            Parent root = ctx.loadAndInject("/fxml/RecoveryDialog.fxml");
            Stage dialog = new Stage();
            dialog.initOwner(ctx.getPrimaryStage());
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setTitle("Récupération par Clé de Secours");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
