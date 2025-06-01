package pt.up.fe.comp2025.analysis.passes;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.ast.Kind;

import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.TypeUtils;

public class ArrayLiteralChecker extends AnalysisVisitor {

    @Override
    protected void buildVisitor() {
        addVisit(Kind.ARRAY_INIT, this::visitArrayLiteral);
    }

    private Void visitArrayLiteral(JmmNode arrayLiteral, SymbolTable table) {
        var typeUtils = new TypeUtils(table);

        // all elements of array literal
        var elements = arrayLiteral.getChildren();
        if (elements.isEmpty()) {
            addReport(newError(arrayLiteral, "Empty array literals are not allowed. Provide at least one element or specify the array type explicitly."));

            return null;
        }

        var firstElementType = typeUtils.getExprType(elements.getFirst());

        // check if all elements have the same type
        for (var element : elements) {
            var elementType = typeUtils.getExprType(element);
            if (!elementType.equals(firstElementType)) {
                addReport(newError(arrayLiteral, String.format("Inconsistent array literal types: expected %s, found %s", firstElementType.getName(), elementType.getName())));

                return null;
            }
        }

        return null;
    }
}
