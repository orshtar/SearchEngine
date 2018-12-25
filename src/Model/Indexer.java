package Model;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 *
 * this class creates the dictionary and the posting files
 * by using inverted index for the terms and for cities
 * also the intexer save information about the docs
 * and the languages
 *
 */
public class Indexer {

    private static Map<String,TermObject > dictionary=new LinkedHashMap<>();
    private static Map<String,String> postingFile=new LinkedHashMap<>();
    private static Map<String,String> docs=new LinkedHashMap<>();
    private static Map<String, CityObject> cities=new LinkedHashMap<>();
    private static Map<String, String> cityPosting=new LinkedHashMap<>();
    private static Set<String> languages=new LinkedHashSet<>();
    private static int fileNum=1;


    /**
     *
     * this function gets information from the parse and save the terms and cities to their dictionaries
     * save the posting of the therms to temporary files and saves all information needed of the docs
     *
     * @param map list of the terms and their positions in the doc
     * @param docNum the code of the doc
     * @param max_tf most frequent term in doc
     * @param city city of the doc
     * @param language language of the doc
     */
    public void invertIndex(Map<String,String> map, String docNum, int max_tf, int docLen, String city,String language, Map<String, Integer> entities){
        String entitie="";
        for(String s: entities.keySet()){
            entitie+=(s+"/"+entities.get(s)+"*");
        }
        docs.put(docNum,max_tf+","+map.size()+","+docLen+","+city+","+language+","+entitie);
        if(!language.equals("") && !languages.contains(language))
            languages.add(language);//adds the language to list of languages
        if(!city.equals("")){
            if (!cities.containsKey(city)) {
                CityObject c = new CityObject(city);//creates a new city object
                if(c.isCapital())//saves the city in the dictionary
                    cities.put(city, c);
                else
                    cities.put(city,null);
            }
            if(cityPosting.containsKey(city)){//add the doc number and positions in the doc of the city to an existing city in the posting file of cities
                String s=cityPosting.get(city)+","+docNum;
                cityPosting.replace(city,s);
            }
            else{
                String s=docNum;
                cityPosting.put(city,s);
            }
        }
        for(String term: map.keySet()){//pass on all terms from the parse

            if(dictionary.containsKey(term.toLowerCase())) {
                TermObject to=dictionary.get(term.toLowerCase());
                to.addDoc(map.get(term).split("\\*").length);//add the doc and the number of appearance of the term in the text
                String newPos;
                if (postingFile.containsKey(term.toLowerCase())) {
                    newPos=postingFile.get(term.toLowerCase())+","+docNum+"*"+map.get(term);//add the doc and the positions to the posting
                    postingFile.replace(term.toLowerCase(),newPos);
                }
                else{
                    newPos=term.toLowerCase()+":"+docNum+"*"+map.get(term);//add new line with the doc and the positions to the posting
                    postingFile.put(term.toLowerCase(),newPos);
                }
            }
            else if(dictionary.containsKey(term.toUpperCase())){
                TermObject to=dictionary.get(term.toUpperCase());
                if(term.charAt(0)>='a' && term.charAt(0)<='z') {
                    dictionary.remove(term.toUpperCase());//update the term to lower case in the dictionary by the given rules
                    dictionary.put(term.toLowerCase(),to);
                }
                to.addDoc(map.get(term).split("\\*").length);//add the doc and the number of appearance of the term in the text
                String newPos;
                if (postingFile.containsKey(term.toLowerCase())) {
                    newPos=postingFile.get(term.toLowerCase())+","+docNum+"*"+map.get(term);//add the doc and the positions to the posting
                    postingFile.replace(term.toLowerCase(),newPos);
                }
                else{
                    newPos=term.toLowerCase()+":"+docNum+"*"+map.get(term);//add new line with the doc and the positions to the posting
                    postingFile.put(term.toLowerCase(),newPos);
                }
            }
            else{
                TermObject newObj=new TermObject((short)1,map.get(term).split("\\*").length);//create new term object
                if(term.charAt(0)>='a' && term.charAt(0)<='z')//insert the term to dictionary
                    dictionary.put(term.toLowerCase(),newObj);
                else
                    dictionary.put(term.toUpperCase(),newObj);
                postingFile.put(term.toLowerCase(),term.toLowerCase()+":"+docNum+"*"+map.get(term));//add new line with the doc and the positions to the posting
            }

        }
        Parse.clear();//clear the terms from the parse memory
    }


    /**
     *
     * returns a set of languages of the docs
     *
     * @return set of languages
     */
    public static Set<String> getLangs(){
        return languages;
    }

    /**
     *
     * move the city posting to the disk and clear it from the main memory
     *
     * @param savePath path to save the file
     */
    public static void moveCitytoDisk(String savePath,boolean stem){
        try {
            FileWriter fw=new FileWriter(savePath+"/cities.txt");
            BufferedWriter bw=new BufferedWriter(fw);
            TreeSet<String> t=new TreeSet<>(cityPosting.keySet());//sort by the city name
            for(String s: t) {
                String pos="";
                if(s.length()>3)
                    pos=search(savePath,s.toLowerCase(),stem, s.toLowerCase().charAt(0)+"");
                String[] split=cityPosting.get(s).split(",");
                for(String doc:split){
                    if(!pos.contains(doc))
                        pos+=(","+doc);
                }
                bw.write(s+": " + pos+"\n");//write each city to the files
            }
            fw.flush();// flush and close the file
            bw.flush();
            fw.close();
            bw.close();
        } catch(IOException e){e.printStackTrace();}
        cityPosting.clear();//clear the city posting from main memory
    }

    /**
     *
     * move to the disk the terms posting and the information about the docs
     * terms postings are save to temporary files
     * clear the term posting and information of the docs from main memory
     *
     * @param savePath the path to save all files to
     * @param isStem boolean which shows if stemming is on
     */
    public static void moveToMem(String savePath, boolean isStem){
        char c='a';//set a char to know if stemming is on
        if(isStem)
            c='b';
        try {
            FileWriter fw=new FileWriter(savePath+"/"+fileNum+c+".txt");//open a new temporary file
            BufferedWriter bw=new BufferedWriter(fw);
            TreeSet<String> t=new TreeSet<>(postingFile.keySet());//sort all the terms by alphabetical order
            for(String s: t) {
                bw.write(postingFile.get(s)+"\n");//write each term to the file
            }
            fw.flush();//flush and close the file
            bw.flush();
            fw.close();
            bw.close();
        } catch(IOException e){e.printStackTrace();}
        postingFile.clear();//clear the term posting from main memory
        fileNum++;
        try {
            FileWriter fw=new FileWriter(savePath+"/docs"+c+".txt",true);//open the doc file
            BufferedWriter bw=new BufferedWriter(fw);
            for(String s: docs.keySet()) {
                bw.write(s+":"+docs.get(s)+"\n");//write each doc to the file
            }
            fw.flush();//flush and close the file
            bw.flush();
            fw.close();
            bw.close();
        } catch(IOException e){e.printStackTrace();}
        docs.clear();//clear the information about the city
    }


    /**
     *
     * check if a term is in the term dictionary
     *
     * @param term the term we want to check
     * @returntrue if the dictionary contains the term, false otherwise
     */
    public static boolean containsTerm (String term){
        return dictionary.containsKey(term.toLowerCase())|| dictionary.containsKey(term.toUpperCase());
    }

    /**
     *
     * return the number of the terms save in the dictionary
     *
     * @return the size of the dictionary
     */
    public static int getTermNum(){
        return dictionary.size();
    }

    /**
     *
     * clear the term and cities dictionaries from main memory
     *
     */
    public static void clearDict(){
        if(dictionary!=null)
            dictionary.clear();
        if(cities!=null)
            cities.clear();
        fileNum=1;
    }

    /**
     *
     * sort the dictionary by alphabetical order and save it to disk with the frequency of each term
     *
     * @param path the path to save all files to
     * @param stem boolean which shows if stemming is on
     */
    public static void sortDict(String path, boolean stem){
        char c='a';//set a char to know if stemming is on
        if(stem){
            c='b';
        }
        try {
            FileWriter fw=new FileWriter(path+"/dictionary"+c+".txt");//open a dictionary file
            BufferedWriter bw=new BufferedWriter(fw);
            TreeSet<String> t=new TreeSet<>(dictionary.keySet());//sort the term dictionary
            for(String s: t) {
                bw.write("Term: "+s+", f: "+(dictionary.get(s)).toString()+"\n");//write each term and the frequency in all the corpus
            }
            fw.flush();//flush and close the file
            bw.flush();
            fw.close();
            bw.close();
        } catch(IOException e){e.printStackTrace();}
    }

    /**
     *
     * divide all the temporary posting files to alphabetical postings files
     *
     * @param path path to the temporary posting file
     * @param isStem boolean which shows if stemming is on
     */
    public static void divide(String path, boolean isStem){
        char stem='a';//set a char to know if stemming is on
        if(isStem)
            stem='b';
        File file=new File(path);
        String[] fileList=file.list();//get all files in the path
        String s="";
        for(String f: fileList){
            if(f.charAt(0)>='1' && f.charAt(0)<='9') {//if a file is a temporary posting file
                try {
                    String p = new String(Files.readAllBytes(Paths.get(path + "/" + f)), StandardCharsets.UTF_8);//read the file
                    String[] lines = p.split("\n");//split the files by line
                    char c , curr='1';
                    for(String line:lines){
                        if(!line.equals("")) {
                            c = line.charAt(0);//get the first char
                            if (c < 'a' || c > 'z')
                                c = '1';
                            if (curr != c) {//if the firt char is different from the previous one
                                if (curr == '1') {//move to disk all the terms which begin in the previous letter
                                    add(s, path + "/nums" + stem + ".txt");
                                } else {
                                    add(s, path + "/" + curr + stem + ".txt");
                                }
                                curr = c;//set the first char as current
                                s = "";//rest the string

                            }
                            s += ("\n" + line);//add the term to the string

                        }
                    }
                    File t=new File(path + "/" + f);//delete the temporary file
                    t.delete();
                } catch (IOException e) {e.printStackTrace();
                }
            }
        }
        sort(path,stem);//sort all the alphabetical postings files
    }

    /**
     *
     * add a string to a file by the given file
     *
     * @param text the string to save in the file
     * @param path path to the file including the name of the file and suffix
     */
    private static void add(String text,String path){
        try {
            File yourFile = new File(path);
            yourFile.createNewFile();//create the file if it does not exist
            FileWriter fw=new FileWriter(path,true);
            BufferedWriter bw=new BufferedWriter(fw);
            bw.write(text);//write the text
            fw.flush();//flush and close the file
            bw.flush();
            fw.close();
            bw.close();
        } catch(IOException e){e.printStackTrace();}
    }

    /**
     *
     * sorts all the terms postings and delete duplicates
     *
     * @param path path where all the posting are
     * @param stem  a char to know if stemming is on
     */
    private static void sort(String path, char stem){
        File file=new File(path);
        String[] fileList=file.list();//get list of all file in the path
        Map<String,String> terms=new LinkedHashMap<>();
        for(String f: fileList){
            if(f.charAt(0)>='a' && f.charAt(0)<='z' && f.charAt(f.length()-5)==stem) {//check if it is a posting file
                try {
                    String p = new String(Files.readAllBytes(Paths.get(path + "/" + f)), StandardCharsets.UTF_8);//read a posting file
                    String[] lines = p.split("\n");//split by lines
                    for(int i=0;i<lines.length;i++){
                        if(lines[i].split(":").length>1) {
                            if(!terms.containsKey(lines[i].split(":")[0]))
                                terms.put(lines[i].split(":")[0], lines[i].split(":")[1]);
                            else{
                                String temp=terms.get(lines[i].split(":")[0]);
                                temp+=(","+lines[i].split(":")[1]);
                                terms.replace(lines[i].split(":")[0],temp);
                            }
                        }
                    }
                    FileWriter fw = new FileWriter(path + "/" + f);
                    BufferedWriter bw = new BufferedWriter(fw);
                    TreeSet<String> t = new TreeSet<>(terms.keySet());//sort the lines by term
                    String curr = "-1";
                    int i=0;
                    for (String term : t) {
                        bw.write(term+":"+terms.get(term)+"\n");//write the term and the posting
                    }
                    fw.flush();//flush and close the file
                    bw.flush();
                    fw.close();
                    bw.close();
                } catch (IOException e) {e.printStackTrace();}
            }
            terms.clear();
        }


    }

    /**
     *
     * this function search a term in the posting file and return list of doc and locations
     *
     * @param path path where all posting files are
     * @param toFind term to search
     * @param toStem if stemming is on
     * @return list of doc and locations which the term appears
     */
    public static String search(String path, String toFind, boolean toStem, String fileName){
        if(!fileName.equals("cities.txt")) {
            if (toStem)//add stem char
                fileName += "b.txt";
            else
                fileName += "a.txt";
        }
        String p="";
        try {
            p = new String(Files.readAllBytes(Paths.get(path + "/" + fileName)), StandardCharsets.UTF_8);//read a posting file
        }catch (IOException e){}
        String[] lines=p.split("\n");
        int start = 0;
        int end = lines.length - 1;
        while (start <= end) {//to a binary search
            int middle = (start + end) / 2;
            if (toFind.equals(lines[middle].split(":")[0])) {
                return lines[middle].split(":")[1];
            }
            else if (toFind.compareTo(lines[middle].split(":")[0])<0) {
                end = middle - 1;
            }
            else if (toFind.compareTo(lines[middle].split(":")[0])>0) {
                start = middle + 1;
            }

        }
        return "";//string not found
    }

    public static Set<String> getCities() {
        return cityPosting.keySet();
    }
}
