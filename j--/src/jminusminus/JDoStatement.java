package jminusminus;

/**
 * The AST node for a do-statement.
 */
class JDoStatement extends JStatement {
    // Body.
    private JStatement body;

    // Test expression.
    private JExpression condition;

    // Determines if a break is present
    private boolean hasBreak;

    // Stores the name of the break label
    private String breakLabel;

    // Determines if a continue is present
    private boolean hasContinue;

    // Stores the name of the continue label
    private String continueLabel;

    /**
     * Constructs an AST node for a do-statement.
     *
     * @param line      line in which the do-statement occurs in the source file.
     * @param body      the body.
     * @param condition test expression.
     */
    public JDoStatement(int line, JStatement body, JExpression condition) {
        super(line);
        this.body = body;
        this.condition = condition;
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
    public JStatement analyze(Context context) {
        // Push this instance into JMember enclosing statement
        JMember.enclosingStatement.push(this);

        // Analyze body and condition of code.
        body = (JStatement) body.analyze(context);
        condition = condition.analyze(context);
        // Confirm that the condition is a boolean.
        condition.type().mustMatchExpected(line(), Type.BOOLEAN);
        
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

        // Label placed at start of do loop.
        String topLabel = output.createLabel();
        output.addLabel(topLabel);

        // Generate code for body.
        body.codegen(output);
        
        // If there is a continue statement, put label at the end of the body
        if (hasContinue) {
            output.addLabel(continueLabel);
        }

        // Loop back to start if condition is true.
        condition.codegen(output, topLabel, true);

        // Break statement skips to end of do statement
        if (hasBreak) {
            output.addLabel(breakLabel);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JDoStatement:" + line, e);
        JSONElement e1 = new JSONElement();
        e.addChild("Body", e1);
        body.toJSON(e1);
        JSONElement e2 = new JSONElement();
        e.addChild("Condition", e2);
        condition.toJSON(e2);
    }
}
