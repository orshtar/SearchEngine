package Model;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * this class ranks documents with a formula consists on BM25 and positions of words.
 */
public class Ranker {

    private static double avdl;
    private static double N;
    private static double k=1.4;
    private static double b=0.55;

    /**
     *
     * @param path- path to posting files
     * @param isStem- is stemming has done
     * @return  a map of doc number and its length
     */
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
        avdl=sum/count;  // computes average doc length of all docs in corpus
        N=count; // number of docs
        return m;
    }

    /**
     *
     * @param postings- map of posting of word and its occurences in the query
     * @param postingsS- map of posting of semantic words and its occurences in the query(same as the original word)
     * @param desc- map of posting of word in description and its occurences in the description
     * @param isStem- is stemming had done
     * @param path- path to posting files
     * @param cityPos- postings of selected cities by user
     * @param isSemantic- is semantic search done
     * @return a list of relevant doc nums sorted by their rank
     */
    public List<String> rank(Map<String,String> postings, Map<String,String> postingsS, Map<String, String> desc, boolean isStem, String path, List<String> cityPos, boolean isSemantic){
        Map<String,Integer> docsLen=setAvdl(path,isStem);  // get average docs length
        Map<String, Double> docRank=new LinkedHashMap<>();
        Map<String, Double> m=new LinkedHashMap<>();
        Map<String, Double> rankS=new LinkedHashMap<>();
        for(String pos:postings.keySet()){   // for each posting of word in query
            String[] docs=pos.split(",");
            for(String doc:docs) {     //for each doc
                if (!doc.equals("")) {
                    boolean isDocCity = false;
                    String docNo = doc.split("\\*")[0];
                    String docPos = doc.split("\\*")[1];
                    for (String posC : cityPos) {
                        if (posC.contains(docNo + "*") || posC.contains(docNo + ",") || posC.contains(docNo + "\n")) {
                            isDocCity = true;     // selected city appear in doc
                            break;
                        }
                    }
                    if (isDocCity || cityPos.size() == 0) {  // if doc contains at least one selected city
                        double bm = 0;
                        int countQ = postings.get(pos).split("\\*").length;   // count occurences in query
                        int countD = doc.split("\\*").length - 1;  // count occurences in doc
                        int docLength = docsLen.get(docNo);
                        int df = docs.length;
                        double t = ((k + 1) * countD) / (countD + k * (1 - b + b * (docLength / avdl)));
                        double log = Math.log((N + 1) / df) / Math.log(2);
                        t = t * countQ * log;    //compute the bm formula
                        bm += 0.75 * t;   //bm has 0.65 weight of total rank
                        m.clear();
                        for (String pos2 : postings.keySet()) {   // compare each word to other words in qurey (to find positions)
                            if (!postings.get(pos).equals(postings.get(pos2))) {
                                double dist = 0;
                                int realDist = minDist(postings.get(pos), postings.get(pos2));  // minimal distance between 2 words in query
                                if (pos2.contains(docNo + "*")) {
                                    String temp1 = pos2.split(docNo + "\\*")[1];
                                    String temp2 = temp1.split(",")[0];
                                    int minDist = minDist(docPos, temp2);   // minimal distance between the words in mutual document
                                    dist = realDist * (minDist - realDist + 1);
                                    double posScore = 1.0 / (dist);    // compute position score for each pair of words
                                    if (m.containsKey(postings.get(pos2))) {
                                        double temp = m.get(postings.get(pos2));
                                        if (temp < posScore)
                                            m.replace(postings.get(pos2), posScore);
                                    } else
                                        m.put(postings.get(pos2), posScore);    // keep map of position scores
                                }
                            }
                        }
                        double sum = 0;
                        for (String p : m.keySet()) {
                            sum += m.get(p);     // sum position score of word with all other words
                        }
                        bm += (0.25 * (sum));    // position score has 0.35 weight of total rank
                        if (docRank.containsKey(docNo)) {
                            double temp = docRank.get(docNo);
                            temp += bm;
                            docRank.replace(docNo, temp);     //save rank for each doc
                        } else {
                            docRank.put(docNo, bm);
                        }
                    }
                }
            }
        }
        for(String pos:postingsS.keySet()){     // for each semantic word *semantic words only have bm score*
            String[] docs=pos.split(",");
            for(String doc:docs) {
                String docNo = doc.split("\\*")[0];
                boolean isDocCity=false;
                for (String posC : cityPos) {
                    if (posC.contains(docNo + "*") || posC.contains(docNo + ",") || posC.contains(docNo + "\n")) {
                        isDocCity = true;    // selected city appear in doc
                        break;
                    }
                }
                if (isDocCity || cityPos.size() == 0) {    // if doc contains at least one selected city
                    double bm = 0;
                    int countQ = postingsS.get(pos).split("\\*").length;  // count occurences in query
                    int countD = doc.split("\\*").length - 1;       // count occurences in doc
                    int docLength = docsLen.get(docNo);
                    int df = docs.length;
                    double t = ((k + 1) * countD) / (countD + k * (1 - b + b * (docLength / avdl)));
                    double log = Math.log((N + 1) / df) / Math.log(2);
                    t = t * countQ * log;
                    bm += t;   //compute the bm formula
                    m.clear();
                    if (rankS.containsKey(docNo)) {
                        double temp = rankS.get(docNo);
                        temp += bm;
                        rankS.replace(docNo, temp);    //save rank for each doc
                    } else {
                        rankS.put(docNo, bm);
                    }
                }
            }

        }
        Map<String, Double> descRank=new LinkedHashMap<>();
        for(String pos:desc.keySet()) {          // for each word in description   *description words only have bm score*
            String[] docs = pos.split(",");
            for (String doc : docs) {
                boolean isDocCity = false;
                String docNo = doc.split("\\*")[0];
                for (String posC : cityPos) {
                    if (posC.contains(docNo + "*") || posC.contains(docNo + ",") || posC.contains(docNo + "\n")) {
                        isDocCity = true;    // selected city appear in doc
                        break;
                    }
                }
                if (isDocCity || cityPos.size() == 0) {     // if doc contains at least one selected city
                    double bm = 0;
                    int countQ = desc.get(pos).split("\\*").length;    // count occurences in description
                    int countD = doc.split("\\*").length - 1;      // count occurences in doc
                    int docLength = docsLen.get(docNo);
                    int df = docs.length;
                    double t = ((k + 1) * countD) / (countD + k * (1 - b + b * (docLength / avdl)));
                    double log = Math.log((N + 1) / df) / Math.log(2);
                    t = t * countQ * log;
                    bm += t;     //compute the bm formula
                    if(descRank.containsKey(docNo)){
                        double temp=descRank.get(docNo);
                        temp+=bm;
                        descRank.replace(docNo,temp);    //save rank for each doc
                    }
                    else{
                        descRank.put(docNo,bm);
                    }
                }
            }
        }
        Map<String, Double> ranking=new LinkedHashMap<>();
        double Wq=0.4;
        double Wd=0.6;
        double Ws=0;
        if(isSemantic){       //each part of the formula has a different weight in final rank
            Wq=0.2;
            Wd=0.5;
            Ws=0.3;
        }
        for(String word:docRank.keySet()){   //compute final rank for each doc
            double rank=Wq*docRank.get(word);
            if(descRank.containsKey(word)){
                rank+=Wd*descRank.get(word);
            }
            if(rankS.containsKey(word)){
                rank+=Ws*rankS.get(word);
            }
            ranking.put(word,rank);
        }
        for(String des: descRank.keySet()){   //compute final rank for each doc from description which wasnt in the query words list
            if(!ranking.containsKey(des)){
                double rank=Wd*descRank.get(des);
                if(rankS.containsKey(des)){
                    rank+=Ws*rankS.get(des);
                }
                ranking.put(des,rank);
            }
        }
        for(String docNo:rankS.keySet()){    //compute final rank for each doc from seamantic words which wasnt in the query words list
            if(!ranking.containsKey(docNo)){
                double rank=Ws*rankS.get(docNo);
                ranking.put(docNo,rank);
            }
        }
        List<String> sortedDocs=sort(ranking);   //sort docs in descending order by rank
        return sortedDocs;
    }

    /**
     *
     * @param pos1-  posting of first word
     * @param pos2-  posting of second word
     * @return the minimal distance by the positions of each word in the posting
     */
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

    /**
     *
     * @param docRank- map to sort
     * @return a sorted list of doc numbers.
     */
    private List<String> sort(Map<String, Double> docRank) {
        Map<Double,String> m=new LinkedHashMap<>();
        for(String key:docRank.keySet()){
            Double bm=docRank.get(key);
            m.put(bm,key);
        }
        TreeSet<Double> t=new TreeSet<>(m.keySet());
        TreeSet<Double> t2=(TreeSet)t.descendingSet();   // sort ranks by descending order
        List<String> docs=new LinkedList<>();
        int i=0;
        for(Double d:t2){
            if(i==50)
                break;      // only save top 50 ranked docs
            docs.add(m.get(d));
            i++;
        }
        return docs;
    }

}
