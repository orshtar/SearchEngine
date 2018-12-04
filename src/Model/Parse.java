package Model;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.Month;
import java.util.*;

public class Parse {

    private static Set<String> stopWords;
    private static Set<Character> punctuation;
    private static Map<String,String> terms;
    private int max=0;

    public void parse(String doc, String docNO, String city, boolean stem,String language){
        int position=0;
        String text = doc;
        text+=" , , , ,";
        String[] splitText = text.split(" ");
        int count = 0,skip=0;
        if(!city.equals("")) {
            city = cleanTerm(city);
            city=city.toUpperCase();
        }
        if(!language.equals("")) {
            language = cleanTerm(language);
        }
        double num;
        Stemmer stemmer=new Stemmer();
        for (int i = 0; i < splitText.length; i++) {
            position++;
            skip = 0;
            splitText[i] = cleanTerm(splitText[i]);
            if (!splitText[i].equals("")) {
                String str = "";
                num = isNumber(splitText[i]);
                String[] s = null;
                if (num != -1) {
                    String sNum="";
                    if(num%1==0)
                        sNum = ((int)num) +"";
                    else
                        sNum=String.format("%.2f",num);
                    if (splitText[i].charAt(0) == '$') {
                        if ((cleanTerm(splitText[i + 1])).equalsIgnoreCase("million")) {
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
                    } else if ((cleanTerm(splitText[i + 1])).equalsIgnoreCase("Dollars")) {
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
                        skip = 1;
                    } else if ((cleanTerm(splitText[i + 2])).equalsIgnoreCase("Dollars")) {
                        if (isNumber(splitText[i + 1]) != -1) {
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
                    } else if ((cleanTerm(splitText[i + 3])).equalsIgnoreCase("Dollars")) {
                        if ((cleanTerm(splitText[i + 2])).equalsIgnoreCase("U.S.")) {
                            if ((cleanTerm(splitText[i + 1])).equalsIgnoreCase("million")) {
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
                            str = splitText[i];
                        }
                    } else if((cleanTerm(splitText[i + 1])).equals("miles")){
                        str=splitText[i]+" "+cleanTerm(splitText[i + 1]);
                        skip=1;
                    }else if(splitText[i + 1].equalsIgnoreCase("p.m.") || splitText[i + 1].equalsIgnoreCase("a.m.")){
                        str=splitText[i]+" "+splitText[i + 1];
                        skip=1;
                    }
                    else if (splitText[i].charAt(splitText[i].length() - 1) == '%') {
                        str = splitText[i];
                    } else if ((cleanTerm(splitText[i + 1])).equalsIgnoreCase("percent")
                            || (cleanTerm(splitText[i + 1])).equalsIgnoreCase("percentage")) {
                        str = splitText[i] + "%";
                        skip = 1;
                    } else if (!isMonth(cleanTerm(splitText[i + 1])).equals("")) {
                        if (num < 10)
                            str =isMonth(cleanTerm(splitText[i + 1])) + "-0" + splitText[i];
                        else if (num < 32)
                            str = isMonth(cleanTerm(splitText[i + 1]))+ "-" + splitText[i];
                        else {
                            if (num > 1000)
                                str = splitText[i] + "-" + isMonth(cleanTerm(splitText[i + 1]));
                            else
                                str = "19" + splitText[i] + "-" + isMonth(cleanTerm(splitText[i + 1]));
                        }
                        skip = 1;
                    } else if (i > 0 && !isMonth(cleanTerm(splitText[i- 1])).equals("")) {
                        if (num > 31) {
                            if (num > 1000)
                                str = splitText[i] + "-" + isMonth(cleanTerm(splitText[i- 1]));
                            else
                                str = "19" + splitText[i] + "-" +isMonth(cleanTerm(splitText[i- 1]));
                        } else {
                            if (num < 10)
                                str =isMonth(cleanTerm(splitText[i- 1])) + "-0" + splitText[i];
                            else
                                str =isMonth(cleanTerm(splitText[i- 1])) + "-" + splitText[i];
                        }
                    } else {
                        if (1000 <= num && num < 1000000) {
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
                } else {
                    if (splitText[i].equalsIgnoreCase("between") && isNumber(splitText[i + 1]) != -1
                            && splitText[i + 2].equalsIgnoreCase("and") && isNumber(splitText[i + 3]) != -1) {
                        str = isNumber(splitText[i + 1]) + "-" + isNumber(splitText[i + 3]);
                        skip = 3;
                    } else {
                        boolean v=true;
                        str = splitText[i];
                        if (str.contains(",")) {
                            s = str.split(",");
                            if (s.length >= 1)
                                str = cleanTerm(s[0]);
                        }
                        if (str.contains(".") ) {
                            s = str.split(".");
                            if (s.length >= 1)
                                str = cleanTerm(s[0]);
                        }
                        if (str.contains("\n")) {
                            s = str.split("\n");
                            if (s.length >= 1)
                                str = cleanTerm(s[0]);
                        }
                        if (str.contains("\r")) {
                            s = str.split("\r");
                            if (s.length >= 1)
                                str = cleanTerm(s[0]);
                        }
                        if (str.contains("\r\n")) {
                            s = str.split("\r\n");
                            if (s.length >= 1)
                                str = cleanTerm(s[0]);
                        }
                        if (str.contains("/")) {
                            s = str.split("/");
                            if (s.length >= 1)
                                str = cleanTerm(s[0]);
                        }
                        if (str.contains(" ")) {
                            s = str.split(" ");
                            if (s.length >= 1)
                                str = cleanTerm(s[0]);
                        }
                        if (str.contains("--")) {
                            s = str.split("--");
                            if (s.length >= 1)
                                str = cleanTerm(s[0]);
                        }
                        if (str.contains("\'")) {
                            s = str.split("'");
                            if (s.length >= 1)
                                str = cleanTerm(s[0]);
                        }
                        if (str.contains("(")) {
                            s = str.split("\\(");
                            if (s.length >= 1)
                                str = cleanTerm(s[0]);
                        }
                        if (str.contains(")")) {
                            s = str.split("\\)");
                            if (s.length >= 1)
                                str = cleanTerm(s[0]);
                        }
                    }
                }
                if (!stopWords.contains(splitText[i].toLowerCase())) {
                    if(stem && !Indexer.containsTerm(str)){
                        stemmer.add(str.toCharArray(),str.toCharArray().length);
                        stemmer.stem();
                        str=new String(stemmer.toString());
                    }
                    if (terms.containsKey(str.toUpperCase())) {
                        if(str.charAt(0) >= 'a' && str.charAt(0) <= 'z'){
                            String temp = terms.get(str.toUpperCase());
                            temp+=("*"+position);
                            if(temp.split("\\*").length>max)
                                max=temp.split("\\*").length;
                            terms.remove(str.toUpperCase());
                            terms.put(str.toLowerCase(), temp);
                        }
                        else {
                            String temp = terms.get(str.toUpperCase());
                            temp+=("*"+position);
                            if(temp.split("\\*").length>max)
                                max=temp.split("\\*").length;
                            terms.replace(str.toUpperCase(), temp);
                        }
                    } else if (terms.containsKey(str.toLowerCase())) {
                        String temp = terms.get(str.toLowerCase());
                        temp+=("*"+position);
                        if(temp.split("\\*").length>max)
                            max=temp.split("\\*").length;
                        terms.replace(str.toLowerCase(), temp);
                    } else {
                        if (!str.equals("")) {
                            if (str.charAt(0) >= 'A' && str.charAt(0) <= 'Z')
                                str = str.toUpperCase();
                            else
                                str = str.toLowerCase();
                            terms.put(str, position+"");
                        }
                    }
                    i = i + skip;
                    if(s!=null && s.length>1){
                        splitText[i]=s[1];
                        i--;
                    }
                }
            }
        }
        Indexer i=new Indexer();
        i.invertIndex(terms,docNO,max,city,language);
    }

    private String cleanTerm(String s){
        String ans=s;
        while(!ans.equals("") && punctuation.contains(ans.charAt(0))){
            ans=ans.substring(1);
        }
        while(!ans.equals("") && punctuation.contains(ans.charAt(ans.length()-1))){
            ans=ans.substring(0,ans.length()-1);
        }
        return ans;
    }

    private String isMonth(String s){
        String ans="";
        if(s.length()>3) {
            int m=0;
            try {
                m = Month.valueOf(s.toUpperCase()).getValue();
                if (m < 10)
                    ans = "0" + m;
                else
                    ans = m + "";
            } catch (IllegalArgumentException e) {
                ans = "";
            }
        }
        else{
            s=s.toUpperCase();
            switch (s) {
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

    private double isNumber(String s) {
        if(s.length()>0 && ((s.charAt(0)>='0' && s.charAt(0)<='9') || s.charAt(0)=='$')){
            if(s.charAt(0)=='$') {
                s = s.substring(1);
                if(s.length()==0)
                    return -1;
            }
            if(s.charAt(s.length()-1)=='%')
                s=s.substring(0,s.length()-1);
            if(s.contains(",")){
                s=s.replaceAll(",","");
            }
            try{
                double num1=Double.parseDouble(s);
                return num1;
            }
            catch (NumberFormatException e){
                if(s.contains("/")){
                    String[] strings=s.split("/");
                    try{
                        if(strings.length==2) {
                            double num1 = Double.parseDouble(strings[0]);
                            double num2 = Double.parseDouble(strings[1]);
                            if(num2==0)
                                return 0;
                            return num1 / num2;
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
                String line=sc.nextLine();
                stopWords.add(line);
            }
        } catch(FileNotFoundException e){}

    }

    public static void clear(){
        if(terms!=null)
            terms.clear();
    }

    public static void clearAll(){
        if(stopWords!=null)
            stopWords.clear();
        if(punctuation!=null)
            punctuation.clear();
        clear();
    }

}
