package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;
import pt.up.fe.comp.jmm.analysis.table.Type;

public class LengthExprChecker extends AnalysisVisitor {

    @Override
    public void buildVisitor() {
        addVisit(Kind.LENGTH_EXPR, this::visitLengthExpr);
    }

    private Void visitLengthExpr(JmmNode lengthExpr, SymbolTable table) {
        var typeUtils = new TypeUtils(table);

        // Check if the name is "length"
        String name = lengthExpr.get("name");
        if (!"length".equals(name)) {
            addReport(newError(lengthExpr, "Invalid use of LengthExpr: expected 'length', but found '" + name + "'."));
            return null;
        }

        // Ensure the object before '.' is an array
        Type objectType = typeUtils.getExprType(lengthExpr.getChild(0));
        if (!objectType.isArray()) {
            addReport(newError(lengthExpr, "Invalid use of 'length': the object is not an array."));
        }

        return null;
    }
}