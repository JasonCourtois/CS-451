package jminusminus;

import static jminusminus.CLConstants.LOOKUPSWITCH;
import static jminusminus.CLConstants.TABLESWITCH;

import java.util.ArrayList;

/**
 * The AST node for a switch-statement.
 */
class JSwitchStatement extends JStatement {
    // Test expression.
    private JExpression condition;

    // List of switch-statement groups.
    private ArrayList<SwitchStatementGroup> switchStmtGroups;

    // Determines if a break is present
    private boolean hasBreak;

    // Stores the name of the break label
    private String breakLabel;

    private int hi = Integer.MIN_VALUE;

    private int lo = Integer.MAX_VALUE;

    private int nLabels = 0;

    private int opcode;

    /**
     * Constructs an AST node for a switch-statement.
     *
     * @param line             line in which the switch-statement occurs in the
     *                         source file.
     * @param condition        test expression.
     * @param switchStmtGroups list of statement groups.
     */
    public JSwitchStatement(int line, JExpression condition, ArrayList<SwitchStatementGroup> switchStmtGroups) {
        super(line);
        this.condition = condition;
        this.switchStmtGroups = switchStmtGroups;
    }

    /**
     * Sets the hasBreak variable to true, signifying that the control flow
     * statement has a break in it.
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
     * {@inheritDoc}
     */
    public JStatement analyze(Context context) {
        // Push this instance into JMember enclosing statement.
        JMember.enclosingStatement.push(this);

        // Create a new local context for this switch statement.
        LocalContext localContext = new LocalContext(context);


        // Analyze the condition and verify that it is an integer.
        condition = condition.analyze(localContext);
        condition.type().mustMatchExpected(line, Type.INT);

        // First check if there are switch statement groups present.
        if (switchStmtGroups != null) {
            // Loop through all groups.
            for (SwitchStatementGroup group : switchStmtGroups) {
                // Loop through all labels, analyze them and confirm they are integers.
                for (JExpression label : group.getSwitchLabels()) {
                    // Increment the total number of labels
                    nLabels ++;

                    // The only situation where label is null is for the default case:
                    if (label == null) {
                        continue;
                    }

                    // Analyze label and verify it is an int.
                    label = label.analyze(localContext);
                    label.type().mustMatchExpected(line, Type.INT);

                    // Get the value of the integer, then compare it to the hi and lo values.
                    int labelValue = ((JLiteralInt) label).toInt();
                    if (labelValue > hi) {
                        hi = labelValue;
                    }
                    if (labelValue < lo) {
                        lo = labelValue;
                    }
                }

                // If there are block statements present, analyze them as well.
                if (group.block() != null) {
                    for (JStatement block : group.block()) {
                        block.analyze(localContext);
                    }
                }
            }

            // Compute the correct opcode that must be used for this switch statement
            long tableSpaceCost = 5 + hi - lo ;
            long tableTimeCost = 3;
            long lookupSpaceCost = 3 + 2 * nLabels ;
            long lookupTimeCost = nLabels ;
            opcode = nLabels > 0 &&
            ( tableSpaceCost + 3 * tableTimeCost <= lookupSpaceCost + 3 * lookupTimeCost ) ?
            TABLESWITCH : LOOKUPSWITCH ;
        }

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

        if (opcode == TABLESWITCH) {
            
        } else if (opcode == LOOKUPSWITCH) {

        }
        
        if (hasBreak) {
            output.addLabel(breakLabel);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JSwitchStatement:" + line, e);
        JSONElement e1 = new JSONElement();
        e.addChild("Condition", e1);
        condition.toJSON(e1);
        for (SwitchStatementGroup group : switchStmtGroups) {
            group.toJSON(e);
        }
    }
}

/**
 * A switch-statement group consists of a list of switch labels and a block of
 * statements.
 */
class SwitchStatementGroup {
    // Switch labels.
    private ArrayList<JExpression> switchLabels;

    // Block of statements.
    private ArrayList<JStatement> block;

    /**
     * Constructs a switch-statement group.
     *
     * @param switchLabels switch labels.
     * @param block        block of statements.
     */
    public SwitchStatementGroup(ArrayList<JExpression> switchLabels, ArrayList<JStatement> block) {
        this.switchLabels = switchLabels;
        this.block = block;
    }

    /**
     * Returns the switch labels associated with this switch-statement group.
     *
     * @return the switch labels associated with this switch-statement group.
     */
    public ArrayList<JExpression> getSwitchLabels() {
        return switchLabels;
    }

    /**
     * Returns the block of statements associated with this switch-statement group.
     *
     * @return the block of statements associated with this switch-statement group.
     */
    public ArrayList<JStatement> block() {
        return block;
    }

    /**
     * Stores information about this switch statement group in JSON format.
     *
     * @param json the JSON emitter.
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("SwitchStatementGroup", e);
        for (JExpression label : switchLabels) {
            JSONElement e1 = new JSONElement();
            if (label != null) {
                e.addChild("Case", e1);
                label.toJSON(e1);
            } else {
                e.addChild("Default", e1);
            }
        }
        if (block != null) {
            for (JStatement stmt : block) {
                stmt.toJSON(e);
            }
        }
    }
}
