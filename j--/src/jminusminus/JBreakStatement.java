package jminusminus;

import static jminusminus.CLConstants.GOTO;

/**
 * An AST node for a break-statement.
 */
class JBreakStatement extends JStatement {
    // Stores the enclosing statement for this break statement.
    JStatement enclosingStatement;

    // Stores the breakLabel string from enclosing context.
    String breakLabel;

    /**
     * Constructs an AST node for a break-statement.
     *
     * @param line line in which the break-statement occurs in the source file.
     */
    public JBreakStatement(int line) {
        super(line);
    }

    /**
     * {@inheritDoc}
     */
    public JStatement analyze(Context context) {
        enclosingStatement = JMember.enclosingStatement.peek();

        // Set the hasBreak variable to true in the enclosing statement by checking if the enclosing 
        // statement is an instance of one of the following classes.
        if (enclosingStatement instanceof JDoStatement) {
            ((JDoStatement)enclosingStatement).hasBreak();
        } else if (enclosingStatement instanceof JWhileStatement) {
            ((JWhileStatement)enclosingStatement).hasBreak();
        } else if (enclosingStatement instanceof JForStatement) {
            ((JForStatement)enclosingStatement).hasBreak();
        } else if (enclosingStatement instanceof JSwitchStatement) {
            ((JSwitchStatement)enclosingStatement).hasBreak();
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        // Similar to analyze, get the break label from the enclosing statement by checking 
        // to see if the statement is an instance of one of the following classes.
        if (enclosingStatement instanceof JDoStatement) {
            breakLabel = ((JDoStatement)enclosingStatement).breakLabel();
        } else if (enclosingStatement instanceof JWhileStatement) {
            breakLabel = ((JWhileStatement)enclosingStatement).breakLabel();
        } else if (enclosingStatement instanceof JForStatement) {
            breakLabel = ((JForStatement)enclosingStatement).breakLabel();
        } else if (enclosingStatement instanceof JSwitchStatement) {
            breakLabel = ((JSwitchStatement)enclosingStatement).breakLabel();
        }

        // Jump to the specified break label.
        output.addBranchInstruction(GOTO, breakLabel);
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JBreakStatement:" + line, e);
    }
}
