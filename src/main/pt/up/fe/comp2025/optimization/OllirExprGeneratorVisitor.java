package pt.up.fe.comp2025.optimization;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp2025.ast.TypeUtils;

import static pt.up.fe.comp2025.ast.Kind.*;

/**
 * Generates OLLIR code from JmmNodes that are expressions.
 */
public class OllirExprGeneratorVisitor extends AJmmVisitor<Void, OllirExprResult> {

    private static final String SPACE = " ";
    private static final String ASSIGN = ":=";
    private final String END_STMT = ";\n";

    private final SymbolTable table;

    private final TypeUtils types;
    private final OptUtils ollirTypes;


    public OllirExprGeneratorVisitor(SymbolTable table) {
        this.table = table;
        this.types = new TypeUtils(table);
        this.ollirTypes = new OptUtils(types);
    }


    @Override
    protected void buildVisitor() {
        addVisit(VAR_REF_EXPR, this::visitVarRef);
        addVisit(BINARY_EXPR, this::visitBinExpr);
        addVisit(INTEGER_LITERAL, this::visitInteger);
        addVisit(BOOLEAN_LITERAL, this::visitBoolean);
        addVisit(PARENT_EXPR, this::visitParentExpr);
        addVisit(NEW_OBJECT_EXPR, this::visitNewObject);
        //addVisit(ARRAY_ACCESS_EXPR, this::visitArrayAccess);
        //addVisit(LENGTH_EXPR, this::visitLength);
        //addVisit(STRING_LITERAL, this::visitString);
        //addVisit(METHOD_CALL_EXPR, this::visitMethodCall);
        addVisit(THIS_EXPR, this::visitThis);
        //addVisit(UNARY_NOT_EXPR, this::visitUnaryNot);
        //addVisit(BINARY_EXPR, this::visitBinExpr);
        //addVisit(ARRAY_INIT_EXPR, this::visitArrayInit);
        //addVisit(NEW_INT_ARRAY_EXPR, this::visitNewIntArray);


//        setDefaultVisit(this::defaultVisit);
    }

    private OllirExprResult visitThis(JmmNode node, Void unused) {
        String code = "this." + table.getClassName();
        return new OllirExprResult(code);
    }

    private OllirExprResult visitNewObject(JmmNode node, Void unused) {
        String className = node.get("name");
        String tempVar = ollirTypes.nextTemp();
        Type type = new Type(className, false);
        String ollirType = ollirTypes.toOllirType(type);

        String assignment = String.format("%s%s :=%s new(%s)%s;\n", 
            tempVar, ollirType, ollirType, className, ollirType);

        String constructorCall = String.format("invokespecial(%s%s, \"<init>\").V;\n",
            tempVar, ollirType);

        String computation = assignment + constructorCall;
        String code = tempVar + ollirType;
    
        return new OllirExprResult(code, computation);
    }

    private OllirExprResult visitParentExpr(JmmNode node, Void unused) {
        return visit(node.getChild(0));
    }

    private OllirExprResult visitBoolean(JmmNode node, Void unused) {
        var boolType = TypeUtils.newBooleanType();
        String ollirBoolType = ollirTypes.toOllirType(boolType);
        String code = (node.get("value").equals("true") ? "1" : "0") + ollirBoolType;
        return new OllirExprResult(code);
    }


    private OllirExprResult visitInteger(JmmNode node, Void unused) {
        var intType = TypeUtils.newIntType();
        String ollirIntType = ollirTypes.toOllirType(intType);
        String code = node.get("value") + ollirIntType;
        return new OllirExprResult(code);
    }


    private OllirExprResult visitBinExpr(JmmNode node, Void unused) {

        var lhs = visit(node.getChild(0));
        var rhs = visit(node.getChild(1));

        StringBuilder computation = new StringBuilder();

        // code to compute the children
        computation.append(lhs.getComputation());
        computation.append(rhs.getComputation());

        // code to compute self
        Type resType = types.getExprType(node);
        String resOllirType = ollirTypes.toOllirType(resType);
        String code = ollirTypes.nextTemp() + resOllirType;

        computation.append(code).append(SPACE)
                .append(ASSIGN).append(resOllirType).append(SPACE)
                .append(lhs.getCode()).append(SPACE);

        Type type = types.getExprType(node);
        computation.append(node.get("op")).append(ollirTypes.toOllirType(type)).append(SPACE)
                .append(rhs.getCode()).append(END_STMT);

        return new OllirExprResult(code, computation);
    }


    private OllirExprResult visitVarRef(JmmNode node, Void unused) {

        var id = node.get("name");
        Type type = types.getExprType(node);
        String ollirType = ollirTypes.toOllirType(type);

        String code = id + ollirType;

        return new OllirExprResult(code);
    }

    /**
     * Default visitor. Visits every child node and return an empty result.
     *
     * @param node
     * @param unused
     * @return
     */
    private OllirExprResult defaultVisit(JmmNode node, Void unused) {

        for (var child : node.getChildren()) {
            visit(child);
        }

        return OllirExprResult.EMPTY;
    }

}
