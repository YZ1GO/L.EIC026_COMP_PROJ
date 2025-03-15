package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;

public class VoidTypeChecker extends AnalysisVisitor {

    @Override
    public void buildVisitor() {
        addVisit(Kind.VAR_DECL, this::visitVarDecl);
    }

    private Void visitVarDecl(JmmNode varDecl, SymbolTable table) {
        String typeName = varDecl.getChild(0).get("name");

        if (typeName.equals("void")) {
            addReport(Report.newError(
                    Stage.SEMANTIC,
                    varDecl.getLine(),
                    varDecl.getColumn(),
                    "Variables cannot be declared with type 'void'.",
                    null)
            );
        }

        return null;
    }
}