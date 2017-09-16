import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;

public class Test {

    @org.junit.Test
    public void test() {

        LocalDate date = LocalDate.of(2014, 3, 18);
        int year = date.getYear();
        Month month = date.getMonth();
        int day = date.getDayOfMonth();
        DayOfWeek dow = date.getDayOfWeek();
        int len = date.lengthOfMonth();
        boolean leap = date.isLeapYear();
        System.out.println(leap);

    }

}
