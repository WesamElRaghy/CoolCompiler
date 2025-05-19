import ast.*;
import java.util.*;

/**
 * Generates Three-Address Code (TAC) from AST
 */
public class IRGenerator {
    private List<String> code;
    private int tempCounter;
    private int labelCounter;

    public IRGenerator() {
        code = new ArrayList<>();
        tempCounter = 0;
        labelCounter = 0;
    }

    /**
     * Generate IR code from the AST
     */
    public List<String> generate(ProgramNode ast) {
        // Clear previous state
        code.clear();
        tempCounter = 0;
        labelCounter = 0;

        // Add program header comment
        code.add("# Three-Address Code IR");

        // Process each class
        for (ClassNode classNode : ast.getClasses()) {
            generateClassIR(classNode);
        }

        return code;
    }

    private void generateClassIR(ClassNode classNode) {
        // Add class header comment
        code.add("\n# Class " + classNode.getName());

        if (classNode.getParentName() != null) {
            code.add("# Inherits from " + classNode.getParentName());
        }

        // Process each feature (attribute or method)
        for (FeatureNode feature : classNode.getFeatures()) {
            if (feature instanceof AttributeNode) {
                generateAttributeIR((AttributeNode) feature);
            } else if (feature instanceof MethodNode) {
                generateMethodIR((MethodNode) feature);
            }
        }
    }

    private void generateAttributeIR(AttributeNode attr) {
        code.add("\n# Attribute " + attr.getName() + " : " + attr.getType());

        // Generate initializer if present
        if (attr.getInitExpr() != null) {
            // Get result of init expression
            String temp = generateExpressionIR(attr.getInitExpr());
            code.add(attr.getName() + " = " + temp);
        } else {
            // Default initialization based on type
            String defaultValue = "0";  // Default for Int
            if (attr.getType().equals("Bool")) {
                defaultValue = "false";
            } else if (attr.getType().equals("String")) {
                defaultValue = "\"\"";
            }
            code.add(attr.getName() + " = " + defaultValue);
        }
    }

    private void generateMethodIR(MethodNode method) {
        code.add("\n# Method " + method.getName() + " : " + method.getType());

        // Method label
        String methodLabel = "method_" + method.getName();
        code.add(methodLabel + ":");

        // Parameters
        for (FormalNode param : method.getParameters()) {
            code.add("# Param " + param.getName() + " : " + param.getType());
        }

        // Method body
        String resultTemp = null;
        for (ExpressionNode expr : method.getBody()) {
            resultTemp = generateExpressionIR(expr);
        }

        // Return statement (use the result of the last expression)
        if (resultTemp != null) {
            code.add("return " + resultTemp);
        }
    }

    private String generateExpressionIR(ExpressionNode expr) {
        if (expr instanceof IntegerLiteralNode) {
            return String.valueOf(((IntegerLiteralNode) expr).getValue());
        } else if (expr instanceof BooleanLiteralNode) {
            return ((BooleanLiteralNode) expr).getValue() ? "true" : "false";
        } else if (expr instanceof StringLiteralNode) {
            return "\"" + ((StringLiteralNode) expr).getValue() + "\"";
        } else if (expr instanceof IdentifierNode) {
            return ((IdentifierNode) expr).getName();
        } else if (expr instanceof BinaryOperationNode) {
            return generateBinaryOpIR((BinaryOperationNode) expr);
        } else if (expr instanceof UnaryOperationNode) {
            return generateUnaryOpIR((UnaryOperationNode) expr);
        } else if (expr instanceof AssignmentNode) {
            return generateAssignmentIR((AssignmentNode) expr);
        } else if (expr instanceof MethodCallNode) {
            return generateMethodCallIR((MethodCallNode) expr);
        } else if (expr instanceof IfNode) {
            return generateIfIR((IfNode) expr);
        } else if (expr instanceof WhileNode) {
            return generateWhileIR((WhileNode) expr);
        }

        // Fallback
        String temp = newTemp();
        code.add(temp + " = null # Unhandled expression type: " + expr.getClass().getName());
        return temp;
    }

    private String generateBinaryOpIR(BinaryOperationNode node) {
        // Generate code for left and right operands
        String leftTemp = generateExpressionIR(node.getLeft());
        String rightTemp = generateExpressionIR(node.getRight());

        // Create result temporary
        String resultTemp = newTemp();

        // Generate the operation
        String op = "";
        switch (node.getOperator()) {
            case PLUS: op = "+"; break;
            case MINUS: op = "-"; break;
            case MULTIPLY: op = "*"; break;
            case DIVIDE: op = "/"; break;
            case MOD: op = "%"; break;
            case LT: op = "<"; break;
            case LE: op = "<="; break;
            case EQ: op = "=="; break;
            case NE: op = "!="; break;
            case GE: op = ">="; break;
            case GT: op = ">"; break;
            case AND: op = "&&"; break;
            case OR: op = "||"; break;
            default: op = "?"; // Unknown operator
        }

        code.add(resultTemp + " = " + leftTemp + " " + op + " " + rightTemp);
        return resultTemp;
    }

    private String generateUnaryOpIR(UnaryOperationNode node) {
        // Generate code for the operand
        String operandTemp = generateExpressionIR(node.getOperand());

        // Create result temporary
        String resultTemp = newTemp();

        // Generate the operation
        switch (node.getOperator()) {
            case NOT:
                code.add(resultTemp + " = !" + operandTemp);
                break;
            case NEGATIVE:
                code.add(resultTemp + " = -" + operandTemp);
                break;
            default:
                code.add(resultTemp + " = ?" + operandTemp); // Unknown operator
        }

        return resultTemp;
    }

    private String generateAssignmentIR(AssignmentNode node) {
        // Generate code for the value
        String valueTemp = generateExpressionIR(node.getValue());

        // For simple assignment
        if (node.getType() == AssignmentNode.AssignmentType.SIMPLE) {
            code.add(node.getVariable() + " = " + valueTemp);
            return valueTemp;
        }

        // For compound assignments
        String operator = "";
        switch (node.getType()) {
            case PLUS_ASSIGN: operator = "+"; break;
            case MINUS_ASSIGN: operator = "-"; break;
            case MULT_ASSIGN: operator = "*"; break;
            case DIV_ASSIGN: operator = "/"; break;
            default: operator = "?"; // Unknown operator
        }

        // Generate the compound assignment
        String resultTemp = newTemp();
        code.add(resultTemp + " = " + node.getVariable() + " " + operator + " " + valueTemp);
        code.add(node.getVariable() + " = " + resultTemp);

        return resultTemp;
    }

    private String generateMethodCallIR(MethodCallNode node) {
        // Generate code for object (if any)
        String objectTemp = node.getObject() != null ?
                generateExpressionIR(node.getObject()) : "this";

        // Generate code for arguments
        List<String> argTemps = new ArrayList<>();
        for (ExpressionNode arg : node.getArguments()) {
            argTemps.add(generateExpressionIR(arg));
        }

        // Create call statement
        String resultTemp = newTemp();
        StringBuilder callBuilder = new StringBuilder();
        callBuilder.append(resultTemp).append(" = ")
                .append(objectTemp).append(".").append(node.getMethodName()).append("(");

        for (int i = 0; i < argTemps.size(); i++) {
            if (i > 0) callBuilder.append(", ");
            callBuilder.append(argTemps.get(i));
        }

        callBuilder.append(")");
        code.add(callBuilder.toString());

        return resultTemp;
    }

    private String generateIfIR(IfNode node) {
        // Generate labels
        String thenLabel = newLabel("then");
        String elseLabel = newLabel("else");
        String endLabel = newLabel("endif");

        // Generate condition code
        String condTemp = generateExpressionIR(node.getCondition());

        // Generate branch instruction
        code.add("if " + condTemp + " goto " + thenLabel);
        code.add("goto " + elseLabel);

        // Then branch
        code.add(thenLabel + ":");
        String thenTemp = generateExpressionIR(node.getThenExpr());
        String resultTemp = newTemp();
        code.add(resultTemp + " = " + thenTemp);
        code.add("goto " + endLabel);

        // Else branch
        code.add(elseLabel + ":");
        String elseTemp = generateExpressionIR(node.getElseExpr());
        code.add(resultTemp + " = " + elseTemp);

        // End if
        code.add(endLabel + ":");

        return resultTemp;
    }

    private String generateWhileIR(WhileNode node) {
        // Generate labels
        String startLabel = newLabel("while");
        String bodyLabel = newLabel("loop");
        String endLabel = newLabel("endwhile");

        // Loop header
        code.add(startLabel + ":");

        // Generate condition code
        String condTemp = generateExpressionIR(node.getCondition());

        // Generate branch instruction
        code.add("if " + condTemp + " goto " + bodyLabel);
        code.add("goto " + endLabel);

        // Loop body
        code.add(bodyLabel + ":");
        generateExpressionIR(node.getBody());
        code.add("goto " + startLabel);

        // End while
        code.add(endLabel + ":");

        // While loops return void in COOL, but we need to return something
        String resultTemp = newTemp();
        code.add(resultTemp + " = void");
        return resultTemp;
    }

    // Helper methods
    private String newTemp() {
        return "t" + (tempCounter++);
    }

    private String newLabel(String prefix) {
        return prefix + "_" + (labelCounter++);
    }

    /**
     * Get the generated IR code as a string
     */
    public String getIRCode() {
        StringBuilder sb = new StringBuilder();
        for (String line : code) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }
}