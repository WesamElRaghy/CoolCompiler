import ast.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SemanticTester {
    private ProgramNode ast;
    private SemanticAnalyzer analyzer;

    public SemanticTester(ProgramNode ast) {
        this.ast = ast;
        this.analyzer = new SemanticAnalyzer();
    }

    public boolean analyze() {
        analyzer.analyze(ast);

        // Print semantic errors if any
        if (analyzer.hasErrors()) {
            analyzer.printErrors();
            return false;
        }

        System.out.println("Semantic analysis completed successfully.");
        analyzer.getSymbolTable().printSymbolTable();
        return true;
    }

    // Generate a DOT file with type annotations
    public void generateTypedAST(String filename) {
        try {
            StringBuilder dotBuilder = new StringBuilder();
            dotBuilder.append("digraph TypedAST {\n");
            dotBuilder.append("  node [shape=box];\n");

            // Build a more detailed AST representation with type information
            buildTypedASTDot(ast, dotBuilder, "node0", 0);

            dotBuilder.append("}\n");

            Files.write(Paths.get(filename), dotBuilder.toString().getBytes());
            System.out.println("Typed AST saved to " + filename);
        } catch (IOException e) {
            System.err.println("Error generating typed AST: " + e.getMessage());
        }
    }

    private int buildTypedASTDot(ASTNode node, StringBuilder sb, String nodeId, int counter) {
        if (node == null) return counter;

        // Add node label with type information if available
        String label = node.toString();
        if (node instanceof ExpressionNode) {
            ExpressionNode expr = (ExpressionNode) node;
            if (expr.getExpressionType() != null) {
                label += " : " + expr.getExpressionType();
            }
        }

        sb.append("  ").append(nodeId).append(" [label=\"")
                .append(escapeDotString(label)).append("\"];\n");

        // Handle specific node types
        if (node instanceof ProgramNode) {
            ProgramNode program = (ProgramNode) node;
            for (int i = 0; i < program.getClasses().size(); i++) {
                ClassNode classNode = program.getClasses().get(i);
                counter++;
                String childId = "node" + counter;
                sb.append("  ").append(nodeId).append(" -> ").append(childId).append(";\n");
                counter = buildTypedASTDot(classNode, sb, childId, counter);
            }
        } else if (node instanceof ClassNode) {
            ClassNode classNode = (ClassNode) node;
            for (int i = 0; i < classNode.getFeatures().size(); i++) {
                FeatureNode featureNode = classNode.getFeatures().get(i);
                counter++;
                String childId = "node" + counter;
                sb.append("  ").append(nodeId).append(" -> ").append(childId).append(";\n");
                counter = buildTypedASTDot(featureNode, sb, childId, counter);
            }
        } else if (node instanceof MethodNode) {
            MethodNode methodNode = (MethodNode) node;

            // Add parameters
            if (!methodNode.getParameters().isEmpty()) {
                counter++;
                String paramsId = "node" + counter;
                sb.append("  ").append(paramsId).append(" [label=\"Parameters\"];\n");
                sb.append("  ").append(nodeId).append(" -> ").append(paramsId).append(";\n");

                for (FormalNode param : methodNode.getParameters()) {
                    counter++;
                    String paramId = "node" + counter;
                    sb.append("  ").append(paramId).append(" [label=\"")
                            .append(escapeDotString(param.toString())).append("\"];\n");
                    sb.append("  ").append(paramsId).append(" -> ").append(paramId).append(";\n");
                }
            }

            // Add body
            if (!methodNode.getBody().isEmpty()) {
                counter++;
                String bodyId = "node" + counter;
                sb.append("  ").append(bodyId).append(" [label=\"Body\"];\n");
                sb.append("  ").append(nodeId).append(" -> ").append(bodyId).append(";\n");

                for (ExpressionNode expr : methodNode.getBody()) {
                    counter++;
                    String exprId = "node" + counter;
                    sb.append("  ").append(bodyId).append(" -> ").append(exprId).append(";\n");
                    counter = buildTypedASTDot(expr, sb, exprId, counter);
                }
            }
        } else if (node instanceof AttributeNode) {
            AttributeNode attrNode = (AttributeNode) node;
            if (attrNode.getInitExpr() != null) {
                counter++;
                String initId = "node" + counter;
                sb.append("  ").append(initId).append(" [label=\"Init\"];\n");
                sb.append("  ").append(nodeId).append(" -> ").append(initId).append(";\n");

                counter++;
                String exprId = "node" + counter;
                sb.append("  ").append(initId).append(" -> ").append(exprId).append(";\n");
                counter = buildTypedASTDot(attrNode.getInitExpr(), sb, exprId, counter);
            }
        } else if (node instanceof BinaryOperationNode) {
            BinaryOperationNode binOp = (BinaryOperationNode) node;

            // Left operand
            counter++;
            String leftId = "node" + counter;
            sb.append("  ").append(nodeId).append(" -> ").append(leftId).append(";\n");
            counter = buildTypedASTDot(binOp.getLeft(), sb, leftId, counter);

            // Right operand
            counter++;
            String rightId = "node" + counter;
            sb.append("  ").append(nodeId).append(" -> ").append(rightId).append(";\n");
            counter = buildTypedASTDot(binOp.getRight(), sb, rightId, counter);
        } else if (node instanceof UnaryOperationNode) {
            UnaryOperationNode unaryOp = (UnaryOperationNode) node;

            counter++;
            String operandId = "node" + counter;
            sb.append("  ").append(nodeId).append(" -> ").append(operandId).append(";\n");
            counter = buildTypedASTDot(unaryOp.getOperand(), sb, operandId, counter);
        } else if (node instanceof UnaryOperationNode) {
            UnaryOperationNode unaryOp = (UnaryOperationNode) node;

            counter++;
            String operandId = "node" + counter;
            sb.append("  ").append(nodeId).append(" -> ").append(operandId).append(";\n");
            counter = buildTypedASTDot(unaryOp.getOperand(), sb, operandId, counter);
        } else if (node instanceof AssignmentNode) {
            AssignmentNode assignNode = (AssignmentNode) node;

            // Variable name
            counter++;
            String varId = "node" + counter;
            sb.append("  ").append(varId).append(" [label=\"")
                    .append(escapeDotString(assignNode.getVariable())).append("\"];\n");
            sb.append("  ").append(nodeId).append(" -> ").append(varId).append(";\n");

            // Value expression
            counter++;
            String valueId = "node" + counter;
            sb.append("  ").append(nodeId).append(" -> ").append(valueId).append(";\n");
            counter = buildTypedASTDot(assignNode.getValue(), sb, valueId, counter);
        } else if (node instanceof MethodCallNode) {
            MethodCallNode callNode = (MethodCallNode) node;

            // Object expression (if any)
            if (callNode.getObject() != null) {
                counter++;
                String objId = "node" + counter;
                sb.append("  ").append(objId).append(" [label=\"Object\"];\n");
                sb.append("  ").append(nodeId).append(" -> ").append(objId).append(";\n");

                counter++;
                String objExprId = "node" + counter;
                sb.append("  ").append(objId).append(" -> ").append(objExprId).append(";\n");
                counter = buildTypedASTDot(callNode.getObject(), sb, objExprId, counter);
            }

            // Arguments
            if (!callNode.getArguments().isEmpty()) {
                counter++;
                String argsId = "node" + counter;
                sb.append("  ").append(argsId).append(" [label=\"Arguments\"];\n");
                sb.append("  ").append(nodeId).append(" -> ").append(argsId).append(";\n");

                for (ExpressionNode arg : callNode.getArguments()) {
                    counter++;
                    String argId = "node" + counter;
                    sb.append("  ").append(argsId).append(" -> ").append(argId).append(";\n");
                    counter = buildTypedASTDot(arg, sb, argId, counter);
                }
            }
        } else if (node instanceof IfNode) {
            IfNode ifNode = (IfNode) node;

            // Condition
            counter++;
            String condId = "node" + counter;
            sb.append("  ").append(condId).append(" [label=\"Condition\"];\n");
            sb.append("  ").append(nodeId).append(" -> ").append(condId).append(";\n");

            counter++;
            String condExprId = "node" + counter;
            sb.append("  ").append(condId).append(" -> ").append(condExprId).append(";\n");
            counter = buildTypedASTDot(ifNode.getCondition(), sb, condExprId, counter);

            // Then branch
            counter++;
            String thenId = "node" + counter;
            sb.append("  ").append(thenId).append(" [label=\"Then\"];\n");
            sb.append("  ").append(nodeId).append(" -> ").append(thenId).append(";\n");

            counter++;
            String thenExprId = "node" + counter;
            sb.append("  ").append(thenId).append(" -> ").append(thenExprId).append(";\n");
            counter = buildTypedASTDot(ifNode.getThenExpr(), sb, thenExprId, counter);

            // Else branch
            counter++;
            String elseId = "node" + counter;
            sb.append("  ").append(elseId).append(" [label=\"Else\"];\n");
            sb.append("  ").append(nodeId).append(" -> ").append(elseId).append(";\n");

            counter++;
            String elseExprId = "node" + counter;
            sb.append("  ").append(elseId).append(" -> ").append(elseExprId).append(";\n");
            counter = buildTypedASTDot(ifNode.getElseExpr(), sb, elseExprId, counter);
        } else if (node instanceof WhileNode) {
            WhileNode whileNode = (WhileNode) node;

            // Condition
            counter++;
            String condId = "node" + counter;
            sb.append("  ").append(condId).append(" [label=\"Condition\"];\n");
            sb.append("  ").append(nodeId).append(" -> ").append(condId).append(";\n");

            counter++;
            String condExprId = "node" + counter;
            sb.append("  ").append(condId).append(" -> ").append(condExprId).append(";\n");
            counter = buildTypedASTDot(whileNode.getCondition(), sb, condExprId, counter);

            // Body
            counter++;
            String bodyId = "node" + counter;
            sb.append("  ").append(bodyId).append(" [label=\"Body\"];\n");
            sb.append("  ").append(nodeId).append(" -> ").append(bodyId).append(";\n");

            counter++;
            String bodyExprId = "node" + counter;
            sb.append("  ").append(bodyId).append(" -> ").append(bodyExprId).append(";\n");
            counter = buildTypedASTDot(whileNode.getBody(), sb, bodyExprId, counter);
        }

        return counter;
    }

    private String escapeDotString(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"").replace("\n", "\\n");
    }
}