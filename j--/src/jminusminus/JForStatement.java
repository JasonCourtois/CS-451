package jminusminus;

import static jminusminus.CLConstants.GOTO;

import java.util.ArrayList;

/**
 * The AST node for a for-statement.
 */
class JForStatement extends JStatement {
    // Initialization.
    private ArrayList<JStatement> init;

    // Test expression
    private JExpression condition;

    // Update.
    private ArrayList<JStatement> update;

    // The body.
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
     * Constructs an AST node for a for-statement.
     *
     * @param line      line in which the for-statement occurs in the source file.
     * @param init      the initialization.
     * @param condition the test expression.
     * @param update    the update.
     * @param body      the body.
     */
    public JForStatement(int line, ArrayList<JStatement> init, JExpression condition, ArrayList<JStatement> update,
                         JStatement body) {
        super(line);
        this.init = init;
        this.condition = condition;
        this.update = update;
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
    public JForStatement analyze(Context context) {
        // Push this instance into JMember enclosing statement
        JMember.enclosingStatement.push(this);

        LocalContext localContext = new LocalContext(context);

        // Init, condition, and update are all optional in for statements. Whenever using these variables we must check they are not null.

        // If init is present, analyze every statement it has
        if (init != null) {
            for (JStatement statement : init) {
                statement = (JStatement) statement.analyze(localContext);
            }
        }
        
        // If condition is present, analyze it
        if (condition != null) {
            condition = condition.analyze(localContext);
            condition.type().mustMatchExpected(line, Type.BOOLEAN);
        }
        
        // If update is present, analyze every statement it has
        if (update != null) {
            for (JStatement statement : update) {
                statement = (JStatement) statement.analyze(localContext);
            }
        }
        
        body = (JStatement) body.analyze(localContext);
        

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

        // Label for test condition of for loop.
        String testLabel = output.createLabel();
        // Label for end of for loop.
        String endLabel = output.createLabel();

        // Preform codegen on init statements if present
        if (init != null) {
            for (JStatement statement : init) {
                statement.codegen(output);
            }
        }

        // Add label for our test condition, and preform codegen if present.
        output.addLabel(testLabel);
        if (condition != null) {
            condition.codegen(output, endLabel, false);
        }
        
        // Run the code of our body
        body.codegen(output);

        // If there is a continue statement, put label at the end of the body
        if (hasContinue) {
            output.addLabel(continueLabel);
        }

        // Run any update statements that are present
        if (update != null) {
            for (JStatement statement : update) {
                statement.codegen(output);
            }
        }
        
        // Jump back to our test condition.
        output.addBranchInstruction(GOTO, testLabel);
        // We will only reach this label once the for loop terminates.
        output.addLabel(endLabel);

        // Break label skips to end of for statement
        if (hasBreak) {
            output.addLabel(breakLabel);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JForStatement:" + line, e);
        if (init != null) {
            JSONElement e1 = new JSONElement();
            e.addChild("Init", e1);
            for (JStatement stmt : init) {
                stmt.toJSON(e1);
            }
        }
        if (condition != null) {
            JSONElement e1 = new JSONElement();
            e.addChild("Condition", e1);
            condition.toJSON(e1);
        }
        if (update != null) {
            JSONElement e1 = new JSONElement();
            e.addChild("Update", e1);
            for (JStatement stmt : update) {
                stmt.toJSON(e1);
            }
        }
        if (body != null) {
            JSONElement e1 = new JSONElement();
            e.addChild("Body", e1);
            body.toJSON(e1);
        }
    }
}
