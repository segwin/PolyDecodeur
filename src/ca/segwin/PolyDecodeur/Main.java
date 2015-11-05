package ca.segwin.PolyDecodeur;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        final Menu menuAide = new Menu("Aide");

        final MenuItem about = new MenuItem("À propos de PolyDécodeur");
        menuAide.getItems().add(about);

        final MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(menuAide);

        Controller.stage = primaryStage;

        Parent root = FXMLLoader.load(getClass().getResource("PolyDecodeur.fxml"));
        primaryStage.setTitle("PolyDécodeur");

        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
