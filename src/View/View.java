package View;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Set;

import Model.Model;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

/**
 *
 *this class is the controller of the view.fxml
 * the class send requests to the model
 *
 */
public class View {

    @FXML
    private ComboBox<String> langs;
    @FXML
    private TextField dataText;
    @FXML
    private TextField saveIn;
    @FXML
    private CheckBox stem;

    private Model model;

    private String dataPath;
    private String savePath;


    /**
     *
     * a simple constructor
     *
     */
    public View(){
        model=new Model();
        dataPath="";
        savePath="";
    }


    /**
     *
     * this function get two path from the user one for the files and one to save the postings file
     * and pass the paths to the model
     *
     */
    public void Search(){
        dataPath=dataText.getText();
        savePath=saveIn.getText();
        if(dataPath.equals("") || savePath.equals("")){//check if the user entered the paths
            Alert al=new Alert(Alert.AlertType.ERROR);
            al.setContentText("All fields are required");
            al.showAndWait();
            dataText.clear();
            saveIn.clear();
        }
        else {
            boolean isStem = stem.isSelected();//get if to do stemming
            File dataSet = new File(dataPath);
            String corpusPath = "";
            String stpPath = "";
            for (String s : dataSet.list()) {//separate the path of data to path to stop word and path to the file
                File f = new File(dataPath + "/" + s);
                if (f.isDirectory())
                    corpusPath = dataPath + "/" + s;
                else
                    stpPath = dataPath + "/" + s;
            }
            if(corpusPath.equals("") || stpPath.equals("")){//if the stop word is missing or the files
                Alert al=new Alert(Alert.AlertType.ERROR);
                al.setContentText("Path not found");
                al.showAndWait();
                dataText.clear();
                saveIn.clear();
            }
            else {
                Long start = System.currentTimeMillis();//start counter
                int docNum = model.Search(corpusPath, stpPath, isStem, savePath);//call the model
                Long end = System.currentTimeMillis();//end counter
                double t = end - start;
                t = t / 1000;
                int terms = model.getTermsNum();//get numbers of terms
                Alert al = new Alert(Alert.AlertType.INFORMATION);//show finish alert
                al.setTitle("Done!");
                al.setHeaderText(null);
                al.setContentText("Number of total docs: " + docNum + "\n"
                        + "Number of terms: " + terms + "\n"
                        + "Total runtime in sec: " + t);
                al.showAndWait();
                Set<String> langus=model.getLangs();
                ObservableList<String> list= FXCollections.observableArrayList(langus);
                langs.setItems(list);
                langs.setPromptText("Languages");
            }
        }
    }

    /**
     *
     * open a window to search a folder where the data is
     *
     */
    public void browseData(){
        DirectoryChooser dc=new DirectoryChooser();
        dc.setTitle("Open data set");
        dc.setInitialDirectory(new File("C:"));
        File f=dc.showDialog(new Stage());
        if(f!=null)
            dataText.setText(f.getPath());
    }

    /**
     *
     * open a window to search a folder where to save the posting file
     *
     */
    public void browseSave(){
        DirectoryChooser dc=new DirectoryChooser();
        dc.setTitle("Open save location");
        dc.setInitialDirectory(new File("C:"));
        File f=dc.showDialog(new Stage());
        if(f!=null)
            saveIn.setText(f.getPath());
    }

    /**
     *
     * reset the program and delete all memory
     *
     */
    public void reset(){
        if(savePath.equals(""))
            savePath=saveIn.getText();
        File dir=new File(savePath);
        for(File f:dir.listFiles())
            f.delete();
        model.reset();
        Alert al = new Alert(Alert.AlertType.INFORMATION);//show finish alert
        al.setTitle("Done!");
        al.setHeaderText(null);
        al.setContentText("All documents deleted");
        al.showAndWait();
    }

    /**
     *
     * open a file with the dictionary
     *
     */
    public void showDict(){
        saveDict();
        char c='a';
        if(stem.isSelected())
            c='b';
        File f;
        try{
            f=new File(savePath+"/dictionary"+c+".txt");
            Desktop.getDesktop().open(f);
        }catch(IOException e){System.out.println(e.getMessage());}
    }

    /**
     *
     * save the dictionary to a file
     *
     */
    public void saveDict(){
        model.saveDict(savePath, stem.isSelected());
    }
}
