package ast;

public abstract class ExpressionNode extends ASTNode {
    // This will be set during semantic analysis
    private String expressionType;

    public ExpressionNode(int line, int column) {
        super(line, column);
    }

    public String getExpressionType() {
        return expressionType;
    }

    public void setExpressionType(String type) {
        this.expressionType = type;
    }
}