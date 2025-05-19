import java.util.*;

/**
 * Performs optimizations on the Three-Address Code IR
 */
public class IROptimizer {
    private List<String> irCode;
    private List<String> optimizedCode;

    public IROptimizer(List<String> irCode) {
        this.irCode = irCode;
        this.optimizedCode = new ArrayList<>();
    }

    /**
     * Apply various optimization techniques to the IR code
     */
    public List<String> optimize() {
        // Start with a copy of the original IR
        optimizedCode = new ArrayList<>(irCode);

        // Apply optimization techniques
        constantFolding();
        constantPropagation();
        deadCodeElimination();
        removeUnusedVariables();

        return optimizedCode;
    }

    /**
     * Constant folding: Replace expressions with constants at compile time
     * Example: x = 5 + 3 -> x = 8
     */
    private void constantFolding() {
        List<String> result = new ArrayList<>();

        for (String line : optimizedCode) {
            // Skip comments and labels
            if (line.trim().startsWith("#") || line.trim().endsWith(":") || line.trim().isEmpty()) {
                result.add(line);
                continue;
            }

            // Look for arithmetic expressions with constants
            if (line.contains("=") && line.matches(".*=.*[+\\-*/].+")) {
                // Split into left and right sides
                String[] parts = line.split("=", 2);
                String leftSide = parts[0].trim();
                String rightSide = parts[1].trim();

                // Check if we have simple arithmetic with constants
                if (rightSide.matches("\\d+\\s*[+\\-*/]\\s*\\d+")) {
                    try {
                        // Extract the numbers and operator
                        String[] exprParts = rightSide.split("\\s*[+\\-*/]\\s*");
                        if (exprParts.length == 2) {
                            int a = Integer.parseInt(exprParts[0].trim());
                            int b = Integer.parseInt(exprParts[1].trim());
                            int result_val = 0;

                            // Perform the operation
                            if (rightSide.contains("+")) {
                                result_val = a + b;
                            } else if (rightSide.contains("-")) {
                                result_val = a - b;
                            } else if (rightSide.contains("*")) {
                                result_val = a * b;
                            } else if (rightSide.contains("/")) {
                                if (b != 0) {
                                    result_val = a / b;
                                } else {
                                    // Division by zero - leave original expression
                                    result.add(line);
                                    continue;
                                }
                            }

                            // Rewrite the line with the calculated constant
                            result.add(leftSide + " = " + result_val);
                            continue;
                        }
                    } catch (NumberFormatException e) {
                        // Not valid integers - leave original expression
                        result.add(line);
                        continue;
                    }
                }
            }

            // If not handled above, keep the original line
            result.add(line);
        }

        optimizedCode = result;
    }

    /**
     * Constant propagation: Replace variables with their constant values
     * Example: x = 5; y = x -> y = 5
     */
    private void constantPropagation() {
        Map<String, String> constants = new HashMap<>();
        List<String> result = new ArrayList<>();

        for (String line : optimizedCode) {
            // Skip comments and labels
            if (line.trim().startsWith("#") || line.trim().endsWith(":") || line.trim().isEmpty()) {
                result.add(line);
                continue;
            }

            // First, update the line with known constants
            String updatedLine = line;
            for (Map.Entry<String, String> entry : constants.entrySet()) {
                String var = entry.getKey();
                String value = entry.getValue();

                // Replace the variable with its value, but only if it's a standalone variable
                // Not within a larger identifier (e.g., don't replace 'x' in 'max')
                updatedLine = updatedLine.replaceAll("\\b" + var + "\\b(?![\\w\\.])", value);
            }

            // Check if this is a simple assignment of a constant (x = 5)
            if (updatedLine.matches(".+\\s*=\\s*\\d+\\s*$") ||
                    updatedLine.matches(".+\\s*=\\s*true\\s*$") ||
                    updatedLine.matches(".+\\s*=\\s*false\\s*$") ||
                    updatedLine.matches(".+\\s*=\\s*\".*\"\\s*$")) {

                // Extract variable and constant value
                String[] parts = updatedLine.split("=", 2);
                String var = parts[0].trim();
                String value = parts[1].trim();

                // Only track variables, not complex expressions or array elements
                if (var.matches("[a-zA-Z][a-zA-Z0-9_]*")) {
                    // Update our constant tracking
                    constants.put(var, value);
                }
            } else if (updatedLine.contains("=")) {
                // If the variable is reassigned, remove it from constants
                String[] parts = updatedLine.split("=", 2);
                String var = parts[0].trim();
                if (var.matches("[a-zA-Z][a-zA-Z0-9_]*")) {
                    constants.remove(var);
                }
            }

            // Add the updated line (with constants propagated)
            result.add(updatedLine);
        }

        optimizedCode = result;
    }

    /**
     * Dead code elimination: Remove code that doesn't affect output
     * Example: x = 5; x = 10; -> x = 10;
     */
    private void deadCodeElimination() {
        // Track variables that are written but not read before next write
        Set<String> deadAssignments = new HashSet<>();
        Map<String, Integer> lastAssignmentLine = new HashMap<>();
        List<String> result = new ArrayList<>();

        // First pass: identify dead assignments
        for (int i = 0; i < optimizedCode.size(); i++) {
            String line = optimizedCode.get(i);

            // Skip comments and labels
            if (line.trim().startsWith("#") || line.trim().endsWith(":") || line.trim().isEmpty()) {
                continue;
            }

            // Check for assignments
            if (line.contains("=") && !line.contains("if") && !line.contains("goto")) {
                String[] parts = line.split("=", 2);
                String var = parts[0].trim();

                // Only consider simple variable assignments, not array elements
                if (var.matches("[a-zA-Z][a-zA-Z0-9_]*")) {
                    // If this variable was previously assigned but not used, mark that assignment as dead
                    if (lastAssignmentLine.containsKey(var)) {
                        deadAssignments.add(var + "_" + lastAssignmentLine.get(var));
                    }

                    // Record this assignment
                    lastAssignmentLine.put(var, i);
                }
            }

            // Check for variable usage in expressions
            for (Map.Entry<String, Integer> entry : lastAssignmentLine.entrySet()) {
                String var = entry.getKey();
                // Check if variable is used on right side of expression
                if (line.matches(".*=.*\\b" + var + "\\b.*") ||
                        line.matches(".*if\\s+\\b" + var + "\\b.*") ||
                        line.matches(".*return\\s+\\b" + var + "\\b.*")) {
                    // Variable is used, so its assignment is not dead
                    deadAssignments.remove(var + "_" + entry.getValue());
                }
            }
        }

        // Second pass: remove dead assignments
        for (int i = 0; i < optimizedCode.size(); i++) {
            String line = optimizedCode.get(i);

            // Skip dead assignments
            if (line.contains("=")) {
                String[] parts = line.split("=", 2);
                String var = parts[0].trim();

                if (var.matches("[a-zA-Z][a-zA-Z0-9_]*") && deadAssignments.contains(var + "_" + i)) {
                    // Add a comment to show what was eliminated
                    result.add("# ELIMINATED: " + line);
                    continue;
                }
            }

            // Keep all other lines
            result.add(line);
        }

        optimizedCode = result;
    }

    /**
     * Remove unused variables: Eliminate variables that aren't used
     */
    private void removeUnusedVariables() {
        Set<String> usedVariables = new HashSet<>();
        Map<String, List<Integer>> variableDefinitions = new HashMap<>();
        List<String> result = new ArrayList<>();

        // First pass: collect variable definitions and usages
        for (int i = 0; i < optimizedCode.size(); i++) {
            String line = optimizedCode.get(i);

            // Skip comments and labels
            if (line.trim().startsWith("#") || line.trim().endsWith(":") || line.trim().isEmpty()) {
                continue;
            }

            // Check for assignments (variable definitions)
            if (line.contains("=") && !line.contains("if") && !line.contains("goto")) {
                String[] parts = line.split("=", 2);
                String var = parts[0].trim();

                // Only consider simple variables, not array elements
                if (var.matches("[a-zA-Z][a-zA-Z0-9_]*")) {
                    // Track this definition
                    if (!variableDefinitions.containsKey(var)) {
                        variableDefinitions.put(var, new ArrayList<>());
                    }
                    variableDefinitions.get(var).add(i);

                    // Also check for variable usages on the right side
                    String rightSide = parts[1].trim();
                    for (String v : getVariablesFromExpression(rightSide)) {
                        usedVariables.add(v);
                    }
                }
            } else {
                // Check for variable usages in other statements
                for (String v : getVariablesFromExpression(line)) {
                    usedVariables.add(v);
                }
            }
        }

        // Second pass: remove unused variable definitions
        for (int i = 0; i < optimizedCode.size(); i++) {
            String line = optimizedCode.get(i);

            // Check if this is a definition for an unused variable
            if (line.contains("=") && !line.contains("if") && !line.contains("goto")) {
                String[] parts = line.split("=", 2);
                String var = parts[0].trim();

                if (var.matches("[a-zA-Z][a-zA-Z0-9_]*") && !usedVariables.contains(var)) {
                    // Only remove if this isn't a call with side effects
                    String rightSide = parts[1].trim();
                    if (!rightSide.contains("(") && !rightSide.contains(".")) {
                        // Add a comment to show what was eliminated
                        result.add("# ELIMINATED UNUSED: " + line);
                        continue;
                    }
                }
            }

            // Keep all other lines
            result.add(line);
        }

        optimizedCode = result;
    }

    /**
     * Extract variable names from an expression
     */
    private Set<String> getVariablesFromExpression(String expr) {
        Set<String> vars = new HashSet<>();
        // Simple regex to find variables - includes letters, numbers, underscore
        // but must start with a letter
        String[] parts = expr.split("[^a-zA-Z0-9_]");
        for (String part : parts) {
            if (part.matches("[a-zA-Z][a-zA-Z0-9_]*")) {
                // Exclude keywords
                if (!part.equals("if") && !part.equals("goto") && !part.equals("return") &&
                        !part.equals("true") && !part.equals("false") && !part.equals("null")) {
                    vars.add(part);
                }
            }
        }
        return vars;
    }

    /**
     * Get the optimized IR code as a string
     */
    public String getOptimizedIRCode() {
        StringBuilder sb = new StringBuilder();
        for (String line : optimizedCode) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }
}
