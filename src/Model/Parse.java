package Model;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.Month;
import java.util.*;

/**
 *
 * this class gets a text from read file and turn the words to term
 * by removing unnecessary punctuation and removing words which are in stop words file
 * the class also saves for each term the locations in the text
 *
 */
public class Parse {

    private static Set<String> stopWords;
    private static Set<Character> punctuation;
    private static Map<String,String> terms;
    private int max=0;

    /**
     *
     * this function gets a text from read file and turn the words to term and pass them to the indexer
     *
     * @param doc the text from the doc
     * @param docNO number of the doc
     * @param city city of the doc
     * @param stem boolean which shows if stemming is on
     * @param language the language of the doc
     */
    public Map<String, String> parse(String doc, String docNO, String city, boolean stem,String language, boolean isQ){
        Map<String, Integer> entities = new LinkedHashMap<>();
        int position=0, docLen=0;
        String text = doc;
        text+=" , , , ,";
        String[] splitText = text.split(" ");//split the text by spaces
        int skip;
        if(!city.equals("")) {
            city = cleanTerm(city);//clean the city from unnecessary punctuation
            city=city.toUpperCase();//save the city in upper case letters
        }
        if(!language.equals("")) {
            language = cleanTerm(language);//clean the language from unnecessary punctuation
        }
        double num;
        Stemmer stemmer=new Stemmer();
        for (int i = 0; i < splitText.length; i++) {
            position++;
            skip = 0;
            splitText[i] = cleanTerm(splitText[i]);//clean the word from unnecessary punctuation
            if (!splitText[i].equals("")) {
                String str ;
                num = isNumber(splitText[i]);//check if a string is a number
                String[] s = null;
                if (num != -1) {//the string is a number
                    String sNum;//save the number as string with only max of two digits after the point
                    if(num%1==0)
                        sNum = ((int)num) +"";
                    else
                        sNum=String.format("%.2f",num);
                    if (splitText[i].charAt(0) == '$') {//the word is a number with $ at the start
                        if ((cleanTerm(splitText[i + 1])).equalsIgnoreCase("million")) {//add M if necessary and the word Dollars
                            str = sNum + " M" + " Dollars";
                            skip = 1;
                        } else if ((cleanTerm(splitText[i + 1])).equalsIgnoreCase("billion")) {
                            num = num * 1000;
                            if(num%1==0)
                                sNum = ((int)num) +"";
                            else
                                sNum=String.format("%.2f",num);
                            str = sNum + " M" + " Dollars";
                            skip = 1;
                        } else if ((cleanTerm(splitText[i + 1])).equalsIgnoreCase("trillion")) {
                            num = num * 1000000;
                            if(num%1==0)
                                sNum = ((int)num) +"";
                            else
                                sNum=String.format("%.2f",num);
                            str = sNum + " M" + " Dollars";
                            skip = 1;
                        } else {
                            if (num >= 1000000) {
                                num = num / 1000000;
                                if(num%1==0)
                                    sNum = ((int)num) +"";
                                else
                                    sNum=String.format("%.2f",num);
                                str = sNum + " M" + " Dollars";
                            } else {
                                str = sNum + " Dollars";
                            }
                        }
                    } else if ((cleanTerm(splitText[i + 1])).equalsIgnoreCase("Dollars")) {//the word is a number and there is a dollars after
                        if (num >= 1000000) {//add M if necessary
                            num = num / 1000000;
                            if(num%1==0)
                                sNum = ((int)num) +"";
                            else
                                sNum=String.format("%.2f",num);
                            str = sNum + " M" + " Dollars";
                        } else {
                            str = sNum + " Dollars";
                        }
                        skip = 1;
                    } else if ((cleanTerm(splitText[i + 2])).equalsIgnoreCase("Dollars")) {//the word is number and the second word is dollars
                        if (isNumber(splitText[i + 1]) != -1) {//add m if necessary
                            str = splitText[i] + " " + splitText[i + 1] + " " + "Dollars";
                            skip = 2;
                        } else if (splitText[i + 1].equalsIgnoreCase("m")) {
                            str = sNum + " M Dollars";
                            skip = 2;
                        } else if (splitText[i + 1].equalsIgnoreCase("bn")) {
                            num = num * 1000;
                            if(num%1==0)
                                sNum = ((int)num) +"";
                            else
                                sNum=String.format("%.2f",num);
                            str = sNum + " M Dollars";
                            skip = 2;
                        } else
                            str = splitText[i];
                    } else if ((cleanTerm(splitText[i + 3])).equalsIgnoreCase("Dollars")) {// the word is a number and the third word is dollars
                        if ((cleanTerm(splitText[i + 2])).equalsIgnoreCase("U.S.")) {//the second wors is U.S.
                            if ((cleanTerm(splitText[i + 1])).equalsIgnoreCase("million")) {//add m if necessary
                                str = sNum + " M Dollars";
                                skip = 3;
                            } else if ((cleanTerm(splitText[i + 1])).equalsIgnoreCase("billion")) {
                                num = num * 1000;
                                if(num%1==0)
                                    sNum = ((int)num) +"";
                                else
                                    sNum=String.format("%.2f",num);
                                str = sNum + " M Dollars";
                                skip = 3;
                            } else if ((cleanTerm(splitText[i + 1])).equalsIgnoreCase("trillion")) {
                                num = num * 1000000;
                                if(num%1==0)
                                    sNum = ((int)num) +"";
                                else
                                    sNum=String.format("%.2f",num);
                                str = sNum + " M Dollars";
                                skip = 3;
                            } else {
                                str = splitText[i];
                            }
                        } else {
                            str = splitText[i];//save word as is
                        }
                    } else if((cleanTerm(splitText[i + 1])).equals("miles")){// if the word is number and the second word is miles
                        str=splitText[i]+" "+cleanTerm(splitText[i + 1]);//save the term together
                        skip=1;
                    }else if(splitText[i + 1].equalsIgnoreCase("p.m.") || splitText[i + 1].equalsIgnoreCase("a.m.")){// if the word is number and the second word is a.m. or p.m.
                        str=splitText[i]+" "+splitText[i + 1];//save the terms together
                        skip=1;
                    }
                    else if (splitText[i].charAt(splitText[i].length() - 1) == '%') {//the word is number ending with %
                        str = splitText[i];//save as is
                    } else if ((cleanTerm(splitText[i + 1])).equalsIgnoreCase("percent")
                            || (cleanTerm(splitText[i + 1])).equalsIgnoreCase("percentage")) {// if the word is number and the second word is percent or percentage
                        str = splitText[i] + "%";//add % to the end of the file
                        skip = 1;
                    } else if (!isMonth(cleanTerm(splitText[i + 1])).equals("")) {//the word i number with a month after
                        if (num < 10)
                            str =isMonth(cleanTerm(splitText[i + 1])) + "-0" + splitText[i];//save MM-DD
                        else if (num < 32)
                            str = isMonth(cleanTerm(splitText[i + 1]))+ "-" + splitText[i];//save MM-DD
                        else {
                            if (num > 1000)
                                str = splitText[i] + "-" + isMonth(cleanTerm(splitText[i + 1]));//save YYYY-MM
                            else
                                str = "19" + splitText[i] + "-" + isMonth(cleanTerm(splitText[i + 1]));//save YYYY-MM
                        }
                        skip = 1;
                    } else if (i > 0 && !isMonth(cleanTerm(splitText[i- 1])).equals("")) {//the word i number with a month before
                        if (num > 31) {
                            if (num > 1000)
                                str = splitText[i] + "-" + isMonth(cleanTerm(splitText[i- 1]));//save YYYY-MM
                            else
                                str = "19" + splitText[i] + "-" +isMonth(cleanTerm(splitText[i- 1]));//save YYYY-MM
                        } else {
                            if (num < 10)
                                str =isMonth(cleanTerm(splitText[i- 1])) + "-0" + splitText[i];//save MM-DD
                            else
                                str =isMonth(cleanTerm(splitText[i- 1])) + "-" + splitText[i];//save MM-DD
                        }
                    } else {//the word is just a number
                        if (1000 <= num && num < 1000000) {//add K/M/B if necessary
                            num = num / 1000;
                            if(num%1==0)
                                sNum = ((int)num) +"";
                            else
                                sNum=String.format("%.2f",num);
                            str = sNum + "K";
                        } else if ((cleanTerm(splitText[i + 1])).equalsIgnoreCase("thousand")) {
                            str = sNum + "K";
                            skip = 1;
                        } else if (1000000 <= num && num < 1000000000) {
                            num = num / 1000000;
                            if(num%1==0)
                                sNum = ((int)num) +"";
                            else
                                sNum=String.format("%.2f",num);
                            str = sNum + "M";
                        } else if ((cleanTerm(splitText[i + 1])).equalsIgnoreCase("million")) {
                            str = sNum + "M";
                            skip = 1;
                        } else if (num >= 1000000000) {
                            num = num / 1000000000;
                            if(num%1==0)
                                sNum = ((int)num) +"";
                            else
                                sNum=String.format("%.2f",num);
                            str = sNum + "B";
                        } else if ((cleanTerm(splitText[i + 1])).equalsIgnoreCase("billion")) {
                            str = sNum + "B";
                            skip = 1;
                        } else if ((cleanTerm(splitText[i + 1])).equalsIgnoreCase("trillion")) {
                            num = num * 1000;
                            if(num%1==0)
                                sNum = ((int)num) +"";
                            else
                                sNum=String.format("%.2f",num);
                            str = sNum + "B";
                            skip = 1;
                        } else {
                            if (isNumber(splitText[i + 1]) != -1 && splitText[i + 1].contains("/")) {
                                str = sNum + " " + splitText[i + 1];
                                skip = 1;
                            } else {
                                str = sNum + "";
                            }
                        }
                    }
                } else {//the word is not a number
                    if (splitText[i].equalsIgnoreCase("between") && isNumber(splitText[i + 1]) != -1
                            && splitText[i + 2].equalsIgnoreCase("and") && isNumber(splitText[i + 3]) != -1) {//if the word is a range
                        str = isNumber(splitText[i + 1]) + "-" + isNumber(splitText[i + 3]);//save the range
                        skip = 3;
                    } else {
                        str = splitText[i];//split the words by punctuation
                        if (str.contains(",")) {
                            s = str.split(",");
                            if (s.length > 1) {
                                str = cleanTerm(s[0]);//clean the firs
                                splitText[i]=cleanTerm(s[1]);//enter the second word
                                skip=-1;//to the term once again
                            }
                        }
                        if (str.contains("\"")) {
                            s = str.split("\"");
                            if (s.length > 1) {
                                str = cleanTerm(s[0]);//clean the firs
                                splitText[i]=cleanTerm(s[1]);//enter the second word
                                skip=-1;//to the term once again
                            }
                        }
                        if (str.contains(".") ) {
                            s = str.split(".");
                            if (s.length > 1) {
                                str = cleanTerm(s[0]);//clean the firs
                                splitText[i]=cleanTerm(s[1]);//enter the second word
                                skip=-1;//to the term once again
                            }
                        }
                        if (str.contains("\n")) {
                            s = str.split("\n");
                            if (s.length > 1) {
                                str = cleanTerm(s[0]);//clean the firs
                                splitText[i]=cleanTerm(s[1]);//enter the second word
                                skip=-1;//to the term once again
                            }
                        }
                        if (str.contains("\r")) {
                            s = str.split("\r");
                            if (s.length > 1) {
                                str = cleanTerm(s[0]);//clean the firs
                                splitText[i]=cleanTerm(s[1]);//enter the second word
                                skip=-1;//to the term once again
                            }
                        }
                        if (str.contains("\r\n")) {
                            s = str.split("\r\n");
                            if (s.length > 1) {
                                str = cleanTerm(s[0]);//clean the firs
                                splitText[i]=cleanTerm(s[1]);//enter the second word
                                skip=-1;//to the term once again
                            }
                        }
                        if (str.contains("/")) {
                            s = str.split("/");
                            if (s.length > 1) {
                                str = cleanTerm(s[0]);//clean the firs
                                splitText[i]=cleanTerm(s[1]);//enter the second word
                                skip=-1;//to the term once again
                            }
                        }
                        if (str.contains(" ")) {
                            s = str.split(" ");
                            if (s.length > 1) {
                                str = cleanTerm(s[0]);//clean the firs
                                splitText[i]=cleanTerm(s[1]);//enter the second word
                                skip=-1;//to the term once again
                            }
                        }
                        if (str.contains("--")) {
                            s = str.split("--");
                            if (s.length > 1) {
                                str = cleanTerm(s[0]);//clean the firs
                                splitText[i]=cleanTerm(s[1]);//enter the second word
                                skip=-1;//to the term once again
                            }
                        }
                        if (str.contains("\'")) {
                            s = str.split("'");
                            if (s.length > 1) {
                                str = cleanTerm(s[0]);//clean the firs
                                splitText[i]=cleanTerm(s[1]);//enter the second word
                                skip=-1;//to the term once again
                            }
                        }
                        if (str.contains(":")) {
                            s = str.split(":");
                            if (s.length > 1) {
                                str = cleanTerm(s[0]);//clean the firs
                                splitText[i]=cleanTerm(s[1]);//enter the second word
                                skip=-1;//to the term once again
                            }
                        }
                        if (str.contains("`")) {
                            s = str.split("`");
                            if (s.length > 1) {
                                str = cleanTerm(s[0]);//clean the firs
                                splitText[i]=cleanTerm(s[1]);//enter the second word
                                skip=-1;//to the term once again
                            }
                        }
                        if (str.contains("(")) {
                            s = str.split("\\(");
                            if (s.length > 1) {
                                str = cleanTerm(s[0]);//clean the firs
                                splitText[i]=cleanTerm(s[1]);//enter the second word
                                skip=-1;//to the term once again
                            }
                        }
                        if (str.contains(")")) {
                            s = str.split("\\)");
                            if (s.length > 1) {
                                str = cleanTerm(s[0]);//clean the firs
                                splitText[i]=cleanTerm(s[1]);//enter the second word
                                skip=-1;//to the term once again
                            }
                        }
                    }
                }
                if (!stopWords.contains(str.toLowerCase())) {//if the word is not a stop word
                    docLen++;
                    if(stem && !Indexer.containsTerm(str)){//if the stem is on and the dictionary does not contains the term
                        stemmer.add(str.toCharArray(),str.toCharArray().length);
                        stemmer.stem();//stem the word
                        str=stemmer.toString();
                    }
                    if (terms.containsKey(str.toUpperCase())) {
                        if(str.charAt(0) >= 'a' && str.charAt(0) <= 'z'){
                            String temp = terms.get(str.toUpperCase());//get the positions of the term
                            temp+=("*"+position);//add the new position
                            if(temp.split("\\*").length>max)//check if this is the most frequent term in doc
                                max=temp.split("\\*").length;
                            terms.remove(str.toUpperCase());
                            terms.put(str.toLowerCase(), temp);
                        }
                        else {
                            String temp = terms.get(str.toUpperCase());//get the positions of the term
                            temp+=("*"+position);//add the new position
                            if(temp.split("\\*").length>max)//check if this is the most frequent term in doc
                                max=temp.split("\\*").length;
                            terms.replace(str.toUpperCase(), temp);
                        }
                    } else if (terms.containsKey(str.toLowerCase())) {
                        String temp = terms.get(str.toLowerCase());//get the positions of the term
                        temp+=("*"+position);//add the new position
                        if(temp.split("\\*").length>max)//check if this is the most frequent term in doc
                            max=temp.split("\\*").length;
                        terms.replace(str.toLowerCase(), temp);
                    } else {
                        if (!str.equals("")) {
                            if (str.charAt(0) >= 'a' && str.charAt(0) <= 'z')//save the word in lower case or upper case by the first letter
                                str = str.toLowerCase();
                            else
                                str = str.toUpperCase();
                            terms.put(str, position+"");
                        }
                    }
                    i = i + skip;//add the number of terms united
                }
            }
        }
        if(!isQ) {
            for(String t: terms.keySet()){
                if(Character.isUpperCase(t.charAt(0)))
                    entities.put(t,0);
            }
            entities=findDominant(entities);
            Indexer i = new Indexer();
            i.invertIndex(terms, docNO, max, docLen, city, language, entities);//call the indexer
            return null;
        }else{
            return terms;
        }
    }

    private Map<String, Integer> findDominant(Map<String, Integer> entities) {
        Map<Integer,List<String>> m=new LinkedHashMap<>();
        for(String key:entities.keySet()){
            String post=terms.get(key);
            int f=post.split("\\*").length;
            if(m.containsKey((f))){
                List<String> temp=m.get(f);
                temp.add(key);
                m.replace(f,temp);
            }
            else {
                List<String> temp=new LinkedList<>();
                temp.add(key);
                m.put(f, temp);
            }
        }
        TreeSet<Integer> t=new TreeSet<>(m.keySet());
        TreeSet<Integer> t2=(TreeSet)t.descendingSet();
        Map<String, Integer> ans=new LinkedHashMap<>();
        int i=0;
        for(Integer f:t2){
            if(i>=5)
                break;
            for(String entity:m.get(f)){
                if(i>=5)
                    break;
                ans.put(entity, f);
                i++;
            }
        }
        return ans;
    }

    /**
     *
     * clean a term from unnecessary punctuation
     *
     * @param s string to clean
     * @return the clean string
     */
    private String cleanTerm(String s){
        String ans=s;
        while(!ans.equals("") && punctuation.contains(ans.charAt(0))){//while there are punctuation at the start remove them
            ans=ans.substring(1);
        }
        while(!ans.equals("") && punctuation.contains(ans.charAt(ans.length()-1))){//while there are punctuation at the end remove them
            ans=ans.substring(0,ans.length()-1);
        }
        return ans;
    }

    /**
     *
     * check if a string is a month
     *
     * @param s string to check
     * @return the number of the month(MM)
     */
    private String isMonth(String s){
        String ans="";
        if(s.length()>3) {//if the string is full month name
            int m;
            try {
                m = Month.valueOf(s.toUpperCase()).getValue();//get the number of the month
                if (m < 10)//add zero if necessary
                    ans = "0" + m;
                else
                    ans = m + "";
            } catch (IllegalArgumentException e) {//s was not a month
                ans = "";
            }
        }
        else{//the string is in the short form of the monthe
            s=s.toUpperCase();
            switch (s) {//try to find a match
                case "JAN":  ans = "01";
                    break;
                case "FEB":  ans ="02";
                    break;
                case "MAR":  ans = "03";
                    break;
                case "APR":  ans ="04";
                    break;
                case "MAY":  ans = "05";
                    break;
                case "JUN":  ans = "06";
                    break;
                case "JUL":  ans = "07";
                    break;
                case "AUG":  ans = "08";
                    break;
                case "SEP":  ans = "09";
                    break;
                case "OCT": ans = "10";
                    break;
                case "NOV": ans = "11";
                    break;
                case "DEC": ans = "12";
                    break;
            }
        }
        return ans;
    }

    /**
     *
     * check if a string is a number
     *
     * @param s string to check
     * @return the number if the string is number otherwise return -1
     */
    private double isNumber(String s) {
        if(s.length()>0 && ((s.charAt(0)>='0' && s.charAt(0)<='9') || s.charAt(0)=='$')){
            if(s.charAt(0)=='$') {
                s = s.substring(1);//remove the $ from the start
                if(s.length()==0)
                    return -1;
            }
            if(s.charAt(s.length()-1)=='%')
                s=s.substring(0,s.length()-1);//remove the % from the end
            if(s.contains(",")){
                s=s.replaceAll(",","");
            }
            try{
                return Double.parseDouble(s);//if it is a normal number
            }
            catch (NumberFormatException e){//if it is a fraction
                if(s.contains("/")){
                    String[] strings=s.split("/");
                    try{
                        if(strings.length==2) {
                            double num1 = Double.parseDouble(strings[0]);//if it is a normal number
                            double num2 = Double.parseDouble(strings[1]);//if it is a normal number
                            if(num2==0)
                                return 0;
                            return num1 / num2;//return the value of the fraction
                        }
                    }
                    catch (NumberFormatException e1){
                        return -1;
                    }
                }
            }
        }
        return -1;
    }

    /**
     *
     * init a list of stop word from a file and init a list of punctuation
     *
     * @param path path to the stop word file
     */
    public static void initStopWords(String path){
        stopWords=new LinkedHashSet<>();
        terms=new LinkedHashMap<>();
        punctuation=new LinkedHashSet<>();
        punctuation.add(':');
        punctuation.add(',');
        punctuation.add('.');
        punctuation.add('"');
        punctuation.add('\n');
        punctuation.add('(');
        punctuation.add(')');
        punctuation.add('*');
        punctuation.add('+');
        punctuation.add('=');
        punctuation.add('#');
        punctuation.add('?');
        punctuation.add('!');
        punctuation.add(';');
        punctuation.add('&');
        punctuation.add('|');
        punctuation.add('[');
        punctuation.add(']');
        punctuation.add('`');
        punctuation.add('<');
        punctuation.add('>');
        punctuation.add('-');
        punctuation.add('/');
        punctuation.add('{');
        punctuation.add('}');
        punctuation.add('\'');
        Scanner sc;
        File file=new File(path);
        try {
            sc = new Scanner(file);
            while(sc.hasNextLine()){
                String line=sc.nextLine();//enter all the words to the kis
                stopWords.add(line);
            }
        } catch(FileNotFoundException e){e.printStackTrace();}

    }

    /**
     *
     * remove the terms from main memory
     *
     */
    public static void clear(){
        if(terms!=null)
            terms.clear();
    }

    /**
     *
     * clear all main mamory of the class
     *
     */
    public static void clearAll(){
        if(stopWords!=null)
            stopWords.clear();
        if(punctuation!=null)
            punctuation.clear();
        clear();
    }

}
