package ast;

public class IfNode extends ExpressionNode {
    private ExpressionNode condition;
    private ExpressionNode thenExpr;
    private ExpressionNode elseExpr;

    public IfNode(int line, int column, ExpressionNode condition,
                  ExpressionNode thenExpr, ExpressionNode elseExpr) {
        super(line, column);
        this.condition = condition;
        this.thenExpr = thenExpr;
        this.elseExpr = elseExpr;
    }

    public ExpressionNode getCondition() {
        return condition;
    }

    public ExpressionNode getThenExpr() {
        return thenExpr;
    }

    public ExpressionNode getElseExpr() {
        return elseExpr;
    }

    @Override
    public String toString() {
        return "if " + condition + " then " + thenExpr + " else " + elseExpr + " fi";
    }

    @Override
    protected int generateDOT(StringBuilder sb, String nodeId, int counter) {
        sb.append("  ").append(nodeId).append(" [label=\"If\"];\n");

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

        // Then branch
        counter++;
        String thenId = "node" + counter;
        sb.append("  ").append(thenId).append(" [label=\"Then\"];\n");
        sb.append("  ").append(nodeId).append(" -> ").append(thenId).append(";\n");

        counter++;
        String thenExprId = "node" + counter;
        sb.append("  ").append(thenExprId).append(" [label=\"")
                .append(escapeDotString(thenExpr.toString())).append("\"];\n");
        sb.append("  ").append(thenId).append(" -> ").append(thenExprId).append(";\n");
        counter = thenExpr.generateDOT(sb, thenExprId, counter);

        // Else branch
        counter++;
        String elseId = "node" + counter;
        sb.append("  ").append(elseId).append(" [label=\"Else\"];\n");
        sb.append("  ").append(nodeId).append(" -> ").append(elseId).append(";\n");

        counter++;
        String elseExprId = "node" + counter;
        sb.append("  ").append(elseExprId).append(" [label=\"")
                .append(escapeDotString(elseExpr.toString())).append("\"];\n");
        sb.append("  ").append(elseId).append(" -> ").append(elseExprId).append(";\n");
        counter = elseExpr.generateDOT(sb, elseExprId, counter);

        return counter;
    }

    private String escapeDotString(String s) {
        return s.replace("\"", "\\\"").replace("\n", "\\n");
    }
}