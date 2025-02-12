import java.lang.Integer;
import java.lang.System;

public class Seq {
    public static void main(String[] args) {
        int start = Integer.parseInt(args[0]);
        int step = Integer.parseInt(args[1]);
        int stop = Integer.parseInt(args[2]);

        while (stop > start) {
            System.out.println(start);
            start += step;
        }
        
        return;
    }
}
