package pt.up.fe.comp2025.utils;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;

public class GetPutFieldUtils {
    public static boolean isClassField(String varName, String methodName, SymbolTable table) {
        boolean isField = table.getFields().stream().anyMatch(field -> field.getName().equals(varName));
        boolean isParam = table.getParameters(methodName).stream().anyMatch(param -> param.getName().equals(varName));
        boolean isLocal = table.getLocalVariables(methodName).stream().anyMatch(local -> local.getName().equals(varName));
        return isField && !isParam && !isLocal;
    }
}
