package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ClassExtendsChecker extends AnalysisVisitor {

    @Override
    public void buildVisitor() {
        addVisit(Kind.CLASS_DECL, this::visitClassDecl);
    }

    private Void visitClassDecl(JmmNode classDecl, SymbolTable table) {
        Optional<String> extendedClassOpt = Optional.ofNullable(table.getSuper());
    
        if (extendedClassOpt.isEmpty()) {
            return null;
        }

        String extendedClass = extendedClassOpt.get();

        if(table.getImports().stream()
                .flatMap(importName -> Arrays.stream(importName.substring(1, importName.length() - 1).split(",")))
                .anyMatch(importName -> importName.trim().equals(extendedClass))){
            return null;
        }

        addReport(newError(
                classDecl,
                String.format("Class '%s' extends '%s', but '%s' is not imported.", classDecl.get("name"), extendedClass, extendedClass))
        );

        return null;
    }
}