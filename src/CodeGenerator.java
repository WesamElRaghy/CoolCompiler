import java.util.*;

/**
 * Generates x86-64 assembly code from the optimized TAC IR
 */
public class CodeGenerator {
    private List<String> irCode;
    private List<String> assemblyCode;
    private Map<String, Integer> variables;
    private int currentStackOffset;
    private Set<String> labels;

    public CodeGenerator(List<String> irCode) {
        this.irCode = irCode;
        this.assemblyCode = new ArrayList<>();
        this.variables = new HashMap<>();
        this.currentStackOffset = 0;
        this.labels = new HashSet<>();
    }

    /**
     * Generate x86-64 assembly code from the IR
     */
    public List<String> generate() {
        // Clear previous state
        assemblyCode.clear();
        variables.clear();
        currentStackOffset = 0;
        labels.clear();

        // Add assembly header
        addHeader();

        // First pass: collect all labels
        for (String line : irCode) {
            if (line.endsWith(":")) {
                labels.add(line.substring(0, line.length() - 1).trim());
            }
        }

        // Second pass: generate code
        String currentMethod = null;
        boolean inMethod = false;

        for (String line : irCode) {
            // Skip comments or empty lines
            if (line.trim().startsWith("#") || line.trim().isEmpty()) {
                assemblyCode.add("# " + line.trim());
                continue;
            }

            // Handle method labels
            if (line.startsWith("method_") && line.endsWith(":")) {
                if (inMethod) {
                    // End previous method
                    endMethod();
                }

                // Start new method
                currentMethod = line.substring(0, line.length() - 1).trim();
                inMethod = true;
                startMethod(currentMethod);
                continue;
            }

            // Handle other labels
            if (line.endsWith(":")) {
                assemblyCode.add(line);
                continue;
            }

            // Handle return statements
            if (line.startsWith("return ")) {
                String returnValue = line.substring("return ".length()).trim();
                handleReturn(returnValue);
                continue;
            }

            // Handle if/goto statements
            if (line.startsWith("if ") && line.contains(" goto ")) {
                String[] parts = line.split(" goto ", 2);
                String condition = parts[0].substring("if ".length()).trim();
                String target = parts[1].trim();
                handleConditionalJump(condition, target);
                continue;
            }

            if (line.startsWith("goto ")) {
                String target = line.substring("goto ".length()).trim();
                handleJump(target);
                continue;
            }

            // Handle assignments
            if (line.contains("=")) {
                String[] parts = line.split("=", 2);
                String target = parts[0].trim();
                String expression = parts[1].trim();
                handleAssignment(target, expression);
                continue;
            }

            // Add unhandled lines as comments
            assemblyCode.add("# UNHANDLED: " + line);
        }

        // End last method if still in one
        if (inMethod) {
            endMethod();
        }

        // Add footer
        addFooter();

        return assemblyCode;
    }

    private void addHeader() {
        assemblyCode.add(".section .text");
        assemblyCode.add(".global main");
        assemblyCode.add("");
    }

    private void addFooter() {
        assemblyCode.add("");
        assemblyCode.add("# End of assembly code");
    }

    private void startMethod(String methodName) {
        // Reset stack offset for new method
        currentStackOffset = 0;
        variables.clear();

        // Special case for main method
        String asmMethodName = methodName.equals("method_main") ? "main" : methodName;

        assemblyCode.add(asmMethodName + ":");
        assemblyCode.add("    push %rbp");
        assemblyCode.add("    mov %rsp, %rbp");
        // Reserve stack space - will be updated at the end of the method
        assemblyCode.add("    sub $0, %rsp  # Stack space placeholder");
    }

    private void endMethod() {
        // Update stack space reservation
        if (currentStackOffset > 0) {
            // Find and update the stack reservation instruction
            for (int i = assemblyCode.size() - 1; i >= 0; i--) {
                if (assemblyCode.get(i).contains("sub $0, %rsp  # Stack space placeholder")) {
                    // Round to 16-byte alignment
                    int alignedOffset = (currentStackOffset + 15) & ~15;
                    assemblyCode.set(i, "    sub $" + alignedOffset + ", %rsp  # Stack space");
                    break;
                }
            }
        }

        // Add standard method cleanup and return
        assemblyCode.add("    mov %rbp, %rsp");
        assemblyCode.add("    pop %rbp");
        assemblyCode.add("    ret");
        assemblyCode.add("");
    }

    private void handleReturn(String value) {
        // Load return value into rax
        if (value.matches("\\d+")) {
            // Numeric literal
            assemblyCode.add("    mov $" + value + ", %rax");
        } else if (value.equals("true")) {
            assemblyCode.add("    mov $1, %rax");
        } else if (value.equals("false")) {
            assemblyCode.add("    mov $0, %rax");
        } else if (variables.containsKey(value)) {
            // Variable reference
            int offset = variables.get(value);
            assemblyCode.add("    mov " + offset + "(%rbp), %rax");
        } else {
            // Assume it's a register or already has the right value
            assemblyCode.add("    # Return " + value);
        }
    }

    private void handleConditionalJump(String condition, String target) {
        if (condition.contains("==")) {
            String[] parts = condition.split("==");
            String left = parts[0].trim();
            String right = parts[1].trim();
            loadComparisonOperands(left, right);
            assemblyCode.add("    cmp %rbx, %rax");
            assemblyCode.add("    je " + target);
        } else if (condition.contains("!=")) {
            String[] parts = condition.split("!=");
            String left = parts[0].trim();
            String right = parts[1].trim();
            loadComparisonOperands(left, right);
            assemblyCode.add("    cmp %rbx, %rax");
            assemblyCode.add("    jne " + target);
        } else if (condition.contains("<")) {
            String[] parts = condition.split("<");
            String left = parts[0].trim();
            String right = parts[1].trim();
            loadComparisonOperands(left, right);
            assemblyCode.add("    cmp %rbx, %rax");
            assemblyCode.add("    jl " + target);
        } else if (condition.contains(">")) {
            String[] parts = condition.split(">");
            String left = parts[0].trim();
            String right = parts[1].trim();
            loadComparisonOperands(left, right);
            assemblyCode.add("    cmp %rbx, %rax");
            assemblyCode.add("    jg " + target);
        } else if (condition.contains("<=")) {
            String[] parts = condition.split("<=");
            String left = parts[0].trim();
            String right = parts[1].trim();
            loadComparisonOperands(left, right);
            assemblyCode.add("    cmp %rbx, %rax");
            assemblyCode.add("    jle " + target);
        } else if (condition.contains(">=")) {
            String[] parts = condition.split(">=");
            String left = parts[0].trim();
            String right = parts[1].trim();
            loadComparisonOperands(left, right);
            assemblyCode.add("    cmp %rbx, %rax");
            assemblyCode.add("    jge " + target);
        } else {
            // Simple variable or value check
            loadValueToRegister(condition, "%rax");
            assemblyCode.add("    cmp $0, %rax");
            assemblyCode.add("    jne " + target);
        }
    }

    private void loadComparisonOperands(String left, String right) {
        loadValueToRegister(left, "%rax");
        loadValueToRegister(right, "%rbx");
    }

    private void loadValueToRegister(String value, String register) {
        if (value.matches("\\d+")) {
            // Numeric literal
            assemblyCode.add("    mov $" + value + ", " + register);
        } else if (value.equals("true")) {
            assemblyCode.add("    mov $1, " + register);
        } else if (value.equals("false")) {
            assemblyCode.add("    mov $0, " + register);
        } else if (variables.containsKey(value)) {
            // Variable reference
            int offset = variables.get(value);
            assemblyCode.add("    mov " + offset + "(%rbp), " + register);
        } else {
            // Temporary variable or unknown - add a comment 
            assemblyCode.add("    # Load " + value + " into " + register);
            assemblyCode.add("    mov $0, " + register + "  # Placeholder");
        }
    }

    private void handleJump(String target) {
        assemblyCode.add("    jmp " + target);
    }

    private void handleAssignment(String target, String expression) {
        // Allocate stack space for the variable if needed
        if (!variables.containsKey(target)) {
            currentStackOffset -= 8;  // 64-bit values
            variables.put(target, currentStackOffset);
        }

        int targetOffset = variables.get(target);

        // Check for simple assignments
        if (expression.matches("\\d+") || expression.equals("true") || expression.equals("false")) {
            // Direct literal assignment
            int value = expression.equals("true") ? 1 : (expression.equals("false") ? 0 : Integer.parseInt(expression));
            assemblyCode.add("    mov $" + value + ", " + targetOffset + "(%rbp)");
            return;
        }

        if (variables.containsKey(expression)) {
            // Direct variable copy
            int sourceOffset = variables.get(expression);
            assemblyCode.add("    mov " + sourceOffset + "(%rbp), %rax");
            assemblyCode.add("    mov %rax, " + targetOffset + "(%rbp)");
            return;
        }

        // Handle binary operations
        if (expression.contains("+")) {
            String[] parts = expression.split("\\+");
            String left = parts[0].trim();
            String right = parts[1].trim();

            loadValueToRegister(left, "%rax");
            loadValueToRegister(right, "%rbx");
            assemblyCode.add("    add %rbx, %rax");
            assemblyCode.add("    mov %rax, " + targetOffset + "(%rbp)");
            return;
        }

        if (expression.contains("-")) {
            String[] parts = expression.split("-");
            String left = parts[0].trim();
            String right = parts[1].trim();

            loadValueToRegister(left, "%rax");
            loadValueToRegister(right, "%rbx");
            assemblyCode.add("    sub %rbx, %rax");
            assemblyCode.add("    mov %rax, " + targetOffset + "(%rbp)");
            return;
        }

        if (expression.contains("*")) {
            String[] parts = expression.split("\\*");
            String left = parts[0].trim();
            String right = parts[1].trim();

            loadValueToRegister(left, "%rax");
            loadValueToRegister(right, "%rbx");
            assemblyCode.add("    imul %rbx, %rax");
            assemblyCode.add("    mov %rax, " + targetOffset + "(%rbp)");
            return;
        }

        if (expression.contains("/")) {
            String[] parts = expression.split("/");
            String left = parts[0].trim();
            String right = parts[1].trim();

            loadValueToRegister(left, "%rax");
            assemblyCode.add("    cqo");  // Sign-extend rax into rdx:rax
            loadValueToRegister(right, "%rbx");
            assemblyCode.add("    idiv %rbx");  // Divide rdx:rax by rbx
            assemblyCode.add("    mov %rax, " + targetOffset + "(%rbp)");
            return;
        }

        // For unhandled expressions, add a comment
        assemblyCode.add("    # UNHANDLED ASSIGNMENT: " + target + " = " + expression);
        assemblyCode.add("    mov $0, " + targetOffset + "(%rbp)  # Placeholder");
    }

    /**
     * Get the generated assembly code as a string
     */
    public String getAssemblyCode() {
        StringBuilder sb = new StringBuilder();
        for (String line : assemblyCode) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }
}