package pt.up.fe.comp2025.symboltable;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;
import pt.up.fe.specs.util.SpecsCheck;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static pt.up.fe.comp2025.ast.Kind.*;
import static pt.up.fe.comp2025.ast.TypeUtils.convertType;

public class JmmSymbolTableBuilder {

    // In case we want to already check for some semantic errors during symbol table building.
    private List<Report> reports;

    public List<Report> getReports() {
        return reports;
    }

    private static Report newError(JmmNode node, String message) {
        return Report.newError(
                Stage.SEMANTIC,
                node.getLine(),
                node.getColumn(),
                message,
                null);
    }

    public JmmSymbolTable build(JmmNode root) {

        reports = new ArrayList<>();

        // TODO: After your grammar supports more things inside the program (e.g., imports) you will have to change this
        // DONE: Updated to support imports, superClass and fields
        JmmNode classDecl = root.getChild(root.getNumChildren() - 1);
        SpecsCheck.checkArgument(Kind.CLASS_DECL.check(classDecl), () -> "Expected a class declaration: " + classDecl);
        String className = classDecl.get("name");
        List<String> methods = buildMethods(classDecl);
        Map<String, Type> returnTypes = buildReturnTypes(classDecl);
        Map<String, List<Symbol>> params = buildParams(classDecl);
        Map<String, List<Symbol>> locals = buildLocals(classDecl);
        
        List<String> imports = buildImports(root);
        String superClassName = classDecl.hasAttribute("parent") ? classDecl.get("parent") : null;
        List<Symbol> fields = buildFields(classDecl);

        return new JmmSymbolTable(className, methods, returnTypes, params, locals, imports, superClassName, fields);
    }


    private Map<String, Type> buildReturnTypes(JmmNode classDecl) {
        Map<String, Type> map = new HashMap<>();

        for (var method : classDecl.getChildren(METHOD_DECL)) {
            var name = method.get("name");
            // TODO: After you add more types besides 'int', you will have to update this
            // DONE: Updated based on convertType from TypeUtils.java
            if (method.getNumChildren() > 0) {
                var returnTypeNode = method.getChild(0);
                var returnType = TypeUtils.convertType(returnTypeNode);
                map.put(name, returnType);
            } else {
                map.put(name, new Type("void", false));
            }
        }

        return map;
    }


    private Map<String, List<Symbol>> buildParams(JmmNode classDecl) {
        Map<String, List<Symbol>> map = new HashMap<>();

        for (var method : classDecl.getChildren(METHOD_DECL)) {
            var name = method.get("name");
            var params = method.getChildren(PARAM).stream()
                    // TODO: When you support new types, this code has to be updated
                    // DONE: Updated based on convertType from TypeUtils.java
                .map(param -> {
                    var typeNode = param.getChild(0);
                    var type = TypeUtils.convertType(typeNode);
                    return new Symbol(type, param.get("name"));
                })
                .toList();

            // Debug statement to print parameters
            // System.out.println("Method: " + name + ", Params: " + params);

            map.put(name, params);
        }

        return map;
    }

    private Map<String, List<Symbol>> buildLocals(JmmNode classDecl) {

        var map = new HashMap<String, List<Symbol>>();

        for (var method : classDecl.getChildren(METHOD_DECL)) {
            var name = method.get("name");
            var locals = method.getChildren(VAR_DECL).stream()
                    // TODO: When you support new types, this code has to be updated
                    // DONE: Updated based on convertType from TypeUtils.java
                    .map(varDecl -> {
                        var typeNode = varDecl.getChild(0);
                        var type = TypeUtils.convertType(typeNode);
                        return new Symbol(type, varDecl.get("name"));
                    })
                    .toList();


            map.put(name, locals);
        }

        return map;
    }

    private List<String> buildMethods(JmmNode classDecl) {

        var methods = classDecl.getChildren(METHOD_DECL).stream()
                .map(method -> method.get("name"))
                .toList();

        return methods;
    }

    private List<String> buildImports(JmmNode root) {
        List<String> imports = new ArrayList<>();
        for (JmmNode importNode : root.getChildren(IMPORT_DECL)) {
            StringBuilder importPath = new StringBuilder(importNode.get("name"));
            for (JmmNode part : importNode.getChildren(ID)) {
                importPath.append(".").append(part.get("name"));
            }
            imports.add(importPath.toString());
        }
        return imports;
    }

    private List<Symbol> buildFields(JmmNode classDecl) {
        return classDecl.getChildren(VAR_DECL).stream()
            .map(varDecl -> new Symbol(convertType(varDecl.getChild(0)), varDecl.get("name")))
            .collect(Collectors.toList());
    }
}
