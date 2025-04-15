package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;

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

        var arrayNode = arrayAccessExpr.getChild(0);
        System.out.println("Array Node: " + arrayNode);

        var arrayInitNodeOpt = arrayNode.getAncestor(Kind.NEW_INT_ARRAY_EXPR);
        System.out.println("Array Init Node Optional: " + arrayInitNodeOpt);

        if (arrayInitNodeOpt.isPresent()) {
            JmmNode arrayInitNode = arrayInitNodeOpt.get();
            System.out.println("Array Init Node: " + arrayInitNode);

            JmmNode sizeExpr = arrayInitNode.getChild(0);
            System.out.println("Size Expression Node: " + sizeExpr);

            var sizeType = typeUtils.getExprType(sizeExpr);
            System.out.println("Size Type: " + sizeType);

            if (sizeType.getName().equals("int")) {
                int arraySize = Integer.parseInt(sizeExpr.get("value"));
                System.out.println("Array Size: " + arraySize);

                // Retrieve the index value (if it's a literal)
                if (arrayAccessExpr.getChild(1).getKind().equals(Kind.INTEGER_LITERAL.toString())) {
                    int indexValue = Integer.parseInt(arrayAccessExpr.getChild(1).get("value"));
                    System.out.println("Index Value: " + indexValue);

                    if (indexValue < 0 || indexValue >= arraySize) {
                        System.out.println("Index out of bounds: " + indexValue);
                        addReport(newError(arrayAccessExpr, String.format("Array index %d is out of bounds (size: %d)", indexValue, arraySize)));
                    }
                }
            }
        }

        return null;
    }
}
