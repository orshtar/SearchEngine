package Model;

import java.io.File;

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
}
