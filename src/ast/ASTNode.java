package ast;

import java.io.FileWriter;
import java.io.IOException;

public abstract class ASTNode {
    private int line;
    private int column;

    public ASTNode(int line, int column) {
        this.line = line;
        this.column = column;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    // For visualization
    public abstract String toString();

    // Generate DOT representation for visualization
    public String toDOT() {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph AST {\n");
        sb.append("  node [shape=box];\n");
        generateDOT(sb, "node0", 0);
        sb.append("}\n");
        return sb.toString();
    }

    // Write DOT file
    public void generateDotFile(String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(toDOT());
        } catch (IOException e) {
            System.err.println("Error writing DOT file: " + e.getMessage());
        }
    }

    // Helper method for DOT generation - each node type will implement this
    protected abstract int generateDOT(StringBuilder sb, String nodeId, int counter);
}