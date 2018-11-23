package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;

public class Main {
/*
    @Override
    public void start(Stage primaryStage) throws Exception{

    }
*/

    public static void main(String[] args) {
        //launch(args);
        /*
        ReadFile rf=new ReadFile();
        File file=new File("corpus");
        String[] folderList=file.list();
        Parse.initStopWords("stop_words.txt");
        for(String name: folderList){
            rf.read("corpus/"+name);
        }
        */
        String str="Dollars";
        System.out.println(str.equalsIgnoreCase("dollars"));
    }
}
