package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.Arrays;

public class AssignTypeChecker extends AnalysisVisitor {

    private final TypeUtils typeUtils;
    private final SymbolTable symbolTable;

    public AssignTypeChecker(SymbolTable table) {
        this.typeUtils = new TypeUtils(table);
        this.symbolTable = table;
    }

    @Override
    public void buildVisitor() {
        addVisit(Kind.ASSIGN_STMT, this::visitAssignStmt);
    }

    private Void visitAssignStmt(JmmNode assignStmt, SymbolTable table) {
        JmmNode varRef = assignStmt.getChild(0);
        JmmNode expr = assignStmt.getChild(1);
    
        Type declaredType = typeUtils.getExprType(varRef);
        Type assignedType = typeUtils.getExprType(expr);

        if (assignedType == null) {
            addReport(Report.newError(
                    Stage.SEMANTIC,
                    expr.getLine(),
                    expr.getColumn(),
                    String.format("Class '%s' is not declared, imported, or part of the class hierarchy.", expr.get("name")),
                    null)
            );
            return null;
        }

        if (!isTypeCompatible(declaredType, assignedType)) {
            String declaredTypeStr = formatType(declaredType);
            String assignedTypeStr = formatType(assignedType);

            addReport(Report.newError(
                    Stage.SEMANTIC,
                    assignStmt.getLine(),
                    assignStmt.getColumn(),
                    String.format("Type mismatch: cannot assign '%s' to variable '%s' of type '%s'.", assignedTypeStr, varRef.get("name"), declaredTypeStr),
                    null)
            );
        }

        return null;
    }

    private boolean isTypeCompatible(Type declaredType, Type assignedType) {
        if (declaredType.isArray() != assignedType.isArray()) {
            return false;
        }

        if (declaredType.equals(assignedType)) {
            return true;
        }

        boolean declaredIsImported = isImported(declaredType.getName());
        boolean assignedIsImported = isImported(assignedType.getName());

        if (declaredIsImported && assignedIsImported) {
            return true;
        }

        if (isSubclass(assignedType.getName(), declaredType.getName())) {
            return true;
        }

        return false;
    }
    
    private boolean isImported(String typeName) {
        return symbolTable.getImports().stream()
                .flatMap(importName -> Arrays.stream(importName.substring(1, importName.length() - 1).split(",")))
                .anyMatch(importName -> importName.trim().equals(typeName));
    }

    private boolean isSubclass(String subclassName, String superclassName) {
        if (subclassName.equals(superclassName)) {
            return true;
        }

        String currentClass = subclassName;
        while (currentClass != null) {
            // Check if the current class extends the superclass
            if (currentClass.equals(superclassName)) {
                return true;
            }

            currentClass = getParentClass(currentClass);
        }

        return false;
    }

    private String getParentClass(String className) {
        if (className.equals(symbolTable.getClassName())) {
            return symbolTable.getSuper();
        }

        // Check if the className matches any imported class
        if (symbolTable.getImports().stream()
                .flatMap(importName -> Arrays.stream(importName.substring(1, importName.length() - 1).split(",")))
                .anyMatch(importName -> importName.trim().equals(className))) {
            return null;
        }


        return null;
    }

    private String formatType(Type type) {
        return type.getName() + (type.isArray() ? "[]" : "");
    }
}