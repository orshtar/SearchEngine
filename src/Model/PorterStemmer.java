package Model;

public class PorterStemmer {

    private String toStem;

    public void add(String toStem){
        this.toStem=toStem;
    }

    public String Stem(){
        return toStem;
    }

    private void stepOne(){
        String s=toStem;
    }



}
