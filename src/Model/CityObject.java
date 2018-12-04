package Model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class CityObject {

    private String state;
    private String curr;
    private String pop;

    public CityObject(String city){
        state="";
        BufferedReader br;
        String line="";
        try {
            URL url = new URL("https://restcountries.eu/rest/v2/capital/"+city);
            br = new BufferedReader(new InputStreamReader(url.openStream()));
            line=br.readLine();
            br.close();
        } catch (IOException e){System.out.println(e.getMessage());}
        if(!line.equals("")) {
            String[] temp = line.split("name");
            if (temp.length >= 2) {
                String[] temp2 = temp[1].split("\"");
                this.state = temp2[2];
            }
            temp = line.split("population");
            String pop = "";
            if (temp.length >= 2) {
                String[] temp2 = temp[1].split(":");
                String[] temp3 = temp2[1].split(",");
                pop = temp3[0];
            }
            double num = Double.parseDouble(pop);
            String sNum;
            String str;
            if (1000 <= num && num < 1000000) {
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
            this.pop = str;
            temp = line.split("currencies");
            if (temp.length >= 2) {
                String[] temp2 = temp[1].split("code");
                String[] temp3 = temp2[1].split("\"");
                this.curr = temp3[2];
            }
        }
    }

    public String toString(){
        return ("state: "+this.state+" curr: "+this.curr+" pop: "+this.pop);
    }

    public boolean isCapital(){
        return !(state.equals(""));
    }
}
