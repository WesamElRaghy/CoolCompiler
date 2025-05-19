package ast;

public class WhileNode extends ExpressionNode {
    private ExpressionNode condition;
    private ExpressionNode body;

    public WhileNode(int line, int column, ExpressionNode condition, ExpressionNode body) {
        super(line, column);
        this.condition = condition;
        this.body = body;
    }

    public ExpressionNode getCondition() {
        return condition;
    }

    public ExpressionNode getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "while " + condition + " loop " + body + " pool";
    }

    @Override
    protected int generateDOT(StringBuilder sb, String nodeId, int counter) {
        sb.append("  ").append(nodeId).append(" [label=\"While\"];\n");

        // Condition
        counter++;
        String condId = "node" + counter;
        sb.append("  ").append(condId).append(" [label=\"Condition\"];\n");
        sb.append("  ").append(nodeId).append(" -> ").append(condId).append(";\n");

        counter++;
        String condExprId = "node" + counter;
        sb.append("  ").append(condExprId).append(" [label=\"")
                .append(escapeDotString(condition.toString())).append("\"];\n");
        sb.append("  ").append(condId).append(" -> ").append(condExprId).append(";\n");
        counter = condition.generateDOT(sb, condExprId, counter);

        // Body
        counter++;
        String bodyId = "node" + counter;
        sb.append("  ").append(bodyId).append(" [label=\"Body\"];\n");
        sb.append("  ").append(nodeId).append(" -> ").append(bodyId).append(";\n");

        counter++;
        String bodyExprId = "node" + counter;
        sb.append("  ").append(bodyExprId).append(" [label=\"")
                .append(escapeDotString(body.toString())).append("\"];\n");
        sb.append("  ").append(bodyId).append(" -> ").append(bodyExprId).append(";\n");
        counter = body.generateDOT(sb, bodyExprId, counter);

        return counter;
    }

    private String escapeDotString(String s) {
        return s.replace("\"", "\\\"").replace("\n", "\\n");
    }
}