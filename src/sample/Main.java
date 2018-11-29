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
import java.util.LinkedHashSet;

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
            rf.read("new/"+name,false);
            System.out.println(i);
            i++;
        }
        //Indexer.moveToMem();

        Long end=System.currentTimeMillis();
        double t=end-start;
        System.out.println(t/60000);
        System.out.println("");
        //Indexer.printDict();
        Parse.print();

    }
}
