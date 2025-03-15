package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MethodCallTypeChecker extends AnalysisVisitor {

    private final TypeUtils typeUtils;

    public MethodCallTypeChecker(SymbolTable table) {
        this.typeUtils = new TypeUtils(table);
    }

    @Override
    public void buildVisitor() {
        addVisit(Kind.METHOD_CALL_EXPR, this::visitMethodCall);
    }

    private Void visitMethodCall(JmmNode methodCallNode, SymbolTable table) {
        String methodName = methodCallNode.get("name");
        JmmNode receiverNode = methodCallNode.getChild(0);
        Type receiverType = typeUtils.getExprType(receiverNode);

        // Skip checks for imported classes
        if (table.getImports().stream()
                .flatMap(importName -> Arrays.stream(importName.substring(1, importName.length() - 1).split(",")))
                .anyMatch(importName -> importName.trim().equals(receiverType.getName()))) {
            return null;
        }

        List<Symbol> methodParams = table.getParameters(methodName);
        if (methodParams == null) {
            if (table.getSuper() == null) {
                addReport(Report.newError(Stage.SEMANTIC, methodCallNode.getLine(), methodCallNode.getColumn(),
                        "Method '" + methodName + "' is undefined.", null));
            }
            return null;
        }

        List<Type> argTypes = methodCallNode.getChildren().subList(1, methodCallNode.getNumChildren())
                .stream().map(typeUtils::getExprType).collect(Collectors.toList());

        boolean isVarArgs = !methodParams.isEmpty() && isLastParamVarArgs(methodParams);

        if (isVarArgs) {
            handleVarArgsCall(methodCallNode, methodName, methodParams, argTypes);
        } else {
            handleNormalCall(methodCallNode, methodName, methodParams, argTypes);
        }

        return null;
    }

    private boolean isLastParamVarArgs(List<Symbol> params) {
        Symbol lastParam = params.getLast();
        return lastParam.getType().isArray() && lastParam.getType().getName().equals("int");
    }

    private void handleVarArgsCall(JmmNode node, String methodName, List<Symbol> params, List<Type> args) {
        int fixedParams = params.size() - 1;
        if (args.size() < fixedParams) {
            addReport(Report.newError(Stage.SEMANTIC, node.getLine(), node.getColumn(),
                    "Insufficient arguments for method '" + methodName + "'.", null));
            return;
        }

        // Check fixed parameters
        for (int i = 0; i < fixedParams; i++) {
            checkTypeMatch(node, methodName, args.get(i), params.get(i).getType(), i + 1);
        }

        // Check varargs (must be int)
        for (int i = fixedParams; i < args.size(); i++) {
            if (!args.get(i).getName().equals("int") || args.get(i).isArray()) {
                addReport(Report.newError(Stage.SEMANTIC, node.getLine(), node.getColumn(),
                        "Varargs argument " + (i - fixedParams + 1) + " must be of type 'int'.", null));
            }
        }
    }

    private void handleNormalCall(JmmNode node, String methodName, List<Symbol> params, List<Type> args) {
        if (params.size() != args.size()) {
            addReport(Report.newError(Stage.SEMANTIC, node.getLine(), node.getColumn(),
                    "Method '" + methodName + "' expects " + params.size() + " arguments but got " + args.size() + ".", null));
            return;
        }

        for (int i = 0; i < params.size(); i++) {
            checkTypeMatch(node, methodName, args.get(i), params.get(i).getType(), i + 1);
        }
    }

    private void checkTypeMatch(JmmNode node, String methodName, Type argType, Type paramType, int pos) {
        if (!argType.equals(paramType)) {
            addReport(Report.newError(Stage.SEMANTIC, node.getLine(), node.getColumn(),
                    "Argument " + pos + " of '" + methodName + "' expects type " + paramType + " but got " + argType + ".", null));
        }
    }
}