package Model;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * this class is intermediary medium between the view and the different classes
 *
 */
public class Model {

    /**
     *
     * perform the process of creating inverted index
     * by calling the relevant functions

     *
     * @param dataPath path where the corpus is found
     * @param stopPath path where the file of the stop words is found
     * @param isStem boolean which shows if stemming is on
     * @param savePath path to save the posting and other information
     * @return the number of docs we analyzed
     */
    public int Search(String dataPath, String stopPath, boolean isStem, String savePath){
        Indexer.clearDict();//clear previous terms in the dictionary
        ReadFile rf=new ReadFile();
        File file=new File(dataPath);
        String[] folderList=file.list();//get all the folder in the data folder
        Parse.initStopWords(stopPath);//init the stop words
        for(String name: folderList){
            rf.read(dataPath+"/"+name,isStem, savePath);//perform inverted index
        }
        Indexer.divide(savePath,isStem);//move the temporary posting file to permanent posting file
        Indexer.moveCitytoDisk(savePath,isStem);//move the cities posting to the disk
        return rf.docsNum;
    }

    /**
     *
     * return the number of term
     *
     * @return the number of terms in the dictionary
     */
    public int getTermsNum(){
        return Indexer.getTermNum();
    }

    /**
     *
     * reset the main memory of the program
     *
     */
    public void reset(){
        Indexer.clearDict();//clear memory from indexer
        Parse.clearAll();//clear memory from parse
        ReadFile.clear();//clear memory from read file
    }

    /**
     *
     * saves the dictionary to disk
     *
     * @param savePath path to save into the dictionary
     * @param stem boolean which shows if stemming is on
     */
    public void saveDict(String savePath, boolean stem){
        Indexer.sortDict(savePath,stem);
    }

    /**
     *
     * return set of languages of the docs
     *
     * @return set of languages
     */
    public Set<String> getLangs(){
        return Indexer.getLangs();
    }

    public Set<String> getCities(String savePath){
        Set<String> ans=new HashSet<>();
        String p="";
        try {
            p = new String(Files.readAllBytes(Paths.get(savePath + "/cities.txt")), StandardCharsets.UTF_8);//read a posting file
        }catch(IOException e){e.printStackTrace();}
        String[] lines=p.split("\n");
        for(String line:lines){
            ans.add(line.split(":")[0]);
        }
        return ans;
    }

    public List<String> searchQuery(String query, boolean isStem, boolean isSemantic, String savePath, List<String> selectedCities) {
        Searcher s=new Searcher();
        List<String> docs=s.SearchQ(query,isStem,isSemantic,savePath,selectedCities);
        return docs;
    }

    public void searchFileQuery(String path, boolean isStem, boolean isSemantic, String savePath, List<String> selectedCities){
        String f="";
        try{
            f = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
        } catch(IOException e){e.printStackTrace();}

    }
}
