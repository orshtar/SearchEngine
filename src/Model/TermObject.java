package Model;

public class TermObject {

    private short df;
    private int f;

    public TermObject( short df, int f){
        this.df=df;
        this.f=f;
    }


    public void addDoc(int ftoAdd){
        df++;
        f+=ftoAdd;
    }

    public String toString(){
        return (f+"");
    }
}
