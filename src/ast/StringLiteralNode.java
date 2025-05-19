package ast;

public class StringLiteralNode extends ExpressionNode {
    private String value;

    public StringLiteralNode(int line, int column, String value) {
        super(line, column);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "\"" + value + "\"";
    }

    @Override
    protected int generateDOT(StringBuilder sb, String nodeId, int counter) {
        // String literals are leaf nodes - no children
        return counter;
    }
}