package pt.up.fe.comp2025.ast;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.symboltable.JmmSymbolTable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static pt.up.fe.comp2025.ast.Kind.BOOLEAN_LITERAL;
import static pt.up.fe.comp2025.ast.Kind.METHOD_DECL;

/**
 * Utility methods regarding types.
 */
public class TypeUtils {

    private final JmmSymbolTable table;

    public TypeUtils(SymbolTable table) {
        this.table = (JmmSymbolTable) table;
    }

    public static Type newIntType() {
        return new Type("int", false);
    }

    public static Type newBooleanType() {
        return new Type("boolean", false);
    }

    public static Type newStringType() {
        return new Type("String", false);
    }

    public static Type convertType(JmmNode typeNode) {
        String name = typeNode.get("name");
        boolean isArray = Boolean.parseBoolean(typeNode.get("isArray"));
        return new Type(name, isArray);
    }

    /**
     * Gets the {@link Type} of an arbitrary expression.
     */
    public Type getExprType(JmmNode expr) {
        switch (Kind.fromString(expr.getKind())) {
            case PARENT_EXPR:
                return getExprType(expr.getChild(0));

            case NEW_OBJECT_EXPR: {
                String className = expr.get("name");

                if (className.equals(table.getClassName())) {
                    return new Type(className, false);
                }

                if (isImported(className)) {
                    return new Type(className, false);
                }

                if (className.equals("String")) {
                    return newStringType();
                }

                return null;
            }

            case ARRAY_ACCESS_EXPR, LENGTH_EXPR, INTEGER_LITERAL:
                return newIntType();

            case STRING_LITERAL:
                return newStringType();

            case METHOD_CALL_EXPR: {
                Type objectType = getExprType(expr.getChild(0));
                String methodName = expr.get("name");

                // Handle imported classes
                if (isImported(objectType.getName())) {
                    return new Type("Object", false); // Assume valid return type for imported classes
                }

                // Handle local methods
                Type returnType = table.getReturnType(methodName);
                if (returnType == null) {
                    throw new RuntimeException("Method '" + methodName + "' not found in class " + objectType.getName());
                }
                return returnType;
            }

            case THIS_EXPR:
                return new Type(table.getClassName(), false);

            case UNARY_NOT_EXPR, BOOLEAN_LITERAL:
                return newBooleanType();

            case BINARY_EXPR: {
                String op = expr.get("op");
                switch (op) {
                    case "*","/","+","-":
                        return newIntType();
                    case "<",">","<=",">=","==","!=","&&","||":
                        return newBooleanType();
                    default:
                        throw new UnsupportedOperationException("Unknown operator: " + op);
                }
            }

            case VAR_REF_EXPR:
                String currentMethod = expr.getAncestor(METHOD_DECL)
                        .map(node -> node.get("name"))
                        .orElseThrow(() -> new RuntimeException("Cannot determine current method for variable: " + expr.get("name")));
                return resolveVariableType(expr.get("name"), currentMethod);

            case ARRAY_INIT, NEW_INT_ARRAY_EXPR:
                return new Type("int", true);

            default:
                throw new UnsupportedOperationException("Unhandled expression type: " + expr.getKind());
        }
    }

    private boolean isImported(String className) {
        return table.getImports().stream()
                .flatMap(importName -> Arrays.stream(importName.substring(1, importName.length() - 1).split(",")))
                .map(String::trim)
                .anyMatch(importName -> importName.equals(className));
    }

    private Type resolveVariableType(String varName, String currentMethod) {
        // Check local variables
        Optional<Type> localType = table.getLocalVariables(currentMethod).stream()
                .filter(local -> local.getName().equals(varName))
                .findFirst()
                .map(Symbol::getType);

        if (localType.isPresent()) return localType.get();

        // Check parameters
        Optional<Type> paramType = table.getParameters(currentMethod).stream()
                .filter(param -> param.getName().equals(varName))
                .findFirst()
                .map(Symbol::getType);

        if (paramType.isPresent()) return paramType.get();

        // Check fields
        Optional<Type> fieldType = table.getFields().stream()
                .filter(field -> field.getName().equals(varName))
                .findFirst()
                .map(Symbol::getType);

        if (fieldType.isPresent()) return fieldType.get();

        // Check imports
        if (isImported(varName)) {
            return new Type(varName, false);
        }

        throw new RuntimeException("Variable '" + varName + "' not found");
    }
}