package pt.up.fe.comp2025.optimization;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.ast.TypeUtils;
import pt.up.fe.specs.util.collections.AccumulatorMap;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static pt.up.fe.comp2025.ast.Kind.TYPE;

/**
 * Utility methods related to the optimization middle-end.
 */
public class OptUtils {

    private static final AtomicInteger ifLabelCounter = new AtomicInteger();

    private static final AtomicInteger whileLabelCounter = new AtomicInteger();

    private final AccumulatorMap<String> temporaries;

    private final TypeUtils types;

    public OptUtils(TypeUtils types) {
        this.types = types;
        this.temporaries = new AccumulatorMap<>();
    }

    public String nextTemp() {
        return nextTemp("tmp");
    }

    public String nextTemp(String prefix) {
        var nextTempNum = temporaries.add(prefix) - 1;

        return prefix + nextTempNum;
    }

    public static List<String> getIfLabels() {
        int id = ifLabelCounter.getAndIncrement();
        return List.of("then" + id, "endif" + id);
    }

    public static List<String> getWhileLabels() {
        int id = whileLabelCounter.getAndIncrement();
        return List.of("while" + id, "endWhile" + id);
    }

    public String toOllirType(JmmNode typeNode) {

        TYPE.checkOrThrow(typeNode);

        return toOllirType(types.convertType(typeNode));
    }

    public String toOllirType(Type type) {
        return (type.isArray() ? ".array" : "") + toOllirType(type.getName());
    }

    private String toOllirType(String typeName) {

        String type = "." + switch (typeName) {
            case "int" -> "i32";
            case "boolean" -> "bool";
            case "void" -> "V";
            default -> {
                if (typeName.endsWith("[]")) {
                    String baseType = typeName.substring(0, typeName.length() - 2);
                    yield toOllirType(baseType) + "[]";
                }
                if (typeName.matches("[a-zA-Z_$][a-zA-Z0-9_$]*")) {
                    yield typeName;
                }
                throw new NotImplementedException("Unsupported type: " + typeName);
            }
        };

        return type;
    }


}
