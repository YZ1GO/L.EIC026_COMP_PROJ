package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp2025.ast.TypeUtils;

import java.util.List;

public class ReturnChecker extends AnalysisVisitor {

    private String currentMethod;
    private Type returnType;
    private final TypeUtils typeUtils;

    public ReturnChecker(SymbolTable symbolTable) {
        this.typeUtils = new TypeUtils(symbolTable);
    }

    @Override
    public void buildVisitor() {
        addVisit(Kind.METHOD_DECL, this::visitMethodDecl);
        addVisit(Kind.RETURN_STMT, this::visitReturnStmt);
    }

    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        currentMethod = method.get("name");
        JmmNode returnTypeNode = method.getChildren().getFirst();
        returnType = TypeUtils.convertType(returnTypeNode);

        // Check if the return type is varargs
        if (isVarargs(returnTypeNode)) {
            addReport(newError(
                    method,
                    "Method '" + currentMethod + "' cannot have a varargs return type.")
            );
        }

        // Check if the method has a return statement if it's not void
        if (!returnType.getName().equals("void")) {
            if (method.getChildren(Kind.RETURN_STMT).isEmpty()) {
                addReport(newError(
                        method,
                        "Non-void method '" + currentMethod + "' must have a return statement.")
                );
            }
            if (method.getChildren(Kind.RETURN_STMT).size() > 1) {
                addReport(newError(
                        method,
                        "Method '" + currentMethod + "' must have only one return statement.")
                );
            }
        }

        // Check if the return statement is the last statement in the method
        JmmNode lastChild = method.getChildren().getLast();
        if (!lastChild.getKind().equals(Kind.RETURN_STMT.toString()) && !returnType.getName().equals("void")) {
            addReport(newError(
                    lastChild,
                    "Return statement in method '" + currentMethod + "' must be the last statement.")
            );
        }

        return null;
    }

    private Void visitReturnStmt(JmmNode returnStmt, SymbolTable table) {
        if (returnType.getName().equals("void")) {
            addReport(newError(
                    returnStmt,
                    "Void method '" + currentMethod + "' should not have a return statement.")
            );
        } else {
            JmmNode exprNode = returnStmt.getChildren().getFirst();
            Type exprType = typeUtils.getExprType(exprNode);

            if (exprNode.getKind().equals(Kind.VAR_REF_EXPR.toString())) {
                String varName = exprNode.get("name");

                // Check if the variable is a parameter or a field
                boolean isParameter = table.getParameters(currentMethod).stream()
                        .anyMatch(param -> param.getName().equals(varName));
                boolean isField = table.getFields().stream()
                        .anyMatch(field -> field.getName().equals(varName));
    
                // If the variable is not a parameter or a field, check if it is initialized in the method
                if (!isParameter && !isField) {
                    JmmNode methodNode = returnStmt.getAncestor(Kind.METHOD_DECL.toString()).orElse(null);
                    if (methodNode != null && !isVariableInitialized(varName, methodNode)) {
                        addReport(newError(
                                returnStmt,
                                String.format("Variable '%s' is not initialized before being returned.", varName)
                                )
                        );
                        return null;
                    }
                }
            }

            if (exprType == null) {
                addReport(newError(
                        returnStmt,
                        String.format("Class '%s' is not declared, imported, or part of the class hierarchy.", exprNode.get("name")))
                );
            } else if (!returnType.equals(exprType)) {
                addReport(newError(
                        returnStmt,
                        "Return type of method '" + currentMethod + "' does not match the declared return type. Expected: " + returnType + ", found: " + exprType)
                );
            }
        }
        return null;
    }

    private boolean isVariableInitialized(String varName, JmmNode methodNode) {
        List<JmmNode> statements = methodNode.getChildren();

        boolean isInitialized = false;

        for (JmmNode statement : statements) {
            if (statement.getKind().equals(Kind.ASSIGN_STMT.toString())) {
                JmmNode varRef = statement.getChild(0);

                // Check if the assignment is to the variable we're tracking
                if (varRef.getKind().equals(Kind.VAR_REF_EXPR.toString()) && varRef.get("name").equals(varName)) {
                    isInitialized = true;
                }
            }

            // If we encounter the return statement, stop checking further
            if (statement.getKind().equals(Kind.RETURN_STMT.toString())) {
                break;
            }
        }

        return isInitialized;
    }

    private boolean isVarargs(JmmNode typeNode) {
        Object isVarArgsObject = typeNode.getObject("isVarArgs");
        return isVarArgsObject instanceof Boolean && (Boolean) isVarArgsObject;
    }
}