package ast;

public class IdentifierNode extends ExpressionNode {
    private String name;

    public IdentifierNode(int line, int column, String name) {
        super(line, column);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    protected int generateDOT(StringBuilder sb, String nodeId, int counter) {
        // Identifiers are leaf nodes - no children
        return counter;
    }
}