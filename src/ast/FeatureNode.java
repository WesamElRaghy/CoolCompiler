package ast;

public abstract class FeatureNode extends ASTNode {
    private String name;
    private String type;

    public FeatureNode(int line, int column, String name, String type) {
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
}