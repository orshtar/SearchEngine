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
    private static Map<String,String> postingFile=new LinkedHashMap<>();
    private static Map<String,String> docs=new LinkedHashMap<>();
    private static Set<String> cities=new LinkedHashSet<>();
    private static int lineNum=0;
    private static int fileNum=1;


    public static void invertIndex(Map<String,Integer> map, String docNum, int max_tf,String city,String docName){
        docs.put(docNum,max_tf+","+map.size()+","+city+","+docName);
        if(!city.equals("") && !cities.contains(city))
            cities.add(city);
        String k=fileNum+"";
        for(String term: map.keySet()){
            if(lineNum>=10000){
                moveToMem();
            }
            if(dictionary.containsKey(term.toLowerCase())) {
                TermObject to=dictionary.get(term.toLowerCase());
                to.addDoc();
                String newPos;
                if (postingFile.containsKey(term.toLowerCase())) {
                    newPos=postingFile.get(term.toLowerCase())+","+docNum+"*"+map.get(term);
                    postingFile.replace(term.toLowerCase(),newPos);
                }
                else{
                    newPos=term.toLowerCase()+":"+docNum+"*"+map.get(term);
                    postingFile.put(term.toLowerCase(),newPos);
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
                String newPos;
                if (postingFile.containsKey(term.toLowerCase())) {
                    newPos=postingFile.get(term.toLowerCase())+","+docNum+"*"+map.get(term);
                    postingFile.replace(term.toLowerCase(),newPos);
                }
                else{
                    newPos=term.toLowerCase()+":"+docNum+"*"+map.get(term);
                    postingFile.put(term.toLowerCase(),newPos);
                    lineNum++;
                }
            }
            else{
                TermObject newObj=new TermObject((short)1,fileNum+"/"+lineNum);
                if(term.charAt(0)>='A' && term.charAt(0)<='Z')
                    dictionary.put(term.toUpperCase(),newObj);
                else
                    dictionary.put(term.toLowerCase(),newObj);
                postingFile.put(term.toLowerCase(),term.toLowerCase()+":"+docNum+"*"+map.get(term));
                lineNum++;
            }

        }
        Parse.clear();
    }


    public static void moveToMem(){
        try {
            FileWriter fw=new FileWriter("folder/"+fileNum+".txt");
            BufferedWriter bw=new BufferedWriter(fw);
            TreeSet<String> t=new TreeSet<>(postingFile.keySet());
            for(String s: t) {
                bw.write(postingFile.get(s)+"\n");
            }
            fw.flush();
            bw.flush();
        } catch(IOException e){}
        postingFile.clear();
        lineNum=0;
        fileNum++;
        try {
            FileWriter fw=new FileWriter("folder/docs.txt",true);
            BufferedWriter bw=new BufferedWriter(fw);
            for(String s: docs.keySet()) {
                bw.write(s+":"+docs.get(s)+"\n");
            }
            fw.flush();
            bw.flush();
        } catch(IOException e){}
        docs.clear();
    }

    public static void printDict(){
        for(String t: dictionary.keySet())
            System.out.println(t/*+(dictionary.get(t)).toString()*/);
    }

    public static boolean containsTerm (String term){
        return dictionary.containsKey(term.toLowerCase())|| dictionary.containsKey(term.toUpperCase());
    }

    public static void merge(){
        File file=new File("folder");
        String[] folderList=file.list();
        for(String name: folderList){

        }
    }

    private static void enter(String posting){
        //new String(Files.readAllBytes(Paths.get("folder/")), StandardCharsets.UTF_8);
        String letterFile="";
        String newLetterFile="";
        String[] terms=posting.split(":");
        char cur='1';
        int i=0, j=0;
        while (i<terms.length){
            if(terms[0].charAt(0)=='$' || terms[0].charAt(0)=='%'
                        || (terms[0].charAt(0)>='0' &&terms[0].charAt(0)<='9')){
                cur='1';
            }
            
        }
    }

}
