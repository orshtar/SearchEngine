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
            File f=new File(path+"/"+name+".txt");
            try{
                sc=new Scanner(f);
                while(sc.hasNextLine()){
                    String str=sc.nextLine();
                    if(str.equals("<DOC>")){
                        String 
                        while()
                    }
                }
            }
            catch(FileNotFoundException e){}



            }
        }
    }
}
