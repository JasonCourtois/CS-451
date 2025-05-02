import java.lang.Integer;
import java.lang.System;

public class TableSwitchDefault {
    public static void main(String[] args) {
        int month = Integer.parseInt(args[0]);
        String monthString = "Nope";
        switch (month) {
            default:
                monthString = "default";
        }
        System.out.println(monthString);
    }
}