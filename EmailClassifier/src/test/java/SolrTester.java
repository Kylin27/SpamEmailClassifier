import java.io.*;
import java.net.URL;

/**
 * Created by win-7 on 2016/1/31.
 */
public class SolrTester {
    public static void main (String [] args ) throws Exception {


        File f ;
        FileOutputStream fot ;
        URL url = SolrTester.class.getResource("/stop_word_set.txt") ;

        f = new File( url.getPath()) ;
        fot = new FileOutputStream(f,true) ;

        fot.write("hello".getBytes());

       fot.flush();

        fot.close();

    }







}


