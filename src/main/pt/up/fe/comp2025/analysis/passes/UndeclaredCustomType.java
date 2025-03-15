package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.specs.util.SpecsCheck;

import java.util.Arrays;
import java.util.List;

/**
 * Checks if custom types are declared or imported.
 */
public class UndeclaredCustomType extends AnalysisVisitor {

    private String currentClassName;
    private List<String> importedClasses;

    @Override
    public void buildVisitor() {
        addVisit(Kind.CLASS_DECL, this::visitClassDecl);
        addVisit(Kind.VAR_DECL, this::visitVarDecl);
    }

    private Void visitClassDecl(JmmNode classDecl, SymbolTable table) {
        currentClassName = table.getClassName();
        importedClasses = table.getImports();
        return null;
    }

    private Void visitVarDecl(JmmNode varDecl, SymbolTable table) {
        var typeName = varDecl.getChild(0).get("name");

        if (isPrimitiveOrString(typeName)) {
            return null;
        }

        if (typeName.equals(currentClassName)) {
            return null;
        }

        if (typeName.equals(table.getSuper())) {
            return null;
        }

        if (importedClasses.stream()
                .flatMap(importName -> Arrays.stream(importName.substring(1, importName.length() - 1).split(",")))
                .anyMatch(importName -> importName.trim().equals(typeName))) {
            return null;
        }

        addReport(Report.newError(
                Stage.SEMANTIC,
                varDecl.getLine(),
                varDecl.getColumn(),
                String.format("Custom type '%s' is not declared, imported, or part of the class hierarchy.", typeName),
                null)
        );

        return null;
    }

    private boolean isPrimitiveOrString(String typeName) {
        return typeName.equals("int") || typeName.equals("boolean") || typeName.equals("void") || typeName.equals("String");
    }
}