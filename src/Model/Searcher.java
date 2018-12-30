package Model;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Searcher {

    public List<String> SearchQ(String query ,String desc, boolean isStem, boolean isSemantic, String postPath, List<String> cities){
        Parse p=new Parse();
        Map<String,String> q=p.parse(query,"","",isStem,"",true);
        Map<String,String> d=p.parse(desc,"","",isStem,"",true);
        Map<String, String> pos=new LinkedHashMap<>();
        Map<String,String> posS=new LinkedHashMap<>();
        Map<String, String> description=new LinkedHashMap<>();
        List<String> postingCity=new LinkedList<>();
        for(String city: cities){
            postingCity.add(Indexer.search(postPath,city,isStem,"cities.txt"));
        }
        for(String des:d.keySet()) {
            String posting;
            if (des.toLowerCase().charAt(0) >= 'a' && des.toLowerCase().charAt(0) <= 'z')
                posting = Indexer.search(postPath, des.toLowerCase(), isStem, des.toLowerCase().charAt(0) + "");
            else
                posting = Indexer.search(postPath, des.toLowerCase(), isStem, "nums");
            description.put(posting, d.get(des));
        }
        for(String term:q.keySet()){
            String posting;
            if(term.toLowerCase().charAt(0)>='a' && term.toLowerCase().charAt(0)<='z')
                posting=Indexer.search(postPath,term.toLowerCase(),isStem,term.toLowerCase().charAt(0)+"");
            else
                posting=Indexer.search(postPath,term.toLowerCase(),isStem,"nums");
            pos.put(posting,q.get(term));
            BufferedReader br;
            String line="";
            if(isSemantic) {
                try {
                    URL url = new URL("https://api.datamuse.com/words?ml=" + term+"&rel_syn="+term);
                    br = new BufferedReader(new InputStreamReader(url.openStream()));
                    line = br.readLine();//read the city page in API
                    br.close();
                } catch(FileNotFoundException e){}
                catch (IOException e1) {
                    e1.printStackTrace();
                }
                if (!line.equals("")) {
                    String[] word = line.split("word");
                    if (word.length > 1) {
                        for (int i = 1; i < 6 && i < word.length; i++) {
                            String[] temp1 = word[i].split("\":\"");
                            String[] temp2 = temp1[1].split("\",\"");
                            String term2 = temp2[0];
                            Map<String, String> parseSem=p.parse(term2,"","",isStem,"",true);
                            for(String term3: parseSem.keySet()){
                                posting = Indexer.search(postPath, term3.toLowerCase(), isStem, term3.toLowerCase().charAt(0) + "");
                                if(!posting.equals(""))
                                    posS.put(posting, q.get(term));
                            }
                        }
                    }
                }
            }
        }
        Ranker r=new Ranker();
        List<String> docs=r.rank(pos,posS,query,description,isStem,postPath,postingCity,isSemantic);
        return docs;
    }
}
