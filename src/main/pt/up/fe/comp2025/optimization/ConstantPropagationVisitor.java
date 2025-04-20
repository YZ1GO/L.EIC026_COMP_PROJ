package pt.up.fe.comp2025.optimization;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static pt.up.fe.comp2025.ast.Kind.*;

public class ConstantPropagationVisitor extends AJmmVisitor<Void, Boolean> {
    private Map<String, Integer> intConsts = new HashMap<>();
    private Map<String, Boolean> boolConsts = new HashMap<>();
    private boolean isChanged = false;

    @Override
    protected void buildVisitor() {
        addVisit(METHOD_DECL, this::visitMethodDecl);
        addVisit(ASSIGN_STMT, this::visitAssignStmt);
        addVisit(ARRAY_ASSIGN_STMT, this::visitArrayAssignStmt);
        addVisit(VAR_REF_EXPR, this::visitVarRefExpr);
        addVisit(IF_STMT, this::visitIfStmt);
        addVisit(WHILE_STMT, this::visitWhileStmt);
        setDefaultVisit(this::defaultVisit);
    }

    private Boolean visitMethodDecl(JmmNode node, Void unused) {
        intConsts.clear();
        boolConsts.clear();
        return visitChildren(node);
    }

    private Boolean visitAssignStmt(JmmNode node, Void unused) {
        JmmNode lhs = node.getChild(0);
        if (!lhs.isInstance(VAR_REF_EXPR)) return visitChildren(node);

        String varName = lhs.get("name");
        boolean rhsChanged = visit(node.getChild(1));
        JmmNode rhs = node.getChild(1);

        if (rhs.isInstance(INTEGER_LITERAL)) {
            intConsts.put(varName, Integer.parseInt(rhs.get("value")));
        } else if (rhs.isInstance(BOOLEAN_LITERAL)) {
            boolConsts.put(varName, Boolean.parseBoolean(rhs.get("value")));
        } else if (rhs.isInstance(VAR_REF_EXPR)) {
            propagateVarValue(varName, rhs.get("name"));
        } else {
            clearConstants(varName);
        }

        return rhsChanged;
    }

    private Boolean visitArrayAssignStmt(JmmNode node, Void unused) {
        boolean indexChanged = visit(node.getChild(0));
        JmmNode indexNode = node.getChild(0);

        // Replace index with constant
        if (indexNode.isInstance(VAR_REF_EXPR)) {
            String indexVar = indexNode.get("name");
            if (intConsts.containsKey(indexVar)) {
                replaceWithLiteral(indexNode, intConsts.get(indexVar));
                indexChanged = true;
            }
        }

        boolean rhsChanged = visit(node.getChild(1));

        JmmNode arrayRef = node.getChild(0).getParent();
        if (arrayRef.isInstance(VAR_REF_EXPR)) {
            String arrayName = arrayRef.get("name");
            clearConstants(arrayName); // Invalidate the array
        }

        return indexChanged || rhsChanged;
    }

    private Boolean visitVarRefExpr(JmmNode node, Void unused) {
        String varName = node.get("name");

        if (intConsts.containsKey(varName)) {
            replaceWithLiteral(node, intConsts.get(varName));
            return true;
        } else if (boolConsts.containsKey(varName)) {
            replaceWithLiteral(node, boolConsts.get(varName));
            return true;
        }
        return false;
    }

    private Boolean visitIfStmt(JmmNode node, Void unused) {
        //System.out.println("Global Constants Before Condition: Integers: " + intConsts + ", Booleans: " + boolConsts);
        Map<String, Integer> originalIntConsts = new HashMap<>(intConsts);
        Map<String, Boolean> originalBoolConsts = new HashMap<>(boolConsts);

        boolean condChanged = visit(node.getChild(0));

        // Handle the "then" branch
        boolean thenChanged = visit(node.getChild(1));
        Map<String, Integer> thenIntConsts = new HashMap<>(intConsts);
        Map<String, Boolean> thenBoolConsts = new HashMap<>(boolConsts);
        //System.out.println("Global Constants After Then Branch: Integers: " + thenIntConsts + ", Booleans: " + thenBoolConsts);

        intConsts = new HashMap<>(originalIntConsts);
        boolConsts = new HashMap<>(originalBoolConsts);

        // Handle the "else" branch (if it exists)
        boolean elseChanged = false;
        Map<String, Integer> elseIntConsts = new HashMap<>();
        Map<String, Boolean> elseBoolConsts = new HashMap<>();
        if (node.getChildren().size() > 2) {
            elseChanged = visit(node.getChild(2));
            elseIntConsts = new HashMap<>(intConsts);
            elseBoolConsts = new HashMap<>(boolConsts);
            //System.out.println("Global Constants After Else Branch: Integers: " + elseIntConsts + ", Booleans: " + elseBoolConsts);
        }

        // Merge the constants from both branches
        intConsts = new HashMap<>(originalIntConsts);
        boolConsts = new HashMap<>(originalBoolConsts);
        mergeBranchConstants(thenIntConsts, intConsts);
        mergeBranchConstants(elseIntConsts, intConsts);
        mergeBranchConstants(thenBoolConsts, boolConsts);
        mergeBranchConstants(elseBoolConsts, boolConsts);

        //System.out.println("Global Constants After If-Else: Integers: " + intConsts + ", Booleans: " + boolConsts);

        return condChanged || thenChanged || elseChanged;
    }

    private <T> void mergeBranchConstants(Map<String, T> branchConsts, Map<String, T> globalConsts) {
        // Collect keys to be removed
        var keysToRemove = new java.util.ArrayList<String>();
        for (String var : globalConsts.keySet()) {
            // Remove if the variable is not in the branch or has a different value
            if (!branchConsts.containsKey(var) || !branchConsts.get(var).equals(globalConsts.get(var))) {
                keysToRemove.add(var);
            }
        }

        // Remove keys after iteration
        for (String key : keysToRemove) {
            globalConsts.remove(key);
        }

        // Remove any variables that exist in the branch but are not in the global constants
        for (String var : branchConsts.keySet()) {
            if (!globalConsts.containsKey(var)) {
                globalConsts.remove(var);
            }
        }

        //System.out.println("Global Const After Merge: " + globalConsts);
    }

    private Boolean visitWhileStmt(JmmNode node, Void unused) {
        JmmNode condition = node.getChild(0);

        if (condition.isInstance(BINARY_EXPR)) {
            JmmNode rhs = condition.getChild(1);

            boolean rhsChanged = visit(rhs);

            boolean bodyChanged = visit(node.getChild(1));

            // Invalidate variables assigned in the loop
            for (JmmNode child : node.getChild(1).getChildren()) {
                if (child.isInstance(ASSIGN_STMT)) {
                    JmmNode varRef = child.getChild(0);
                    if (varRef.isInstance(VAR_REF_EXPR)) {
                        clearConstants(varRef.get("name"));
                    }
                }
            }

            return rhsChanged || bodyChanged;
        }

        return visitChildren(node);
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

    private void propagateVarValue(String targetVar, String sourceVar) {
        if (intConsts.containsKey(sourceVar)) {
            intConsts.put(targetVar, intConsts.get(sourceVar));
        } else if (boolConsts.containsKey(sourceVar)) {
            boolConsts.put(targetVar, boolConsts.get(sourceVar));
        } else {
            clearConstants(targetVar);
        }
    }

    private void clearConstants(String varName) {
        intConsts.remove(varName);
        boolConsts.remove(varName);
    }

    private void replaceWithLiteral(JmmNode varNode, Object value) {
        JmmNode parent = varNode.getParent();
        int index = parent.getChildren().indexOf(varNode);
        if (index == -1) return;

        JmmNode literal = new JmmNodeImpl(Collections.singletonList(value instanceof Integer
                ? INTEGER_LITERAL.toString() : BOOLEAN_LITERAL.toString()));
        literal.put("value", value.toString());

        parent.removeChild(index);
        parent.add(literal, index);
        isChanged = true;
    }

    public boolean isChanged() {
        return isChanged;
    }
}