package controllers;

import app.AppContext;
import crypto.RecoveryKeyManager;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController implements BaseController {

    @FXML private TextField userInput;
    @FXML private PasswordField passInput;
    @FXML private PasswordField confirmInput;

    private AppContext ctx;

    @Override
    public void setAppContext(AppContext ctx) {
        this.ctx = ctx;
    }

    @FXML
    private void handleRegister() {
        String login = userInput.getText().trim();
        String pass = passInput.getText();
        String confirm = confirmInput.getText();

        if (login.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
            ctx.showWarning("Veuillez remplir tous les champs.");
            return;
        }

        if (!pass.equals(confirm)) {
            ctx.showError("Les mots de passe ne correspondent pas.");
            return;
        }

        String key = RecoveryKeyManager.generateKey();
        boolean saved = ctx.promptSaveRecoveryKey(login, key);

        if (saved) {
            ctx.setCurrentLogin(login);
            ctx.setCurrentMasterPassword(pass);
            ctx.setCurrentRecoveryKey(key);
            ctx.getDatabase().clear();

            ctx.saveVault();
            ctx.showMainView();
        } else {
            ctx.showWarning("La sauvegarde de la clé de secours est obligatoire pour continuer.");
        }
    }
}
