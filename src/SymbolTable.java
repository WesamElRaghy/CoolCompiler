import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private Map<String, String> variables;

    public SymbolTable() {
        variables = new HashMap<>();
    }

    public void addVariable(String name, String type) {
        variables.put(name, type);
    }

    public String getType(String name) {
        return variables.get(name);
    }

    public void printTable() {
        System.out.println("Symbol Table:");
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            System.out.printf("%s : %s%n", entry.getKey(), entry.getValue());
        }
    }
}