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
    private static Map<String, CityObject> cities=new LinkedHashMap<>();
    private static Map<String, String> cityPosting=new LinkedHashMap<>();
    private static Set<String> languages=new LinkedHashSet<>();
    private static int fileNum=1;


    public static void invertIndex(Map<String,String> map, String docNum, int max_tf,String city,String language){
        docs.put(docNum,max_tf+","+map.size()+","+city+","+language);
        if(!language.equals("") && !languages.contains(language))
            languages.add(language);
        if(!city.equals("")){
            if (!cities.containsKey(city)) {
                CityObject c = new CityObject(city);
                if(c.isCapital())
                    cities.put(city, c);
                else
                    cities.put(city,null);
            }
            if(cityPosting.containsKey(city)){
                String s;
                if(map.containsKey(city)){
                    s=cityPosting.get(city)+", "+docNum+"*"+map.get(city);
                }
                else{
                    s=cityPosting.get(city)+", "+docNum;
                }
                cityPosting.replace(city,s);
            }
            else{
                String s;
                if(map.containsKey(city)){
                    s=docNum+"*"+map.get(city);
                }
                else{
                    s=docNum;
                }
                cityPosting.put(city,city+": "+s);
            }
        }
        for(String term: map.keySet()){

            if(dictionary.containsKey(term.toLowerCase())) {
                TermObject to=dictionary.get(term.toLowerCase());
                to.addDoc(map.get(term).split("\\*").length);
                String newPos;
                if (postingFile.containsKey(term.toLowerCase())) {
                    newPos=postingFile.get(term.toLowerCase())+","+docNum+"*"+map.get(term);
                    postingFile.replace(term.toLowerCase(),newPos);
                }
                else{
                    newPos=term.toLowerCase()+":"+docNum+"*"+map.get(term);
                    postingFile.put(term.toLowerCase(),newPos);
                }
            }
            else if(dictionary.containsKey(term.toUpperCase())){
                TermObject to=dictionary.get(term.toUpperCase());
                if(term.charAt(0)>='a' && term.charAt(0)<='z') {
                    dictionary.remove(term.toUpperCase());
                    dictionary.put(term.toLowerCase(),to);
                }
                to.addDoc(map.get(term).split("\\*").length);
                String newPos;
                if (postingFile.containsKey(term.toLowerCase())) {
                    newPos=postingFile.get(term.toLowerCase())+","+docNum+"*"+map.get(term);
                    postingFile.replace(term.toLowerCase(),newPos);
                }
                else{
                    newPos=term.toLowerCase()+":"+docNum+"*"+map.get(term);
                    postingFile.put(term.toLowerCase(),newPos);
                }
            }
            else{
                TermObject newObj=new TermObject((short)1,map.get(term).split("\\*").length);
                if(term.charAt(0)>='A' && term.charAt(0)<='Z')
                    dictionary.put(term.toUpperCase(),newObj);
                else
                    dictionary.put(term.toLowerCase(),newObj);
                postingFile.put(term.toLowerCase(),term.toLowerCase()+":"+docNum+"*"+map.get(term));
            }

        }
        Parse.clear();
    }


    public static Set<String> getLangs(){
        return languages;
    }

    public static void moveCitytoDisk(String savePath){
        try {
            FileWriter fw=new FileWriter(savePath+"/cities.txt");
            BufferedWriter bw=new BufferedWriter(fw);
            TreeSet<String> t=new TreeSet<>(cityPosting.keySet());
            for(String s: t) {
                bw.write(cityPosting.get(s)+"\n");
            }
            fw.flush();
            bw.flush();
            fw.close();
            bw.close();
        } catch(IOException e){}
        cityPosting.clear();
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


    public static boolean containsTerm (String term){
        return dictionary.containsKey(term.toLowerCase())|| dictionary.containsKey(term.toUpperCase());
    }

    public static int getTermNum(){
        return dictionary.size();
    }

    public static void clearDict(){
        if(dictionary!=null)
            dictionary.clear();
        if(cities!=null)
            cities.clear();
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
                bw.write("Term: "+s+", f: "+(dictionary.get(s)).toString()+"\n");
            }
            fw.flush();
            bw.flush();
            fw.close();
            bw.close();
        } catch(IOException e){}
    }

    public static void divide(String path, boolean isStem){
        char stem='a';
        if(isStem)
            stem='b';
        File file=new File(path);
        String[] fileList=file.list();
        String s="";
        for(String f: fileList){
            if(f.charAt(0)>='1' && f.charAt(0)<='9') {
                try {
                    String p = new String(Files.readAllBytes(Paths.get(path + "/" + f)), StandardCharsets.UTF_8);
                    String[] lines = p.split("\n");
                    char c = '1', curr='1';
                    for(String line:lines){
                        if(!line.equals("")) {
                            c = line.charAt(0);
                            if (c < 'a' || c > 'z')
                                c = '1';
                            if (curr != c) {
                                if (curr == '1') {
                                    add(s, path + "/nums" + stem + ".txt");
                                } else {
                                    add(s, path + "/" + curr + stem + ".txt");
                                }
                                curr = c;
                                s = "";
                            } else {
                                s += ("\n" + line);
                            }
                        }
                    }
                    File t=new File(path + "/" + f);
                    t.delete();
                } catch (IOException e) {
                }
            }
        }
        sort(path,stem);
    }

    private static void add(String text,String path){
        try {
            File yourFile = new File(path);
            yourFile.createNewFile();
            FileWriter fw=new FileWriter(path,true);
            BufferedWriter bw=new BufferedWriter(fw);
            bw.write(text);
            fw.flush();
            bw.flush();
            fw.close();
            bw.close();
        } catch(IOException e){}
    }

    private static void sort(String path, char stem){
        File file=new File(path);
        String[] fileList=file.list();
        for(String f: fileList){
            if(f.charAt(0)>='a' && f.charAt(0)<='z' && f.charAt(f.length()-5)==stem) {
                try {
                    String p = new String(Files.readAllBytes(Paths.get(path + "/" + f)), StandardCharsets.UTF_8);
                    String[] lines = p.split("\n");
                    FileWriter fw = new FileWriter(path + "/" + f);
                    BufferedWriter bw = new BufferedWriter(fw);
                    TreeSet<String> t = new TreeSet<>(Arrays.asList(lines));
                    String curr = "-1";
                    for (String s : t) {
                        if (!curr.equals( s.split(":")[0])) {
                            curr = s.split(":")[0];
                            bw.write("\n" + s);
                        } else {
                            if(s.split(":").length>1)
                                bw.write(","+s.split(":")[1]);
                        }
                    }
                    fw.flush();
                    bw.flush();
                    fw.close();
                    bw.close();
                } catch (IOException e) {
                }
            }
        }


    }

}
