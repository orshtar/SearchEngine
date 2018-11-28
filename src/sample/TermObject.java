package sample;

public class TermObject {

    private short df;
    private String posting;

    public TermObject( short df, String posting){
        this.df=df;
        this.posting=posting;
    }


    public void addDoc(){
        df++;
    }

    public String getPosting(){
        return this.posting;
    }

    public void setPosting(String newPos){
        posting=newPos;
    }

    public String toString(){
        String ans="";
        ans+=(": df-"+df);
        ans+=(" pos-"+posting);
        return ans;
    }
}
