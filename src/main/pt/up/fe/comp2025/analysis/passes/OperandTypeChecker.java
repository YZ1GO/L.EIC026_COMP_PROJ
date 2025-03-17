package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;

public class OperandTypeChecker extends AnalysisVisitor {

    @Override
    protected void buildVisitor() {
        addVisit(Kind.BINARY_EXPR, this::visitBinaryExpr);
        addVisit(Kind.UNARY_NOT_EXPR, this::visitUnaryNotExpr);
    }

    private Void visitBinaryExpr(JmmNode binaryExpr, SymbolTable table) {
        var typeUtils = new TypeUtils(table);
        var op1 = binaryExpr.getChild(0);
        var op2 = binaryExpr.getChild(1);

        var op1_type = typeUtils.getExprType(op1);
        var op2_type = typeUtils.getExprType(op2);

        String operator = binaryExpr.get("op");

        if (op1_type.isArray() || op2_type.isArray()) {
            addReport(newError(binaryExpr, String.format("Operator '%s' cannot be applied to arrays", operator)));
            return null;
        }

        // && and || only work on booleans
        if ((operator.equals("&&") || operator.equals("||")) && (!op1_type.getName().equals("boolean") || !op2_type.getName().equals("boolean"))) {
            addReport(newError(binaryExpr, "Logical operator '" + operator + "' requires boolean operands, but found '" +
                    op1_type.getName() + "' and '" + op2_type.getName() + "'")
            );
            return null;
        }

        // +, -, * and / only work on integers
        if ((operator.equals("+") || operator.equals("-") ||
                operator.equals("*") || operator.equals("/")) &&
                (!op1_type.getName().equals("int") || !op2_type.getName().equals("int"))) {
            addReport(Report.newError(
                    Stage.SEMANTIC,
                    binaryExpr.getLine(),
                    binaryExpr.getColumn(),
                    "Arithmetic operator '" + operator + "' requires integer operands, but found '" +
                            op1_type.getName() + "' and '" + op2_type.getName() + "'",
                    null)
            );
            return null;
        }

        return null;
    }

    private Void visitUnaryNotExpr(JmmNode unaryNotExpr, SymbolTable table) {
        var typeUtils = new TypeUtils(table);
        var operand = unaryNotExpr.getChild(0);

        var operandType = typeUtils.getExprType(operand);

        // ! operator only works on booleans
        if (!operandType.getName().equals("boolean")) {
            addReport(Report.newError(
                    Stage.SEMANTIC,
                    unaryNotExpr.getLine(),
                    unaryNotExpr.getColumn(),
                    "Logical NOT operator '!' requires a boolean operand, but found '" + operandType.getName() + "'",
                    null)
            );
        }

        return null;
    }
}