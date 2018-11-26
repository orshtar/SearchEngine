package sample;

import java.io.*;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

public class Indexer {

    private static Hashtable<String,TermObject > dictionary=new Hashtable<>();
    private static HashMap<Integer,String> postingFile=new HashMap<>();
    private static String fileName="posting1.txt";
    private static int lineNum=0;


    public static void invertIndex(Map<String,Integer> map, String docNum){
        for(String term: map.keySet()){
            if(lineNum>=5000){
                moveToMem();
            }
            if(dictionary.containsKey(term.toLowerCase())) {
                TermObject to=dictionary.get(term.toLowerCase());
                to.addDoc(docNum);
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
                    to.setTerm();
                }
                to.addDoc(docNum);
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
                TermObject newObj=new TermObject(term,1,docNum,fileName+"/"+lineNum);
                if(term.charAt(0)>='A' && term.charAt(0)<='Z')
                    dictionary.put(term.toUpperCase(),newObj);
                else
                    dictionary.put(term.toLowerCase(),newObj);
                postingFile.put(lineNum,docNum+": "+map.get(term)+"/null");
                lineNum++;
            }

        }
        Parse.clear();
    }

    public static void moveToMem(){
        try {
            File file = new File("folder/"+fileName);
            boolean ifCreate = file.createNewFile();
            if(ifCreate){
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
                int i=7;
                int num=0;
                while(fileName.charAt(i)!='.'){
                    num=num*10+(fileName.charAt(i)-'0');
                    i++;
                }
                num++;
                fileName="posting"+num+".txt";
            }
        } catch(IOException e){}

    }

    public static void printDict(){
        for(String t: dictionary.keySet())
            System.out.println((dictionary.get(t)).toString());
    }
}
