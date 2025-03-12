package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;

public class OperandTypeChecker extends AnalysisVisitor {

    @Override
    protected void buildVisitor() {
        addVisit(Kind.BINARY_EXPR, this::visitBinaryExpr);
    }

    private Void visitBinaryExpr(JmmNode binaryExpr, SymbolTable table) {
        var typeUtils = new TypeUtils(table);

        var op1 = binaryExpr.getChild(0);
        var op2 = binaryExpr.getChild(1);

        var op1_type = typeUtils.getExprType(op1);
        var op2_type = typeUtils.getExprType(op2);

        boolean hasArray = false;
        if (op1_type.getName().equals(op2_type.getName())) {
            if (!op1_type.isArray() && op2_type.isArray()) return null;
            hasArray = true;
        }

        String m = hasArray ? "Cannot operate on an array and an non-array" : String.format("Can't operate on different types ('%s' and '%s')", op1_type.getName(), op2_type.getName());
        addReport(Report.newError(
                Stage.SEMANTIC,
                binaryExpr.getLine(),
                binaryExpr.getColumn(),
                m,
                null)
        );

        return null;
    }
}