package pt.up.fe.comp2025.ast;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.symboltable.JmmSymbolTable;

/**
 * Utility methods regarding types.
 */
public class TypeUtils {

    private final JmmSymbolTable table;
    private String currentMethod;  // Add this field

    public TypeUtils(SymbolTable table) {
        this.table = (JmmSymbolTable) table;
    }

    // Setter for currentMethod
    public void setCurrentMethod(String currentMethod) {
        this.currentMethod = currentMethod;
    }

    public static Type newIntType() {
        return new Type("int", false);
    }

    public static Type convertType(JmmNode typeNode) {
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
        switch (expr.getKind()) {
            case "IntegerLiteral":
                return newIntType();  // int
            case "BooleanLiteral":
                return new Type("boolean", false);  // boolean
            case "VarRefExpr":
                String varName = expr.get("name");
                // Check if the variable is a parameter
                if (table.getParameters(currentMethod).stream().anyMatch(param -> param.getName().equals(varName))) {
                    return table.getParameters(currentMethod).stream()
                            .filter(param -> param.getName().equals(varName))
                            .findFirst()
                            .get()
                            .getType();
                }
                // Check if the variable is a local variable
                if (table.getLocalVariables(currentMethod).stream().anyMatch(var -> var.getName().equals(varName))) {
                    return table.getLocalVariables(currentMethod).stream()
                            .filter(var -> var.getName().equals(varName))
                            .findFirst()
                            .get()
                            .getType();
                }
                // Check if the variable is a field
                if (table.getFields().stream().anyMatch(field -> field.getName().equals(varName))) {
                    return table.getFields().stream()
                            .filter(field -> field.getName().equals(varName))
                            .findFirst()
                            .get()
                            .getType();
                }
                return new Type("unknown", false);  // Unknown type
            case "BinaryExpr":
                // For simplicity, assume the type of a binary expression is the type of the left operand
                return getExprType(expr.getChildren().get(0));
            default:
                return new Type("unknown", false);  // Unknown type
        }
    }
}