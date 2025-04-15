package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MethodCallTypeChecker extends AnalysisVisitor {
    @Override
    public void buildVisitor() {
        addVisit(Kind.METHOD_CALL_EXPR, this::visitMethodCall);
    }

    private Void visitMethodCall(JmmNode methodCallNode, SymbolTable table) {
        var typeUtils = new TypeUtils(table);
        String methodName = methodCallNode.get("name");

        // Check if the method call has a receiver
        if (methodCallNode.getNumChildren() == 0) {
            addReport(newError(methodCallNode, "Method call '" + methodName + "' is missing a receiver object."));
            return null;
        }

        JmmNode receiverNode = methodCallNode.getChild(0);
        Type receiverType = typeUtils.getExprType(receiverNode);

        if (receiverType == null) {
            addReport(newError(methodCallNode, "Receiver object for method call '" + methodName + "' is invalid or undefined."));
            return null;
        }

        if (typeUtils.isImportedOrExtendedOrInherited(receiverType)) {
            // Assume the method returns the same type as the enclosing method's return type
            Optional<JmmNode> methodDeclOpt = methodCallNode.getAncestor(Kind.METHOD_DECL);
            if (methodDeclOpt.isPresent()) {
                JmmNode methodDecl = methodDeclOpt.get();
                JmmNode returnTypeNode = methodDecl.getChildren().getFirst();
                TypeUtils.convertType(returnTypeNode);
            } else {
                addReport(newError(methodCallNode, "Method call on imported or extended class outside of method declaration."));
            }
            return null;
        }

        // For non-imported and non-extended classes
        Type returnType = table.getReturnType(methodName);
        if (returnType == null) {
            String superClassName = table.getSuper();
            if (superClassName != null && !superClassName.isEmpty()) {
                return null;
            }

            addReport(newError(methodCallNode, "Method '" + methodName + "' not found in class " + receiverType.getName() + "."));
            return null;
        }

        Optional<List<Symbol>> methodParamsOpt = Optional.ofNullable(table.getParameters(methodName));

        if (methodParamsOpt.isEmpty()) {
            Optional<String> superClassOpt = Optional.ofNullable(table.getSuper());
            if (superClassOpt.isEmpty()) {
                addReport(newError(methodCallNode, "Method '" + methodName + "' is undefined."));
            }
            return null;
        }

        List<Symbol> methodParams = methodParamsOpt.get();
        boolean foundVarArgs = false;

        for (int i = 0; i < methodParams.size(); i++) {
            Symbol param = methodParams.get(i);
            Object isVarArgsObject = param.getType().getObject("isVarArgs");
            boolean isVarArgs = isVarArgsObject instanceof Boolean && (Boolean) isVarArgsObject;

            if (isVarArgs) {
                if (foundVarArgs) {
                    addReport(newError(
                            methodCallNode,
                            "Only one parameter can be declared as varargs.")
                    );
                    return null;
                }

                foundVarArgs = true;

                // Check if the varargs parameter is not the last parameter
                if (i != methodParams.size() - 1) {
                    addReport(newError(
                            methodCallNode,
                            "Varargs parameter must be the last parameter in the method signature.")
                    );
                    return null;
                }
            }
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

        Object isVarArgsObject = lastParam.getType().getObject("isVarArgs");
        boolean isVarArgs = isVarArgsObject instanceof Boolean && (Boolean) isVarArgsObject;

        return isVarArgs;
    }

    private void handleVarArgsCall(JmmNode node, String methodName, List<Symbol> params, List<Type> args) {
        int fixedParams = params.size() - 1;
        if (args.size() < fixedParams) {
            addReport(newError(
                    node,
                    "Insufficient arguments for method '" + methodName + "'.")
            );
            return;
        }

        // Check fixed parameters
        for (int i = 0; i < fixedParams; i++) {
            checkTypeMatch(node, methodName, args.get(i), params.get(i).getType(), i + 1);
        }

        List<Type> varargsArgs = args.subList(fixedParams, args.size());

        if (varargsArgs.isEmpty()) {
            return;
        }

        // Check if varargs parameter is single int array
        if (varargsArgs.size() == 1 && varargsArgs.getFirst().isArray()) {
            Type arrayType = varargsArgs.getFirst();
            if (!arrayType.getName().equals("int") || !arrayType.isArray()) {
                addReport(newError(
                        node,
                        "Varargs argument must be an array of type 'int'.")
                );
            }
            return;
        }

        // Check if all elements are integers
        for (Type argType : varargsArgs) {
            if (!argType.getName().equals("int") || argType.isArray()) {
                addReport(newError(
                        node,
                        "Varargs argument must be either a single array of type 'int' or multiple integers.")
                );
                return;
            }
        }
    }

    private void handleNormalCall(JmmNode node, String methodName, List<Symbol> params, List<Type> args) {
        if (params.size() != args.size()) {
            addReport(newError(
                    node,
                    "Method '" + methodName + "' expects " + params.size() + " arguments but got " + args.size() + ".")
            );
            return;
        }

        for (int i = 0; i < params.size(); i++) {
            checkTypeMatch(node, methodName, args.get(i), params.get(i).getType(), i + 1);
        }

    }

    private void checkTypeMatch(JmmNode node, String methodName, Type argType, Type paramType, int pos) {
        if (!argType.equals(paramType)) {
            addReport(newError(
                    node,
                    "Argument " + pos + " of '" + methodName + "' expects type " + paramType + " but got " + argType + ".")
            );
        }
    }
}