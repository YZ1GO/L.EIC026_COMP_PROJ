package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;

public class ArrayInitChecker extends AnalysisVisitor {

    private final TypeUtils typeUtils;

    public ArrayInitChecker(SymbolTable table) {
        this.typeUtils = new TypeUtils(table);
    }

    @Override
    protected void buildVisitor() {
        addVisit(Kind.ARRAY_INIT, this::visitArrayInit);
        addVisit(Kind.NEW_INT_ARRAY_EXPR, this::visitNewIntArrayExpr);
    }

    private Void visitArrayInit(JmmNode arrayInit, SymbolTable table) {
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
            addReport(Report.newError(
                    Stage.SEMANTIC,
                    arrayInit.getLine(),
                    arrayInit.getColumn(),
                    "Array initialization contains non-integer types.",
                    null)
            );
        }

        return null;
    }

    private Void visitNewIntArrayExpr(JmmNode newArrayExpr, SymbolTable table) {
        // Ensure the array size is provided and is of type `int`
        if (newArrayExpr.getNumChildren() != 1) {
            addReport(Report.newError(
                    Stage.SEMANTIC,
                    newArrayExpr.getLine(),
                    newArrayExpr.getColumn(),
                    "Array size must be specified as a single integer inside the brackets.",
                    null)
            );
            return null;
        }

        JmmNode sizeExpr = newArrayExpr.getChild(0);
        var sizeType = typeUtils.getExprType(sizeExpr);

        if (!sizeType.getName().equals("int") || sizeType.isArray()) {
            addReport(Report.newError(
                    Stage.SEMANTIC,
                    sizeExpr.getLine(),
                    sizeExpr.getColumn(),
                    "Array size must be of type 'int'.",
                    null)
            );
        }

        return null;
    }
}