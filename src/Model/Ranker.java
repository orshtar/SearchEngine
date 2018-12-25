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
    private static double k=1.65;
    private static double b=0.85;

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
        System.out.println(N);
        System.out.println(avdl);
        return m;
    }

    public List<String> rank(List<String> postings, boolean isSemantic, String query, boolean isStem, String path, List<String> cityPos){
        Map<String,Integer> docsLen=setAvdl(path,isStem);
        Map<String, Double> docRank=new LinkedHashMap<>();
        int i=0;
        for(String pos:postings){
            String[] docs=pos.split(",");
            for(String doc:docs){
                boolean isDocCity=false;
                String docNo=doc.split("\\*")[0];
                for(String posC: cityPos){
                    if(posC.contains(docNo+"*") || posC.contains(docNo+",") || posC.contains(docNo+"\n")){
                        isDocCity=true;
                        break;
                    }
                }
                if(isDocCity || cityPos.size()==0) {
                    double bm = 0;
                    int countQ = getOccur(query.split(" ")[i], query);
                    int countD = doc.split("\\*").length - 1;
                    int docLength = docsLen.get(docNo);
                    int df = docs.length;
                    double t = ((k + 1) * countD) / (countD + k * (1 - b + b * (docLength / avdl)));
                    double log = Math.log((N + 1) / df) / Math.log(2);
                    t = t * countQ * log;
                    bm += t;
                    if (docRank.containsKey(docNo)) {
                        double temp = docRank.get(docNo);
                        temp += bm;
                        docRank.replace(docNo, temp);
                    } else {
                        docRank.put(docNo, bm);
                    }
                }
            }
            i++;
        }
        List<String> sortedDocs=sort(docRank);
        return sortedDocs;
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
