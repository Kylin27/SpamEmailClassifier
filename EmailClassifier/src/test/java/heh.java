import java.net.URL;

/**
 * Created by win-7 on 2016/1/31.
 */
public class heh {
    public static void main (String [] args ) throws Exception {

        URL url = heh.class.getResource("/stop_word_set.txt") ;
        System.out.println(url.getPath());

    }

}
