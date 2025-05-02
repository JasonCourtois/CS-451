import java.lang.Integer;
import java.lang.System;

public class TableSwitch1 {
    public static void main(String[] args) {
        int month = Integer.parseInt(args[0]);
        String monthString = "Nope";
        switch (month) {
            case 4:
                monthString = "4";
                break;
            case 1:
                monthString = "1";
                break;
            case 2:
                monthString = "2";
                break;
            default:
                monthString = "default";
                break;

        }
        System.out.println(monthString);
    }
}