package sample;

import java.io.File;

public class Parse {

    public static void parse(String doc){
        String docNO="";
        int i=0;
        while(doc.charAt(i+12)!=' '){
            docNO+=doc.charAt(i+12);
            i++;
        }
        while(true){
            if(doc.charAt(i)!='<' && doc.charAt(i+1)=='T' && doc.charAt(i+2)=='E' &&
                    doc.charAt(i+3)=='X' && doc.charAt(i+4)=='T')
                break;
            i++;
        }
        i+=5;
        String text=doc.substring(i);
        String[] splitText=text.split(" ");

    }

    private void contains(String word){
        File stop=new File("stop_words.txt");
    }
}
