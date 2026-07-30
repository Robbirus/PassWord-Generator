package controllers;

import app.AppContext;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import model.AppSettings;

public class SettingsController implements BaseController {

    @FXML private CheckBox showPasswordsCheck;
    @FXML private CheckBox darkModeCheck;
    @FXML private Spinner<Integer> clipboardSpinner;
    @FXML private Spinner<Integer> autoLockSpinner;

    private AppContext ctx;

    @FXML
    private void initialize() {
        showPasswordsCheck.setSelected(AppSettings.isShowPasswordsByDefault());
        darkModeCheck.setSelected(AppSettings.isDarkMode());

        clipboardSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 300, AppSettings.getClipboardClearDelay(), 5));
        autoLockSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 60, AppSettings.getAutoLockDelay(), 1));
    }

    @Override
    public void setAppContext(AppContext ctx) {
        this.ctx = ctx;
    }

    @FXML
    private void handleSave() {
        AppSettings.setShowPasswordsByDefault(showPasswordsCheck.isSelected());
        AppSettings.setDarkMode(darkModeCheck.isSelected());
        AppSettings.setClipboardClearDelay(clipboardSpinner.getValue());
        AppSettings.setAutoLockDelay(autoLockSpinner.getValue());

        ctx.applyTheme();
        ctx.refreshDashboard();
        ctx.resetInactivityTimer();
        ctx.showInfo("Succès", "Paramètres sauvegardés avec succès !");
    }
}
