package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;

import java.util.List;

public class ClassExtendsChecker extends AnalysisVisitor {

    @Override
    public void buildVisitor() {
        addVisit(Kind.CLASS_DECL, this::visitClassDecl);
    }

    private Void visitClassDecl(JmmNode classDecl, SymbolTable table) {
        if (classDecl.hasAttribute("extends")) {
            String extendedClass = classDecl.get("extends");

            List<String> imports = table.getImports();

            if (!imports.contains(extendedClass)) {
                addReport(Report.newError(
                        Stage.SEMANTIC,
                        classDecl.getLine(),
                        classDecl.getColumn(),
                        String.format("Class '%s' extends '%s', but '%s' is not imported.", classDecl.get("name"), extendedClass, extendedClass),
                        null)
                );
            }
        }

        return null;
    }
}