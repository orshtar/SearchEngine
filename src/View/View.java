package View;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

import Model.Model;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
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
    private MenuButton citiess;

    private Model model;

    private String dataPath;
    private String savePath;
    private List<String> returnedDocsSingle;
    private Map<String,List<String>> returnedDocsMulti;
    private String currentQID;
    /**
     *
     * a simple constructor
     *
     */
    public View(){
        model=new Model();
        dataPath="";
        savePath="";
        returnedDocsSingle=new LinkedList<>();
        returnedDocsMulti=new LinkedHashMap<>();
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
            String stpPath = checkDataPath(dataPath);
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
        if(!model.isPostingEmpty(savePath)) {
            setCities();
            setLang();
        }
    }

    private void setLang() {
        Set<String> leng=model.getLang(savePath);
        ObservableList<String> l=FXCollections.observableArrayList(leng);
        langs.setItems(l);

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
        }catch(IOException e){e.printStackTrace();}
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
        if (!savePath.equals("")) {
            Set<String> cities = model.getCities(savePath);
            for (String city : cities) {
                CheckBox c = new CheckBox(city);
                CustomMenuItem c1=new CustomMenuItem(c);
                c1.setHideOnClick(false);
                citiess.getItems().add(c1);
            }
        }
    }

    public void checkCities(){
        if(citiess.getItems().size()==0){
            Alert al = new Alert(Alert.AlertType.WARNING);
            al.setHeaderText(null);
            al.setContentText("Please insert save location!");
            al.showAndWait();
        }
    }

    public void searchTextQuery(){
        returnedDocsSingle.clear();
        returnedDocsMulti.clear();
        if(queryText.getText().equals("")){
            Alert al = new Alert(Alert.AlertType.WARNING);
            al.setHeaderText(null);
            al.setContentText("Please type a query!");
            al.showAndWait();
        }
        else if(dataPath.equals("")){
            Alert al = new Alert(Alert.AlertType.WARNING);
            al.setHeaderText(null);
            al.setContentText("Please insert path to data");
            al.showAndWait();
        }
        else{
            String stpPath=checkDataPath(dataPath);
            List<String> selectedCities=getSelectedCities();
            int randomID = (int)(Math.random() * 998 + 1);
            currentQID=randomID+"";
            returnedDocsSingle=model.searchQuery(queryText.getText(),randomID+"",stem.isSelected(),isSemantic.isSelected(),stpPath,savePath,selectedCities);
            Group g = new Group();
            GridPane grid = new GridPane();
            setLabels(returnedDocsSingle,grid);
            g.getChildren().add(grid);
            Stage stage = new Stage();
            stage.setScene(new Scene(g));
            stage.setTitle("Relevant Docs");
            stage.show();
        }
    }

    private void showEntities(String docNO){
        String entities=model.getEntities(docNO, savePath,stem.isSelected());
        Group g = new Group();
        GridPane grid = new GridPane();
        Label l=new Label(entities);
        l.setAlignment(Pos.CENTER);
        grid.addRow(0,l);
        g.getChildren().add(grid);
        grid.setPadding(new Insets(10,10,10,10));
        Stage stage = new Stage();
        stage.setScene(new Scene(g));
        stage.setTitle("Entities");
        stage.show();
    }

    private void setLabels(List<String> docs, GridPane g) {
        int i=0;
        for(String docNO:docs){
            Label l=new Label(docNO);
            l.setOnMouseClicked((e) -> showEntities(l.getText()));
            g.addRow(i,l);
            g.setPadding(new Insets(10,10,10,10));
            i++;
        }
    }

    private List<String> getSelectedCities(){
        List<String> selectedCities=new LinkedList<>();
        for(MenuItem c:citiess.getItems()){
            CustomMenuItem c1=(CustomMenuItem)c;
            CheckBox b=(CheckBox)c1.getContent();
            if(b.isSelected())
                selectedCities.add(b.getText());
        }
        return selectedCities;
    }

    private String checkDataPath(String dataPath){
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
        return stpPath;
    }

    public void searchFileQuery(){
        returnedDocsMulti.clear();
        returnedDocsSingle.clear();
        if(queryPath.getText().equals("")){
            Alert al = new Alert(Alert.AlertType.ERROR);
            al.setHeaderText(null);
            al.setContentText("Please insert queries file!");
            al.showAndWait();
        }
        else if(dataPath.equals("")){
            Alert al = new Alert(Alert.AlertType.ERROR);
            al.setHeaderText(null);
            al.setContentText("Please insert path to data");
            al.showAndWait();
        }
        else{
            String stpPath=checkDataPath(dataPath);
            List<String> selectedCities=getSelectedCities();
            returnedDocsMulti=model.searchFileQuery(queryPath.getText(),stem.isSelected(),isSemantic.isSelected(),stpPath,savePath,selectedCities);
            Group g = new Group();
            GridPane grid = new GridPane();
            setLabelsMulti(returnedDocsMulti,grid);
            g.getChildren().add(grid);
            Stage stage = new Stage();
            stage.setScene(new Scene(g));
            stage.setTitle("Relevant Docs");
            stage.show();
        }
    }

    private void setLabelsMulti(Map<String, List<String>> returnedDocsMulti, GridPane grid) {
        int i=0, j=0;
        for(String qNum:returnedDocsMulti.keySet()){
            ColumnConstraints col=new ColumnConstraints();
            col.setHalignment(HPos.CENTER);
            grid.getColumnConstraints().add(col);

        }
        for(i=0; i<51; i++){
            RowConstraints row=new RowConstraints();
            row.setValignment(VPos.CENTER);
            grid.getRowConstraints().add(row);
        }
        for(String qNum:returnedDocsMulti.keySet()) {
            i=0;
            Label l1=new Label(qNum);
            grid.add(l1,j,i);
            i++;
            for (String docNO : returnedDocsMulti.get(qNum)) {
                Label l = new Label(docNO);
                l.setOnMouseClicked((e) -> showEntities(l.getText()));
                grid.add(l,j,i);
                grid.setHgap(10);
                grid.setVgap(2);
                grid.setPadding(new Insets(10, 10, 10, 10));
                i++;
            }
            j++;
        }
    }

    public void browseQueryFile(){
        FileChooser fc=new FileChooser();
        fc.setTitle("Select Queries File");
        File f=fc.showOpenDialog(new Stage());
        if(f!=null)
            queryPath.setText(f.getPath());
    }

    public void saveResults(){
        if(returnedDocsSingle.isEmpty() && returnedDocsMulti.isEmpty()){
            Alert al = new Alert(Alert.AlertType.WARNING);
            al.setHeaderText(null);
            al.setContentText("Please search a query before saving results!");
            al.showAndWait();
        }
        else {
            DirectoryChooser dc = new DirectoryChooser();
            dc.setTitle("Select location");
            File f = dc.showDialog(new Stage());
            String location = "";
            if (f != null)
                location = f.getPath();
            if (!location.equals("")) {
                if (!returnedDocsMulti.isEmpty())
                    model.saveResultsMulti(location, returnedDocsMulti, isSemantic.isSelected(), stem.isSelected());
                else if (!returnedDocsSingle.isEmpty())
                    model.saveResultsSingle(location, returnedDocsSingle, currentQID, isSemantic.isSelected(), stem.isSelected());
                Alert al = new Alert(Alert.AlertType.INFORMATION);
                al.setHeaderText(null);
                al.setContentText("Results file saved");
                al.showAndWait();
            }
        }
    }
}
