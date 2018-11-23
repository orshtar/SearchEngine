package sample;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Parse {

    private static List<String> stopWords;

    public static void parse(String doc){
        String docNO="";
        int i=0;
        while(doc.charAt(i+12)!=' '){
            docNO+=doc.charAt(i+12);
            i++;
        }
        while(true){
            if(doc.charAt(i)=='<' && doc.charAt(i+1)=='T' && doc.charAt(i+2)=='E' &&
                    doc.charAt(i+3)=='X' && doc.charAt(i+4)=='T')
                break;
            if(i==doc.length())
                break;
            i++;
        }
        if(i<doc.length()) {
            i += 6;
            String text = doc.substring(i);
            String[] splitText = text.split(" ");
            int count = 0;
            for (i = 0; i < splitText.length; i++) {
                if (!splitText[i].equals("") && !stopWords.contains(splitText[i])) {
                    String term = "";
                    double num = 0;/*isNumber(splitText[i]);*/
                    if (num != -1) {
                        if (splitText[i].charAt(0) != '$' && !splitText[i + 1].equals("Dollars") && !splitText[i + 2].equals("Dollars")) {

                            if ((num >= 1000 && num < 1000000) || splitText[i + 1].equals("Thousand") || splitText[i + 2].equals("Thousand")) {
                                num = num / 1000;
                                term = num + "K";
                            } else if ((num >= 1000000 && num < 1000000000) || splitText[i + 1].equals("Million") || splitText[i + 2].equals("Million")) {
                                num = num / 1000000;
                                term = num + "M";
                            } else if ((num >= 1000000000) || splitText[i + 1].equals("Billion") || splitText[i + 2].equals("Million") || splitText[i + 2].equals("Million")) {
                                num = num / 1000000000;
                                term = num + "B";
                            } else {
                                term = num + "";
                            }
                        } else {
                            if (splitText[i + 1].equals("million") || splitText[i + 1].equals("billion") || splitText[i + 1].equals("trillion")) {
                                term = term + splitText[i + 1];
                            }
                            if (splitText[i + 1].equals("Dollars") || (splitText[i + 1].equals("U.S.") && splitText[i + 2].equals("dollars"))
                                    || splitText[i].charAt(0) == '$') {
                                term = term + " Dollars";
                            }
                        }
                        if (splitText[i + 1].equals("percent") || splitText[i].charAt(0) == '$') {
                            term = term + " Dollars";
                        }

                    }
                }
            }
        }
    }
    public static void initStopWords(String path){
        stopWords=new LinkedList<>();
        Scanner sc;
        File file=new File(path);
        try {
            sc = new Scanner(file);
            while(sc.hasNextLine()){
                String line=sc.nextLine();
                stopWords.add(line);
            }
        } catch(FileNotFoundException e){}

    }
}
