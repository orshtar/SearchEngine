package View;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application{

    public static Stage primStage;

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxmlLoader=new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("View.fxml").openStream());
        primaryStage.setTitle("Search Engine");
        Scene scene = new Scene(root, 800, 400);
        primaryStage.setScene(scene);
        primStage=primaryStage;
        primaryStage.show();
    }

    public static void main(String[] args) {

        launch(args);
    }
}
