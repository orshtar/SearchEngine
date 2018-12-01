package sample;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class ReadFile {

    private static Map<String,Integer > filesNum;
    private static int shortName;

    public ReadFile(){
        filesNum=new LinkedHashMap<>();
        shortName=0;
    }

    public void read(String path,boolean stem){
        File file=new File(path);
        String[] fileList=file.list();
        for(String name: fileList) {
            String p=path+"/"+name;
            String f="";
            String city="";
            String docName="";
            try {
                f= new String(Files.readAllBytes(Paths.get(p)), StandardCharsets.UTF_8);
            }catch (IOException e){}
            String[] docs=f.split("<DOC>");
            for(String doc:docs){
                String[] text=doc.split("<TEXT>");
                if(text.length>=2){
                    String[] cities=text[0].split("<F P=104>");
                    if(cities.length>=2){
                        String[] cities2=cities[1].split("</F>");
                        if(cities2[0].length()>0) {
                            String[] temp = cities2[0].split(" ");
                            int j = 0;
                            while (j < temp.length && temp[j].equals(""))
                                j++;
                            city = temp[j];
                        }
                    }
                    String[] names=text[0].split("<H3> <TI> ");
                    if(names.length>=2){
                        String[] names2=names[1].split("</TI></H3>");
                        if(names2[0].length()>0) {
                            String[] temp = names2[0].split(" ");
                            int j = 0;
                            while (j < temp.length && temp[j].equals(""))
                                j++;
                            while (j < temp.length && !temp[j].equals("")) {
                                docName +=" ";
                                docName += temp[j];
                                j++;
                            }
                        }
                    }
                    else{
                        names=text[0].split("<HEADLINE>");
                        if(names.length>=2){
                            String[] names2=names[1].split("</HEADLINE>");
                            if(names2[0].length()>0) {
                                String[] temp = names2[0].split(" ");
                                int j = 0;
                                while (j < temp.length && temp[j].equals(""))
                                    j++;
                                while (j < temp.length && !temp[j].equals("")) {
                                    docName +=" ";
                                    docName += temp[j];
                                    j++;
                                }
                            }
                        }
                    }
                    String[] docNumbers=text[0].split("<DOCNO>");
                    String docNO=docNumbers[1];
                    int i=0;
                    if(docNO.charAt(i)==' ')
                        i++;
                    while (docNO.charAt(i)!=' ' && docNO.charAt(i)!='<')
                        i++;
                    String[] strings=docNO.substring(0,i).split("-");
                    if(strings.length>=2){
                        docNO=getFileName(strings[0]);
                        try {
                            docNO += ("-" + Integer.parseInt(strings[1]));
                        }catch (NumberFormatException e){
                            docNO += ("-" +strings[1]);
                        }
                    }
                    else
                        docNO=docNO.substring(0,i);
                    String[] text2=text[1].split("</TEXT>");
                    Parse parser=new Parse();
                    parser.parse(text2[0],docNO,city, stem,docName);
                }
            }
        }
    }

    public static String getFileName(String name){
        if(!filesNum.containsKey(name)) {
            filesNum.put(name, shortName);
            shortName++;
        }
        return filesNum.get(name) + "";
    }
}
