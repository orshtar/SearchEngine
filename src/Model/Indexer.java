package Model;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
        docs.put(docNum,max_tf+","+map.size()+","+city+","/*+docName*/);
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

    private static void initFiles(){
        try {
            File file = new File("folder2/nums.txt");
            file.createNewFile();
            for(char c='a'; c<='z'; c++){
                file = new File("folder2/"+c+".txt");
                file.createNewFile();
            }
        }catch( IOException e){}
    }

    public static void sortPostings(){
        initFiles();
        mergeFiles();
    }

    private static void mergeFiles(){
        File file=new File("folder");
        String[] fileList=file.list();
        int i=0;
        for(String name: fileList) {
            System.out.println(i++);
            try {
                String p = new String(Files.readAllBytes(Paths.get("folder/" + name)), StandardCharsets.UTF_8);
                sort(p);
            } catch(IOException e){System.out.println(e.fillInStackTrace());}
        }
    }

    private static void sort(String file){/*
        char curr='1', c;
        String[] lines=file.split("\n");
        int i=0, j=0;
        BufferedWriter bw=null;
        String currentFile;
        try{
            FileWriter fw=new FileWriter("folder2/num.txt",false);
            currentFile=new String(Files.readAllBytes(Paths.get("folder2/nums.txt")), StandardCharsets.UTF_8);
            String newFile="";
            String[] splitLine=lines[i].split(":");
            bw=new BufferedWriter(fw);
                c=splitLine[0].charAt(0);
                if(c<'a' || c>'z'){
                    c='1';
                }
                if(c!=curr){
                    bw.write(newFile);
                    bw.flush();
                    fw=new FileWriter("folder2/" + c + ".txt",false);
                    bw=new BufferedWriter(fw);
                    currentFile=new String(Files.readAllBytes(Paths.get("folder2/" + c + ".txt")), StandardCharsets.UTF_8);
                }
        }catch(IOException e){}
        */
        TreeSet<String> t;
        String currentFile="";
        FileWriter fw=null;
        BufferedWriter bw=null;
        try {
            currentFile = new String(Files.readAllBytes(Paths.get("folder2/nums.txt")), StandardCharsets.UTF_8);
             fw=new FileWriter("folder2/nums.txt",false);
            bw=new BufferedWriter(fw);
        }catch(IOException e){}
        int i=0;

        t=new TreeSet<>(new HashSet<String>(Arrays.asList((new String(currentFile)).split("\n"))));
        String[] lines=file.split("\n");
        char c,curr='1';
        while(i<lines.length){
            c=lines[i].charAt(0);
            if(c<'a' || c>'z')
                c='1';
            if(c!=curr){
                String str="";
                String last="&";
                for(String s:t){
                    if(last.equals(s.split(":")[0]))
                        str+=s.split(":")[1];
                    else {
                        str += ("\n"+s);
                        last=s.split(":")[0];
                    }
                }
                try {
                    bw.write(str);
                    bw.flush();
                }catch (IOException e){}
                t.clear();
                try {
                    currentFile = new String(Files.readAllBytes(Paths.get("folder2/"+c+".txt")), StandardCharsets.UTF_8);
                    fw=new FileWriter("folder2/"+c+".txt",false);
                    bw=new BufferedWriter(fw);
                }catch(IOException e){}
                i--;
                t=new TreeSet<>(new HashSet<String>(Arrays.asList((new String(currentFile)).split("\n"))));
                curr=c;
            }
            else{
                t.add(lines[i]);
            }
            i++;
        }
    }

}
