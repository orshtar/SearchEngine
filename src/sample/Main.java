package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{

    }


    public static void main(String[] args) {
        //launch(args);
        ReadFile rf=new ReadFile();
        File file=new File("corpus");
        String[] folderList=file.list();
        for(String name: folderList){
            rf.read("corpus/"+name);
        }
    }
}
