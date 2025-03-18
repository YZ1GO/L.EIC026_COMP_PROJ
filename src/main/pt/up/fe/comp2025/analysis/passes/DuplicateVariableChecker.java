package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class DuplicateVariableChecker extends AnalysisVisitor {

    private final Set<String> declaredVariables;

    public DuplicateVariableChecker() {
        this.declaredVariables = new HashSet<>();
    }

    @Override
    public void buildVisitor() {
        addVisit(Kind.METHOD_DECL, this::visitMethodDecl);
        addVisit(Kind.VAR_DECL, this::visitVarDeclStmt);
    }

    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        String currentMethod = method.get("name");
        declaredVariables.clear();

        Optional<List<Symbol>> parametersOpt = Optional.ofNullable(table.getParameters(currentMethod));

        // Check for duplicate parameter names
        if (parametersOpt.isPresent()) {
            Set<String> parameterNames = new HashSet<>();
            for (var param : parametersOpt.get()) {
                String paramName = param.getName();
                if (parameterNames.contains(paramName)) {
                    addReport(newError(
                            method,
                            String.format("Duplicate parameter declaration: '%s' is already declared in method '%s'.", paramName, currentMethod))
                    );
                } else {
                    parameterNames.add(paramName);
                }
            }

            // Add method parameters to the declaredVariables set
            declaredVariables.addAll(parameterNames);
        }

        return null;
    }

    private Void visitVarDeclStmt(JmmNode varDeclStmt, SymbolTable table) {
        String varName = varDeclStmt.get("name");

        // Check for duplicate declarations in the current scope
        if (declaredVariables.contains(varName)) {
            addReport(newError(
                    varDeclStmt,
                    String.format("Duplicate variable declaration: '%s' is already declared in this scope.", varName))
            );
        } else {
            // Add the variable to the set of declared variables
            declaredVariables.add(varName);
        }

        return null;
    }
}