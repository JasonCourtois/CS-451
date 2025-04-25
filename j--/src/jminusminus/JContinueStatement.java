package jminusminus;

import static jminusminus.CLConstants.GOTO;

/**
 * An AST node for a continue-statement.
 */
class JContinueStatement extends JStatement {
    // Stores the enclosing statement for this continue statement.
    JStatement enclosingStatement;

    // Stores the continueLabel string from enclosing context.
    String continueLabel;
    
    /**
     * Constructs an AST node for a continue-statement.
     *
     * @param line line in which the continue-statement occurs in the source file.
     */
    public JContinueStatement(int line) {
        super(line);
    }

    /**
     * {@inheritDoc}
     */
    public JStatement analyze(Context context) {
        enclosingStatement = JMember.enclosingStatement.peek();

        // Set the hasContinue variable to true in the enclosing statement by checking if the enclosing 
        // statement is an instance of one of the following classes.
        if (enclosingStatement instanceof JDoStatement) {
            ((JDoStatement)enclosingStatement).hasContinue();
        } else if (enclosingStatement instanceof JWhileStatement) {
            ((JWhileStatement)enclosingStatement).hasContinue();
        } else if (enclosingStatement instanceof JForStatement) {
            ((JForStatement)enclosingStatement).hasContinue();
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        // Similar to analyze, get the continue label from the enclosing statement by checking 
        // to see if the statement is an instance of one of the following classes.
        if (enclosingStatement instanceof JDoStatement) {
            continueLabel = ((JDoStatement)enclosingStatement).continueLabel();
        } else if (enclosingStatement instanceof JWhileStatement) {
            continueLabel = ((JWhileStatement)enclosingStatement).continueLabel();
        } else if (enclosingStatement instanceof JForStatement) {
            continueLabel = ((JForStatement)enclosingStatement).continueLabel();
        }

        // Jump to the specified continue label.
        output.addBranchInstruction(GOTO, continueLabel);
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JContinueStatement:" + line, e);
    }
}
