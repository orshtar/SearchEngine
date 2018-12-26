package View;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import Model.Model;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
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
    @FXML
    private CheckBox isSemantic;
    @FXML
    private TextField queryText;
    @FXML
    private TextField queryPath;
    @FXML
    private ComboBox<CheckMenuItem> citiess;

    private Model model;

    private String dataPath;
    private String savePath;
    private List<String> returnedDocs;

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
    public void Index(){
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
            String stpPath = "";
            for (String s : dataSet.list()) {//separate the path of data to path to stop word and path to the file
                File f = new File(dataPath + "/" + s);
                if (!f.isDirectory())
                    stpPath = dataPath + "/" + s;
            }
            if( stpPath.equals("")){//if the stop word is missing
                Alert al=new Alert(Alert.AlertType.ERROR);
                al.setContentText("Path not found");
                al.showAndWait();
                dataText.clear();
                saveIn.clear();
            }
            else {
                Long start = System.currentTimeMillis();//start counter
                int docNum = model.Search(dataPath, stpPath, isStem, savePath);//call the model
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
        if(f!=null) {
            dataText.setText(f.getPath());
            dataPath=f.getPath();
        }
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
        if(f!=null) {
            saveIn.setText(f.getPath());
            savePath=f.getPath();
        }
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

    public void setCities() {
        if(citiess.getItems().size()==0) {
            if (!savePath.equals("")) {
                Set<String> cities = model.getCities(savePath);
                for (String city : cities) {
                    CheckMenuItem c = new CheckMenuItem(city);
                    citiess.getItems().add(c);
                }
            } else {
                Alert al = new Alert(Alert.AlertType.INFORMATION);
                al.setHeaderText(null);
                al.setContentText("Please insert save location!");
                al.showAndWait();
            }
        }
    }

    public void searchTextQuery(){
        returnedDocs.clear();
        if(queryText.getText().equals("")){
            Alert al = new Alert(Alert.AlertType.INFORMATION);
            al.setHeaderText(null);
            al.setContentText("Please type a query!");
            al.showAndWait();
        }
        else{
            //List<String> selectedCities=citiess.getItems();
            int randomID = (int)(Math.random() * 998 + 1);
            //returnedDocs=model.searchQuery(queryText.getText(),randomID+"",stem.isSelected(),isSemantic.isSelected(),savePath,selectedCities);

        }
    }

    public void searchFileQuery(){
        returnedDocs.clear();
        if(queryPath.getText().equals("")){
            Alert al = new Alert(Alert.AlertType.INFORMATION);
            al.setHeaderText(null);
            al.setContentText("Please insert queries file!");
            al.showAndWait();
        }
        else{
            //List<String> selectedCities=citiess.getItems();
            //returnedDocs=model.searchFileQuery(queryPath.getText(),stem.isSelected(),isSemantic.isSelected(),savePath,selectedCities);
        }
    }

    public void browseQueryFile(){
        FileChooser fc=new FileChooser();
        fc.setTitle("Select Queries File");
        //fc.setInitialDirectory(new File("C:"));
        File f=fc.showOpenDialog(new Stage());
        if(f!=null)
            queryPath.setText(f.getPath());
    }

    public void saveResults(){
        DirectoryChooser dc=new DirectoryChooser();
        dc.setTitle("Select location");
        dc.setInitialDirectory(new File("C:"));
        File f=dc.showDialog(new Stage());
        String location="";
        if(f!=null)
           location=f.getPath();
        if(!location.equals("")){

        }
    }
}
