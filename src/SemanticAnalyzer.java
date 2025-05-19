import ast.*;
import java.util.*;

public class SemanticAnalyzer {
    private EnhancedSymbolTable symbolTable;
    private List<String> errors;
    private String currentClass;

    public SemanticAnalyzer() {
        symbolTable = new EnhancedSymbolTable();
        errors = new ArrayList<>();
        currentClass = null;
    }

    public EnhancedSymbolTable getSymbolTable() {
        return symbolTable;
    }

    public List<String> getErrors() {
        return errors;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public void printErrors() {
        System.out.println("Semantic Errors:");
        for (String error : errors) {
            System.out.println("  " + error);
        }
    }

    // Add this method to your SemanticAnalyzer class
    private void registerClassAttributes() {
        // First, collect all class attributes including inherited ones
        for (String className : symbolTable.getClassNames()) {
            // Create a set of attributes for this class
            Map<String, String> attributes = new HashMap<>();

            // Start with this class's attributes
            EnhancedSymbolTable.ClassInfo classInfo = symbolTable.getClassInfo(className);
            attributes.putAll(classInfo.attributes);

            // Add inherited attributes by traversing class hierarchy
            String parent = classInfo.parentName;
            while (parent != null) {
                EnhancedSymbolTable.ClassInfo parentInfo = symbolTable.getClassInfo(parent);
                if (parentInfo != null) {
                    // Add parent attributes that don't conflict with existing ones
                    for (Map.Entry<String, String> entry : parentInfo.attributes.entrySet()) {
                        if (!attributes.containsKey(entry.getKey())) {
                            attributes.put(entry.getKey(), entry.getValue());
                        }
                    }
                    parent = parentInfo.parentName;
                } else {
                    break;
                }
            }

            // Update class's attributes with full set including inherited ones
            classInfo.attributes.clear();
            classInfo.attributes.putAll(attributes);
        }
    }

    public void analyze(ProgramNode program) {
        // First pass: register all classes
        // First pass: register all classes
        registerClasses(program);

        if (hasErrors()) {
            return;
        }

        // Register class attributes including inheritance
        registerClassAttributes();
        // Register built-in types first
        registerBuiltInTypes();

        // Then register program classes
        for (ClassNode classNode : program.getClasses()) {
            String className = classNode.getName();
            String parentName = classNode.getParentName();

            try {
                // Only add if not already a built-in type
                if (!symbolTable.classExists(className)) {
                    symbolTable.addClass(className, parentName);
                }
            } catch (RuntimeException e) {
                errors.add(e.getMessage());
            }
        }

        if (hasErrors()) {
            return;
        }

        // Second pass: check inheritance hierarchy for cycles
        checkInheritanceCycles();

        if (hasErrors()) {
            return;
        }

        // Third pass: register all methods and attributes
        registerMethodsAndAttributes(program);

        if (hasErrors()) {
            return;
        }

        // Fourth pass: type check all expressions
        typeCheckProgram(program);
    }

    private void registerBuiltInTypes() {
        // Register all the basic types needed
        if (!symbolTable.classExists("Object")) {
            symbolTable.addClass("Object", null);
        }

        if (!symbolTable.classExists("IO")) {
            symbolTable.addClass("IO", "Object");
        }

        if (!symbolTable.classExists("Int")) {
            symbolTable.addClass("Int", "Object");
        }

        if (!symbolTable.classExists("String")) {
            symbolTable.addClass("String", "Object");
        }

        if (!symbolTable.classExists("Bool")) {
            symbolTable.addClass("Bool", "Object");
        }

        if (!symbolTable.classExists("Void")) {
            symbolTable.addClass("Void", "Object");
        }
    }

    // First pass: register all classes
    private void registerClasses(ProgramNode program) {
        for (ClassNode classNode : program.getClasses()) {
            String className = classNode.getName();
            String parentName = classNode.getParentName();

            try {
                symbolTable.addClass(className, parentName);
            } catch (RuntimeException e) {
                errors.add(e.getMessage());
            }
        }
    }

    // Second pass: check inheritance cycles
    private void checkInheritanceCycles() {
        // For each class, traverse its inheritance chain to check for cycles
        for (String className : symbolTable.getClassInfo("Object").attributes.keySet()) {
            Set<String> visited = new HashSet<>();
            String current = className;

            while (current != null && !current.equals("Object")) {
                if (visited.contains(current)) {
                    errors.add("Semantic Error: Inheritance cycle detected involving class " + current);
                    break;
                }

                visited.add(current);
                EnhancedSymbolTable.ClassInfo classInfo = symbolTable.getClassInfo(current);
                if (classInfo == null) {
                    break;
                }

                current = classInfo.parentName;
            }
        }
    }

    // Third pass: register methods and attributes
    private void registerMethodsAndAttributes(ProgramNode program) {
        // First, register all class attributes
        for (ClassNode classNode : program.getClasses()) {
            String className = classNode.getName();
            EnhancedSymbolTable.ClassInfo classInfo = symbolTable.getClassInfo(className);

            for (FeatureNode feature : classNode.getFeatures()) {
                if (feature instanceof AttributeNode) {
                    AttributeNode attr = (AttributeNode) feature;
                    String name = attr.getName();
                    String type = attr.getType();

                    // Add to class attributes table
                    try {
                        classInfo.addAttribute(name, type);
                    } catch (RuntimeException e) {
                        errors.add(e.getMessage());
                    }
                }
            }
        }

        // Then, register and check all methods
        for (ClassNode classNode : program.getClasses()) {
            currentClass = classNode.getName();

            // Enter class scope
            symbolTable.enterScope(currentClass, "class");

            // Add all attributes to the scope - including inherited ones
            addAttributesToScope(currentClass);

            // Register methods
            for (FeatureNode feature : classNode.getFeatures()) {
                if (feature instanceof MethodNode) {
                    MethodNode method = (MethodNode) feature;
                    registerMethod(method);
                }
            }

            // Exit class scope
            symbolTable.exitScope();
        }
    }

    private void addAttributesToScope(String className) {
        EnhancedSymbolTable.ClassInfo classInfo = symbolTable.getClassInfo(className);
        if (classInfo == null) return;

        // Add all attributes (no need to handle inheritance - already done)
        for (Map.Entry<String, String> entry : classInfo.attributes.entrySet()) {
            try {
                symbolTable.addVariable(entry.getKey(), entry.getValue());
            } catch (RuntimeException e) {
                // Variables might already exist in scope - ignore duplicates
                if (!e.getMessage().contains("already defined")) {
                    errors.add(e.getMessage());
                }
            }
        }
    }

    private void registerAttribute(AttributeNode attr) {
        String name = attr.getName();
        String type = attr.getType();

        try {
            // Add to symbol table
            EnhancedSymbolTable.ClassInfo classInfo = symbolTable.getClassInfo(currentClass);
            classInfo.addAttribute(name, type);

            // Also add to current scope
            symbolTable.addVariable(name, type);

            // If there's an initializer expression, visit it
            if (attr.getInitExpr() != null) {
                ExpressionNode initExpr = attr.getInitExpr();
                String initType = typeCheck(initExpr);

                // Check for type compatibility
                if (!symbolTable.conformsTo(initType, type)) {
                    errors.add("Semantic Error: Type mismatch in attribute " + name + " initialization. Expected " +
                            type + ", got " + initType);
                }
            }
        } catch (RuntimeException e) {
            errors.add(e.getMessage());
        }
    }

    private void registerMethod(MethodNode method) {
        String name = method.getName();
        String returnType = method.getType();
        List<String> paramTypes = new ArrayList<>();

        try {
            // Check for override violations in parent classes
            checkMethodOverride(method);

            // Add parameters to list
            for (FormalNode param : method.getParameters()) {
                paramTypes.add(param.getType());
            }

            // Add to class info
            EnhancedSymbolTable.ClassInfo classInfo = symbolTable.getClassInfo(currentClass);
            classInfo.addMethod(name, returnType, paramTypes);

            // Add to symbol table (current scope)
            symbolTable.addMethod(name, returnType, paramTypes);
        } catch (RuntimeException e) {
            errors.add(e.getMessage());
        }
    }

    private void checkMethodOverride(MethodNode method) {
        String name = method.getName();
        String returnType = method.getType();
        List<String> paramTypes = new ArrayList<>();

        for (FormalNode param : method.getParameters()) {
            paramTypes.add(param.getType());
        }

        // Get parent class name
        EnhancedSymbolTable.ClassInfo classInfo = symbolTable.getClassInfo(currentClass);
        if (classInfo.parentName == null) {
            return; // No parent class to check
        }

        String parentClass = classInfo.parentName;
        EnhancedSymbolTable.MethodInfo parentMethod = symbolTable.getMethod(name, parentClass);

        if (parentMethod != null) {
            // Check return type
            if (!parentMethod.returnType.equals(returnType)) {
                errors.add("Semantic Error: Method " + name + " in class " + currentClass +
                        " has a different return type from overridden method in parent class");
            }

            // Check parameter count
            if (parentMethod.paramTypes.size() != paramTypes.size()) {
                errors.add("Semantic Error: Method " + name + " in class " + currentClass +
                        " has a different number of parameters from overridden method in parent class");
                return;
            }

            // Check parameter types
            for (int i = 0; i < paramTypes.size(); i++) {
                if (!parentMethod.paramTypes.get(i).equals(paramTypes.get(i))) {
                    errors.add("Semantic Error: Method " + name + " in class " + currentClass +
                            " has different parameter types from overridden method in parent class");
                    break;
                }
            }
        }
    }

    // Fourth pass: type check expressions
    private void typeCheckProgram(ProgramNode program) {
        for (ClassNode classNode : program.getClasses()) {
            currentClass = classNode.getName();

            // Enter class scope
            symbolTable.enterScope(currentClass, "class");

            // Type check attribute initializers
            for (FeatureNode feature : classNode.getFeatures()) {
                if (feature instanceof AttributeNode) {
                    AttributeNode attr = (AttributeNode) feature;
                    if (attr.getInitExpr() != null) {
                        String initType = typeCheck(attr.getInitExpr());

                        // Validate type compatibility
                        if (!symbolTable.conformsTo(initType, attr.getType())) {
                            errors.add("Semantic Error: Type mismatch in attribute " + attr.getName() +
                                    " initialization. Expected " + attr.getType() + ", got " + initType);
                        }
                    }
                }
            }

            // Type check method bodies
            for (FeatureNode feature : classNode.getFeatures()) {
                if (feature instanceof MethodNode) {
                    MethodNode method = (MethodNode) feature;
                    typeCheckMethod(method);
                }
            }

            // Exit class scope
            symbolTable.exitScope();
        }
    }

    private void typeCheckMethod(MethodNode method) {
        // Enter method scope
        symbolTable.enterScope(method.getName(), "method");

        try {
            // Add parameters to scope
            for (FormalNode param : method.getParameters()) {
                try {
                    symbolTable.addVariable(param.getName(), param.getType());
                } catch (RuntimeException e) {
                    errors.add(e.getMessage());
                }
            }

            // Type check body
            String methodType = method.getType();
            String bodyType = null;

            for (ExpressionNode expr : method.getBody()) {
                bodyType = typeCheck(expr);
            }

            // Check return type compatibility with method's declared return type
            if (bodyType != null && !symbolTable.conformsTo(bodyType, methodType)) {
                errors.add("Semantic Error: Method " + method.getName() + " in class " + currentClass +
                        " has a body of type " + bodyType + " which doesn't conform to the declared return type " +
                        methodType);
            }
        } finally {
            // Exit method scope - always do this even if errors occur
            symbolTable.exitScope();
        }
    }

    private String typeCheck(ExpressionNode expr) {
        if (expr == null) {
            return "Object"; // Default for null expressions
        }

        // Binary operations
        if (expr instanceof BinaryOperationNode) {
            return typeCheckBinaryOp((BinaryOperationNode) expr);
        }

        // Unary operations
        if (expr instanceof UnaryOperationNode) {
            return typeCheckUnaryOp((UnaryOperationNode) expr);
        }

        // Integer literals
        if (expr instanceof IntegerLiteralNode) {
            expr.setExpressionType("Int");
            return "Int";
        }

        // String literals
        if (expr instanceof StringLiteralNode) {
            expr.setExpressionType("String");
            return "String";
        }

        // Boolean literals
        if (expr instanceof BooleanLiteralNode) {
            expr.setExpressionType("Bool");
            return "Bool";
        }

        // Identifiers
        if (expr instanceof IdentifierNode) {
            return typeCheckIdentifier((IdentifierNode) expr);
        }

        // Assignment
        if (expr instanceof AssignmentNode) {
            return typeCheckAssignment((AssignmentNode) expr);
        }

        // Method call
        if (expr instanceof MethodCallNode) {
            return typeCheckMethodCall((MethodCallNode) expr);
        }

        // If statement
        if (expr instanceof IfNode) {
            return typeCheckIf((IfNode) expr);
        }

        // While loop
        if (expr instanceof WhileNode) {
            return typeCheckWhile((WhileNode) expr);
        }

        // Fallback
        errors.add("Semantic Error: Unknown expression type: " + expr.getClass().getName());
        return "Object";
    }

    private String typeCheckBinaryOp(BinaryOperationNode node) {
        String leftType = typeCheck(node.getLeft());
        String rightType = typeCheck(node.getRight());
        BinaryOperationNode.Operator op = node.getOperator();

        // Arithmetic operations require Int operands
        if (op == BinaryOperationNode.Operator.PLUS ||
                op == BinaryOperationNode.Operator.MINUS ||
                op == BinaryOperationNode.Operator.MULTIPLY ||
                op == BinaryOperationNode.Operator.DIVIDE ||
                op == BinaryOperationNode.Operator.MOD) {

            if (!leftType.equals("Int")) {
                errors.add("Semantic Error: Left operand of " + op + " must be Int, got " + leftType);
            }

            if (!rightType.equals("Int")) {
                errors.add("Semantic Error: Right operand of " + op + " must be Int, got " + rightType);
            }

            node.setExpressionType("Int");
            return "Int";
        }

        // Comparison operations (except equality) require Int operands
        if (op == BinaryOperationNode.Operator.LT ||
                op == BinaryOperationNode.Operator.LE ||
                op == BinaryOperationNode.Operator.GT ||
                op == BinaryOperationNode.Operator.GE) {

            if (!leftType.equals("Int")) {
                errors.add("Semantic Error: Left operand of " + op + " must be Int, got " + leftType);
            }

            if (!rightType.equals("Int")) {
                errors.add("Semantic Error: Right operand of " + op + " must be Int, got " + rightType);
            }

            node.setExpressionType("Bool");
            return "Bool";
        }

        // Equality (=) works on all types
        if (op == BinaryOperationNode.Operator.EQ || op == BinaryOperationNode.Operator.NE) {
            // No type restrictions, any two types can be compared for equality
            node.setExpressionType("Bool");
            return "Bool";
        }

        // Logical operations require Bool operands
        if (op == BinaryOperationNode.Operator.AND || op == BinaryOperationNode.Operator.OR) {
            if (!leftType.equals("Bool")) {
                errors.add("Semantic Error: Left operand of " + op + " must be Bool, got " + leftType);
            }

            if (!rightType.equals("Bool")) {
                errors.add("Semantic Error: Right operand of " + op + " must be Bool, got " + rightType);
            }

            node.setExpressionType("Bool");
            return "Bool";
        }

        // Should never get here
        errors.add("Semantic Error: Unknown binary operator: " + op);
        return "Object";
    }

    private String typeCheckUnaryOp(UnaryOperationNode node) {
        String exprType = typeCheck(node.getOperand());
        UnaryOperationNode.Operator op = node.getOperator();

        if (op == UnaryOperationNode.Operator.NOT) {
            if (!exprType.equals("Bool")) {
                errors.add("Semantic Error: Operand of NOT must be Bool, got " + exprType);
            }

            node.setExpressionType("Bool");
            return "Bool";
        }

        if (op == UnaryOperationNode.Operator.NEGATIVE) {
            if (!exprType.equals("Int")) {
                errors.add("Semantic Error: Operand of negation must be Int, got " + exprType);
            }

            node.setExpressionType("Int");
            return "Int";
        }

        // Should never get here
        errors.add("Semantic Error: Unknown unary operator: " + op);
        return "Object";
    }

    private String typeCheckIdentifier(IdentifierNode node) {
        String name = node.getName();
        String type = symbolTable.getVariableType(name);

        if (type == null) {
            // Check if it's a class attribute
            EnhancedSymbolTable.ClassInfo classInfo = symbolTable.getClassInfo(currentClass);
            if (classInfo != null && classInfo.attributes.containsKey(name)) {
                type = classInfo.attributes.get(name);
            } else {
                // Check parent classes for the attribute
                String parentClass = classInfo != null ? classInfo.parentName : null;
                while (type == null && parentClass != null) {
                    EnhancedSymbolTable.ClassInfo parentInfo = symbolTable.getClassInfo(parentClass);
                    if (parentInfo != null) {
                        if (parentInfo.attributes.containsKey(name)) {
                            type = parentInfo.attributes.get(name);
                            break;
                        }
                        parentClass = parentInfo.parentName;
                    } else {
                        break;
                    }
                }

                if (type == null) {
                    errors.add("Semantic Error: Undefined identifier: " + name);
                    return "Object";
                }
            }
        }

        node.setExpressionType(type);
        return type;
    }

    private String typeCheckAssignment(AssignmentNode node) {
        String varName = node.getVariable();
        String varType = symbolTable.getVariableType(varName);

        if (varType == null) {
            errors.add("Semantic Error: Assignment to undefined variable: " + varName);
            return "Object";
        }

        String valueType = typeCheck(node.getValue());

        // Check assignment compatibility
        if (!symbolTable.conformsTo(valueType, varType)) {
            errors.add("Semantic Error: Cannot assign " + valueType + " to " + varName + " of type " + varType);
        }

        // For compound assignments, check that the operation is valid for the types
        if (node.getType() != AssignmentNode.AssignmentType.SIMPLE) {
            if (!varType.equals("Int") || !valueType.equals("Int")) {
                errors.add("Semantic Error: Compound assignment operator " + node.getType() +
                        " requires Int operands, got " + varType + " and " + valueType);
            }
        }

        node.setExpressionType(varType);
        return varType;
    }

    private String typeCheckMethodCall(MethodCallNode node) {
        String objectType = "SELF_TYPE";
        if (node.getObject() != null) {
            objectType = typeCheck(node.getObject());
        }

        // Replace SELF_TYPE with the current class name
        if (objectType.equals("SELF_TYPE")) {
            objectType = currentClass;
        }

        String methodName = node.getMethodName();
        EnhancedSymbolTable.MethodInfo methodInfo = symbolTable.getMethod(methodName, objectType);

        if (methodInfo == null) {
            errors.add("Semantic Error: Undefined method " + methodName + " for type " + objectType);
            return "Object";
        }

        // Check argument count
        if (methodInfo.paramTypes.size() != node.getArguments().size()) {
            errors.add("Semantic Error: Method " + methodName + " requires " + methodInfo.paramTypes.size() +
                    " arguments, but " + node.getArguments().size() + " were provided");
            return methodInfo.returnType;
        }

        // Check argument types
        for (int i = 0; i < methodInfo.paramTypes.size(); i++) {
            String expectedType = methodInfo.paramTypes.get(i);
            String actualType = typeCheck(node.getArguments().get(i));

            if (!symbolTable.conformsTo(actualType, expectedType)) {
                errors.add("Semantic Error: Argument " + (i+1) + " of method " + methodName +
                        " must be of type " + expectedType + ", got " + actualType);
            }
        }

        node.setExpressionType(methodInfo.returnType);
        return methodInfo.returnType;
    }

    private String typeCheckIf(IfNode node) {
        String condType = typeCheck(node.getCondition());

        if (!condType.equals("Bool")) {
            errors.add("Semantic Error: If condition must be of type Bool, got " + condType);
        }

        String thenType = typeCheck(node.getThenExpr());
        String elseType = typeCheck(node.getElseExpr());

        // The type of the if expression is the least common ancestor of the then and else branches
        String resultType = symbolTable.leastCommonAncestor(thenType, elseType);
        node.setExpressionType(resultType);
        return resultType;
    }

    private String typeCheckWhile(WhileNode node) {
        String condType = typeCheck(node.getCondition());

        if (!condType.equals("Bool")) {
            errors.add("Semantic Error: While condition must be of type Bool, got " + condType);
        }

        // Type check the body but ignore its type
        typeCheck(node.getBody());

        // In COOL, a while loop always returns Object
        node.setExpressionType("Object");
        return "Object";
    }
}