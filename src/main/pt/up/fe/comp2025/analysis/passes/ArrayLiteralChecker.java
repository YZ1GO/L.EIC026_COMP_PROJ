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
        //addVisit(Kind.VAR_DECL_STMT, this::visitVarDeclStmt);
    }

    private Void visitArrayLiteral(JmmNode arrayLiteral, SymbolTable table) {
        var typeUtils = new TypeUtils(table);

        // Get the elements of the array literal
        var elements = arrayLiteral.getChildren();
        if (elements.isEmpty()) {
            addReport(Report.newError(
                    Stage.SEMANTIC,
                    arrayLiteral.getLine(),
                    arrayLiteral.getColumn(),
                    "Empty array literals are not allowed. Provide at least one element or specify the array type explicitly.",
                    null)
            );
            return null;
        }

        // Get the type of the first element
        var firstElementType = typeUtils.getExprType(elements.getFirst());

        // Check if all elements have the same type
        for (var element : elements) {
            var elementType = typeUtils.getExprType(element);
            if (!elementType.equals(firstElementType)) {
                addReport(Report.newError(
                        Stage.SEMANTIC,
                        arrayLiteral.getLine(),
                        arrayLiteral.getColumn(),
                        String.format("Inconsistent array literal types: expected %s, found %s",
                                firstElementType.getName(), elementType.getName()),
                        null)
                );
                return null;
            }
        }

        return null;
    }

    private Void visitVarDeclStmt(JmmNode varDeclStmt, SymbolTable table) {

        return null;
    }
}
