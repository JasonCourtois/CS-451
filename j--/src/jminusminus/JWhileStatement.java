package jminusminus;

import static jminusminus.CLConstants.GOTO;

/**
 * The AST node for a while-statement.
 */
class JWhileStatement extends JStatement {
    // Test expression.
    private JExpression condition;

    // Body.
    private JStatement body;

    // Determines if a break is present
    private boolean hasBreak;

    // Stores the name of the break label
    private String breakLabel;

    // Determines if a continue is present
    private boolean hasContinue;

    // Stores the name of the continue label
    private String continueLabel;

    /**
     * Constructs an AST node for a while-statement.
     *
     * @param line      line in which the while-statement occurs in the source file.
     * @param condition test expression.
     * @param body      the body.
     */
    public JWhileStatement(int line, JExpression condition, JStatement body) {
        super(line);
        this.condition = condition;
        this.body = body;
    }

    /**
     * Sets the hasBreak variable to true, signifying that the control flow statement has a break in it.
     */
    public void hasBreak() {
        hasBreak = true;
    }

    /**
     * @return String of break label for this control flow statement
     */
    public String breakLabel() {
        return breakLabel;
    }

    /**
     * Sets the hasContinue variable to true, signifying that the control flow statement has a continue in it.
     */
    public void hasContinue() {
        hasContinue = true;
    }

    /**
     * @return String of continue label for this control flow statement
     */
    public String continueLabel() {
        return continueLabel;
    }

    /**
     * {@inheritDoc}
     */
    public JWhileStatement analyze(Context context) {
        // Push this instance into JMember enclosing statement
        JMember.enclosingStatement.push(this);
        
        condition = condition.analyze(context);
        condition.type().mustMatchExpected(line(), Type.BOOLEAN);
        body = (JStatement) body.analyze(context);

        // Pop this instance into JMember enclosing statement
        JMember.enclosingStatement.pop();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        // Create a break label if one is present
        if (hasBreak) {
            breakLabel = output.createLabel();
        }

        // Create a continue label if one is present
        if (hasContinue) {
            continueLabel = output.createLabel();
        }

        String testLabel = output.createLabel();
        String endLabel = output.createLabel();
        output.addLabel(testLabel);
        condition.codegen(output, endLabel, false);
        body.codegen(output);

        // If there is a continue statement, put label at the end of the body
        if (hasContinue) {
            output.addLabel(continueLabel);
        }
        
        output.addBranchInstruction(GOTO, testLabel);
        output.addLabel(endLabel);

        // Break statement skips to end of while loop
        if (hasBreak) {
            output.addLabel(breakLabel);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JWhileStatement:" + line, e);
        JSONElement e1 = new JSONElement();
        e.addChild("Condition", e1);
        condition.toJSON(e1);
        JSONElement e2 = new JSONElement();
        e.addChild("Body", e2);
        body.toJSON(e2);
    }
}
