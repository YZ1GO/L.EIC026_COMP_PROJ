package pt.up.fe.comp2025.ast;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.symboltable.JmmSymbolTable;

import java.util.List;
import java.util.Optional;

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
    
    public static Type convertType(JmmNode typeNode) {

        // TODO: When you support new types, this must be updated
        // DONE: Updated to support new types
        String name = typeNode.get("name");
        boolean isArray = Boolean.parseBoolean(typeNode.get("isArray"));

        return new Type(name, isArray);
    }


    /**
     * Gets the {@link Type} of an arbitrary expression.
     *
     * @param expr
     * @return
     */
    public Type getExprType(JmmNode expr) {

        // TODO: Update when there are new types
        switch (expr.getKind()) {
            case "ParentExpr":
                return getExprType(expr.getChild(0));

            case "NewIntArrayExpr":
                return new Type("int", true);

            case "NewObjectExpr":
                return new Type(expr.get("name"), false);

            case "ArrayAccessExpr": {
                Type arrayType = getExprType(expr.getChild(0));
                if (!arrayType.isArray()) {
                    throw new RuntimeException("Array access on non-array type");
                }
                return new Type(arrayType.getName(), false);
            }

            case "LengthExpr", "IntegerLiteral":
                return newIntType();

            case "MethodCallExpr": {
                Type objectType = getExprType(expr.getChild(0));
                String methodName = expr.get("name");

                Type returnType = table.getReturnType(methodName);
                if (returnType == null) {
                    throw new RuntimeException("Method '" + methodName + "' not found in class " + objectType.getName());
                }
                return returnType;
            }

            case "ThisExpr":
                return new Type(table.getClassName(), false);

            case "UnaryNotExpr", "BooleanLiteral":
                return new Type("boolean", false);

            case "BinaryExpr": {
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

            case "VarRefExpr":
                return resolveVariableType(expr.get("name"));

                //TODO: Test cases for array literals
            case "ArrayLiteral": {
                List<JmmNode> elements = expr.getChildren();
                if (elements.isEmpty()) {
                    throw new RuntimeException("Empty array literals are not allowed. Provide at least one element or specify the array type explicitly.");
                }
                
                Type commonType = getExprType(elements.getFirst());
                for (JmmNode element : elements) {
                    Type elemType = getExprType(element);
                    if (!elemType.equals(commonType)) {
                        throw new RuntimeException("Inconsistent array literal types");
                    }
                }
                return new Type(commonType.getName(), true);
            }

            default:
                throw new UnsupportedOperationException("Unhandled expression type: " + expr.getKind());
        }
    }

    // TODO: handle imports
    private Type resolveVariableType(String varName) {
        // Check fields (class-level variables)
        Optional<Type> fieldType = table.getFields().stream()
                .filter(field -> field.getName().equals(varName))
                .findFirst()
                .map(Symbol::getType);

        if (fieldType.isPresent()) {
            return fieldType.get();
        }

        // Check parameters and locals (method-level variables)
        for (String methodName : table.getMethods()) {
            // Check parameters
            Optional<Type> paramType = table.getParameters(methodName).stream()
                    .filter(param -> param.getName().equals(varName))
                    .findFirst()
                    .map(Symbol::getType);

            if (paramType.isPresent()) {
                return paramType.get();
            }

            // Check locals
            Optional<Type> localType = table.getLocalVariables(methodName).stream()
                    .filter(local -> local.getName().equals(varName))
                    .findFirst()
                    .map(Symbol::getType);

            if (localType.isPresent()) {
                return localType.get();
            }
        }

        throw new RuntimeException("Variable '" + varName + "' not found");
    }
}
