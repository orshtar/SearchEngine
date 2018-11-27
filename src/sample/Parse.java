package sample;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.Month;
import java.util.*;

public class Parse {

    private static List<String> stopWords;
    private static List<Character> punctuation;
    private static Map<String,Integer> terms;

    public static void parse(String doc, String docNO){
        String text = doc;
        text+=" , , , ,";
        String[] splitText = text.split(" ");
        int count = 0,skip=0;
        double num;
        for (int i = 0; i < splitText.length; i++) {
            skip=0;
            splitText[i]=cleanTerm(splitText[i]);
            if(!splitText[i].equals("")){
                String str="";
                num=isNumber(splitText[i]);
                if(num!=-1){
                    if(splitText[i].charAt(0)=='$' ) {
                        if ((cleanTerm(splitText[i + 1])).equalsIgnoreCase("million")) {
                            str = num + " M" + " Dollars";
                            skip=1;
                        } else if ((cleanTerm(splitText[i + 1])).equalsIgnoreCase("billion")) {
                            num = num * 1000;
                            str = num + " M" + " Dollars";
                            skip=1;
                        } else if ((cleanTerm(splitText[i + 1])).equalsIgnoreCase("trillion")) {
                            num = num * 1000000;
                            str = num + " M" + " Dollars";
                            skip=1;
                        } else {
                            if (num >= 1000000) {
                                num = num / 1000000;
                                str = num + " M" + " Dollars";
                            } else {
                                str = num + " Dollars";
                            }
                        }
                    }
                    else if((cleanTerm(splitText[i+1])).equalsIgnoreCase("Dollars")) {
                        if(num>=1000000) {
                            num = num / 1000000;
                            str = num + " M" + " Dollars";
                        }
                        else {
                            str = num + " Dollars";
                        }
                        skip=1;
                    }
                    else if((cleanTerm(splitText[i+2])).equalsIgnoreCase("Dollars")) {
                        if(isNumber(splitText[i+1])!=-1) {
                            str = splitText[i] + " " + splitText[i + 1] + " " + "Dollars";
                            skip=2;
                        }
                        else if(splitText[i+1].equalsIgnoreCase("m")) {
                            str =num + " M Dollars";
                            skip=2;
                        }
                        else if(splitText[i+1].equalsIgnoreCase("bn")) {
                            num = num * 1000;
                            str = num + " M Dollars";
                            skip=2;
                        }
                        else
                            str = splitText[i];
                    }
                    else if((cleanTerm(splitText[i+3])).equalsIgnoreCase("Dollars")){
                        if((cleanTerm(splitText[i+2])).equalsIgnoreCase("U.S.")) {
                            if ((cleanTerm(splitText[i + 1])).equalsIgnoreCase("million")) {
                                str = num + " M Dollars";
                                skip = 3;
                            } else if ((cleanTerm(splitText[i + 1])).equalsIgnoreCase("billion")) {
                                num = num * 1000;
                                str = num + " M Dollars";
                                skip = 3;
                            } else if ((cleanTerm(splitText[i + 1])).equalsIgnoreCase("trillion")) {
                                num = num * 1000000;
                                str = num + " M Dollars";
                                skip = 3;
                            }
                            else {
                                str=splitText[i];
                            }
                        }
                        else{
                            str=splitText[i];
                        }
                    }
                    else if(splitText[i].charAt(splitText[i].length()-1)=='%' ){
                        str=splitText[i];
                    }
                    else if((cleanTerm(splitText[i+1])).equalsIgnoreCase("percent")
                            ||(cleanTerm(splitText[i+1])).equalsIgnoreCase("percentage")){
                        str=num+"%";
                        skip=1;
                    }
                    else if(!isMonth(splitText[i+1]).equals("")){
                        if(num<10)
                            str=isMonth(splitText[i+1])+"-0"+num;
                        else if(num<32)
                            str=isMonth(splitText[i+1])+"-"+num;
                        else{
                            if(num>1000)
                                str=num+"-"+isMonth(splitText[i+1]);
                            else
                                str="19"+num+"-"+isMonth(splitText[i+1]);
                        }
                        skip=1;
                    }
                    else if(i>0 && !isMonth(splitText[i-1]).equals("")){
                        if (num > 31) {
                            if(num>1000)
                             str=num+"-"+isMonth(splitText[i-1]);
                            else
                                str="19"+num+"-"+isMonth(splitText[i-1]);
                        }
                        else{
                            if(num<10)
                                str=isMonth(splitText[i-1])+"-0"+num;
                            else
                                str=isMonth(splitText[i-1])+"-"+num;
                        }
                    }
                    else{
                        if (1000 <= num && num < 1000000) {
                            num=num/1000;
                            str=num+"K";
                        }
                        else if((cleanTerm(splitText[i+1])).equalsIgnoreCase("thousand")){
                            str=num+"K";
                            skip=1;
                        }
                        else if(1000000<= num && num < 1000000000){
                            num=num/1000000;
                            str=num+"M";
                        }
                        else if((cleanTerm(splitText[i+1])).equalsIgnoreCase("million")){
                            str=num+"M";
                            skip=1;
                        }
                        else if(num>=1000000000){
                            num=num/1000000000;
                            str=num+"B";
                        }
                        else if((cleanTerm(splitText[i+1])).equalsIgnoreCase("billion")){
                            str=num+"B";
                            skip=1;
                        }
                        else if((cleanTerm(splitText[i+1])).equalsIgnoreCase("trillion")){
                            num=num*1000;
                            str=num+"B";
                            skip=1;
                        }
                        else{
                            if(isNumber(splitText[i+1])!=-1 && splitText[i+1].contains("/")){
                                str=num+" "+splitText[i+1];
                                skip=1;
                            }
                            else
                                str=splitText[i]+"";
                        }
                    }
                }
                else{
                    if(splitText[i].equalsIgnoreCase("between") && isNumber(splitText[i+1])!=-1
                            && splitText[i+2].equalsIgnoreCase("and") && isNumber(splitText[i+3])!=-1){
                        str=isNumber(splitText[i+1])+"-"+isNumber(splitText[i+3]);
                        skip=3;
                    }
                    else{
                        str=splitText[i];
                    }
                }
                if(!stopWords.contains(splitText[i].toLowerCase())) {
                    if (terms.containsKey(str.toUpperCase())) {
                        if(str.charAt(0)>='A' && str.charAt(0)<='Z') {
                            int temp=terms.get(str.toUpperCase());
                            temp++;
                            terms.replace(str.toUpperCase(), temp);
                        }
                        else{
                            int temp=terms.get(str.toUpperCase());
                            temp++;
                            terms.remove(str.toUpperCase());
                            terms.put(str.toLowerCase(), temp);
                        }
                    }else if(terms.containsKey(str.toLowerCase())){
                        int temp=terms.get(str.toLowerCase());
                        temp++;
                        terms.replace(str.toLowerCase(), temp);
                    }
                    else {
                        if(!str.equals("")) {
                            if (str.charAt(0) >= 'A' && str.charAt(0) <= 'Z')
                                str = str.toUpperCase();
                            else
                                str = str.toLowerCase();
                            terms.put(str, 1);
                        }
                    }
                    i=i+skip;
                }
            }
        }
        //Indexer.invertIndex(terms,docNO);

    }

    private static String cleanTerm(String s){
        String ans=s;
        while(!ans.equalsIgnoreCase("") && punctuation.contains(ans.charAt(0))){
            ans=ans.substring(1);
        }
        while(!ans.equalsIgnoreCase("") && punctuation.contains(ans.charAt(ans.length()-1))){
            ans=ans.substring(0,ans.length()-1);
        }
        return ans;
    }

    private static String isMonth(String s){
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

    private static double isNumber(String s) {
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
        stopWords=new LinkedList<>();
        terms=new HashMap<>();
        punctuation=new LinkedList<>();
        punctuation.add(':');
        punctuation.add(',');
        punctuation.add('.');
        punctuation.add('"');
        punctuation.add('\n');
        punctuation.add('(');
        punctuation.add(')');
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
        terms.clear();
    }

    public static void print(){
        for(String s:terms.keySet()){
            System.out.println("key: "+s+", pos: "+terms.get(s));
        }
    }
}
