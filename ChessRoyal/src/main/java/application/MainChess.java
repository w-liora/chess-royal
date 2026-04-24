package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Lance directement le plateau d'échecs (sans passer par l'écran TCP).
 * Utile pour tester l'UI indépendamment du réseau.
 *
 * Pour l'intégrer plus tard au flux réseau existant :
 *   → remplacer ce Main par le chargement de PlateauView.fxml
 *     depuis ConnexionCtrl après réception du message INIT.
 */
public class MainChess extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/PlateauView.fxml"));
            Scene scene = new Scene(root);

            primaryStage.setTitle("Chess Royal – Plateau");
            primaryStage.setScene(scene);
            primaryStage.sizeToScene();
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}