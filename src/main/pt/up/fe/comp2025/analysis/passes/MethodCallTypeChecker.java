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
        var methodName = methodCallNode.get("name");

        Type objectType = typeUtils.getExprType(methodCallNode.getChild(0));

        if(table.getImports().stream()
                .flatMap(importName -> Arrays.stream(importName.substring(1, importName.length() - 1).split(",")))
                .anyMatch(importName -> importName.trim().equals(objectType.getName()))){

            return null;
        }
    
        // Check if the method exists in the class
        List<Symbol> methodParameters = table.getParameters(methodName);
        if (methodParameters == null) {
            // Check if the class extends another class
            String superClassName = table.getSuper();
            if (superClassName == null || superClassName.isEmpty()) {
                addReport(Report.newError(
                        Stage.SEMANTIC,
                        methodCallNode.getLine(),
                        methodCallNode.getColumn(),
                        String.format("Method '%s' is not declared in the class and the class does not extend another class.", methodName),
                        null)
                );
                return null;
            } else {
                // Assume the method exists in the superclass
                return null;
            }
        }

        // Get the argument types of the method call
        List<Type> argumentTypes = methodCallNode.getChildren().subList(1, methodCallNode.getNumChildren()).stream()
                .map(arg -> typeUtils.getExprType(arg))
                .collect(Collectors.toList());

        // Check if the number of arguments matches the method signature
        if (argumentTypes.size() != methodParameters.size()) {
            addReport(Report.newError(
                    Stage.SEMANTIC,
                    methodCallNode.getLine(),
                    methodCallNode.getColumn(),
                    String.format("Method '%s' expects %d arguments, but %d were provided.",
                            methodName, methodParameters.size(), argumentTypes.size()),
                    null)
            );
            return null;
        }

        // Check if each argument type is compatible with the corresponding parameter type
        for (int i = 0; i < argumentTypes.size(); i++) {
            Type argumentType = argumentTypes.get(i);
            Type parameterType = methodParameters.get(i).getType();

            if (!isTypeCompatible(argumentType, parameterType)) {
                addReport(Report.newError(
                        Stage.SEMANTIC,
                        methodCallNode.getLine(),
                        methodCallNode.getColumn(),
                        String.format("Argument %d of method '%s' is of type '%s', but expected type '%s'.",
                                i + 1, methodName, formatType(argumentType), formatType(parameterType)),
                        null)
                );
                return null;
            }
        }

        return null;
    }

    private boolean isTypeCompatible(Type argumentType, Type parameterType) {
        return argumentType.getName().equals(parameterType.getName()) && argumentType.isArray() == parameterType.isArray();
    }

    private String formatType(Type type) {
        // Format the type to include array information
        return type.getName() + (type.isArray() ? "[]" : "");
    }
}