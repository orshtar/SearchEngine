package Model;

import java.io.File;
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

    public Set<String> getCities(){
        return Indexer.getCities();
    }
}
