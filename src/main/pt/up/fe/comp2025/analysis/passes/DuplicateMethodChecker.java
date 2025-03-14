package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;

import java.util.HashSet;
import java.util.Set;

public class DuplicateMethodChecker extends AnalysisVisitor {

    private final Set<String> methodNames;

    public DuplicateMethodChecker() {
        this.methodNames = new HashSet<>();
    }

    @Override
    public void buildVisitor() {
        addVisit(Kind.METHOD_DECL, this::visitMethodDecl);
    }

    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        String methodName = method.get("name");

        if (methodNames.contains(methodName)) {
            addReport(Report.newError(
                    Stage.SEMANTIC,
                    method.getLine(),
                    method.getColumn(),
                    String.format("Duplicate method declaration: '%s' is already defined in this class.", methodName),
                    null)
            );
        } else {
            methodNames.add(methodName);
        }

        return null;
    }
}