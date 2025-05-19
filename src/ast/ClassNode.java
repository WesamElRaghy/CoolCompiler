package ast;

import java.util.ArrayList;
import java.util.List;

public class ClassNode extends ASTNode {
    private String name;
    private String parentName;
    private List<FeatureNode> features;

    public ClassNode(int line, int column, String name, String parentName) {
        super(line, column);
        this.name = name;
        this.parentName = parentName;
        this.features = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public String getParentName() {
        return parentName;
    }

    public List<FeatureNode> getFeatures() {
        return features;
    }

    public void addFeature(FeatureNode feature) {
        features.add(feature);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Class " + name);
        if (parentName != null) {
            sb.append(" inherits ").append(parentName);
        }
        sb.append(" {\n");

        for (FeatureNode feature : features) {
            sb.append("  ").append(feature).append("\n");
        }

        sb.append("}");
        return sb.toString();
    }

    @Override
    protected int generateDOT(StringBuilder sb, String nodeId, int counter) {
        int localCounter = counter;

        for (FeatureNode feature : features) {
            localCounter++;
            String childId = "node" + localCounter;
            sb.append("  ").append(childId).append(" [label=\"")
                    .append(escapeDotString(feature.toString())).append("\"];\n");
            sb.append("  ").append(nodeId).append(" -> ").append(childId).append(";\n");

            localCounter = feature.generateDOT(sb, childId, localCounter);
        }

        return localCounter;
    }

    private String escapeDotString(String s) {
        return s.replace("\"", "\\\"").replace("\n", "\\n");
    }
}