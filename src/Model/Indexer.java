package Model;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static com.sun.xml.internal.ws.util.VersionUtil.compare;


public class Indexer {

    private static Map<String,TermObject > dictionary=new LinkedHashMap<>();
    private static Map<String,String> postingFile=new LinkedHashMap<>();
    private static Map<String,String> docs=new LinkedHashMap<>();
    private static Set<String> cities=new LinkedHashSet<>();
    private static Set<String> languages=new LinkedHashSet<>();
    private static int lineNum=0;
    private static int fileNum=1;


    public static void invertIndex(Map<String,Integer> map, String docNum, int max_tf,String city,String language){
        docs.put(docNum,max_tf+","+map.size()+","+city+","+language);
        if(!city.equals("") && !cities.contains(city))
            cities.add(city);
        if(!language.equals("") && !languages.contains(language))
            languages.add(language);
        String k=fileNum+"";
        for(String term: map.keySet()){
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

    public static Set<String> getLangs(){
        return languages;
    }


    public static void moveToMem(String savePath, boolean isStem){
        char c='a';
        if(isStem)
            c='b';
        try {
            FileWriter fw=new FileWriter(savePath+"/"+fileNum+c+".txt");
            BufferedWriter bw=new BufferedWriter(fw);
            TreeSet<String> t=new TreeSet<>(postingFile.keySet());
            for(String s: t) {
                bw.write(postingFile.get(s)+"\n");
            }
            fw.flush();
            bw.flush();
            fw.close();
            bw.close();
        } catch(IOException e){}
        postingFile.clear();
        lineNum=0;
        fileNum++;
        try {
            FileWriter fw=new FileWriter(savePath+"/docs"+c+".txt",true);
            BufferedWriter bw=new BufferedWriter(fw);
            for(String s: docs.keySet()) {
                bw.write(s+":"+docs.get(s)+"\n");
            }
            fw.flush();
            bw.flush();
            fw.close();
            bw.close();
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

    public static int getTermNum(){
        return dictionary.size();
    }

    public static void clearDict(){
        if(dictionary!=null)
            dictionary.clear();
    }

    private static void initNewFiles(){


    }

    public static void sortPostings(){
        //initNewFiles();
        try {
            File file = new File("folder2/all.txt");
            file.createNewFile();
        }catch (IOException e){}
        mergePostings();


    }

    private static void mergePostings(){
        File file=new File("folder");
        String[] fileList=file.list();
        int i=0;
        for(String name: fileList) {
            System.out.println(i++);
            try {
                String p = new String(Files.readAllBytes(Paths.get("folder/" + name)), StandardCharsets.UTF_8);
                sort(p,"folder2/"+(i/80)+".txt");
            } catch(IOException e){System.out.println(e.fillInStackTrace());}
        }
    }

    private static void sort(String file, String path){
        String[] arr1=file.split("\n");
        String p="";
        try{
             p= new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
        }catch(IOException e){}
        String[] arr2=p.split("\n");
        BufferedWriter bw=null;
        try{
            FileWriter fw=new FileWriter(path,false);
            bw=new BufferedWriter(fw);
            int i=0, j=0;
            String str="";
            while(i<arr1.length && j<arr2.length){
                    if ((arr1[i].split(":"))[0].compareTo ((arr2[j].split(":"))[0]) > 0) {
                        str=str+arr2[j]+"\n";
                        j++;
                    } else if ((arr1[i].split(":"))[0].compareTo ((arr2[j].split(":"))[0]) == 0 && arr2[j].split(":").length>1) {
                        str=str+arr1[i] + (arr2[j].split(":"))[1]+"\n";
                        i++;
                        j++;
                    } else {
                        str=str+arr1[i]+"\n";
                        i++;
                    }
            }
            bw.write(str);
            while(i<arr1.length){
                bw.write(arr1[i]+"\n");
                i++;
            }
            while(j<arr2.length){
                bw.write(arr2[j]+"\n");
                j++;
            }

            bw.flush();
    }catch(IOException e){}
    }

    public static void sortDict(String path, boolean stem){
        char c='a';
        if(stem){
            c='b';
        }
        try {
            FileWriter fw=new FileWriter(path+"/dictionary"+c+".txt");
            BufferedWriter bw=new BufferedWriter(fw);
            TreeSet<String> t=new TreeSet<>(dictionary.keySet());
            for(String s: t) {
                bw.write("Term: "+s+", df: "+(dictionary.get(s)).toString()+"\n");
            }
            fw.flush();
            bw.flush();
            fw.close();
            bw.close();
        } catch(IOException e){}
    }

}
