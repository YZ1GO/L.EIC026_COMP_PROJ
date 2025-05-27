package pt.up.fe.comp2025.backend;

import org.specs.comp.ollir.*;
import org.specs.comp.ollir.inst.BinaryOpInstruction;
import org.specs.comp.ollir.type.ArrayType;
import org.specs.comp.ollir.type.BuiltinType;
import org.specs.comp.ollir.type.ClassType;
import org.specs.comp.ollir.type.BuiltinKind;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import org.specs.comp.ollir.type.Type;
import pt.up.fe.specs.util.exceptions.NotImplementedException;
import java.util.List;

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

    public String getDescriptor(Type ollirType) {
        if (ollirType instanceof BuiltinType builtinType) {
            return switch (builtinType.getKind()) {
                case INT32 -> "I";
                case BOOLEAN -> "Z";
                case STRING -> "Ljava/lang/String;";
                case VOID -> "V";
            };
        } else if (ollirType instanceof ArrayType arrayType) {
            return "[" + getDescriptor(arrayType.getElementType());
        } else if (ollirType instanceof ClassType classType) {
            return "L" + convertClassPath(classType.getName())+";";
        }

        throw new NotImplementedException("Unsupported type: " + ollirType.getClass().getSimpleName());
    }

    public String getPrefix(Type ollirType) {
        if (ollirType instanceof ArrayType || ollirType instanceof ClassType) {
            return "a";
        }

        if (ollirType instanceof BuiltinType builtinType) {
            return switch (builtinType.getKind()) {
                case INT32, BOOLEAN -> "i";
                case STRING -> "a";
                default -> throw new NotImplementedException(builtinType.getKind());
            };
        }

        throw new NotImplementedException(ollirType);
    }

    public String getArrayType(Type ollirType) {
        if (ollirType instanceof ArrayType arrayType) {
            var elementType = arrayType.getElementType();
            if (elementType instanceof BuiltinType builtinType) {
                return switch (builtinType.getKind()) {
                    case INT32 -> "int";
                    default -> throw new NotImplementedException("Array type not supported for: " + builtinType.getKind());
                };
            }
        }
        throw new NotImplementedException("Array type not supported for: " + ollirType.getClass().getSimpleName());
    }

    public String getMethodDescriptor(Type returnType, List<Element> arguments) {
        var descriptor = new StringBuilder("(");

        for (var arg : arguments) {
            descriptor.append(getDescriptor(arg.getType()));
        }

        descriptor.append(")");
        descriptor.append(getDescriptor(returnType));

        return descriptor.toString();
    }

    public String istore(int reg) {
        return reg >= 0 && reg <= 3 ? "istore_" + reg : "istore " + reg;
    }

    public String iload(int reg) {
        return reg >= 0 && reg <= 3 ? "iload_" + reg : "iload " + reg;
    }

    public String astore(int reg) {
        return reg >= 0 && reg <= 3 ? "astore_" + reg : "astore " + reg;
    }

    public String aload(int reg) {
        return reg >= 0 && reg <= 3 ? "aload_" + reg : "aload " + reg;
    }

    public boolean isVoid(Type t) {
        return t instanceof BuiltinType
                && ((BuiltinType) t).getKind() == BuiltinKind.VOID;
    }

    public String extractMethodName(Element methodElement) {
        if (methodElement instanceof LiteralElement literalElement) {
            String fullMethodName = literalElement.getLiteral();
            return fullMethodName.contains(".")
                    ? fullMethodName.substring(fullMethodName.indexOf('.') + 1)
                    : fullMethodName;
        } else {
            throw new NotImplementedException("Method name element type: " + methodElement.getClass());
        }
    }

    public String getIf(OperationType op) {
        return switch (op) {
            case NEQ, AND, OR, ANDB, ORB, NOT, NOTB -> "ifne ";
            case EQ -> "ifeq ";
            case LTH -> "iflt ";
            case LTE -> "ifle ";
            case GTH -> "ifgt ";
            case GTE -> "ifge ";
            default -> throw new NotImplementedException(op);
        };
    }

}
