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

        // Check if the method has a return statement if it's not void
        if (!returnType.getName().equals("void")) {
            if (method.getChildren(Kind.RETURN_STMT).isEmpty()) {
                addReport(Report.newError(
                        Stage.SEMANTIC,
                        method.getLine(),
                        method.getColumn(),
                        "Non-void method '" + currentMethod + "' must have a return statement.",
                        null)
                );
            }
            if (method.getChildren(Kind.RETURN_STMT).size() > 1) {
                addReport(Report.newError(
                        Stage.SEMANTIC,
                        method.getLine(),
                        method.getColumn(),
                        "Method '" + currentMethod + "' must have only one return statement.",
                        null)
                );
            }
        }

        // Check if the return statement is the last statement in the method
        JmmNode lastChild = method.getChildren().getLast();
        if (!lastChild.getKind().equals(Kind.RETURN_STMT.toString()) && !returnType.getName().equals("void")) {
            addReport(Report.newError(
                    Stage.SEMANTIC,
                    lastChild.getLine(),
                    lastChild.getColumn(),
                    "Return statement in method '" + currentMethod + "' must be the last statement.",
                    null)
            );
        }

        return null;
    }

    private Void visitReturnStmt(JmmNode returnStmt, SymbolTable table) {
        if (returnType.getName().equals("void")) {
            addReport(Report.newError(
                    Stage.SEMANTIC,
                    returnStmt.getLine(),
                    returnStmt.getColumn(),
                    "Void method '" + currentMethod + "' should not have a return statement.",
                    null)
            );
        } else {
            JmmNode exprNode = returnStmt.getChildren().getFirst();
            Type exprType = typeUtils.getExprType(exprNode);

            /*if (exprNode.getKind().equals(Kind.VAR_REF_EXPR.toString())) {
                String varName = exprNode.get("name");

                // Check if the variable is initialized
                JmmNode methodNode = returnStmt.getAncestor(Kind.METHOD_DECL.toString()).orElse(null);
                if (methodNode != null && !isVariableInitialized(varName, methodNode)) {
                    addReport(Report.newError(
                            Stage.SEMANTIC,
                            returnStmt.getLine(),
                            returnStmt.getColumn(),
                            String.format("Variable '%s' is not initialized before being returned.", varName),
                            null)
                    );
                    return null;
                }
            }*/

            if (exprType == null) {
                addReport(Report.newError(
                        Stage.SEMANTIC,
                        returnStmt.getLine(),
                        returnStmt.getColumn(),
                        String.format("Class '%s' is not declared, imported, or part of the class hierarchy.", exprNode.get("name")),
                        null)
                );
            } else if (!returnType.equals(exprType)) {
                addReport(Report.newError(
                        Stage.SEMANTIC,
                        returnStmt.getLine(),
                        returnStmt.getColumn(),
                        "Return type of method '" + currentMethod + "' does not match the declared return type. Expected: " + returnType + ", found: " + exprType,
                        null)
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
}