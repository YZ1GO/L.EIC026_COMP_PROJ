package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;

public class VarargsDeclarationChecker extends AnalysisVisitor {

    @Override
    public void buildVisitor() {
        addVisit(Kind.VAR_DECL, this::visitVarDecl);
    }

    private Void visitVarDecl(JmmNode varDeclNode, SymbolTable table) {
        if (isVarargs(varDeclNode)) {
            addReport(Report.newError(
                    Stage.SEMANTIC,
                    varDeclNode.getLine(),
                    varDeclNode.getColumn(),
                    "Variables cannot be declared as varargs.",
                    null
            ));
        }
        return null;
    }

    private boolean isVarargs(JmmNode declNode) {
        JmmNode typeNode = declNode.getChild(0);

        Object isVarArgsObject = typeNode.getObject("isVarArgs");
        return isVarArgsObject instanceof Boolean && (Boolean) isVarArgsObject;
    }
}