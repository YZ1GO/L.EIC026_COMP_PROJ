package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;

public class WhileTypeChecker extends AnalysisVisitor {

    @Override
    protected void buildVisitor() {
        addVisit(Kind.WHILE_STMT, this::visitWhileStmt);
    }

    private Void visitWhileStmt(JmmNode whileStmt, SymbolTable table) {
        var typeUtils = new TypeUtils(table);

        var exprType = typeUtils.getExprType(whileStmt.getChild(0));
        if (!exprType.getName().equals("boolean")) {
            addReport(Report.newError(
                    Stage.SEMANTIC,
                    whileStmt.getLine(),
                    whileStmt.getColumn(),
                    "While statement must evaluate to boolean value",
                    null)
            );
        }

        return null;
    }
}
