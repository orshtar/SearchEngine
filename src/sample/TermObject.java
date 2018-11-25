package sample;

public class TermObject {

    private String term;
    private int df;
    private String docList;
    private String posting;

    public TermObject(String term, int df, String docl, String posting){
        this.term=term;
        this.df=df;
        this.docList=docl;
        this.posting=posting;
    }

    public boolean equals(Object o){
        boolean ans=false;
        if(o instanceof TermObject){
            if(this.term.equals(((TermObject)o).getTerm())){
                ans=true;
            }
        }
        return ans;
    }

    public int hashCode(){
        return term.hashCode();
    }

    public String getTerm(){
        return this.term;
    }

    public void setTerm(){
        this.term=term.toLowerCase();
    }

    public void addDoc(String newDoc){
        this.docList+=(", "+newDoc);
        df++;
    }

    public String getPosting(){
        return this.posting;
    }
}
