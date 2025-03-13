package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;
import pt.up.fe.comp.jmm.analysis.table.Type;

public class ConditionTypeChecker extends AnalysisVisitor {

    private final TypeUtils typeUtils;

    public ConditionTypeChecker(SymbolTable table) {
        this.typeUtils = new TypeUtils(table);
    }

    @Override
    public void buildVisitor() {
        addVisit(Kind.IF_STMT, this::visitConditionStmt);
        addVisit(Kind.WHILE_STMT, this::visitConditionStmt);
    }

    private Void visitConditionStmt(JmmNode conditionStmt, SymbolTable table) {
        JmmNode conditionExpr = conditionStmt.getChild(0);

        Type conditionType = typeUtils.getExprType(conditionExpr);

        if (!conditionType.getName().equals("boolean") || conditionType.isArray()) {
            addReport(Report.newError(
                    Stage.SEMANTIC,
                    conditionStmt.getLine(),
                    conditionStmt.getColumn(),
                    String.format("Type mismatch: condition expression must be of type 'boolean', but found '%s'.", conditionType.getName()),
                    null)
            );
        }

        return null;
    }
}