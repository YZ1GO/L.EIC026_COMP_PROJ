package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp2025.utils.VariableInitializationUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class AssignTypeChecker extends AnalysisVisitor {
    @Override
    public void buildVisitor() {
        addVisit(Kind.ASSIGN_STMT, this::visitAssignStmt);
        addVisit(Kind.THIS_EXPR, this::visitThisExpr);
        addVisit(Kind.ARRAY_ASSIGN_STMT, this::visitArrayAssignStmt);
    }

    private Void visitArrayAssignStmt(JmmNode arrayAssignStmt, SymbolTable table) {
        var typeUtils = new TypeUtils(table);

        String arrayVarName = arrayAssignStmt.get("name");

        JmmNode indexExpr = arrayAssignStmt.getChild(0);
        JmmNode valueExpr = arrayAssignStmt.getChild(1);

        // Check if the array variable is initialized before being used
        JmmNode methodNode = arrayAssignStmt.getAncestor(Kind.METHOD_DECL.toString()).orElse(null);
        if (methodNode != null && !VariableInitializationUtils.isVariableInitialized(arrayVarName, methodNode)) {
            addReport(newError(
                    arrayAssignStmt,
                    String.format("Array variable '%s' is used before being initialized.", arrayVarName)
            ));
        }

        // Check if variables in the index expression are initialized
        List<JmmNode> indexVars = indexExpr.getDescendants(Kind.VAR_REF_EXPR.toString());
        for (JmmNode var : indexVars) {
            String usedVarName = var.get("name");
            if (methodNode != null && !VariableInitializationUtils.isVariableInitialized(usedVarName, methodNode)) {
                addReport(newError(
                        var,
                        String.format("Variable '%s' is used in the array index before being initialized.", usedVarName)
                ));
            }
        }

        // Check if variables in the value expression are initialized
        List<JmmNode> valueVars = valueExpr.getDescendants(Kind.VAR_REF_EXPR.toString());
        for (JmmNode var : valueVars) {
            String usedVarName = var.get("name");
            if (methodNode != null && !VariableInitializationUtils.isVariableInitialized(usedVarName, methodNode)) {
                addReport(newError(
                        var,
                        String.format("Variable '%s' is used in the array value before being initialized.", usedVarName)
                ));
            }
        }

        // Type checking logic
        Type arrayType = typeUtils.getExprType(arrayAssignStmt);
        Type indexType = typeUtils.getExprType(indexExpr);
        Type valueType = typeUtils.getExprType(valueExpr);

        if (!arrayType.isArray()) {
            addReport(newError(
                    arrayAssignStmt,
                    String.format("Variable '%s' is not an array but is used in an array assignment.", arrayVarName)
            ));
            return null;
        }

        if (!indexType.equals(TypeUtils.newIntType())) {
            addReport(newError(
                    indexExpr,
                    String.format("Index expression must be of type 'int' but found '%s'.", formatType(indexType))
            ));
        }

        Type baseArrayType = new Type(arrayType.getName(), false);

        if (!isTypeCompatible(baseArrayType, valueType, table)) {
            addReport(newError(
                    valueExpr,
                    String.format("Type mismatch: cannot assign '%s' to array of '%s'.",
                            formatType(valueType), formatType(baseArrayType))
            ));
        }

        return null;
    }

    private Void visitAssignStmt(JmmNode assignStmt, SymbolTable table) {
        var typeUtils = new TypeUtils(table);
        JmmNode varRef = assignStmt.getChild(0);
        JmmNode expr = assignStmt.getChild(1);

        // Check if the left-hand side variable is initialized before being used
        String varName = varRef.get("name");
        JmmNode methodNode = assignStmt.getAncestor(Kind.METHOD_DECL.toString()).orElse(null);

        if (methodNode != null && expr.getKind().equals(Kind.VAR_REF_EXPR.toString())) {
            String rhsVarName = expr.get("name");
            if (rhsVarName.equals(varName) && !VariableInitializationUtils.isVariableInitialized(varName, methodNode)) {
                addReport(newError(
                        varRef,
                        String.format("Variable '%s' is used before being initialized.", varName)
                ));
            }
        }

        // Check for uninitialized variables in the right-hand side
        List<JmmNode> usedVars = expr.getDescendants(Kind.VAR_REF_EXPR.toString());
        for (JmmNode var : usedVars) {
            String usedVarName = var.get("name");

            if (methodNode != null && !VariableInitializationUtils.isVariableInitialized(usedVarName, methodNode)) {
                addReport(newError(
                        var,
                        String.format("Variable '%s' is used before being initialized.", usedVarName)
                ));
            }
        }

        // Type checking logic
        Type declaredType = typeUtils.getExprType(varRef);
        Type assignedType = typeUtils.getExprType(expr);

        if (assignedType == null) {
            addReport(newError(
                    expr,
                    String.format("Class '%s' is not declared, imported, or part of the class hierarchy.", expr.get("name")))
            );
            return null;
        }

        boolean isAssumedMethodCall = isMethodCallOnAssumedType(expr, typeUtils);

        if (!isAssumedMethodCall && !isTypeCompatible(declaredType, assignedType, table)) {
            String declaredTypeStr = formatType(declaredType);
            String assignedTypeStr = formatType(assignedType);

            addReport(newError(
                    assignStmt,
                    String.format("Type mismatch: cannot assign '%s' to variable '%s' of type '%s'.", assignedTypeStr, varRef.get("name"), declaredTypeStr))
            );
        }

        return null;
    }

    private boolean isMethodCallOnAssumedType(JmmNode expr, TypeUtils typeUtils) {
        if (!expr.getKind().equals(Kind.METHOD_CALL_EXPR.toString())) {
            return false;
        }

        JmmNode receiverNode = expr.getChild(0);
        Type receiverType = typeUtils.getExprType(receiverNode);

        return receiverType != null && typeUtils.isImportedOrExtendedOrInherited(receiverType);
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