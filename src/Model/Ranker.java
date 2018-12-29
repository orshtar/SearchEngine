package Model;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Ranker {

    private static double avdl;
    private static double N;
    private static double k=1.4;
    private static double b=0.70;

    public Map<String, Integer> setAvdl(String path, boolean isStem){
        Map<String,Integer> m=new LinkedHashMap<>();
        String stem="a";
        if(isStem)
            stem="b";
        String p="";
        double sum=0, count=0;
        try{
            p= new String(Files.readAllBytes(Paths.get(path + "/docs"+stem+".txt")), StandardCharsets.UTF_8);//read the file
        }catch(IOException e){e.printStackTrace();}
        String[] docsLines=p.split("\n");
        for(String s:docsLines){
            if(!s.equals("")){
                String length=s.split(",")[2];
                sum+=Double.parseDouble(length);
                count++;
                m.put(s.split(":")[0],Integer.parseInt(s.split(",")[2]));
            }
        }
        avdl=sum/count;
        N=count;
        return m;
    }

    public List<String> rank(Map<String,String> postings, String query, Map<String, String> desc, boolean isStem, String path, List<String> cityPos){
        Map<String,Integer> docsLen=setAvdl(path,isStem);
        Map<String, Double> docRank=new LinkedHashMap<>();
        Map<String, Double> m=new LinkedHashMap<>();
        for(String pos:postings.keySet()){
            String[] docs=pos.split(",");
            for(String doc:docs) {
                if (!doc.equals("")) {
                    boolean isDocCity = false;
                    String docNo = doc.split("\\*")[0];
                    String docPos = doc.split("\\*")[1];
                    for (String posC : cityPos) {
                        if (posC.contains(docNo + "*") || posC.contains(docNo + ",") || posC.contains(docNo + "\n")) {
                            isDocCity = true;
                            break;
                        }
                    }
                    if (isDocCity || cityPos.size() == 0) {
                        double bm = 0;
                        int countQ = postings.get(pos).split("\\*").length;
                        int countD = doc.split("\\*").length - 1;
                        int docLength = docsLen.get(docNo);
                        int df = docs.length;
                        double t = ((k + 1) * countD) / (countD + k * (1 - b + b * (docLength / avdl)));
                        double log = Math.log((N + 1) / df) / Math.log(2);
                        t = t * countQ * log;
                        bm += 0.55 * t;
                        m.clear();
                        for (String pos2 : postings.keySet()) {
                            if (!postings.get(pos).equals(postings.get(pos2))) {
                                double dist = 0;
                                int realDist = minDist(postings.get(pos), postings.get(pos2));
                                if (pos2.contains(docNo + "*")) {
                                    String temp1 = pos2.split(docNo + "\\*")[1];
                                    String temp2 = temp1.split(",")[0];
                                    int minDist = minDist(docPos, temp2);
                                    dist = realDist * (minDist - realDist + 1);
                                    double posScore = 1.0 / (dist);
                                    if (m.containsKey(postings.get(pos2))) {
                                        double temp = m.get(postings.get(pos2));
                                        if (temp < posScore)
                                            m.replace(postings.get(pos2), posScore);
                                    } else
                                        m.put(postings.get(pos2), posScore);
                                }
                            }
                        }
                        double sum = 0;
                        for (String p : m.keySet()) {
                            sum += m.get(p);
                        }
                        bm += (0.45 * (sum));
                        if (docRank.containsKey(docNo)) {
                            double temp = docRank.get(docNo);
                            temp += bm;
                            docRank.replace(docNo, temp);
                        } else {
                            docRank.put(docNo, bm);
                        }
                    }
                }
            }
        }
        Map<String, Double> descRank=new LinkedHashMap<>();
        Map<String, Double> sumDocs=new LinkedHashMap<>();
        Map<String, Double> docsSize=new LinkedHashMap<>();
        for(String pos:desc.keySet()) {
            String[] docs = pos.split(",");
            for (String doc : docs) {
                boolean isDocCity = false;
                String docNo = doc.split("\\*")[0];
                for (String posC : cityPos) {
                    if (posC.contains(docNo + "*") || posC.contains(docNo + ",") || posC.contains(docNo + "\n")) {
                        isDocCity = true;
                        break;
                    }
                }
                if (isDocCity || cityPos.size() == 0) {
                    double bm = 0;
                    int countQ = desc.get(pos).split("\\*").length;
                    int countD = doc.split("\\*").length - 1;
                    int docLength = docsLen.get(docNo);
                    int df = docs.length;
                    double t = ((k + 1) * countD) / (countD + k * (1 - b + b * (docLength / avdl)));
                    double log = Math.log((N + 1) / df) / Math.log(2);
                    t = t * countQ * log;
                    bm += 0.6*t;
                    double sum=0, sizeD=0, sizeQ=0;
                    sum+=((countD/docLength)*((Math.log((docsLen.size()/docs.length)))/Math.log(2)));
                    if(descRank.containsKey(docNo)){
                        double temp=descRank.get(docNo);
                        temp+=bm;
                        descRank.replace(docNo,temp);
                    }
                    else{
                        descRank.put(docNo,bm);
                    }
                }
            }
        }
        Map<String, Double> ranking=new LinkedHashMap<>();
        for(String word:docRank.keySet()){
            if(descRank.containsKey(word)){
                ranking.put(word,0.4*docRank.get(word)+0.6*descRank.get(word));
            }
            else{
                ranking.put(word, 0.4*docRank.get(word));
            }
        }
        for(String des: descRank.keySet()){
            if(!ranking.containsKey(des)){
                ranking.put(des,0.6*descRank.get(des));
            }
        }
        List<String> sortedDocs=sort(ranking);
        return sortedDocs;
    }

    private int minDist(String pos1, String pos2) {
        String[] temp1=pos1.split("\\*");
        String[] temp2=pos2.split("\\*");
        int[] arr1= Arrays.stream(temp1).mapToInt(Integer::parseInt).toArray();
        int[] arr2= Arrays.stream(temp2).mapToInt(Integer::parseInt).toArray();
        Arrays.sort(arr1);
        Arrays.sort(arr2);
        int ans=1000000;
        int a=0, b=0;
        while(a<arr1.length && b<arr2.length){
            if(Math.abs(arr1[a]-arr2[b])<ans)
                ans=Math.abs(arr1[a]-arr2[b]);
            if(arr1[a]<arr2[b])
                a++;
            else
                b++;
        }
        return ans;
    }

    private List<String> sort(Map<String, Double> docRank) {
        Map<Double,String> m=new LinkedHashMap<>();
        for(String key:docRank.keySet()){
            Double bm=docRank.get(key);
            m.put(bm,key);
        }
        TreeSet<Double> t=new TreeSet<>(m.keySet());
        TreeSet<Double> t2=(TreeSet)t.descendingSet();
        List<String> docs=new LinkedList<>();
        int i=0;
        for(Double d:t2){
            if(i==50)
                break;
            docs.add(m.get(d));
            i++;
        }
        return docs;
    }

    private int getOccur(String w, String query) {
        int ans=0;
        String[] temp=query.split(" ");
        for (String s:temp){
            if(s.equalsIgnoreCase(w))
                ans++;
        }
        return ans;
    }

    private int getDocL(String docNo, boolean isStem, String path) {
        String doc=Indexer.search(path,docNo,isStem,"docs");
        String length=doc.split(",")[2];
        return Integer.parseInt(length);
    }

    private Map<String, Double> docsLen(String path, boolean isStem){
        Map<String,Double> m=new LinkedHashMap<>();
        if(isStem)
            path+=("/docsb.txt");
        else
            path+=("/docsa.txt");
        String p="";
        try{
            p = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
        }catch (IOException e){e.printStackTrace();}
        String[] docs=p.split("\n");
        for(String doc: docs){
            m.put(doc.split(":")[0],Double.parseDouble(doc.split(",")[2]));
        }
        return m;
    }

}
