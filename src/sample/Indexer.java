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

    private static HashMap<String,TermObject > dictionary=new HashMap<>();
    private static HashMap<String,String> postingFile=new HashMap<>();
    private static String fileName="1.txt";
    private static int lineNum=0;
    private static HashMap<Character,Integer> linePos=new HashMap<>();

/*
    public static void invertIndex(Map<String,Integer> map, String docNum){
        for(String term: map.keySet()){
            if(lineNum>=30000){
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
                postingFile.put(lineNum,docNum+": "+map.get(term)+"/x");
                lineNum++;
            }

        }
        Parse.clear();
    }


    public static void moveToMem(){
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

    }
*/
    public static void printDict(){
        for(String t: dictionary.keySet())
            System.out.println((dictionary.get(t)).toString());
    }

    public static void invertIndex(Map<String,Integer> map, String docNum){
        for(String term: map.keySet()){
            if(lineNum>=100000)
                moveToMem();
            if(dictionary.containsKey(term.toLowerCase())){
                dictionary.get(term.toLowerCase()).addDoc(docNum);
            }
            else if(dictionary.containsKey(term.toUpperCase())){
                if(term.charAt(0)>='A' && term.charAt(0)<='Z'){
                    dictionary.get(term.toUpperCase()).addDoc(docNum);
                }
                else{
                    TermObject t=dictionary.get(term.toUpperCase());
                    t.addDoc(docNum);
                    t.setTerm();
                    dictionary.remove(term.toUpperCase());
                    dictionary.put(term.toLowerCase(),t);
                }
            }
            else{
                lineNum++;
                int num;
                char c=term.toLowerCase().charAt(0);
                if((c>='0' && c<='9') || c=='$' || c=='%')
                    c='1';
                if(linePos.containsKey(c)){
                    num=linePos.get(c);
                    linePos.replace(c,num+1);
                }
                else {
                    num=0;
                    linePos.put(c,num+1);
                }
                if(term.charAt(0)>='A' && term.charAt(0)<='Z'){
                    TermObject t=new TermObject(term.toUpperCase(),1,docNum,num+"");
                    dictionary.put(term.toUpperCase(),t);
                }
                else{
                    TermObject t=new TermObject(term.toLowerCase(),1,docNum,num+"");
                    dictionary.put(term.toLowerCase(),t);
                }
            }
            if(postingFile.containsKey(term.toLowerCase())){
                String newPos=postingFile.get(term.toLowerCase());
                newPos=newPos+", "+docNum+":"+map.get(term);
                postingFile.replace(term.toLowerCase(),newPos);
            }
            else{
                String newPos=docNum+":"+map.get(term);
                postingFile.put(term.toLowerCase(),newPos);
            }
        }
        Parse.clear();
    }

    public static void moveToMem() {
        SortedSet<String> keys=new TreeSet<>(postingFile.keySet());
        char curr='1';
        String file = "";
        try {
            file = new String(Files.readAllBytes(Paths.get("folder/"+curr+".txt")), StandardCharsets.UTF_8);
        } catch (IOException e) {
        }
        for(String term: keys) {
            if(term.toLowerCase().charAt(0) != curr){
                curr=term.toLowerCase().charAt(0);
                if(curr=='$' || curr=='%' ||(curr>='0' && curr<='9'))
                    curr='1';
                try {
                    file = new String(Files.readAllBytes(Paths.get("folder/"+curr+".txt")), StandardCharsets.UTF_8);
                } catch (IOException e) {

                }catch (InvalidPathException w){
                    System.out.println(term);
                }
            }
            String[] lines=file.split("\n");
            String row;
            if(dictionary.containsKey(term.toUpperCase()))
                row=dictionary.get(term.toUpperCase()).getPosting();
            else
                row=dictionary.get(term).getPosting();
            String oldLine=lines[Integer.parseInt(row)];
            String newLine=oldLine+","+postingFile.get(term)+"\n";
            file=file.replaceFirst(oldLine+"\n",newLine);
            File myFoo = new File("folder/"+curr+".txt");
            try {
                FileWriter fooWriter = new FileWriter(myFoo, false);
                fooWriter.write(file);
                fooWriter.close();
            }catch (IOException e){}


        }
    }

    public static void init(){
        try {
            String str="";
            for(int i=0; i<22300; i++)
                str+=(i+"\n");
            PrintWriter writer = new PrintWriter("folder/1.txt", "UTF-8");
            writer.print(str);
            writer.flush();
            writer = new PrintWriter("folder/a.txt", "UTF-8");
            writer.print(str);
            writer.flush();
            writer = new PrintWriter("folder/b.txt", "UTF-8");
            writer.print(str);
            writer.flush();
            writer = new PrintWriter("folder/c.txt", "UTF-8");
            writer.print(str);
            writer.flush();
            writer = new PrintWriter("folder/d.txt", "UTF-8");
            writer.print(str);
            writer.flush();
            writer = new PrintWriter("folder/e.txt", "UTF-8");
            writer.print(str);
            writer.flush();
            writer = new PrintWriter("folder/f.txt", "UTF-8");
            writer.print(str);
            writer.flush();
            writer = new PrintWriter("folder/g.txt", "UTF-8");
            writer.print(str);
            writer.flush();
            writer = new PrintWriter("folder/h.txt", "UTF-8");
            writer.print(str);
            writer.flush();
            writer = new PrintWriter("folder/i.txt", "UTF-8");
            writer.print(str);
            writer.flush();
            writer = new PrintWriter("folder/j.txt", "UTF-8");
            writer.print(str);
            writer.flush();
            writer = new PrintWriter("folder/k.txt", "UTF-8");
            writer.print(str);
            writer.flush();
            writer = new PrintWriter("folder/l.txt", "UTF-8");
            writer.print(str);
            writer.flush();
            writer = new PrintWriter("folder/m.txt", "UTF-8");
            writer.print(str);
            writer.flush();
            writer = new PrintWriter("folder/n.txt", "UTF-8");
            writer.print(str);
            writer.flush();
            writer = new PrintWriter("folder/o.txt", "UTF-8");
            writer.print(str);
            writer.flush();
            writer = new PrintWriter("folder/p.txt", "UTF-8");
            writer.print(str);
            writer.flush();
            writer = new PrintWriter("folder/q.txt", "UTF-8");
            writer.print(str);
            writer.flush();
            writer = new PrintWriter("folder/r.txt", "UTF-8");
            writer.print(str);
            writer.flush();
            writer = new PrintWriter("folder/s.txt", "UTF-8");
            writer.print(str);
            writer.flush();
            writer = new PrintWriter("folder/t.txt", "UTF-8");
            writer.print(str);
            writer.flush();
            writer = new PrintWriter("folder/u.txt", "UTF-8");
            writer.print(str);
            writer.flush();
            writer = new PrintWriter("folder/v.txt", "UTF-8");
            writer.print(str);
            writer.flush();
            writer = new PrintWriter("folder/w.txt", "UTF-8");
            writer.print(str);
            writer.flush();
            writer = new PrintWriter("folder/x.txt", "UTF-8");
            writer.print(str);
            writer.flush();
            writer = new PrintWriter("folder/y.txt", "UTF-8");
            writer.print(str);
            writer.flush();
            writer = new PrintWriter("folder/z.txt", "UTF-8");
            writer.print(str);
            writer.flush();

        }catch (IOException e){}
    }

}
