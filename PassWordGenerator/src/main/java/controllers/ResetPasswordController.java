package controllers;

import app.AppContext;
import crypto.RecoveryKeyManager;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

public class ResetPasswordController implements BaseController {

    @FXML private PasswordField newPassField;
    @FXML private PasswordField confirmPassField;

    private AppContext ctx;
    private String login;

    @Override
    public void setAppContext(AppContext ctx) {
        this.ctx = ctx;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    @FXML
    private void handleSave() {
        String newPass = newPassField.getText();
        String confirmPass = confirmPassField.getText();

        if (!newPass.isEmpty() && newPass.equals(confirmPass)) {
            String newKey = RecoveryKeyManager.generateKey();
            if (ctx.promptSaveRecoveryKey(login, newKey)) {
                ctx.setCurrentLogin(login);
                ctx.setCurrentMasterPassword(newPass);
                ctx.setCurrentRecoveryKey(newKey);

                ctx.saveVault();
                // 1. Retrieve and close the window first
                Stage stage = (Stage) newPassField.getScene().getWindow();
                stage.close();

                // 2. Show the main view after closing the dialog
                ctx.showMainView();
                ctx.showInfo("Succès", "Mot de passe maître réinitialisé avec succès !");
            }
        } else {
            ctx.showError("Les mots de passe ne correspondent pas ou sont vides.");
        }
    }
}
