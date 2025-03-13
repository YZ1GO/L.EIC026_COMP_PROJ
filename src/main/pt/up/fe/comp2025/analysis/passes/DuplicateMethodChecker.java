package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DuplicateMethodChecker extends AnalysisVisitor {

    @Override
    public void buildVisitor() {
        addVisit(Kind.METHOD_DECL, this::visitMethodDecl);
    }

    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        String methodName = method.get("name");

        // Use a map to track method signatures (name + parameter types)
        Map<String, Set<String>> methodSignatures = new HashMap<>();

        // Iterate over all methods in the class
        for (var methodNameFromTable : table.getMethods()) {
            var parameters = table.getParameters(methodNameFromTable); // Get method parameters

            // Debug: Print method name and parameters
            System.out.println("Method: " + methodNameFromTable);
            if (parameters != null) {
                System.out.println("Parameters:");
                for (var param : parameters) {
                    System.out.println("  " + param.getType().getName());
                }
            } else {
                System.out.println("No parameters");
            }

            // Generate a unique signature for the method
            String signature = generateMethodSignature(methodNameFromTable, parameters);

            // Debug: Print the generated signature
            System.out.println("Generated signature: " + signature);

            // Check if the signature already exists
            if (methodSignatures.containsKey(signature)) {
                // Debug: Print duplicate method error
                System.out.println("Duplicate method found: " + methodNameFromTable);

                // Report duplicate method error
                addReport(Report.newError(
                        Stage.SEMANTIC,
                        method.getLine(),
                        method.getColumn(),
                        String.format("Duplicate method declaration: '%s' is already defined in this class.", methodNameFromTable),
                        null)
                );
            } else {
                // Add the signature to the map
                methodSignatures.put(signature, Set.of());
            }
        }

        return null;
    }

    /**
     * Helper method to generate a unique signature for a method.
     * The signature is a combination of the method name and its parameter types.
     */
    private String generateMethodSignature(String methodName, List<Symbol> parameters) {
        StringBuilder signatureBuilder = new StringBuilder(methodName);
        if (parameters != null) {
            for (var param : parameters) {
                signatureBuilder.append(param.getType().getName()); // Append parameter type
            }
        }
        return signatureBuilder.toString();
    }
}