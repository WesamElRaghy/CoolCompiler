package ast;

public class AttributeNode extends FeatureNode {
    private ExpressionNode initExpr;

    public AttributeNode(int line, int column, String name, String type, ExpressionNode initExpr) {
        super(line, column, name, type);
        this.initExpr = initExpr;
    }

    public ExpressionNode getInitExpr() {
        return initExpr;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getName() + " : " + getType());
        if (initExpr != null) {
            sb.append(" <- ").append(initExpr);
        }
        sb.append(";");
        return sb.toString();
    }

    @Override
    protected int generateDOT(StringBuilder sb, String nodeId, int counter) {
        if (initExpr != null) {
            counter++;
            String childId = "node" + counter;
            sb.append("  ").append(childId).append(" [label=\"Init\"];\n");
            sb.append("  ").append(nodeId).append(" -> ").append(childId).append(";\n");

            counter++;
            String exprId = "node" + counter;
            sb.append("  ").append(exprId).append(" [label=\"")
                    .append(escapeDotString(initExpr.toString())).append("\"];\n");
            sb.append("  ").append(childId).append(" -> ").append(exprId).append(";\n");

            counter = initExpr.generateDOT(sb, exprId, counter);
        }
        return counter;
    }

    private String escapeDotString(String s) {
        return s.replace("\"", "\\\"").replace("\n", "\\n");
    }
}