package View;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

import java.io.File;
import java.util.ArrayList;
import Model.Model;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class View {

    @FXML
    private ChoiceBox<String> langs;
    @FXML
    private TextField dataText;
    @FXML
    private TextField saveIn;
    @FXML
    private CheckBox stem;

    private Model model;

    private String dataPath;
    private String savePath;


    public View(){
        model=new Model();
        dataPath="";
        savePath="";
    }

    public void init(){
        ArrayList<String> temp= new ArrayList<>();
        temp.add("English");
        temp.add("Spanish");
        temp.add("Hebrew");
        temp.add("French");
        temp.add("Chinese");
        temp.add("Hindi");
        temp.add("Arabic");
        ObservableList<String> list= FXCollections.observableArrayList(temp);
        langs.setItems(list);
    }

    public void Search(){
        dataPath=dataText.getText();
        savePath=saveIn.getText();
        if(dataPath.equals("") || savePath.equals("")){
            Alert al=new Alert(Alert.AlertType.ERROR);
            al.setContentText("All fields are required");
            al.showAndWait();
            dataText.clear();
            saveIn.clear();
        }
        else {
            boolean isStem = stem.isSelected();
            File dataSet = new File(dataPath);
            String corpusPath = "";
            String stpPath = "";
            for (String s : dataSet.list()) {
                File f = new File(dataPath + "/" + s);
                if (f.isDirectory())
                    corpusPath = dataPath + "/" + s;
                else
                    stpPath = dataPath + "/" + s;
            }
            if(corpusPath.equals("") || stpPath.equals("")){
                Alert al=new Alert(Alert.AlertType.ERROR);
                al.setContentText("Path not found");
                al.showAndWait();
                dataText.clear();
                saveIn.clear();
            }
            else {
                Long start = System.currentTimeMillis();
                int docNum = model.Search(corpusPath, stpPath, isStem, savePath);
                Long end = System.currentTimeMillis();
                double t = end - start;
                t = t / 1000;
                int terms = model.getTermsNum();
                Alert al = new Alert(Alert.AlertType.INFORMATION);
                al.setTitle("Done!");
                al.setHeaderText(null);
                al.setContentText("Number of total docs: " + docNum + "\n"
                        + "Number of terms: " + terms + "\n"
                        + "Total runtime in sec: " + t);
                al.showAndWait();
            }
        }
    }

    public void browseData(){
        DirectoryChooser dc=new DirectoryChooser();
        dc.setTitle("Open data set");
        dc.setInitialDirectory(new File("C:"));
        File f=dc.showDialog(new Stage());
        if(f!=null)
            dataText.setText(f.getPath());
    }

    public void browseSave(){
        DirectoryChooser dc=new DirectoryChooser();
        dc.setTitle("Open save location");
        dc.setInitialDirectory(new File("C:"));
        File f=dc.showDialog(new Stage());
        if(f!=null)
            saveIn.setText(f.getPath());
    }

    public void reset(){
        File dir=new File(savePath);
        for(File f:dir.listFiles())
            f.delete();
        model.reset();
    }
}
