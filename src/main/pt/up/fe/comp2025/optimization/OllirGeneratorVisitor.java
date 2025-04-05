package pt.up.fe.comp2025.optimization;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.ast.TypeUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static pt.up.fe.comp2025.ast.Kind.*;

/**
 * Generates OLLIR code from JmmNodes that are not expressions.
 */
public class OllirGeneratorVisitor extends AJmmVisitor<Void, String> {

    private static final String SPACE = " ";
    private static final String ASSIGN = ":=";
    private final String END_STMT = ";\n";
    private final String NL = "\n";
    private final String L_BRACKET = " {\n";
    private final String R_BRACKET = "}\n";


    private final SymbolTable table;

    private final TypeUtils types;
    private final OptUtils ollirTypes;


    private final OllirExprGeneratorVisitor exprVisitor;

    public OllirGeneratorVisitor(SymbolTable table) {
        this.table = table;
        this.types = new TypeUtils(table);
        this.ollirTypes = new OptUtils(types);
        exprVisitor = new OllirExprGeneratorVisitor(table);
    }


    @Override
    protected void buildVisitor() {

        addVisit(PROGRAM, this::visitProgram);
        addVisit(CLASS_DECL, this::visitClass);
        addVisit(METHOD_DECL, this::visitMethodDecl);
        addVisit(PARAM, this::visitParam);
        addVisit(RETURN_STMT, this::visitReturn);
        addVisit(ASSIGN_STMT, this::visitAssignStmt);


        addVisit(VAR_DECL, this::visitVarDecl);
        addVisit(IMPORT_DECL, this::visitImportDecl);

        addVisit(BLOCK_STMT, this::visitBlockStmt);
        addVisit(IF_STMT, this::visitIfStmt);

//        setDefaultVisit(this::defaultVisit);
    }

    private String visitImportDecl(JmmNode node, Void unused) {
        String name = node.get("name");

        // Remove brackets from the name (e.g., "[java, util, List]" -> "java, util, List")
        name = name.substring(1, name.length() - 1);

        List<String> importNames = Arrays.stream(name.split(","))
                .map(String::trim)
                .collect(Collectors.toList());

        String importPath = String.join(".", importNames);

        return "import " + importPath + ";\n";
    }

    private String visitVarDecl(JmmNode node, Void unused) {

        var varName = node.get("name");

        JmmNode typeNode = node.getChild(0);
        String typeCode = ollirTypes.toOllirType(TypeUtils.convertType(typeNode));

        return varName + typeCode + ";\n";
    }

    private String visitAssignStmt(JmmNode node, Void unused) {

        var rhs = exprVisitor.visit(node.getChild(1));

        StringBuilder code = new StringBuilder();

        // code to compute the children
        code.append(rhs.getComputation());

        // code to compute self
        // statement has type of lhs
        var left = node.getChild(0);
        Type thisType = types.getExprType(left);
        String typeString = ollirTypes.toOllirType(thisType);
        var varCode = left.get("name") + typeString;


        code.append(varCode);
        code.append(SPACE);

        code.append(ASSIGN);
        code.append(typeString);
        code.append(SPACE);

        code.append(rhs.getCode());

        code.append(END_STMT);

        return code.toString();
    }


    private String visitReturn(JmmNode node, Void unused) {
        // TODO: Hardcoded for int type, needs to be expanded
        StringBuilder code = new StringBuilder();

        var expr = node.getNumChildren() > 0 ? exprVisitor.visit(node.getChild(0)) : OllirExprResult.EMPTY;

        Type retType = expr != OllirExprResult.EMPTY ? types.getExprType(node.getChild(0)) : TypeUtils.newVoidType();

        code.append(expr.getComputation());
        code.append("ret");
        code.append(ollirTypes.toOllirType(retType));
        code.append(SPACE);

        if (expr != OllirExprResult.EMPTY) {
            code.append(expr.getCode());
        }

        code.append(END_STMT);

        return code.toString();
    }


    private String visitParam(JmmNode node, Void unused) {

        var typeCode = ollirTypes.toOllirType(node.getChild(0));
        var id = node.get("name");

        String code = id + typeCode;

        return code;
    }


    private String visitMethodDecl(JmmNode node, Void unused) {

        StringBuilder code = new StringBuilder(".method ");

        boolean isPublic = node.getBoolean("isPublic", false);
        if (isPublic) {
            code.append("public ");
        }

        boolean isStatic = node.getBoolean("isStatic", false);
        if (isStatic) {
            code.append("static ");
        }
        
        // name
        var name = node.get("name");
        code.append(name);

        // params
        // TODO: Hardcoded for a single parameter, needs to be expanded
        // DONE: Expanded to handle multiple parameters
        var params = node.getChildren(PARAM);
        String paramsCode = params.isEmpty() ? "" : params.stream()
                .map(this::visit)
                .collect(Collectors.joining(", "));
        code.append("(").append(paramsCode).append(")");

        // type
        // TODO: Hardcoded for int, needs to be expanded
        // DONE: Expanded to handle multiple types
        Type returnType = TypeUtils.convertType(node.getChild(0));
        String retType = ollirTypes.toOllirType(returnType);
        code.append(retType);
        code.append(L_BRACKET);


        // rest of its children stmts
        var stmtsCode = node.getChildren(STMT).stream()
                .map(this::visit)
                .collect(Collectors.joining("\n   ", "   ", ""));

        code.append(stmtsCode);

        // Add default return statement if the method is void and no explicit return is present
        if (returnType.getName().equals("void") && node.getChildren(STMT).stream()
                .noneMatch(stmt -> stmt.getKind().equals(RETURN_STMT))) {
            code.append("   ret.V;\n");
        }

        code.append(R_BRACKET);
        code.append(NL);

        return code.toString();
    }


    private String visitClass(JmmNode node, Void unused) {

        StringBuilder code = new StringBuilder();

        code.append(NL);
        code.append(table.getClassName());
        
        String superClass = table.getSuper();
        if (superClass != null && !superClass.isEmpty()) {
            code.append(" extends ").append(superClass);
        }

        code.append(L_BRACKET);
        code.append(NL);
        code.append(NL);

        table.getFields().forEach(
                field -> {
                    code.append(".field public ");
                    code.append(field.getName());
                    code.append(ollirTypes.toOllirType(field.getType()));
                    code.append(END_STMT);
                    code.append(NL);
                }
        );

        code.append(NL);

        code.append(buildConstructor());
        code.append(NL);

        for (var child : node.getChildren(METHOD_DECL)) {
            var result = visit(child);
            code.append(result);
        }

        code.append(R_BRACKET);

        return code.toString();
    }

    private String buildConstructor() {

        return """
                .construct %s().V {
                    invokespecial(this, "<init>").V;
                }
                """.formatted(table.getClassName());
    }


    private String visitProgram(JmmNode node, Void unused) {

        StringBuilder code = new StringBuilder();

        node.getChildren().stream()
                .map(this::visit)
                .forEach(code::append);

        return code.toString();
    }

    private String visitBlockStmt(JmmNode node, Void unused) {
        StringBuilder code = new StringBuilder();

        for (var v : node.getChildren()) {
            code.append(visit(v));
        }

        return code.toString();
    }

    private String visitIfStmt(JmmNode node, Void unused) {
        StringBuilder code = new StringBuilder();

        var label = OptUtils.getIfLabels();
        var thenL = label.get(0);
        var endifL = label.get(1);

        var cond = exprVisitor.visit(node.getChild(0));
        var thenStmt = node.getChild(1);
        var elseStmt = node.getChildren().size() > 2 ? node.getChild(2) : null;

        code.append(cond.getComputation());

        code.append("if (").append(cond.getCode()).append(") goto ").append(thenL).append(END_STMT);

        // else block
        if (elseStmt != null) {
            code.append(visit(elseStmt));

        }

        code.append("goto ").append(endifL).append(END_STMT);

        // then block
        code.append(thenL).append(":").append(NL);
        code.append(visit(thenStmt));


        code.append(endifL).append(":").append(NL);


        return code.toString();
    }


    /**
     * Default visitor. Visits every child node and return an empty string.
     *
     * @param node
     * @param unused
     * @return
     */
    private String defaultVisit(JmmNode node, Void unused) {

        for (var child : node.getChildren()) {
            visit(child);
        }

        return "";
    }
}
