package Model;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Searcher {

    public void SearchQ(String query, boolean isStem, boolean isSemantic, String postPath){
        Parse p=new Parse();
        Map<String,String> q=p.parse(query,"","",isStem,"",true);
        List<String> pos=new LinkedList<>();
        for(String term:q.keySet()){
            String posting;
            if(term.toLowerCase().charAt(0)>='a' && term.toLowerCase().charAt(0)<='z')
                posting=Indexer.search(postPath,term.toLowerCase(),isStem,term.toLowerCase().charAt(0)+"");
            else
                posting=Indexer.search(postPath,term.toLowerCase(),isStem,"nums");
            pos.add(posting);
        }
        Ranker r=new Ranker();
        r.rank(pos,isSemantic,query,isStem,postPath);
    }
}
