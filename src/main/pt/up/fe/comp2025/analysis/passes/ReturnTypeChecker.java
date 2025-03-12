package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp2025.ast.TypeUtils;

/**
 * Ensures that methods have return statements that match their declared return type.
 */
public class ReturnTypeChecker extends AnalysisVisitor {

    private String currentMethod;
    private Type returnType;
    private final TypeUtils typeUtils;

    public ReturnTypeChecker(SymbolTable symbolTable) {
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
            if (!returnType.equals(exprType)) {
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
}