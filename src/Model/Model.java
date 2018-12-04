package Model;

import java.io.File;
import java.util.Set;

public class Model {

    public int Search(String dataPath, String stopPath, boolean isStem, String savePath){
        Indexer.clearDict();
        ReadFile rf=new ReadFile();
        File file=new File(dataPath);
        String[] folderList=file.list();
        Parse.initStopWords(stopPath);
        for(String name: folderList){
            rf.read(dataPath+"/"+name,isStem, savePath);
        }
        Indexer.divide(savePath,isStem);
        Indexer.moveCitytoDisk(savePath);
        return rf.docsNum;
    }

    public int getTermsNum(){
        return Indexer.getTermNum();
    }

    public void reset(){
        Indexer.clearDict();
        Parse.clearAll();
        ReadFile.clear();
    }

    public void saveDict(String savePath, boolean stem){
        Indexer.sortDict(savePath,stem);
    }

    public Set<String> getLangs(){
        return Indexer.getLangs();
    }
}
