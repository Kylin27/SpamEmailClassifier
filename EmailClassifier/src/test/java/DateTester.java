import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by win-7 on 2016/2/3.
 */
public class DateTester {
    @Test
    public void am_pm_tester(){

        Calendar dateTime = Calendar.getInstance() ;

        if ( dateTime.get(Calendar.AM_PM) == Calendar.AM)
            System.out.println("am") ;
        else if ( dateTime.get(Calendar.AM_PM) == Calendar.PM)
            System.out.println("pm") ;

    }
}
