package Model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

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
        if(folderList!=null) {
            for (String name : folderList) {
                rf.read(dataPath + "/" + name, isStem, savePath);//perform inverted index
            }
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

    /**
     *
     * @param savePath- the path to the cities' posting
     * @return all cities found in corpus
     */
    public Set<String> getCities(String savePath){
        Set<String> ans=new HashSet<>();
        String p="";
        try {
            p = new String(Files.readAllBytes(Paths.get(savePath + "/cities.txt")), StandardCharsets.UTF_8);//read a posting file
        }catch(IOException e){return ans;}     //if posting not found return empty cities list
        String[] lines=p.split("\n");
        for(String line:lines){
            ans.add(line.split(":")[0]);     //find only city name
        }
        return ans;
    }

    /**
     *
     * @param query- the query to search
     * @param desc- description of the query from query file
     * @param qNum- query number
     * @param isStem- if stemming need to be done
     * @param isSemantic- if semantic search need to be done
     * @param dataPath- path to corpus
     * @param savePath- path to posting files
     * @param selectedCities- list of cities the user chose
     * @return list of doc nums that are relevant to the query
     */
    public List<String> searchQuery(String query, String desc, String qNum, boolean isStem, boolean isSemantic,String dataPath, String savePath, List<String> selectedCities) {
        Parse.initStopWords(dataPath);
        Searcher s=new Searcher();
        List<String> docs=s.SearchQ(query,desc,isStem,isSemantic,savePath,selectedCities); //call the searcher
        return docs;
    }

    /**
     *
     * @param path- path to queries file
     * @param isStem- if stemming need to be done
     * @param isSemantic- if semantic search need to be done
     * @param stpPath- path to stop words
     * @param savePath- path to posting files
     * @param selectedCities- list of cities the user chose
     * @return a map- for each query number a list of relevant docs
     */
    public Map<String,List<String>> searchFileQuery(String path, boolean isStem, boolean isSemantic, String stpPath, String savePath, List<String> selectedCities){
        Map<String,List<String>> returnedDocs=new LinkedHashMap<>();
        String f="";
        try{
            f = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
        } catch(IOException e){e.printStackTrace();}
        String[] queries=f.split("<top>");
        String qNum="",query="";
        int i=0;
        for(String q:queries){
            i++;
            if(!q.equals("")) {
                String[] num=q.split("<num> Number: ");    // extract the query num
                if(num.length>1){
                    qNum=num[1].split("\n")[0];
                }
                String[] title = q.split("<title>");     // extract the query itself
                if (title.length > 1) {
                    query=title[1].split("\n")[0];
                }
                String description="";
                String[] desc = q.split("<desc> Description: ");    // extract the query description
                if (desc.length > 1) {
                    description=desc[1].split("\n<narr>")[0];
                }
                List<String> tempDocs=searchQuery(query,description,qNum,isStem,isSemantic,stpPath,savePath,selectedCities);   //search relevant docs for each qury
                returnedDocs.put(qNum,tempDocs);
            }
        }
        return returnedDocs;
    }

    /**
     *
     * @param docNO- doc num we want to get entities to
     * @param savePath- path to posting files
     * @param stem- if stemming need to be done
     * @return stroing of entities for the doc
     */
    public String getEntities(String docNO, String savePath, boolean stem) {
        String posDoc=Indexer.search(savePath,docNO,stem,"docs"); //search doc num in docs posting
        String[] psik=posDoc.split(",");
        String entities=psik[psik.length-1];
        entities=entities.replaceAll("/",": ");
        entities=entities.replaceAll("\\*","\n");
        return entities;
    }

    /**
     *
     * @param savePath- peth to posting files
     * @return true if there are posting files in path(false if empty)
     */
    public boolean isPostingEmpty(String savePath) {
        boolean ans;
        File f=new File(savePath);
        ans=(f.list().length==0);
        return ans;
    }

    /**
     *
     * @param location- path to save the results in
     * @param returnedDocsMulti- map of relevant docs for each query
     * @param isSemantic- is semantic search is on
     * @param isStem- is stemming need to be done
     */
    public void saveResultsMulti(String location, Map<String, List<String>> returnedDocsMulti, boolean isSemantic, boolean isStem) {
        String addChar="a";
        if(isStem){
            addChar="b";     // mark file by stemming
        }
        if(isSemantic){
            addChar+="s";    // mark file by semantic
        }
        else
            addChar+="n";
        try {
            FileWriter fw = new FileWriter(location + "/searchResults"+addChar+".txt");//open a dictionary file
            BufferedWriter bw = new BufferedWriter(fw);
            for(String qID: returnedDocsMulti.keySet()){
                for(String docNum: returnedDocsMulti.get(qID)){
                    bw.write(qID+" 0 "+docNum+" 1 42.38 mt\n");   // write docs in treceval format
                }
            }
            fw.flush();//flush and close the file
            bw.flush();
            fw.close();
            bw.close();
        } catch(IOException e){e.printStackTrace();}
    }

    /**
     *
     * @param location- path to save the results in
     * @param returnedDocsSingle- map of relevant docs for each query
     * @param qID- number of qury (random)
     * @param isSemantic- is semantic search is on
     * @param isStem- is stemming need to be done
     */
    public void saveResultsSingle(String location, List<String> returnedDocsSingle, String qID, boolean isSemantic, boolean isStem) {
        String addChar="a";
        if(isStem){
            addChar="b";      // mark file by stemming
        }
        if(isSemantic){
            addChar+="s";    // mark file by semantic
        }
        else
            addChar+="n";
        try {
            FileWriter fw = new FileWriter(location + "/searchResults"+addChar+".txt");//open a dictionary file
            BufferedWriter bw = new BufferedWriter(fw);
            for(String doc:returnedDocsSingle){
                bw.write(qID+" 0 "+doc+" 1 42.38 mt\n");      // write docs in treceval format
            }
            fw.flush();//flush and close the file
            bw.flush();
            fw.close();
            bw.close();
        } catch(IOException e){e.printStackTrace();}
    }

    /**
     *
     * @param savePath- path to posting files
     * @return all languager found on corpus
     */
    public Set<String> getLang(String savePath) {
        String p="";
        Set<String> ans=new HashSet<>();
        try {
            p = new String(Files.readAllBytes(Paths.get(savePath + "/docsa.txt")), StandardCharsets.UTF_8);//read a posting file
        }catch(IOException e){
            try{
                p = new String(Files.readAllBytes(Paths.get(savePath + "/docsb.txt")), StandardCharsets.UTF_8);//read a posting file
            }catch (IOException e1){e1.printStackTrace();}
        }
        String[] docs=p.split("\n");
        for(String doc: docs){
            String[] split=doc.split(",");     //get language from file
            if(split.length>=5 && !split[4].equals(""))
                ans.add(split[4]);
        }
        return  ans;
    }
}
