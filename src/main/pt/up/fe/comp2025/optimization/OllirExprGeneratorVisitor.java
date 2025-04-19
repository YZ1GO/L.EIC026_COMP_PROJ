package pt.up.fe.comp2025.optimization;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp2025.ast.TypeUtils;

import javax.swing.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
        addVisit(LENGTH_EXPR, this::visitLength);
        addVisit(STRING_LITERAL, this::visitString);
        addVisit(METHOD_CALL_EXPR, this::visitMethodCall);
        addVisit(THIS_EXPR, this::visitThis);
        addVisit(UNARY_NOT_EXPR, this::visitUnaryNot);
        addVisit(ARRAY_INIT, this::visitArrayInit);
        addVisit(NEW_INT_ARRAY_EXPR, this::visitNewIntArray);
        addVisit(ARRAY_ACCESS_EXPR, this::visitArrayAccess);


//        setDefaultVisit(this::defaultVisit);
    }

    private OllirExprResult visitArrayAccess(JmmNode node, Void unused) {
        OllirExprResult arrayResult = visit(node.getChild(0));
        OllirExprResult indexResult = visit(node.getChild(1));

        Type elementType = types.getExprType(node);
        String ollirElementType = ollirTypes.toOllirType(elementType);

        String tempVar = ollirTypes.nextTemp();
        String tempVarWithType = tempVar + ollirElementType;

        String computation = arrayResult.getComputation() +
                indexResult.getComputation() +
                tempVarWithType + SPACE + ASSIGN + ollirElementType + SPACE +
                arrayResult.getCode() + "[" + indexResult.getCode() + "]" + ollirElementType + END_STMT;

        return new OllirExprResult(tempVarWithType, computation);
    }

    private OllirExprResult visitNewIntArray(JmmNode node, Void unused) {
        StringBuilder computation = new StringBuilder();

        OllirExprResult sizeResult = visit(node.getChild(0));

        String tempVar = ollirTypes.nextTemp();
        String ollirArrayType = ollirTypes.toOllirType(new Type("int", true));
        String tempVarWithType = tempVar + ollirArrayType;

        computation.append(sizeResult.getComputation());
        computation.append(tempVarWithType).append(SPACE).append(ASSIGN).append(ollirArrayType).append(SPACE)
                .append("new(array, ").append(sizeResult.getCode()).append(")").append(ollirArrayType)
                .append(END_STMT);

        return new OllirExprResult(tempVarWithType, computation.toString());
    }

    private OllirExprResult visitArrayInit(JmmNode node, Void unused) {
        StringBuilder computation = new StringBuilder();
        String tempVar = ollirTypes.nextTemp();

        Type elementType = types.getExprType(node.getChild(0));
        String ollirArrayType = ollirTypes.toOllirType(new Type(elementType.getName(), true));
        String ollirElementType = ollirTypes.toOllirType(elementType);

        String tempVarWithType = tempVar + ollirArrayType;

        computation.append(tempVarWithType).append(SPACE).append(ASSIGN).append(ollirArrayType).append(SPACE)
                .append("new(array, ").append(node.getNumChildren()).append(".i32)").append(ollirArrayType)
                .append(END_STMT);

        for (int i = 0; i < node.getNumChildren(); i++) {
            OllirExprResult elementResult = visit(node.getChild(i));
            computation.append(elementResult.getComputation());
            computation.append(tempVar).append("[").append(i).append(".i32]").append(ollirElementType)
                    .append(SPACE).append(ASSIGN).append(ollirElementType).append(SPACE)
                    .append(elementResult.getCode()).append(END_STMT);
        }

        return new OllirExprResult(tempVarWithType, computation.toString());
    }

    private OllirExprResult visitUnaryNot(JmmNode node, Void unused) {
        OllirExprResult exprResult = visit(node.getChild(0));

        Type exprType = types.getExprType(node);
        String ollirType = ollirTypes.toOllirType(exprType);

        String tempVar = ollirTypes.nextTemp();
        String tempVarWithType = tempVar + ollirType;

        String computation = exprResult.getComputation() +
                tempVarWithType + SPACE + ASSIGN + ollirType + SPACE + "!" + ollirType + SPACE + exprResult.getCode() + END_STMT;

        return new OllirExprResult(tempVarWithType, computation);
    }

    private OllirExprResult visitThis(JmmNode node, Void unused) {
        String code = "this." + table.getClassName();
        return new OllirExprResult(code);
    }

    private OllirExprResult visitMethodCall(JmmNode node, Void unused) {
        String methodName = node.get("name");

        // Visit the object on which the method is called
        OllirExprResult objectResult = visit(node.getChild(0));
        // Arguments computation
        StringBuilder computation = new StringBuilder(objectResult.getComputation());
        StringBuilder argsCode = new StringBuilder();
        for (int i = 1; i < node.getNumChildren(); i++) {
            OllirExprResult argResult = visit(node.getChild(i));
            computation.append(argResult.getComputation());
            argsCode.append(argResult.getCode());
            if (i < node.getNumChildren() - 1) {
                argsCode.append(", ");
            }
        }


        // Determine if the call is static (receiver is a class name)
        boolean isStaticCall = !objectResult.getCode().contains(".");
        String invocationType = isStaticCall ? "invokestatic" : "invokevirtual";


        // Return type of the method
        Type returnType = types.getExprType(node);
        //System.out.println("NODE KIND: " + node.getKind());
        String ollirReturnType = ollirTypes.toOllirType(returnType);

        // Generate the method call code
        String methodCall = invocationType + "(" + objectResult.getCode() + ", \"" + methodName + "\", " + argsCode + ")" + ollirReturnType;

        // Check if the method call is part of an assignment
        JmmNode parent = node.getParent();
        if (parent != null && (parent.getKind().equals("ArrayAssignStmt")
                || parent.getKind().equals("AssignStmt"))) {
            // Don't generate assignment to prevent double-assignment
            return new OllirExprResult(methodCall, computation.toString());
        }

        // If not part of an assignment, check the return type
        if (ollirReturnType.equals(".V")) {
            // It's a void method call used as a statement
            computation.append(methodCall).append(END_STMT);
            return new OllirExprResult(methodCall, computation.toString());
        } else {
            // Assign to temp if not void
            String tempVar = ollirTypes.nextTemp();
            String tempVarWithType = tempVar + ollirReturnType;
            computation.append(tempVarWithType).append(SPACE).append(ASSIGN).append(ollirReturnType).append(SPACE)
                    .append(methodCall).append(END_STMT);
            return new OllirExprResult(tempVarWithType, computation.toString());
        }
    }

    private OllirExprResult visitString(JmmNode node, Void unused) {
        var stringType = TypeUtils.newStringType();
        String ollirStringType = ollirTypes.toOllirType(stringType);
        String code = node.get("value") + ollirStringType;
        return new OllirExprResult(code);
    }

    private OllirExprResult visitLength(JmmNode node, Void unused) {
        OllirExprResult arrayResult = visit(node.getChild(0));
    
        String tempVar = ollirTypes.nextTemp();
        String ollirIntType = ollirTypes.toOllirType(TypeUtils.newIntType());
        String tempVarWithType = tempVar + ollirIntType;
    
        StringBuilder computation = new StringBuilder();
        computation.append(arrayResult.getComputation())
                   .append(tempVarWithType).append(SPACE).append(ASSIGN).append(ollirIntType).append(SPACE)
                   .append("arraylength(").append(arrayResult.getCode()).append(")").append(ollirIntType)
                   .append(END_STMT);
    
        return new OllirExprResult(tempVarWithType, computation.toString());
    }

    private OllirExprResult visitNewObject(JmmNode node, Void unused) {
        String className = node.get("name");
        String tempVar = ollirTypes.nextTemp();
        Type type = new Type(className, false);
        String ollirType = ollirTypes.toOllirType(type);

        String assignment = tempVar + ollirType + SPACE + ASSIGN + ollirType + SPACE + "new(" + className + ")" + ollirType + END_STMT;

        String constructorCall = "invokespecial(" + tempVar + ollirType + ", \"<init>\").V" + END_STMT;

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

        // Check if this VarRef is an imported, extended, or inherited class
        if (types.isImportedOrExtendedOrInherited(new Type(id, false))) {
            return new OllirExprResult(id);
        } else {
            String code = id + ollirType;
            return new OllirExprResult(code);
        }
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
