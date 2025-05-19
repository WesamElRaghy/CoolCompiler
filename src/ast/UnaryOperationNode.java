package ast;

public class UnaryOperationNode extends ExpressionNode {
    public enum Operator {
        NOT("!"), NEGATIVE("-");

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
    private ExpressionNode operand;

    public UnaryOperationNode(int line, int column, Operator operator, ExpressionNode operand) {
        super(line, column);
        this.operator = operator;
        this.operand = operand;
    }

    public Operator getOperator() {
        return operator;
    }

    public ExpressionNode getOperand() {
        return operand;
    }

    @Override
    public String toString() {
        return operator + "(" + operand + ")";
    }

    @Override
    protected int generateDOT(StringBuilder sb, String nodeId, int counter) {
        sb.append("  ").append(nodeId).append(" [label=\"").append(operator).append("\"];\n");

        counter++;
        String operandId = "node" + counter;
        sb.append("  ").append(operandId).append(" [label=\"")
                .append(escapeDotString(operand.toString())).append("\"];\n");
        sb.append("  ").append(nodeId).append(" -> ").append(operandId).append(";\n");

        counter = operand.generateDOT(sb, operandId, counter);

        return counter;
    }

    private String escapeDotString(String s) {
        return s.replace("\"", "\\\"").replace("\n", "\\n");
    }
}