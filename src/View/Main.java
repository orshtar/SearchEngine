package View;

import Model.Parse;
import Model.Ranker;
import Model.Searcher;
import com.sun.deploy.util.StringUtils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;


public class Main {/*extends Application{

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxmlLoader=new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("View.fxml").openStream());
        primaryStage.setTitle("Search Engine");
        Scene scene = new Scene(root, 700, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
*/
    public static void main(String[] args) {

        //launch(args);
        Parse.initStopWords("d:/documents/users/shtaro/Documents/data/stop_words.txt");
        Searcher s=new Searcher();
        s.SearchQ("At a minimum, relevant documents must contain the following information:  year of Nobel prize award, document announces what is obviously a current award, no year is required.",false,false,"d:/documents/users/shtaro/Documents/folder");
    }
}
