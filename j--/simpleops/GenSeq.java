import java.util.ArrayList;

import jminusminus.CLEmitter;

import static jminusminus.CLConstants.*;

public class GenSeq {
    public static void main(String[] args) {
        // Create a new CLEmitter object.
        CLEmitter e = new CLEmitter(true);
        // Create an ArrayList to store our modifiers.
        ArrayList<String> modifiers = new ArrayList<String>();

        // Add public modifier and create a class to represent our Seq program named Seq.
        modifiers.add("public");
        e.addClass(modifiers, "Seq", "java/lang/Object", null, true);

        // Clear modifiers and make a public static main method to our class.
        modifiers.clear();
        modifiers.add("public");
        modifiers.add("static");
        e.addMethod(modifiers, "main", "([Ljava/lang/String;)V", null, true);

        // Below is the code for reading each command line argument.
        // In our Seq program, we had variables args, start, step, and stop.
        // Args has an offset of 0, start has offset of 1, step has an offset of 2, and stop had offset 3.
        // This instruction loads a reference to array args onto the stack.
        e.addNoArgInstruction(ALOAD_0);
        // This puts a 0 onto the stack as we are getting the 0th item in args
        e.addNoArgInstruction(ICONST_0);
        // Places the item at args[0] onto the stack
        e.addNoArgInstruction(AALOAD);
        // Converts the item on top of the stack to a string.
        e.addMemberAccessInstruction(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I");
        // Stores the integer in offset 1, where our start variable was.
        e.addNoArgInstruction(ISTORE_1);

        // The same set of instructions are preformed for step and stop, with offsets 2 and 3 respectively

        // int step = Integer.parseInt(args[1]);
        e.addNoArgInstruction(ALOAD_0);
        e.addNoArgInstruction(ICONST_1);    // need index 1 from args
        e.addNoArgInstruction(AALOAD);
        e.addMemberAccessInstruction(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I");
        e.addNoArgInstruction(ISTORE_2);

        // int stop = Integer.parseInt(args[2]);
        e.addNoArgInstruction(ALOAD_0);
        e.addNoArgInstruction(ICONST_2);    // need index 2 from args
        e.addNoArgInstruction(AALOAD);
        e.addMemberAccessInstruction(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I");
        e.addNoArgInstruction(ISTORE_3);

        // Here is where the start of while loop is.
        e.addLabel("LoopStart");
        // Condition for while loop
        // put start and stop onto the stack, if stop is less than start, jump to end.
        e.addNoArgInstruction(ILOAD_1); // Start
        e.addNoArgInstruction(ILOAD_3); // Stop
        
        e.addBranchInstruction(IF_ICMPGT, "end");

        // Code for printing start, stored at offset 1
        // Get System.out on our stack
        e.addMemberAccessInstruction(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        // Creates a new string buffer, puts refrence on top of stack.
        e.addReferenceInstruction(NEW, "java/lang/StringBuffer");
        // Duplicates the string buffer refrence.
        e.addNoArgInstruction(DUP);
        // Calls the constructor of the string buffer, this pops the top reference of the string buffer from stack.
        e.addMemberAccessInstruction(INVOKESPECIAL, "java/lang/StringBuffer", "<init>", "()V");

    
        // Load the value of start onto the stack, then append it to the string buffer.
        e.addNoArgInstruction(ILOAD_1);
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "java/lang/StringBuffer", "append", "(I)Ljava/lang/StringBuffer;");
        
        // Turn string buffer into a string
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "java/lang/StringBuffer", "toString", "()Ljava/lang/String;");
        // Print string using println
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");

        // Below is the code for adding step to start
        e.addNoArgInstruction(ILOAD_1); // Start
        e.addNoArgInstruction(ILOAD_2); // Step
        e.addNoArgInstruction(IADD);    // Add them
        e.addNoArgInstruction(ISTORE_1); // Store back in start

        // Loop to start
        e.addBranchInstruction(GOTO, "LoopStart");
        e.addLabel("end");
        
        e.addNoArgInstruction(RETURN);
        e.write();
    }
}
