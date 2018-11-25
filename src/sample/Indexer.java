package sample;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class Indexer {

    private static Hashtable<String,TermObject > dictionary;
    private static HashMap<Integer,String> postingFile;

    public Indexer(){
        if(dictionary!=null)
            dictionary=new Hashtable<>();
        if(postingFile!=null)
            postingFile=new HashMap<>();
    }

    public static void invertIndex(Map<String,String> map, String docNum){
        for(String term: map.keySet()){
            if(dictionary.contains(term.toLowerCase()))
                dictionary.get(term.toLowerCase()).addDoc(docNum);
            else if(dictionary.contains(term.toUpperCase())){
                if(term.charAt(0)>='A' && term.charAt(0)<='Z')
                    dictionary.get(term.toUpperCase()).addDoc(docNum);
                else{
                    dictionary.get(term.toUpperCase()).setTerm();
                    dictionary.get(term.toUpperCase()).addDoc(docNum);
                }
            }
            else{
                TermObject newObj=new TermObject(term,1,docNum,"");
                if(term.charAt(0)>='A' && term.charAt(0)<='Z'){
                    dictionary.put(term.toUpperCase(),newObj);
                }
                else
                    dictionary.put(term.toLowerCase(),newObj);
            }
        }
    }
}
