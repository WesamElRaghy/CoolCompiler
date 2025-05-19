package ast;

import java.util.ArrayList;
import java.util.List;

public class MethodNode extends FeatureNode {
    private List<FormalNode> parameters;
    private List<ExpressionNode> body;

    public MethodNode(int line, int column, String name, String returnType) {
        super(line, column, name, returnType);
        this.parameters = new ArrayList<>();
        this.body = new ArrayList<>();
    }

    public void addParameter(FormalNode param) {
        parameters.add(param);
    }

    public void addBodyExpression(ExpressionNode expr) {
        body.add(expr);
    }

    public List<FormalNode> getParameters() {
        return parameters;
    }

    public List<ExpressionNode> getBody() {
        return body;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getName() + "(");

        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(parameters.get(i));
        }

        sb.append(") : ").append(getType()).append(" {\n");

        for (ExpressionNode expr : body) {
            sb.append("    ").append(expr).append(";\n");
        }

        sb.append("  };");
        return sb.toString();
    }

    @Override
    protected int generateDOT(StringBuilder sb, String nodeId, int counter) {
        int localCounter = counter;

        // Parameters
        if (!parameters.isEmpty()) {
            localCounter++;
            String paramsId = "node" + localCounter;
            sb.append("  ").append(paramsId).append(" [label=\"Parameters\"];\n");
            sb.append("  ").append(nodeId).append(" -> ").append(paramsId).append(";\n");

            for (FormalNode param : parameters) {
                localCounter++;
                String paramId = "node" + localCounter;
                sb.append("  ").append(paramId).append(" [label=\"")
                        .append(escapeDotString(param.toString())).append("\"];\n");
                sb.append("  ").append(paramsId).append(" -> ").append(paramId).append(";\n");
            }
        }

        // Body
        if (!body.isEmpty()) {
            localCounter++;
            String bodyId = "node" + localCounter;
            sb.append("  ").append(bodyId).append(" [label=\"Body\"];\n");
            sb.append("  ").append(nodeId).append(" -> ").append(bodyId).append(";\n");

            for (ExpressionNode expr : body) {
                localCounter++;
                String exprId = "node" + localCounter;
                sb.append("  ").append(exprId).append(" [label=\"")
                        .append(escapeDotString(expr.toString())).append("\"];\n");
                sb.append("  ").append(bodyId).append(" -> ").append(exprId).append(";\n");

                localCounter = expr.generateDOT(sb, exprId, localCounter);
            }
        }

        return localCounter;
    }

    private String escapeDotString(String s) {
        return s.replace("\"", "\\\"").replace("\n", "\\n");
    }
}