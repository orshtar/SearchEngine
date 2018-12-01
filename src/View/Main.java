package View;

import Model.Indexer;
import Model.Parse;
import Model.ReadFile;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;

public class Main extends Application{

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxmlLoader=new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("View.fxml").openStream());
        View viewControl = fxmlLoader.getController();
        //viewControl.setPrimStage(primaryStage);
        primaryStage.setTitle("Search");
        Scene scene = new Scene(root, 1000, 650);
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
        /*
        Long start=System.currentTimeMillis();
        ReadFile rf=new ReadFile();
        File file=new File("corpus");
        String[] folderList=file.list();
        Parse.initStopWords("stop_words.txt");
        int i=1;
        for(String name: folderList){
            rf.read("corpus/"+name,false);
            System.out.println(i);
            i++;
        }
        Indexer.moveToMem();

        Long end=System.currentTimeMillis();
        double t=end-start;
        System.out.println(t/60000);
        System.out.println("");
        //Indexer.printDict();
        //Parse.print();
*/
    }
}
