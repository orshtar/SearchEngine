package Model;

/**
 *
 * this class saves information on a term: the number of file the term appear and the frequency of the word in the corpus
 *
 */
public class TermObject {

    private short df;
    private int f;

    /**
     *
     * a simple constructor
     *
     * @param df number of file the term appear
     * @param f frequency of the word in the corpus
     */
    public TermObject( short df, int f){
        this.df=df;
        this.f=f;
    }


    /**
     *
     * adds a new doc and the frequency of the word in the doc
     *
     * @param ftoAdd frequency of the word in the doc
     */
    public void addDoc(int ftoAdd){
        df++;
        f+=ftoAdd;
    }

    /**
     *
     * create a string with the information of the city
     *
     * @return string with state, currency and the size of the population
     */
    public String toString(){
        return (f+"");
    }
}
