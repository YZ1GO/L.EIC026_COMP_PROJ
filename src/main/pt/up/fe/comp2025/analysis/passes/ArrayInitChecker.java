package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;

public class ArrayInitChecker extends AnalysisVisitor {
    @Override
    protected void buildVisitor() {
        addVisit(Kind.ARRAY_INIT, this::visitArrayInit);
        addVisit(Kind.NEW_INT_ARRAY_EXPR, this::visitNewIntArrayExpr);
    }

    private Void visitArrayInit(JmmNode arrayInit, SymbolTable table) {
        var typeUtils = new TypeUtils(table);
        // Check if all elements in the array initialization are integers
        if (arrayInit.getChildren().stream()
                .allMatch(c -> c.getKind().equals(Kind.INTEGER_LITERAL.toString()))) {
            return null;
        }

        var isAllInt = true;
        for (var c : arrayInit.getChildren()) {
            var type = typeUtils.getExprType(c).getName();
            if (!type.equals("int")) {
                isAllInt = false;
                break;
            }
        }

        if (!isAllInt) {
            addReport(newError(
                    arrayInit,
                    "Array initialization contains non-integer types.")
            );
        }

        return null;
    }

    private Void visitNewIntArrayExpr(JmmNode newArrayExpr, SymbolTable table) {
        var typeUtils = new TypeUtils(table);
        // Ensure the array size is provided and is of type `int`
        if (newArrayExpr.getNumChildren() != 1) {
            addReport(newError(
                    newArrayExpr,
                    "Array size must be specified as a single integer inside the brackets.")
            );
            return null;
        }

        JmmNode sizeExpr = newArrayExpr.getChild(0);
        var sizeType = typeUtils.getExprType(sizeExpr);

        if (!sizeType.getName().equals("int") || sizeType.isArray()) {
            addReport(newError(
                    sizeExpr,
                    "Array size must be of type 'int'.")
            );
        }

        return null;
    }
}