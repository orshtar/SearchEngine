package Model;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * this class gets a folder with a file read it and split by docs
 * then send the doc to the parse
 * also the class encode the number of file and save all the full names
 *
 */
public class ReadFile {

    private static Map<String,Integer > filesNum;
    private static int shortName;
    public int docsNum;

    /**
     *  a simple constructor
     */
    public ReadFile(){
        filesNum=new LinkedHashMap<>();
        shortName=0;
        docsNum=0;
    }

    /**
     *
     * this function open a file and divide it to docs sends to the parse
     * also it extract information about the file like language the city  and the number of the doc
     * and encode the number of file
     *
     * @param path path to a file
     * @param stem boolean which shows if stemming is on
     * @param savePath path to save the posting files
     */
    public void read(String path,boolean stem, String savePath){
        File file=new File(path);
        String[] fileList=file.list();
        if(fileList!=null) {
            for (String name : fileList) {
                String p = path + "/" + name;//path to the file
                String f = "";
                String city = "";
                String language = "";
                try {
                    f = new String(Files.readAllBytes(Paths.get(p)), StandardCharsets.UTF_8);//read the file
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
                String[] docs = f.split("<DOC>");//split by docs
                for (String doc : docs) {
                    String[] text = doc.split("<TEXT>");//split by text
                    if (text.length >= 2) {
                        String[] cities = text[0].split("<F P=104>");//split by the tag of city
                        if (cities.length >= 2) {
                            String[] cities2 = cities[1].split("</F>");
                            if (cities2[0].length() > 0) {
                                String[] temp = cities2[0].split(" ");
                                int j = 0;
                                while (j < temp.length && temp[j].equals(""))
                                    j++;
                                city = temp[j];//save the city
                            }
                        }
                        String[] langs = text[1].split("<F P=105>");//split by tag of language
                        if (langs.length >= 2) {
                            String[] langs2 = langs[1].split("</F>");
                            if (langs2[0].length() > 0) {
                                String[] temp = langs2[0].split(" ");
                                int j = 0;
                                while (j < temp.length && temp[j].equals(""))
                                    j++;
                                language = temp[j];//save the language
                            }
                        }
                        String[] docNumbers = text[0].split("<DOCNO>");//split by the doc number
                        String docNO = docNumbers[1];
                        int i = 0;
                        int j=0;
                        while (docNO.charAt(i) == ' ')
                            i++;
                        j=i;
                        while (docNO.charAt(i) != ' ' && docNO.charAt(i) != '<')
                            i++;
                        String[] strings = docNO.substring(j, i).split("-");//split the doc number by -
                    /*if(strings.length>=2){
                        docNO=getFileName(strings[0]);//encode the file num
                        try {
                            docNO += ("-" + Integer.parseInt(strings[1]));//add the doc second part without zeros at the start
                        }catch (NumberFormatException e){
                            docNO += ("-" +strings[1]);
                        }
                    }
                    else*/
                        docNO = docNO.substring(j, i);//save as is
                        String[] text2 = text[1].split("</TEXT>");
                        Parse parser = new Parse();
                        docsNum++;
                        parser.parse(text2[0], docNO, city, stem, language, false);//call the parser
                    }
                }
                Indexer.moveToMem(savePath, stem);//move to disk the posting files of this file
            }
        }
    }

    /**
     *
     * return the encode of the file number
     *
     * @param name strung to encode
     * @return the string encoded
     */
    public static String getFileName(String name){
        if(!filesNum.containsKey(name)) {//if the name was not save add it to encoded map
            filesNum.put(name, shortName);
            shortName++;
        }
        return filesNum.get(name) + "";//return the encode version
    }

    /**
     *
     * clear the main memory of the class
     *
     */
    public static void clear(){
        if(filesNum!=null)
            filesNum.clear();
        shortName=0;
    }
}
