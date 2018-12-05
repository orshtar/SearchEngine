package Model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 *
 * this class saves information on a city
 *
 */
public class CityObject {

    private String state;
    private String curr;
    private String pop;

    /**
     * constructor gets a city and save the state, currency and the size of the population
     * by using API "https://restcountries.eu/rest/v2/capital/
     *
     * @param city the city to save
     *
     *
     */
    public CityObject(String city){
        state="";
        BufferedReader br;
        String line="";
        try {
            URL url = new URL("https://restcountries.eu/rest/v2/capital/"+city);
            br = new BufferedReader(new InputStreamReader(url.openStream()));
            line=br.readLine();//read the city page in API
            br.close();
        } catch (IOException e){}
        if(!line.equals("")) {
            String[] temp = line.split("name");
            if (temp.length >= 2) {
                String[] temp2 = temp[1].split("\"");
                this.state = temp2[2];//save the date
            }
            temp = line.split("population");
            String pop = "";
            if (temp.length >= 2) {
                String[] temp2 = temp[1].split(":");
                String[] temp3 = temp2[1].split(",");
                pop = temp3[0];//save the population
            }
            double num = Double.parseDouble(pop);
            String sNum;
            String str;
            if (1000 <= num && num < 1000000) {// parse the population by the given rules
                num = num / 1000;
                if (num % 1 == 0)
                    sNum = (num) + "";
                else
                    sNum = String.format("%.2f", num);
                str = sNum + "K";
            } else if (1000000 <= num && num < 1000000000) {
                num = num / 1000000;
                if (num % 1 == 0)
                    sNum = (num) + "";
                else
                    sNum = String.format("%.2f", num);
                str = sNum + "M";
            } else if (num >= 1000000000) {
                num = num / 1000000000;
                if (num % 1 == 0)
                    sNum = ((int) num) + "";
                else
                    sNum = String.format("%.2f", num);
                str = sNum + "B";
            } else {
                str = num + "";
            }
            this.pop = str;//save the parse population
            temp = line.split("currencies");
            if (temp.length >= 2) {
                String[] temp2 = temp[1].split("code");
                String[] temp3 = temp2[1].split("\"");
                this.curr = temp3[2];//save the currency
            }
        }
    }

    /**
     *
     * create a string with the information of the city
     *
     * @return string with state, currency and the size of the population
     */
    public String toString(){
        return ("state: "+this.state+" curr: "+this.curr+" pop: "+this.pop);
    }

    /**
     *
     * check if the city is a capital
     *
     * @return true if the ciuty is capital false otherwise
     */
    public boolean isCapital(){
        return !(state.equals(""));
    }
}
