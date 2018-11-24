package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.swing.text.html.parser.Parser;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.Calendar;
import java.util.Date;

public class Main {
/*
    @Override
    public void start(Stage primaryStage) throws Exception{

    }
*/

    public static void main(String[] args) {
        //launch(args);
        ReadFile rf=new ReadFile();
        File file=new File("new");
        String[] folderList=file.list();
        Parse.initStopWords("stop_words.txt");
        for(String name: folderList){
            rf.read("new/"+name);
        }
        Parse.print();


    }
}
