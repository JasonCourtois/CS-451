import java.util.ArrayList;

import jminusminus.CLEmitter;

import static jminusminus.CLConstants.*;

public class GenSeq {
    public static void main(String[] args) {
        CLEmitter myClass = new CLEmitter(true);
        myClass.addClass(null, "Seq", "Object", null, false);
        return;
    }
}
