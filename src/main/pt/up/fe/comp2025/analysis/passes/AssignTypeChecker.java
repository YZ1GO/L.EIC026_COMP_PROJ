package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;
import pt.up.fe.comp.jmm.analysis.table.Type;

public class AssignTypeChecker extends AnalysisVisitor {

    private final TypeUtils typeUtils;
    private final SymbolTable symbolTable;

    public AssignTypeChecker(SymbolTable table) {
        this.typeUtils = new TypeUtils(table);
        this.symbolTable = table;
    }

    @Override
    public void buildVisitor() {
        addVisit(Kind.ASSIGN_STMT, this::visitAssignStmt);
    }

    private Void visitAssignStmt(JmmNode assignStmt, SymbolTable table) {
        JmmNode varRef = assignStmt.getChild(0);
        JmmNode expr = assignStmt.getChild(1);

        Type declaredType = typeUtils.getExprType(varRef);
        Type assignedType = typeUtils.getExprType(expr);

        if (!isTypeCompatible(declaredType, assignedType)) {
            addReport(Report.newError(
                    Stage.SEMANTIC,
                    assignStmt.getLine(),
                    assignStmt.getColumn(),
                    String.format("Type mismatch: cannot assign '%s' to variable '%s' of type '%s'.", assignedType.getName(), varRef.get("name"), declaredType.getName()),
                    null)
            );
        }

        return null;
    }

    private boolean isTypeCompatible(Type declaredType, Type assignedType) {
        // Allow assignment if the types are exactly the same
        if (declaredType.equals(assignedType)) {
            return true;
        }
    
        // Allow assignment if both types are imported classes
        boolean declaredIsImported = isImported(declaredType.getName());
        boolean assignedIsImported = isImported(assignedType.getName());

        return declaredIsImported && assignedIsImported;
    }
    
    private boolean isImported(String typeName) {
        return symbolTable.getImports().stream().anyMatch(imported -> imported.endsWith(typeName));
    }
}