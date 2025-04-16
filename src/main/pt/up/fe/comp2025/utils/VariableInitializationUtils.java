package pt.up.fe.comp2025.utils;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.ast.Kind;

import java.util.List;

import static pt.up.fe.comp2025.ast.Kind.PARAM;

public class VariableInitializationUtils {

    private static boolean isMethodParameter(String varName, JmmNode methodNode) {
        List<JmmNode> params = methodNode.getChildren(PARAM);

        return params.stream()
                .anyMatch(param -> param.get("name").equals(varName));
    }

    public static boolean isVariableInitialized(String varName, JmmNode methodNode) {
        if (isMethodParameter(varName, methodNode)) {
            return true;
        }
        return isVariableInitializedInStatements(varName, methodNode.getChildren());
    }

    private static boolean isVariableInitializedInStatements(String varName, List<JmmNode> statements) {
        for (JmmNode stmt : statements) {
            String kind = stmt.getKind();

            switch (kind) {
                case "AssignStmt" -> {
                    JmmNode lhs = stmt.getChild(0);
                    JmmNode rhs = stmt.getChild(1);

                    // Check if the left-hand side is the variable being assigned
                    if (lhs.getKind().equals("VarRefExpr") && lhs.get("name").equals(varName)) {
                        // Handle self-assignment (e.g., s = s)
                        if (rhs.getKind().equals("VarRefExpr") && rhs.get("name").equals(varName)) {
                            return false; // Self-assignment without prior initialization
                        }

                        // Check if the right-hand side uses the variable before initialization
                        if (rhs.getDescendants("VarRefExpr").stream()
                                .anyMatch(varRef -> varRef.get("name").equals(varName))) {
                            return false; // Variable is used before being initialized
                        }
                        return true; // Variable is initialized
                    }
                }

                case "ArrayAssignStmt" -> {
                    JmmNode arrayRef = stmt.get("name") != null ? stmt : stmt.getChild(0);
                    if (arrayRef.getKind().equals("VarRefExpr") && arrayRef.get("name").equals(varName)) {
                        return true;
                    }
                }

                case "IfStmt" -> {
                    JmmNode thenStmt = stmt.getChild(1);
                    JmmNode elseStmt = stmt.getNumChildren() > 2 ? stmt.getChild(2) : null;

                    boolean thenInit = isVariableInitializedInStatement(varName, thenStmt);
                    boolean elseInit = elseStmt != null && isVariableInitializedInStatement(varName, elseStmt);

                    if (thenInit && elseInit) return true;
                }

                case "WhileStmt" -> {
                    JmmNode condition = stmt.getChild(0);
                    JmmNode body = stmt.getChild(1);

                    // Check initialization in the condition and body
                    boolean conditionInit = isVariableInitializedInStatement(varName, condition);
                    boolean bodyInit = isVariableInitializedInStatements(varName, body.getChildren());

                    if (conditionInit || bodyInit) {
                        return true;
                    }
                }

                default -> {
                    if (kind.equals("BlockStmt")) {
                        if (isVariableInitializedInStatements(varName, stmt.getChildren())) {
                            return true;
                        }
                    } else {
                        if (stmt.getNumChildren() > 0 && isVariableInitializedInStatements(varName, stmt.getChildren())) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private static boolean isVariableInitializedInStatement(String varName, JmmNode stmt) {
        String kind = stmt.getKind();

        if (kind.equals("BlockStmt")) {
            return isVariableInitializedInStatements(varName, stmt.getChildren());
        } else {
            return isVariableInitializedInStatements(varName, List.of(stmt));
        }
    }

    public static JmmNode findArrayInitialization(JmmNode methodNode, String arrayName) {
        // Traverse all children of the method node to find the initialization
        for (JmmNode child : methodNode.getDescendants()) {
            if (child.getKind().equals(Kind.ASSIGN_STMT.toString())) {
                JmmNode lhs = child.getChild(0);
                if (lhs.getKind().equals(Kind.VAR_REF_EXPR.toString()) && lhs.get("name").equals(arrayName)) {
                    JmmNode rhs = child.getChild(1);
                    if (rhs.getKind().equals(Kind.NEW_INT_ARRAY_EXPR.toString())) {
                        return rhs;
                    }
                }
            }
        }
        return null;
    }
}