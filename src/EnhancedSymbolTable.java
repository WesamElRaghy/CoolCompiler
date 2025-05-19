import java.util.*;

public class EnhancedSymbolTable {
    // Class definitions table
    private Map<String, ClassInfo> classTable;

    // Current scope for analysis
    private Scope currentScope;

    public EnhancedSymbolTable() {
        classTable = new HashMap<>();
        currentScope = null;

        // Add built-in types
        addBuiltInTypes();
    }



    private void addBuiltInTypes() {
        // Don't add built-in types if they're already defined in the test file
        // The classes should be defined in the test file, but just make sure Void exists

        // Add Void type if not already present
        if (!classTable.containsKey("Void")) {
            ClassInfo voidClass = new ClassInfo("Void", "Object");
            classTable.put("Void", voidClass);
        }
    }

    // Class information management
    public void addClass(String className, String parentName) {
        if (classTable.containsKey(className)) {
            throw new RuntimeException("Semantic Error: Class " + className + " already defined");
        }

        // Verify parent class exists if specified
        if (parentName != null && !classTable.containsKey(parentName)) {
            throw new RuntimeException("Semantic Error: Parent class " + parentName + " not defined");
        }

        classTable.put(className, new ClassInfo(className, parentName));
    }

    public ClassInfo getClassInfo(String className) {
        return classTable.get(className);
    }

    public boolean classExists(String className) {
        return classTable.containsKey(className);
    }

    // Scope management
    public void enterScope(String scopeName, String scopeType) {
        Scope newScope = new Scope(scopeName, scopeType, currentScope);
        currentScope = newScope;
    }

    public void exitScope() {
        if (currentScope != null) {
            currentScope = currentScope.parent;
        }
    }

    // Variable and method declaration
    public void addVariable(String name, String type) {
        if (currentScope == null) {
            throw new RuntimeException("Semantic Error: No active scope for adding variable " + name);
        }

        // Check if type exists
        if (!classExists(type)) {
            throw new RuntimeException("Semantic Error: Type " + type + " not defined");
        }

        // Check for duplicate in current scope only
        if (currentScope.variables.containsKey(name)) {
            // Only throw if not overriding a variable from an outer scope
            boolean isOverride = false;
            Scope outerScope = currentScope.parent;
            while (outerScope != null) {
                if (outerScope.variables.containsKey(name)) {
                    isOverride = true;
                    break;
                }
                outerScope = outerScope.parent;
            }

            if (!isOverride) {
                throw new RuntimeException("Semantic Error: Variable " + name + " already defined in this scope");
            }
        }

        currentScope.variables.put(name, type);
    }

    public void addMethod(String name, String returnType, List<String> paramTypes) {
        if (currentScope == null) {
            throw new RuntimeException("Semantic Error: No active scope for adding method " + name);
        }

        // Check if return type exists
        if (!classExists(returnType)) {
            throw new RuntimeException("Semantic Error: Return type " + returnType + " not defined");
        }

        // Check all parameter types
        for (String paramType : paramTypes) {
            if (!classExists(paramType)) {
                throw new RuntimeException("Semantic Error: Parameter type " + paramType + " not defined");
            }
        }

        // Check for duplicate method in current scope
        if (currentScope.methods.containsKey(name)) {
            throw new RuntimeException("Semantic Error: Method " + name + " already defined in this scope");
        }

        MethodInfo methodInfo = new MethodInfo(name, returnType, paramTypes);
        currentScope.methods.put(name, methodInfo);
    }

    // Lookup functions
    public String getVariableType(String name) {
        Scope scope = currentScope;
        while (scope != null) {
            if (scope.variables.containsKey(name)) {
                return scope.variables.get(name);
            }
            scope = scope.parent;
        }
        return null; // Not found
    }

    public MethodInfo getMethod(String name, String className) {
        ClassInfo classInfo = classTable.get(className);
        while (classInfo != null) {
            if (classInfo.methods.containsKey(name)) {
                return classInfo.methods.get(name);
            }

            // Try parent class
            if (classInfo.parentName != null) {
                classInfo = classTable.get(classInfo.parentName);
            } else {
                break;
            }
        }
        return null; // Method not found
    }

    // Check if typeA conforms to typeB (is subtype of)
    public boolean conformsTo(String typeA, String typeB) {
        if (typeA.equals(typeB)) {
            return true;
        }

        // Navigate the inheritance chain
        String currentType = typeA;
        while (currentType != null) {
            ClassInfo classInfo = classTable.get(currentType);
            if (classInfo == null) {
                return false;
            }

            if (classInfo.name.equals(typeB)) {
                return true;
            }

            currentType = classInfo.parentName;
        }

        return false;
    }

    // Get the lowest common ancestor type
    public String leastCommonAncestor(String type1, String type2) {
        if (type1.equals(type2)) {
            return type1;
        }

        if (conformsTo(type1, type2)) {
            return type2;
        }

        if (conformsTo(type2, type1)) {
            return type1;
        }

        // Collect the inheritance path for type1
        Set<String> type1Ancestors = new HashSet<>();
        String current = type1;
        while (current != null) {
            type1Ancestors.add(current);
            ClassInfo classInfo = classTable.get(current);
            if (classInfo == null) break;
            current = classInfo.parentName;
        }

        // Find the first common ancestor in type2's inheritance path
        current = type2;
        while (current != null) {
            if (type1Ancestors.contains(current)) {
                return current;
            }
            ClassInfo classInfo = classTable.get(current);
            if (classInfo == null) break;
            current = classInfo.parentName;
        }

        return "Object"; // Default to Object if no common ancestor found
    }

    // Print the symbol table for debugging
    public void printSymbolTable() {
        System.out.println("Enhanced Symbol Table:");
        System.out.println("Classes:");
        for (ClassInfo classInfo : classTable.values()) {
            System.out.println("  " + classInfo.name +
                    (classInfo.parentName != null ? " inherits " + classInfo.parentName : ""));

            System.out.println("  Methods:");
            for (MethodInfo methodInfo : classInfo.methods.values()) {
                System.out.print("    " + methodInfo.name + "(");
                for (int i = 0; i < methodInfo.paramTypes.size(); i++) {
                    if (i > 0) System.out.print(", ");
                    System.out.print(methodInfo.paramTypes.get(i));
                }
                System.out.println(") : " + methodInfo.returnType);
            }

            System.out.println("  Attributes:");
            for (Map.Entry<String, String> entry : classInfo.attributes.entrySet()) {
                System.out.println("    " + entry.getKey() + " : " + entry.getValue());
            }
        }
    }

    // Inner classes
    public static class Scope {
        String name;
        String type; // "class", "method", "block"
        Map<String, String> variables;
        Map<String, MethodInfo> methods;
        Scope parent;

        public Scope(String name, String type, Scope parent) {
            this.name = name;
            this.type = type;
            this.parent = parent;
            this.variables = new HashMap<>();
            this.methods = new HashMap<>();
        }
    }

    public static class ClassInfo {
        String name;
        String parentName;
        Map<String, String> attributes;
        Map<String, MethodInfo> methods;

        public ClassInfo(String name, String parentName) {
            this.name = name;
            this.parentName = parentName;
            this.attributes = new HashMap<>();
            this.methods = new HashMap<>();
        }

        public void addAttribute(String name, String type) {
            attributes.put(name, type);
        }

        public void addMethod(String name, String returnType, List<String> paramTypes) {
            methods.put(name, new MethodInfo(name, returnType, paramTypes));
        }
    }

    public static class MethodInfo {
        String name;
        String returnType;
        List<String> paramTypes;
        List<String> paramNames;

        public MethodInfo(String name, String returnType, List<String> paramTypes) {
            this.name = name;
            this.returnType = returnType;
            this.paramTypes = paramTypes;
            this.paramNames = new ArrayList<>();
        }

        public void addParamName(String name) {
            paramNames.add(name);
        }
    }

    public Set<String> getClassNames() {
        return classTable.keySet();
    }
}