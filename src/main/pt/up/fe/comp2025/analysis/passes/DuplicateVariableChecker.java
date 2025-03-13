package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;

import java.util.HashSet;
import java.util.Set;

public class DuplicateVariableChecker extends AnalysisVisitor {

    private String currentMethod;
    private Set<String> declaredVariables;

    public DuplicateVariableChecker() {
        this.declaredVariables = new HashSet<>();
    }

    @Override
    public void buildVisitor() {
        addVisit(Kind.METHOD_DECL, this::visitMethodDecl);
        addVisit(Kind.VAR_DECL, this::visitVarDeclStmt);
    }

    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        currentMethod = method.get("name");
        declaredVariables.clear(); // Clear the set for the new method

        // Check for duplicate parameter names
        if (table.getParameters(currentMethod) != null) {
            Set<String> parameterNames = new HashSet<>();
            for (var param : table.getParameters(currentMethod)) {
                String paramName = param.getName();
                if (parameterNames.contains(paramName)) {
                    // Report duplicate parameter error
                    addReport(Report.newError(
                            Stage.SEMANTIC,
                            method.getLine(),
                            method.getColumn(),
                            String.format("Duplicate parameter declaration: '%s' is already declared in method '%s'.", paramName, currentMethod),
                            null)
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
        // Get the name of the variable being declared
        String varName = varDeclStmt.get("name");

        // Check for duplicate declarations in the current scope
        if (declaredVariables.contains(varName)) {
            addReport(Report.newError(
                    Stage.SEMANTIC,
                    varDeclStmt.getLine(),
                    varDeclStmt.getColumn(),
                    String.format("Duplicate variable declaration: '%s' is already declared in this scope.", varName),
                    null)
            );
        } else {
            // Add the variable to the set of declared variables
            declaredVariables.add(varName);
        }

        return null;
    }
}