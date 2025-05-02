package jminusminus;

import static jminusminus.CLConstants.LOOKUPSWITCH;
import static jminusminus.CLConstants.TABLESWITCH;

import java.util.ArrayList;
import java.util.TreeMap;

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

    private boolean hasDefault = false;

    private int opcode = 0;

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
        condition.analyze(localContext);
        condition.type().mustMatchExpected(line, Type.INT);

        // First check if there are switch statement groups present.
        if (switchStmtGroups != null) {
            // Loop through all groups.
            for (SwitchStatementGroup group : switchStmtGroups) {
                // Loop through all labels, analyze them and confirm they are integers.
                for (JExpression label : group.getSwitchLabels()) {
                     // The only situation where label is null is for the default case:
                     if (label == null) {
                        hasDefault = true;
                        continue;
                    }

                    // Increment the total number of labels
                    nLabels++;

                    // Analyze label and verify it is an int.
                    label.analyze(localContext);
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

            // Edge Case: there is ONLY a default case present in the switch statement
            if (nLabels == 0 && hasDefault) {
                // Set hi and lo to 0 to only create one excess label - this edgecase is handled in tableSwitch() method
                lo = 0;
                hi = 0;
                opcode = TABLESWITCH;
            } else if (nLabels == 0 && !hasDefault) {
                // If there is no label or no default, our switch statement is empty - assign opcode to be 0.
                opcode = 0;
            } else {
                // Compute the correct opcode that must be used for this switch statement
                long tableSpaceCost = 5 + hi - lo ;
                long tableTimeCost = 3;
                long lookupSpaceCost = 3 + 2 * nLabels ;
                long lookupTimeCost = nLabels ;
                opcode = nLabels > 0 &&
                ( tableSpaceCost + 3 * tableTimeCost <= lookupSpaceCost + 3 * lookupTimeCost ) ?
                TABLESWITCH : LOOKUPSWITCH ;
            }
            
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

        // Run codegen on the condition of our switch statement
        condition.codegen(output);

        // Create an end label to jump to. This label is used if there is no default case present.
        String endLabel = output.createLabel();
    
        if (opcode == TABLESWITCH) {
            tableSwitch(output, endLabel);
        } else if (opcode == LOOKUPSWITCH) {
            lookupSwitch(output, endLabel);
        }
        
        // Place end label at the end of the switch statement.
        output.addLabel(endLabel);

        if (hasBreak) {
            output.addLabel(breakLabel);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void lookupSwitch(CLEmitter output, String endLabel) {
        // Tree map object to store switch label and jump label pairs.
        TreeMap<Integer, String> matchLabelPairs = new TreeMap<Integer, String>();

        // Loop through labels to create matchLabelPairs
        for (SwitchStatementGroup group : switchStmtGroups) {
            for (JExpression label : group.getSwitchLabels()) {
                // Ignore default label
                if (label != null) {
                    // Set the key as the switch label as an integer, and create a new label for this key.
                    matchLabelPairs.put(((JLiteralInt) label).toInt(), output.createLabel());
                }
            }
        }

        // Create a default label for this switch statement.
        String defaultLabel = output.createLabel();

        // If it has a default case, use the new default label. Otherwise use the end label at the end of the switch statement.
        if (hasDefault) {
            output.addLOOKUPSWITCHInstruction(defaultLabel, nLabels, matchLabelPairs);
        } else {
            output.addLOOKUPSWITCHInstruction(endLabel, nLabels, matchLabelPairs);
        }

        // Loop through each group - we know this isn't null as the opcode was set to LOOKUPSWITCH
        for (SwitchStatementGroup group : switchStmtGroups) {
            // Add the default label or corresponding label from matchLabelPairs
            for (JExpression label : group.getSwitchLabels()) {
                if (label == null) {
                    output.addLabel(defaultLabel);
                } else {
                    output.addLabel(matchLabelPairs.get(((JLiteralInt) label).toInt()));
                }
            }
            // If block() isn't null, run codegen on the blocks of code.
            if (group.block() != null) {
                for (JStatement block : group.block()) {
                    block.codegen(output);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void tableSwitch(CLEmitter output, String endLabel) {
        ArrayList<String> labels = new ArrayList<String>();
        ArrayList<Boolean> labelsUsed = new ArrayList<Boolean>();

        // Creates a range of labels
        for (int i = 0; i <= (hi - lo); i++) {
            labels.add(output.createLabel());
            labelsUsed.add(false);
        }

        // Need to loop through labels to see which ones are used or not.
        // This list will be used to determine what excess labels must be placed before the default label.
        for (SwitchStatementGroup group : switchStmtGroups) {
            for (JExpression label : group.getSwitchLabels()) {
                if (label != null) {
                    labelsUsed.set(((JLiteralInt) label).toInt() - lo, true);
                }
            }
        }

        String defaultLabel = output.createLabel();

        // If there is a default statement, use the default label.
        if (hasDefault) {
            output.addTABLESWITCHInstruction(defaultLabel, lo, hi, labels);
        } else {
            // Otherwise, use the end label that is present at the end of the switch statement.
            output.addTABLESWITCHInstruction(endLabel, lo, hi, labels);
        }
        
        // Loop through each group - we know this isn't null as this method should only be called once opcode TABLESWITCH is chosen.
        for (SwitchStatementGroup group : switchStmtGroups) {
            for (JExpression label : group.getSwitchLabels()) {
                if (label == null) {
                    if (nLabels == 0 && hasDefault) {
                        // Edge Case: there is only a default label, no other labels in switch present.
                        // In this case there will only be one label in the labels array list.
                        // Place that label above the default label to satisfy requirements of addTABLESWITCHInstruction
                        // Essentially, our generated code looks like:
                        // case 0:
                        // default:
                        //   {body.codgen code}
                        output.addLabel(labels.get(0));
                        labelsUsed.set(0, true);
                    }

                    // If a default case is present: any unused labels are placed right before the default label.
                    // Unused labels occur when the labels follow a pattern such as case 1, case 2, case 4.
                    // In this example, table switch will be selected and case 3 is unused.
                    for (int i = 0; i <= (hi - lo); i++) {
                        if (!labelsUsed.get(i)) {
                            output.addLabel(labels.get(i));
                        }
                    }
                    output.addLabel(defaultLabel);
                } else {
                    output.addLabel(labels.get(((JLiteralInt) label).toInt() - lo));
                }
            }
            if (group.block() != null) {
                for (JStatement block : group.block()) {
                    block.codegen(output);
                }
            }
        }

        // If no default label is present, place unused labels at end of switch statement.
        if (!hasDefault) {
            for (int i = 0; i <= (hi - lo); i++) {
                if (!labelsUsed.get(i)) {
                    output.addLabel(labels.get(i));
                }
            }
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
