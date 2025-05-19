package ast;

import java.util.ArrayList;
import java.util.List;

public class ProgramNode extends ASTNode {
    private List<ClassNode> classes;

    public ProgramNode(int line, int column) {
        super(line, column);
        this.classes = new ArrayList<>();
    }

    public void addClass(ClassNode classNode) {
        classes.add(classNode);
    }

    public List<ClassNode> getClasses() {
        return classes;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Program:\n");
        for (ClassNode classNode : classes) {
            sb.append(classNode).append("\n");
        }
        return sb.toString();
    }

    @Override
    protected int generateDOT(StringBuilder sb, String nodeId, int counter) {
        sb.append("  ").append(nodeId).append(" [label=\"Program\"];\n");

        int localCounter = counter;
        for (ClassNode classNode : classes) {
            localCounter++;
            String childId = "node" + localCounter;
            sb.append("  ").append(childId).append(" [label=\"Class: ")
                    .append(classNode.getName());

            if (classNode.getParentName() != null) {
                sb.append(" inherits ").append(classNode.getParentName());
            }

            sb.append("\"];\n");
            sb.append("  ").append(nodeId).append(" -> ").append(childId).append(";\n");

            localCounter = classNode.generateDOT(sb, childId, localCounter);
        }

        return localCounter;
    }
}