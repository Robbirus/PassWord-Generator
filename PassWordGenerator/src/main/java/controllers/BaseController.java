package controllers;

import app.AppContext;

/**
 * Implémentée par tout contrôleur FXML qui a besoin d'accéder à l'état
 * partagé de l'application (base de données, session, sauvegarde du coffre...).
 * Remplace l'ancienne référence "mainFrame" (PasswordManagerGUI) passée
 * manuellement à chaque panel Swing.
 */
public interface BaseController {
    void setAppContext(AppContext ctx);
}
