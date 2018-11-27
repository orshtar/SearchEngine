package sample;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

public class ReadFile {

    public void read(String path){
        Scanner sc;
        File file=new File(path);
        String[] fileList=file.list();
        for(String name: fileList){
            String docNO="";
            File f=new File(path+"/"+name);
            try{
                sc=new Scanner(f);
                while(sc.hasNextLine()){
                    String str=sc.nextLine();
                    if(str.equals("<DOC>")) {
                        docNO="";
                        str=sc.nextLine();
                        int i = 8;
                        while (str.charAt(i) != ' ' && str.charAt(i) != '<') {
                            docNO += str.charAt(i);
                            i++;
                        }
                    }
                    String doc="";
                    if(str.equals("<TEXT>")){
                        while(sc.hasNextLine() && !(str.equals("</TEXT>") || str.equals("</DOC>"))) {
                            doc += str;
                            str = sc.nextLine();
                        }
                        Parse.parse(doc,docNO);
                    }

                }
            }
            catch(FileNotFoundException e){System.out.println("why?????");}
        }
    }
}
