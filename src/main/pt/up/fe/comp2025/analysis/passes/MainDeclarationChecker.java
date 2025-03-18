package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;

/**
 * Ensures that the main method is correctly declared as "public static void main(String[] args)".
 */
public class MainDeclarationChecker extends AnalysisVisitor {

    @Override
    public void buildVisitor() {
        addVisit(Kind.METHOD_DECL, this::visitMethodDecl);
    }

    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        String methodName = method.get("name");

        // If this is the "main" method, validate its declaration
        if (methodName.equals("main")) {
            boolean isPublic = method.getOptional("isPublic").map(Boolean::parseBoolean).orElse(false);
            boolean isStatic = method.getOptional("isStatic").map(Boolean::parseBoolean).orElse(false);
            JmmNode returnTypeNode = method.getChildren().get(0);
            String returnType = returnTypeNode.get("name");

            // Check if the method is public, static, and returns void
            if (!isPublic || !isStatic || !returnType.equals("void")) {
                addReport(newError(
                        method,
                        "Main method must be declared as 'public static void main(String[] args)'.")
                );
                return null;
            }

            var params = table.getParameters(methodName);
            if (params.size() == 1) {
                var paramType = params.getFirst().getType();

                // Check if it is "String[]"
                if (!paramType.isArray() || !paramType.getName().equals("String")) {
                    addReport(newError(
                            method,
                            "Main method parameter must be of type 'String[]'.")
                    );
                }
            } else {
                addReport(newError(
                        method,
                        "Main method must have exactly one parameter of type 'String[]'.")
                );
            }
        }

        return null;
    }
}
