package sample;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.Month;
import java.util.*;

public class Parse {

    private static List<String> stopWords;
    private static List<Character> punctuation;
    private static Map<String,String> terms;

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
            String text = doc.substring(i,doc.length()-8);
            text+=" , , , ,";
            String[] splitText = text.split(" ");
            int count = 0,skip=0;
            double num;
            for (i = 0; i < splitText.length; i++) {
                skip=0;
                splitText[i]=clean(splitText[i]);
                if(!splitText[i].equals("")){
                    String str="";
                    num=isNumber(splitText[i]);
                    if(num!=-1){
                        if(splitText[i].charAt(0)=='$' ||  (clean(splitText[i+1])).equalsIgnoreCase("Dollars")
                                || (clean(splitText[i+2])).equalsIgnoreCase("Dollars")
                                ||  (clean(splitText[i+3])).equalsIgnoreCase("Dollars")){
                            int[] arr=new int[1];
                            str=handlePrice( num, splitText[i], splitText[i+1], splitText[i+2], splitText[i+3],arr);
                            skip=arr[0];
                        }
                        else if(splitText[i].charAt(splitText[i].length()-1)=='%' || (clean(splitText[i+1])).equalsIgnoreCase("percent")
                                ||(clean(splitText[i+1])).equalsIgnoreCase("percentage")){
                            str=num+"%";
                            if((clean(splitText[i+1])).equalsIgnoreCase("percent")
                                    ||(clean(splitText[i+1])).equalsIgnoreCase("percentage"))
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
                        else if(!isMonth(splitText[i-1]).equals("")){
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
                            else if((clean(splitText[i+1])).equalsIgnoreCase("thousand")){
                                str=num+"K";
                                skip=1;
                            }
                            else if(1000000<= num && num < 1000000000){
                                num=num/1000000;
                                str=num+"M";
                            }
                            else if((clean(splitText[i+1])).equalsIgnoreCase("million")){
                                str=num+"M";
                                skip=1;
                            }
                            else if(num>=1000000000){
                                num=num/1000000000;
                                str=num+"B";
                            }
                            else if((clean(splitText[i+1])).equalsIgnoreCase("billion")){
                                str=num+"B";
                                skip=1;
                            }
                            else if((clean(splitText[i+1])).equalsIgnoreCase("trillion")){
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
                        if (terms.containsKey(str)) {
                            String s = terms.get(str) + "," + count;
                            terms.replace(str, s);
                        } else {
                            terms.put(str,docNO+" : "+ count + "");
                        }
                        count++;
                        i=i+skip;
                    }
                }
            }
        }
    }

    private static String clean(String s){
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
        double num1 = 0, num2 = 0, num;
        int i = 0, len = s.length() - 1;
        if (i<=len && (s.charAt(0) == '$' || s.charAt(s.length() - 1) == '%' || (s.charAt(0) >= '0' && s.charAt(0) <= '9'))) {
            if (s.charAt(0) == '$')
                i++;
            if (s.charAt(s.length() - 1) == '%')
                len--;
            while (i <= len && (s.charAt(i) >= '0' && s.charAt(i) <= '9')) {
                num1 = num1 * 10 + (s.charAt(i) - '0');
                i++;
            }
            if (i <= len) {
                if (s.charAt(i) == '.') {
                    int j = len;
                    while (j > i && (s.charAt(j) >= '0' && s.charAt(j) <= '9')) {
                        num2 = num2 * 0.1 + (s.charAt(j) - '0');
                        j--;
                    }
                    if (j > i)
                        num = -1;
                    else
                        num = num1 + num2 * 0.1;
                } else if (s.charAt(i) == '/') {
                    i++;
                    while (i <= len && (s.charAt(i) >= '0' && s.charAt(i) <= '9')) {
                        num2 = num2 * 10 + (s.charAt(i) - '0');
                        i++;
                    }
                    if (i <= len)
                        num = -1;
                    else
                        num = num1 / num2;
                }
                else if(s.charAt(i)==','){
                    while(i<=len && s.charAt(i)==','){
                        i++;
                        while(i<=len && (s.charAt(i) >= '0' && s.charAt(i) <= '9')){
                            num1 = num1 * 10 + (s.charAt(i) - '0');
                            i++;
                        }
                    }
                    if(i<=len)
                        num=-1;
                    else
                        num=num1;
                }
                else
                    num = -1;
            } else
                num = num1;
        } else
            num = -1;
        return num;
    }

    private static String handlePrice(double num, String first, String second, String third, String forth,int[] arr){
        String ans="";
        if(second.equalsIgnoreCase("Dollars")){
            if(num<1000000){
                ans=first+" Dollars";
            }
            else{
                num=num/1000000;
                ans=num+" M"+" Dollars";
            }
            arr[0]=1;
        }
        else if(third.equalsIgnoreCase("Dollars")) {
            double secondNum=isNumber(second);
            if(secondNum!=-1){
                ans=first+second+" Dollars";
            }
            else if(second.equalsIgnoreCase("m")){
                ans=first+" M"+" Dollars";
            }
            else if(second.equalsIgnoreCase("bn")){
                num=num*1000;
                ans=num+" M"+" Dollars";
            }
            arr[0]=2;
        }
        else if(forth.equalsIgnoreCase("Dollars")){
            if(second.equalsIgnoreCase("billion")){
                num=num*1000;
                ans=num+" M"+" Dollars";

            }
            else if(second.equalsIgnoreCase("million")){
                ans=first+" M"+" Dollars";
            }
            else if(second.equalsIgnoreCase("trillion")){
                num=num*1000000;
                ans=num+" M"+" Dollars";
            }
            arr[0]=3;
        }
        else if(first.charAt(0)=='$'){
            if(second.equalsIgnoreCase("million")){
                ans=num+" M"+" Dollars";
            }
            else if(second.equalsIgnoreCase("billion")){
                num=num*1000;
                ans=num+" M"+" Dollars";
            }
            else if(second.equalsIgnoreCase("trillion")){
                num=num*1000000;
                ans=num+" M"+" Dollars";
            }
            else{
                if(num>=1000000){
                    num=num/1000000;
                    ans=num+" M"+" Dollars";
                }
                else{
                    ans=num+" Dollars";
                }
            }
            arr[0]=0;
        }
        return ans;
    }

    public static void initStopWords(String path){
        stopWords=new LinkedList<>();
        terms=new HashMap<>();
        punctuation=new LinkedList<>();
        punctuation.add(':');
        punctuation.add(',');
        punctuation.add('.');
        punctuation.add('"');
        punctuation.add('(');
        punctuation.add(')');
        punctuation.add('?');
        punctuation.add('!');
        punctuation.add(';');
        punctuation.add('&');
        punctuation.add('|');
        punctuation.add('[');
        punctuation.add(']');
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

    public static void print(){
        for(String s:terms.keySet()){
            System.out.println("key: "+s+", pos: "+terms.get(s));
        }
    }
}
