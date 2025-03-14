package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;

import java.util.List;

public class ClassNameConflictChecker extends AnalysisVisitor {

    @Override
    public void buildVisitor() {
        addVisit(Kind.CLASS_DECL, this::visitClassDecl);
    }

    private Void visitClassDecl(JmmNode classDecl, SymbolTable table) {
        String className = table.getClassName();

        List<String> imports = table.getImports();

        for (String importedClass : imports) {
            if (importedClass.equals(className)) {
                addReport(Report.newError(
                        Stage.SEMANTIC,
                        classDecl.getLine(),
                        classDecl.getColumn(),
                        String.format("Class name conflict: The class '%s' cannot have the same name as an imported class.", className),
                        null)
                );
                break;
            }
        }

        return null;
    }
}