package ast;

import java.util.ArrayList;
import java.util.List;

public class MethodCallNode extends ExpressionNode {
    private ExpressionNode object; // null if calling on self
    private String methodName;
    private List<ExpressionNode> arguments;

    public MethodCallNode(int line, int column, ExpressionNode object, String methodName) {
        super(line, column);
        this.object = object;
        this.methodName = methodName;
        this.arguments = new ArrayList<>();
    }

    public ExpressionNode getObject() {
        return object;
    }

    public String getMethodName() {
        return methodName;
    }

    public List<ExpressionNode> getArguments() {
        return arguments;
    }

    public void addArgument(ExpressionNode arg) {
        arguments.add(arg);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (object != null) {
            sb.append(object).append(".");
        }

        sb.append(methodName).append("(");

        for (int i = 0; i < arguments.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(arguments.get(i));
        }

        sb.append(")");
        return sb.toString();
    }

    @Override
    protected int generateDOT(StringBuilder sb, String nodeId, int counter) {
        sb.append("  ").append(nodeId).append(" [label=\"Method: ").append(methodName).append("\"];\n");

        // Object (if any)
        if (object != null) {
            counter++;
            String objId = "node" + counter;
            sb.append("  ").append(objId).append(" [label=\"Object\"];\n");
            sb.append("  ").append(nodeId).append(" -> ").append(objId).append(";\n");

            counter++;
            String objExprId = "node" + counter;
            sb.append("  ").append(objExprId).append(" [label=\"")
                    .append(escapeDotString(object.toString())).append("\"];\n");
            sb.append("  ").append(objId).append(" -> ").append(objExprId).append(";\n");

            counter = object.generateDOT(sb, objExprId, counter);
        }

        // Arguments
        if (!arguments.isEmpty()) {
            counter++;
            String argsId = "node" + counter;
            sb.append("  ").append(argsId).append(" [label=\"Arguments\"];\n");
            sb.append("  ").append(nodeId).append(" -> ").append(argsId).append(";\n");

            for (ExpressionNode arg : arguments) {
                counter++;
                String argId = "node" + counter;
                sb.append("  ").append(argId).append(" [label=\"")
                        .append(escapeDotString(arg.toString())).append("\"];\n");
                sb.append("  ").append(argsId).append(" -> ").append(argId).append(";\n");

                counter = arg.generateDOT(sb, argId, counter);
            }
        }

        return counter;
    }

    private String escapeDotString(String s) {
        return s.replace("\"", "\\\"").replace("\n", "\\n");
    }
}