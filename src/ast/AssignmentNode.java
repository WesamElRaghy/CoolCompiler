package ast;

public class AssignmentNode extends ExpressionNode {
    private String variable;
    private ExpressionNode value;
    private AssignmentType type;

    public enum AssignmentType {
        SIMPLE("<-"), PLUS_ASSIGN("+="), MINUS_ASSIGN("-="),
        MULT_ASSIGN("*="), DIV_ASSIGN("/=");

        private final String symbol;

        AssignmentType(String symbol) {
            this.symbol = symbol;
        }

        @Override
        public String toString() {
            return symbol;
        }
    }

    public AssignmentNode(int line, int column, String variable,
                          ExpressionNode value, AssignmentType type) {
        super(line, column);
        this.variable = variable;
        this.value = value;
        this.type = type;
    }

    public String getVariable() {
        return variable;
    }

    public ExpressionNode getValue() {
        return value;
    }

    public AssignmentType getType() {
        return type;
    }

    @Override
    public String toString() {
        return variable + " " + type + " " + value;
    }

    @Override
    protected int generateDOT(StringBuilder sb, String nodeId, int counter) {
        sb.append("  ").append(nodeId).append(" [label=\"").append(type).append("\"];\n");

        // Variable identifier
        counter++;
        String varId = "node" + counter;
        sb.append("  ").append(varId).append(" [label=\"").append(variable).append("\"];\n");
        sb.append("  ").append(nodeId).append(" -> ").append(varId).append(";\n");

        // Value expression
        counter++;
        String valueId = "node" + counter;
        sb.append("  ").append(valueId).append(" [label=\"")
                .append(escapeDotString(value.toString())).append("\"];\n");
        sb.append("  ").append(nodeId).append(" -> ").append(valueId).append(";\n");

        counter = value.generateDOT(sb, valueId, counter);

        return counter;
    }

    private String escapeDotString(String s) {
        return s.replace("\"", "\\\"").replace("\n", "\\n");
    }
}