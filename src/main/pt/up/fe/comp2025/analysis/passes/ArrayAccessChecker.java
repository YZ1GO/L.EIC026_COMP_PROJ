package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;
import pt.up.fe.comp2025.utils.VariableInitializationUtils;

public class ArrayAccessChecker extends AnalysisVisitor {

    @Override
    protected void buildVisitor() {
        addVisit(Kind.ARRAY_ACCESS_EXPR, this::visitArrayAccessExpr);
    }

    private Void visitArrayAccessExpr(JmmNode arrayAccessExpr, SymbolTable table) {
        var typeUtils = new TypeUtils(table);

        var exprType = typeUtils.getExprType(arrayAccessExpr.getChild(0));
        if (!exprType.isArray()) {
            addReport(newError(arrayAccessExpr, String.format("%s is not an array", arrayAccessExpr.getChild(0).toString())));
        }

        var indexType = typeUtils.getExprType(arrayAccessExpr.getChild(1));
        if (!indexType.getName().equals("int") || indexType.isArray()) {
            addReport(newError(arrayAccessExpr, String.format("Array index must be an integer, but found '%s'", indexType.getName())));
        }

        // Find the initialization of the array
        var arrayNode = arrayAccessExpr.getChild(0);
        String arrayName = arrayNode.get("name");

        JmmNode methodNode = arrayAccessExpr.getAncestor(Kind.METHOD_DECL.toString()).orElse(null);
        if (methodNode == null) {
            // Method node not found for array access
            return null;
        }

        JmmNode initNode = VariableInitializationUtils.findArrayInitialization(methodNode, arrayName);
        if (initNode == null) {
            // Array initialization not found
            return null;
        }

        int arraySize = -1;
        if (initNode.getKind().equals(Kind.NEW_INT_ARRAY_EXPR.toString())) {
            JmmNode sizeExpr = initNode.getChild(0);
            if (typeUtils.isStaticallyEvaluable(sizeExpr)) {
                arraySize = typeUtils.evaluateExpression(sizeExpr);
            }
        } else if (initNode.getKind().equals(Kind.ARRAY_INIT.toString())) {
            arraySize = initNode.getChildren().size();
        }

        // Check if the index is within bounds
        if (arraySize != -1 && typeUtils.isStaticallyEvaluable(arrayAccessExpr.getChild(1))) {
            int indexValue = typeUtils.evaluateExpression(arrayAccessExpr.getChild(1));
            if (indexValue < 0 || indexValue >= arraySize) {
                addReport(newError(arrayAccessExpr, String.format("Array index %d is out of bounds (size: %d)", indexValue, arraySize)));
            }
        } else if (arrayAccessExpr.getChild(1).getKind().equals(Kind.LENGTH_EXPR.toString())) {
            // Handle cases where the index uses the 'length' property
            JmmNode lengthExpr = arrayAccessExpr.getChild(1);
            JmmNode arrayExpr = lengthExpr.getChild(0);

            if (!typeUtils.getExprType(arrayExpr).isArray()) {
                addReport(newError(lengthExpr, "'length' can only be used on arrays."));
            } else {
                addReport(newError(arrayAccessExpr, "Array index using 'length' is out of bounds (valid range: 0 to length-1)."));
            }
        }

        return null;
    }
}
