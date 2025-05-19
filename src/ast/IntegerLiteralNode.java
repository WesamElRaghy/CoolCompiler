package ast;

public class IntegerLiteralNode extends ExpressionNode {
    private int value;

    public IntegerLiteralNode(int line, int column, int value) {
        super(line, column);
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    protected int generateDOT(StringBuilder sb, String nodeId, int counter) {
        // Integer literals are leaf nodes - no children
        return counter;
    }
}