package ast;

public class FormalNode extends ASTNode {
    private String name;
    private String type;

    public FormalNode(int line, int column, String name, String type) {
        super(line, column);
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return name + " : " + type;
    }

    @Override
    protected int generateDOT(StringBuilder sb, String nodeId, int counter) {
        // Formal nodes are leaves in the AST, no need to add children
        return counter;
    }
}