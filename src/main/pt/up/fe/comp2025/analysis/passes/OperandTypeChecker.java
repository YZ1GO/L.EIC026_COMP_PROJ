package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;

public class OperandTypeChecker extends AnalysisVisitor {

    private final TypeUtils typeUtils;

    public OperandTypeChecker(SymbolTable table) {
        this.typeUtils = new TypeUtils(table);
    }

    @Override
    public void buildVisitor() {
        addVisit(Kind.METHOD_DECL, this::visitMethodDecl);  // Add this to track the current method
        addVisit(Kind.BINARY_EXPR, this::visitBinaryExpr);
    }

    private Void visitMethodDecl(JmmNode methodDecl, SymbolTable table) {
        // Set the current method in TypeUtils
        String methodName = methodDecl.get("name");
        typeUtils.setCurrentMethod(methodName);
        return null;
    }

    private Void visitBinaryExpr(JmmNode binaryExpr, SymbolTable table) {
        // Get the operator
        String operator = binaryExpr.get("op");

        // Get the left and right operands
        JmmNode leftOperand = binaryExpr.getChildren().get(0);
        JmmNode rightOperand = binaryExpr.getChildren().get(1);

        // Get the types of the left and right operands using TypeUtils
        Type leftType = typeUtils.getExprType(leftOperand);
        Type rightType = typeUtils.getExprType(rightOperand);

        // Check if the types are compatible with the operator
        if (!areTypesCompatible(operator, leftType, rightType)) {
            String message = String.format("Incompatible types for operator '%s': %s and %s",
                    operator, leftType.getName(), rightType.getName());
            addReport(Report.newError(
                    Stage.SEMANTIC,
                    binaryExpr.getLine(),
                    binaryExpr.getColumn(),
                    message,
                    null)
            );
        }

        return null;
    }

    private boolean areTypesCompatible(String operator, Type leftType, Type rightType) {
        switch (operator) {
            case "+":
            case "-":
            case "*":
            case "/":
                // These operators require both operands to be integers (not arrays)
                return leftType.getName().equals("int") && !leftType.isArray() &&
                        rightType.getName().equals("int") && !rightType.isArray();
            case "<":
            case ">":
            case "<=":
            case ">=":
            case "==":
            case "!=":
                // These operators require both operands to be of the same type (and not arrays)
                return leftType.getName().equals(rightType.getName()) &&
                        !leftType.isArray() && !rightType.isArray();
            case "&&":
            case "||":
                // These operators require both operands to be booleans
                return leftType.getName().equals("boolean") && rightType.getName().equals("boolean");
            default:
                return false;
        }
    }
}