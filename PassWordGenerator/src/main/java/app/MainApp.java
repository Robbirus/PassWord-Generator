package app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Mon Gestionnaire de Mots de Passe");

        AppContext ctx = new AppContext(primaryStage);

        // Temporary Root; replaced immediately by the authentication screen
        Scene scene = new Scene(new StackPane(), 1000, 600);
        primaryStage.setScene(scene);

        // App icon
        Image appIcon = new Image(getClass().getResourceAsStream("/icons/app_icon.png"));
        primaryStage.getIcons().add(appIcon);

        ctx.attachScene(scene);
        ctx.showAuthView();

        primaryStage.setOnCloseRequest(e -> Platform.exit());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
