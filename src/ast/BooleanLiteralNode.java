package ast;

public class BooleanLiteralNode extends ExpressionNode {
    private boolean value;

    public BooleanLiteralNode(int line, int column, boolean value) {
        super(line, column);
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value ? "true" : "false";
    }

    @Override
    protected int generateDOT(StringBuilder sb, String nodeId, int counter) {
        // Boolean literals are leaf nodes - no children
        return counter;
    }
}