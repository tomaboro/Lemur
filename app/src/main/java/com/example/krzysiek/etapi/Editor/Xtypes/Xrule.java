package com.example.krzysiek.etapi.Editor.Xtypes;

import com.example.krzysiek.etapi.Editor.RuleExpressions.ALSVExpression;
import com.example.krzysiek.etapi.Editor.RuleExpressions.ActionExpression;
import com.example.krzysiek.etapi.Editor.RuleExpressions.DecisionExpression;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * Created by Krzysiek on 2016-12-15.
 */
public class Xrule implements XTT2StringRepresentation, Serializable {
    private String nameOfSchema; // Mandatory
    private String ID; // Mandatory
    private LinkedList<ALSVExpression> conditionsList = new LinkedList(); // Mandatory
    private LinkedList<DecisionExpression> decisionsList = new LinkedList(); // Mandatory
    private LinkedList<ActionExpression> actionsList = new LinkedList(); // Optional
    private String schemaForTokenDriven; // Optional TODO

    public Xrule(String nameOfSchema, String ID, LinkedList<ALSVExpression> conditionsList, LinkedList<DecisionExpression> decisionsList, LinkedList<ActionExpression> actionsList) {
        this.nameOfSchema = nameOfSchema;
        this.ID = ID;
        this.conditionsList = conditionsList;
        this.decisionsList = decisionsList;
        this.actionsList = actionsList;
    }

    /**
     * Method returns String representation of object, useful in saving model into file
     * @return String represantation of Xrule
     */
    @Override
    public String returnStringForModel() {
        String ruleString = "xrule '" + nameOfSchema + "'/" + ID +": \n";
        int counter = 0;
        for (ALSVExpression i : conditionsList) {
            if(counter == 0) ruleString += "\t   [";
            else ruleString += "\t    ";
            ruleString += i.returnStringForModel() + ",\n";
            ++counter;
        }
        ruleString = ruleString.substring(0, ruleString.length() - 2);
        ruleString += "]\n\t==>\n";
        counter = 0;
        for (DecisionExpression i : decisionsList) {
            if(counter == 0) ruleString += "\t   [";
            else ruleString += "\t    ";
            ruleString += i.returnStringForModel() + ",\n";
            ++counter;
        }
        ruleString = ruleString.substring(0, ruleString.length() - 2);
        ruleString += "]\n\t\t**>\n";
        counter = 0;
        for (ActionExpression i : actionsList) {
            if(counter == 0) ruleString += "\t\t   [";
            else ruleString += "\t\t    ";
            ruleString += i.returnStringForModel() + ",\n";
            ++counter;
        }
        ruleString = ruleString.substring(0, ruleString.length() - 2);
        ruleString += "].";
        return ruleString;
    }
}