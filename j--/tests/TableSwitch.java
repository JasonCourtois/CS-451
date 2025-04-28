import java.lang.Integer;
import java.lang.System;

public class TableSwitch {
    public static void main(String[] args) {
        int month = Integer.parseInt(args[0]);
        String monthString = "Nope";
        switch (month) {
            case 1:
                monthString = "January";
                break;
            case 2:
                monthString = "February";
                break;
        }
        System.out.println(monthString);
    }
}