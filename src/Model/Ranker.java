package Model;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Ranker {

    private static double avdl;
    private static double N;
    private static double k=1.6;
    private static double b=0.75;

    public void setAvdl(String path, boolean isStem){
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
            }
        }
        avdl=sum/count;
        N=count;
        System.out.println(N);
        System.out.println(avdl);

    }

    public void rank(List<String> postings, boolean isSemantic, String query, boolean isStem, String path){
        Map<String, Double> visitedDocs=new LinkedHashMap<>();
        int i=0;
        for(String pos:postings){
            String[] temp=pos.split(":");
            if(temp.length>1){
                String[] docs=temp[1].split(",");
                for(String doc:docs){
                    double bm=0;
                    String docNo=doc.split("\\*")[0];
                    if(!visitedDocs.containsKey(docNo)){
                        int ql=query.split(" ").length;
                        for(int j=i; j<ql; j++){
                            List<String> visitedWords=new LinkedList<>();
                            if(postings.get(j).contains(docNo)){
                                if(!visitedWords.contains(query.split(" ")[j])) {
                                    int countQ = getOccur(query.split(" ")[j], query);
                                    int countD = doc.split("\\*").length-1;
                                    int docLength=getDocL(docNo, isStem, path);
                                    int df=docs.length;
                                    double t=((k+1)*countD)/(countD+k*(1-b+b*(docLength/avdl)));
                                    double log=Math.log((N+1)/df)/Math.log(2);
                                    t=t*countQ*log;
                                    bm+=t;
                                    visitedWords.add(query.split(" ")[j]);
                                }
                            }
                        }
                        visitedDocs.put(docNo,bm);
                    }
                }
            }
        }
        for(String key:visitedDocs.keySet())
            System.out.println(key+":"+visitedDocs.get(key));
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
}
