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

/**
 *  this class responsible for searching a query in docs in posting files
 */
public class Searcher {

    /**
     *
     * @param query- the query to search
     * @param desc- description of the query to search
     * @param isStem- is stemming need to be done
     * @param isSemantic- is semantic search need to be done
     * @param postPath- path ro posting files
     * @param cities- selected cities bt user
     * @return- a list of documetns which at least one word from query appear int hem
     */
    public List<String> SearchQ(String query ,String desc, boolean isStem, boolean isSemantic, String postPath, List<String> cities){
        Parse p=new Parse();
        Map<String,String> q=p.parse(query,"","",isStem,"",true); // parse query
        Map<String,String> d=p.parse(desc,"","",isStem,"",true);  // parse description
        Map<String, String> pos=new LinkedHashMap<>();
        Map<String,String> posS=new LinkedHashMap<>();
        Map<String, String> description=new LinkedHashMap<>();
        List<String> postingCity=new LinkedList<>();
        for(String city: cities){
            postingCity.add(Indexer.search(postPath,city,isStem,"cities.txt"));  // get city posting for each city the user selected
        }
        for(String des:d.keySet()) {
            if(!(des.equalsIgnoreCase("information")||des.equalsIgnoreCase("Identify")
                    ||des.equalsIgnoreCase("discuss")|| des.equalsIgnoreCase("provide")
                    ||des.equalsIgnoreCase("documents")|| des.equalsIgnoreCase("i.e"))) {
                String posting;
                if (des.toLowerCase().charAt(0) >= 'a' && des.toLowerCase().charAt(0) <= 'z')
                    posting = Indexer.search(postPath, des.toLowerCase(), isStem, des.toLowerCase().charAt(0) + "");
                else
                    posting = Indexer.search(postPath, des.toLowerCase(), isStem, "nums");   // find posting of each word in description
                description.put(posting, d.get(des));
            }
        }
        for(String term:q.keySet()){
            boolean a=false;
            String next="";
            String pre="";
            for(String term2:q.keySet()){
                if(a){
                    next=term;
                    break;
                }
                if(term2.equals(term))
                    a=true;
                if(!a)
                    pre=term2;
            }
            String posting;
            if(term.toLowerCase().charAt(0)>='a' && term.toLowerCase().charAt(0)<='z')
                posting=Indexer.search(postPath,term.toLowerCase(),isStem,term.toLowerCase().charAt(0)+"");
            else
                posting=Indexer.search(postPath,term.toLowerCase(),isStem,"nums");   // find posting of each word in query
            pos.put(posting,q.get(term));
            BufferedReader br;
            String line="";
            if(isSemantic) {
                try {
                    URL url = new URL("https://api.datamuse.com/words?ml=" + term+"&rel_syn="+term+"&lc="+pre+"&rc="+next);
                    br = new BufferedReader(new InputStreamReader(url.openStream()));
                    line = br.readLine();
                    br.close();
                } catch(FileNotFoundException e){}
                catch (IOException e1) {
                    e1.printStackTrace();
                }
                if (!line.equals("")) {
                    String[] word = line.split("word");   //find similar words for each word in query
                    if (word.length > 1) {
                        for (int i = 1; i < 4 && i < word.length; i++) {  //find 3 similar words
                            String[] temp1 = word[i].split("\":\"");
                            String[] temp2 = temp1[1].split("\",\"");
                            String term2 = temp2[0];
                            Map<String, String> parseSem=p.parse(term2,"","",isStem,"",true);  //parse semantic words
                            for(String term3: parseSem.keySet()){
                                posting = Indexer.search(postPath, term3.toLowerCase(), isStem, term3.toLowerCase().charAt(0) + "");   // find poating for semantic words
                                if(!posting.equals(""))
                                    posS.put(posting, q.get(term));
                            }
                        }
                    }
                }
            }
        }
        Ranker r=new Ranker();
        List<String> docs=r.rank(pos,posS,description,isStem,postPath,postingCity,isSemantic);  // send all posting found to ranker
        return docs;
    }
}
