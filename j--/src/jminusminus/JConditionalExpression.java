package jminusminus;

import static jminusminus.CLConstants.GOTO;

/**
 * The AST node for a conditional expression.
 */
class JConditionalExpression extends JExpression {
    // Test expression.
    private JExpression condition;

    // Then part.
    private JExpression thenPart;

    // Else part.
    private JExpression elsePart;

    /**
     * Constructs an AST node for a conditional expression.
     *
     * @param line      line in which the conditional expression occurs in the source file.
     * @param condition test expression.
     * @param thenPart  then part.
     * @param elsePart  else part.
     */
    public JConditionalExpression(int line, JExpression condition, JExpression thenPart, JExpression elsePart) {
        super(line);
        this.condition = condition;
        this.thenPart = thenPart;
        this.elsePart = elsePart;
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        // Analyze each part of the conditional expression.
        condition = condition.analyze(context);
        thenPart = thenPart.analyze(context);
        elsePart = elsePart.analyze(context);
        
        // Check that the condition is of type boolean, and that the else and then parts have the same type.
        condition.type().mustMatchExpected(line(), Type.BOOLEAN);
        elsePart.type().mustMatchExpected(line(), thenPart.type());

        // Assign type and then return.
        type = elsePart.type();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        // Generate labels for elsePart and end of statement.
        String elseLabel = output.createLabel();
        String endLabel = output.createLabel();
        // If the condition is false, jump to elseLabel
        condition.codegen(output, elseLabel, false);
        // thenPart - Otherwise, generate code and jump to end.
        thenPart.codegen(output);
        output.addBranchInstruction(GOTO, endLabel);
        // elsePart - add a label here, generate code, and then add end label at end.
        output.addLabel(elseLabel);
        elsePart.codegen(output);
        output.addLabel(endLabel);
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JConditionalExpression:" + line, e);
        JSONElement e1 = new JSONElement();
        e.addChild("Condition", e1);
        condition.toJSON(e1);
        JSONElement e2 = new JSONElement();
        e.addChild("ThenPart", e2);
        thenPart.toJSON(e2);
        JSONElement e3 = new JSONElement();
        e.addChild("ElsePart", e3);
        elsePart.toJSON(e3);
    }
}
