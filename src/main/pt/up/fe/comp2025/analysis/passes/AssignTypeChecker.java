package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.Arrays;
import java.util.Optional;

public class AssignTypeChecker extends AnalysisVisitor {
    @Override
    public void buildVisitor() {
        addVisit(Kind.ASSIGN_STMT, this::visitAssignStmt);
        addVisit(Kind.THIS_EXPR, this::visitThisExpr);
    }

    private Void visitAssignStmt(JmmNode assignStmt, SymbolTable table) {
        var typeUtils = new TypeUtils(table);
        JmmNode varRef = assignStmt.getChild(0);
        JmmNode expr = assignStmt.getChild(1);
    
        Type declaredType = typeUtils.getExprType(varRef);
        Type assignedType = typeUtils.getExprType(expr);


        if (assignedType == null) {
            addReport(newError(
                    expr,
                    String.format("Class '%s' is not declared, imported, or part of the class hierarchy.", expr.get("name")))
            );
            return null;
        }

        boolean isImportedMethodCall = isMethodCallOnImported(expr, table);

        if (!isImportedMethodCall && !isTypeCompatible(declaredType, assignedType, table)) {
            String declaredTypeStr = formatType(declaredType);
            String assignedTypeStr = formatType(assignedType);

            addReport(newError(
                    assignStmt,
                    String.format("Type mismatch: cannot assign '%s' to variable '%s' of type '%s'.", assignedTypeStr, varRef.get("name"), declaredTypeStr))
            );
        }

        return null;
    }

    private boolean isMethodCallOnImported(JmmNode expr, SymbolTable table) {
        var typeUtils = new TypeUtils(table);
        if (!expr.getKind().equals(Kind.METHOD_CALL_EXPR.toString())) {
            return false;
        }

        JmmNode objectNode = expr.getChild(0);
        Type objectType = typeUtils.getExprType(objectNode);

        if (objectType == null) {
            return false;
        }

        String className = objectType.getName();
        return isImported(className, table);
    }
    
    private boolean isImported(String className, SymbolTable table) {
        return table.getImports().stream()
                .flatMap(importName -> Arrays.stream(importName.substring(1, importName.length() - 1).split(",")))
                .map(String::trim)
                .anyMatch(imported -> imported.equals(className));
    }

    private boolean isTypeCompatible(Type declaredType, Type assignedType, SymbolTable table) {
        if (declaredType.isArray() != assignedType.isArray()) {
            return false;
        }

        if (declaredType.equals(assignedType)) {
            return true;
        }

        boolean declaredIsImported = isImported(declaredType.getName(), table);
        boolean assignedIsImported = isImported(assignedType.getName(), table);

        if (declaredIsImported && assignedIsImported) {
            return true;
        }

        if (declaredIsImported && (assignedType.getName().equals(table.getClassName())) && (table.getSuper() != null)) {
            return true;
        }

        if (isSubclass(assignedType.getName(), declaredType.getName(), table)) {
            return true;
        }

        return false;
    }

    private boolean isSubclass(String subclassName, String superclassName, SymbolTable table) {
        if (subclassName.equals(superclassName)) {
            return true;
        }

        String currentClass = subclassName;
        while (currentClass != null) {
            // Check if the current class extends the superclass
            if (currentClass.equals(superclassName)) {
                return true;
            }

            currentClass = getParentClass(currentClass, table);
        }

        return false;
    }

    private String getParentClass(String className, SymbolTable table) {
        if (className.equals(table.getClassName())) {
            return table.getSuper();
        }

        // Check if the className matches any imported class
        if (table.getImports().stream()
                .flatMap(importName -> Arrays.stream(importName.substring(1, importName.length() - 1).split(",")))
                .anyMatch(importName -> importName.trim().equals(className))) {
            return null;
        }


        return null;
    }

    private String formatType(Type type) {
        return type.getName() + (type.isArray() ? "[]" : "");
    }

    private Void visitThisExpr(JmmNode thisExpr, SymbolTable table) {
        // Check if the "this" expression is used in a static context
        Optional<JmmNode> parentMethodOpt = thisExpr.getAncestor(Kind.METHOD_DECL);

        if (parentMethodOpt.isPresent()) {
            JmmNode parentMethod = parentMethodOpt.get();
            if (parentMethod.get("isStatic").equals("true")) {
                addReport(newError(
                        thisExpr,
                        "'this' cannot be used in a static method.")
                );
            }
        }

        return null;
    }
}