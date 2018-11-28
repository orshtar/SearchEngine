package sample;

import org.omg.CORBA.DynAnyPackage.Invalid;
import sun.reflect.generics.tree.Tree;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.*;

public class Indexer {

    private static Map<String,TermObject > dictionary=new LinkedHashMap<>();
    private static Map<Integer,String> postingFile=new LinkedHashMap<>();
    private static String fileName="1.txt";
    private static int lineNum=0;
    //private static HashMap<Character,Integer> linePos=new HashMap<>();


    public static void invertIndex(Map<String,Integer> map, String docNum){
        for(String term: map.keySet()){
            if(lineNum>=30000){
                moveToMem();
            }
            if(dictionary.containsKey(term.toLowerCase())) {
                TermObject to=dictionary.get(term.toLowerCase());
                to.addDoc();
                String pos=to.getPosting();
                String[] temp=pos.split("/");
                String newPos;
                if (temp[0].equals(fileName)) {
                    newPos=docNum+": "+map.get(term)+", "+postingFile.get(Integer.parseInt(temp[1]));
                    postingFile.replace(Integer.parseInt(temp[1]),newPos);
                }
                else{
                    newPos=docNum+": "+map.get(term)+"/"+pos;
                    to.setPosting(fileName+"/"+lineNum);
                    postingFile.put(lineNum,newPos);
                    lineNum++;
                }
            }
            else if(dictionary.containsKey(term.toUpperCase())){
                TermObject to=dictionary.get(term.toUpperCase());
                if(term.charAt(0)>='a' && term.charAt(0)<='z') {
                    dictionary.remove(term.toUpperCase());
                    dictionary.put(term.toLowerCase(),to);
                }
                to.addDoc();
                String pos=to.getPosting();
                String[] temp=pos.split("/");
                String newPos;
                if (temp[0].equals(fileName)) {
                    newPos=docNum+": "+map.get(term)+", "+postingFile.get(Integer.parseInt(temp[1]));
                    postingFile.replace(Integer.parseInt(temp[1]),newPos);
                    int k=0;
                }
                else{
                    newPos=docNum+": "+map.get(term)+"/"+pos;
                    to.setPosting(fileName+"/"+lineNum);
                    postingFile.put(lineNum,newPos);
                    lineNum++;
                }
            }
            else{
                TermObject newObj=new TermObject((short)1,fileName+"/"+lineNum);
                if(term.charAt(0)>='A' && term.charAt(0)<='Z')
                    dictionary.put(term.toUpperCase(),newObj);
                else
                    dictionary.put(term.toLowerCase(),newObj);
                postingFile.put(lineNum,docNum+": "+map.get(term)+"/x");
                lineNum++;
            }

        }
        Parse.clear();
    }


    public static void moveToMem(){
        /*
        try {
            FileWriter fw=new FileWriter("folder/"+fileName);
            BufferedWriter bw=new BufferedWriter(fw);
            Set<Integer> k=postingFile.keySet();
            for(int i: k) {
                bw.write(postingFile.get(i)+"\n");
            }
            fw.flush();
            bw.flush();
            postingFile.clear();
            lineNum=0;
            int i=0;
            int num=0;
            while(fileName.charAt(i)!='.'){
                num=num*10+(fileName.charAt(i)-'0');
                i++;
            }
            num++;
            fileName=num+".txt";
        } catch(IOException e){}
*/
        postingFile.clear();
        lineNum=0;
    }

    public void printDict(){
        for(String t: dictionary.keySet())
            System.out.println((dictionary.get(t)).toString());
    }



}
