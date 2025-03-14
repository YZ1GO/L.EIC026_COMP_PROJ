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
    }

    private Void visitArrayInit(JmmNode arrayInit, SymbolTable table) {

        if (arrayInit.getChildren().stream()
                .allMatch(c -> c.getKind().equals(Kind.INTEGER_LITERAL.toString()))){
            return null;
        }

        var typeUtils = new TypeUtils(table);
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
                    "Array init contains non-integer types",
                    null)
            );
        }

        return null;
    }
}
