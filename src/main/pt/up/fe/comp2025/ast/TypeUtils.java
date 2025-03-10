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

    public TypeUtils(SymbolTable table) {
        this.table = (JmmSymbolTable) table;
    }

    public static Type newIntType() {
        return new Type("int", false);
    }

    public static Type newStringType() {
        return new Type("String", false);
    }

    public static Type newBooleanType() {
        return new Type("boolean", false);
    }

    public static Type newDoubleType() {
        return new Type("double", false);
    }

    public static Type newFloatType() {
        return new Type("float", false);
    }

    public static Type newArrayType(Type baseType) {
        return new Type(baseType.getName(), true);
    }
    
    public static Type convertType(JmmNode typeNode) {

        // TODO: When you support new types, this must be updated
        // DONE: Updated to support new types
        String name = typeNode.get("name");
        boolean isArray = !typeNode.getChildren().isEmpty();

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
        return new Type("int", false);
    }


}
