package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;

import java.util.Arrays;

public class ClassNameConflictChecker extends AnalysisVisitor {

    @Override
    public void buildVisitor() {
        addVisit(Kind.CLASS_DECL, this::visitClassDecl);
    }

    private Void visitClassDecl(JmmNode classDecl, SymbolTable table) {
        String className = table.getClassName();
        
        if (table.getImports().isEmpty()) {
            return null;
        }

        if (table.getImports().stream()
                .flatMap(importName -> Arrays.stream(importName.substring(1, importName.length() - 1).split(",")))
                .map(String::trim)
                .anyMatch(importName -> importName.equals(className))) {
            addReport(newError(
                    classDecl,
                    String.format("Class name conflict: The class '%s' cannot have the same name as an imported class.", className))
            );
        }

        return null;
    }
}