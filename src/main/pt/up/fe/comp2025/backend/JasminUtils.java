package pt.up.fe.comp2025.backend;

import org.specs.comp.ollir.*;
import org.specs.comp.ollir.type.ArrayType;
import org.specs.comp.ollir.type.BuiltinType;
import org.specs.comp.ollir.type.ClassType;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import org.specs.comp.ollir.type.Type;

public class JasminUtils {

    private final OllirResult ollirResult;

    public JasminUtils(OllirResult ollirResult) {
        // Can be useful to have if you expand this class with more methods
        this.ollirResult = ollirResult;
    }

    // convert a class name into a JVM-compatible class path
    public String convertClassPath(String className) {
        for (var s : ollirResult.getOllirClass().getImports()) {
            String[] p = s.split("\\.");
            String last = p[p.length - 1];

            if (last.startsWith(".")) {
                last = last.substring(1);
            }

            if (last.equals(className)) {
                return s.replace(".", "/");
            }
        }

        return className;
    }

    public String getModifier(AccessModifier accessModifier) {
        return accessModifier != AccessModifier.DEFAULT ?
                accessModifier.name().toLowerCase() + " " :
                "";
    }

    public String getType(Type ollirType) {
        if (ollirType instanceof BuiltinType builtinType) {
            return switch (builtinType.getKind()) {
                case INT32 -> "I";
                case BOOLEAN -> "Z";
                case STRING -> "Ljava/lang/String;";
                case VOID -> "V";
            };
        } else if (ollirType instanceof ArrayType arrayType) {
            return "[I";
        } else if (ollirType instanceof ClassType classType) {
            return "L"+convertClassPath(classType.getName())+";";
        }

        return null;
    }
}
