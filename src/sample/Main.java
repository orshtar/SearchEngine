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
        Long start=System.currentTimeMillis();
        ReadFile rf=new ReadFile();
        File file=new File("new");
        String[] folderList=file.list();
        Parse.initStopWords("stop_words.txt");
        int i=1;
        for(String name: folderList){
            rf.read("new/"+name);
            System.out.println(i);
            i++;
        }
        //Indexer.moveToMem();
        //Indexer.printDict();
        Long end=System.currentTimeMillis();
        System.out.println(end-start);
        System.out.println("");
        Parse.print();

    }
}
