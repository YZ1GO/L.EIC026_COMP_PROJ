package pt.up.fe.comp2025.optimization;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;

import java.util.Collections;

import static pt.up.fe.comp2025.ast.Kind.*;

public class ConstantFoldingVisitor extends AJmmVisitor<Void, Boolean> {
    private boolean isChanged = false;

    @Override
    protected void buildVisitor() {
        addVisit(BINARY_EXPR, this::visitBinaryExpr);
        setDefaultVisit(this::defaultVisit);
    }

    private Boolean visitBinaryExpr(JmmNode node, Void unused) {
        JmmNode left = node.getChild(0);
        JmmNode right = node.getChild(1);

        boolean leftChanged = visit(left);
        boolean rightChanged = visit(right);

        // Check if both children are integers
        if (left.isInstance(INTEGER_LITERAL) && right.isInstance(INTEGER_LITERAL)) {
            int leftValue = Integer.parseInt(left.get("value"));
            int rightValue = Integer.parseInt(right.get("value"));
            String operator = node.get("op");

            Object result = switch (operator) {
                case "+" -> leftValue + rightValue;
                case "-" -> leftValue - rightValue;
                case "*" -> leftValue * rightValue;
                case "/" -> rightValue != 0 ? leftValue / rightValue : 0; // Avoid division by zero
                case "<" -> leftValue < rightValue;
                case ">" -> leftValue > rightValue;
                case "<=" -> leftValue <= rightValue;
                case ">=" -> leftValue >= rightValue;
                case "==" -> leftValue == rightValue;
                case "!=" -> leftValue != rightValue;
                default -> throw new IllegalArgumentException("Unsupported operator: " + operator);
            };

            replaceWithLiteral(node, result);
            return true;
        }

        // Check if both children are boolean literals
        if (left.isInstance(BOOLEAN_LITERAL) && right.isInstance(BOOLEAN_LITERAL)) {
            boolean leftValue = Boolean.parseBoolean(left.get("value"));
            boolean rightValue = Boolean.parseBoolean(right.get("value"));
            String operator = node.get("op");

            boolean result = switch (operator) {
                case "&&" -> leftValue && rightValue;
                case "||" -> leftValue || rightValue;
                default -> throw new IllegalArgumentException("Unsupported operator: " + operator);
            };

            replaceWithLiteral(node, result);
            return true;
        }

        return leftChanged || rightChanged;
    }

    private void replaceWithLiteral(JmmNode node, Object value) {
        JmmNode parent = node.getParent();
        int index = parent.getChildren().indexOf(node);
        if (index == -1) return;

        JmmNode literal = new JmmNodeImpl(Collections.singletonList(
                value instanceof Integer ? INTEGER_LITERAL.toString() : BOOLEAN_LITERAL.toString()
        ));
        literal.put("value", value.toString());

        parent.removeChild(index);
        parent.add(literal, index);
        isChanged = true;
    }

    private Boolean defaultVisit(JmmNode node, Void unused) {
        return visitChildren(node);
    }

    private Boolean visitChildren(JmmNode node) {
        boolean anyChanged = false;
        for (JmmNode child : node.getChildren()) {
            anyChanged |= visit(child);
        }
        return anyChanged;
    }

    public boolean isChanged() {
        return isChanged;
    }
}