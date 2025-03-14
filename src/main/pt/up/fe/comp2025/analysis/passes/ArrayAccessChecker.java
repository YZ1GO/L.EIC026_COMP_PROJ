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

        return null;
    }


}
