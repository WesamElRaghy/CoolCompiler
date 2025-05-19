package ast;

public class BinaryOperationNode extends ExpressionNode {
    public enum Operator {
        PLUS("+"), MINUS("-"), MULTIPLY("*"), DIVIDE("/"), MOD("%"),
        LT("<"), LE("<="), EQ("="), GE(">="), GT(">"), NE("!="),
        AND("&&"), OR("||");

        private final String symbol;

        Operator(String symbol) {
            this.symbol = symbol;
        }

        @Override
        public String toString() {
            return symbol;
        }
    }

    private Operator operator;
    private ExpressionNode left;
    private ExpressionNode right;

    public BinaryOperationNode(int line, int column, Operator operator,
                               ExpressionNode left, ExpressionNode right) {
        super(line, column);
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    public Operator getOperator() {
        return operator;
    }

    public ExpressionNode getLeft() {
        return left;
    }

    public ExpressionNode getRight() {
        return right;
    }

    @Override
    public String toString() {
        return "(" + left + " " + operator + " " + right + ")";
    }

    @Override
    protected int generateDOT(StringBuilder sb, String nodeId, int counter) {
        sb.append("  ").append(nodeId).append(" [label=\"").append(operator).append("\"];\n");

        // Left operand
        counter++;
        String leftId = "node" + counter;
        sb.append("  ").append(leftId).append(" [label=\"")
                .append(escapeDotString(left.toString())).append("\"];\n");
        sb.append("  ").append(nodeId).append(" -> ").append(leftId).append(";\n");
        counter = left.generateDOT(sb, leftId, counter);

        // Right operand
        counter++;
        String rightId = "node" + counter;
        sb.append("  ").append(rightId).append(" [label=\"")
                .append(escapeDotString(right.toString())).append("\"];\n");
        sb.append("  ").append(nodeId).append(" -> ").append(rightId).append(";\n");
        counter = right.generateDOT(sb, rightId, counter);

        return counter;
    }

    private String escapeDotString(String s) {
        return s.replace("\"", "\\\"").replace("\n", "\\n");
    }
}