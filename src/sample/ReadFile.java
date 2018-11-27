package sample;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class ReadFile {

    public void read(String path){
        File file=new File(path);
        String[] fileList=file.list();
        for(String name: fileList) {
            String p=path+"/"+name;
            String f="";
            try {
                f= new String(Files.readAllBytes(Paths.get(p)), StandardCharsets.UTF_8);
            }catch (IOException e){}
            String[] docs=f.split("<DOC>");
            for(String doc:docs){
                String[] text=doc.split("<TEXT>");
                if(text.length>=2){
                    String[] docNumbers=text[0].split("<DOCNO>");
                    String docNO=docNumbers[1];
                    int i=0;
                    if(docNO.charAt(i)==' ')
                        i++;
                    while (docNO.charAt(i)!=' ' && docNO.charAt(i)!='<')
                        i++;
                    docNO=docNO.substring(0,i);
                    String[] text2=text[1].split("</TEXT>");
                    Parse.parse(text2[0],docNO);
                }
            }


        }
    }
}
