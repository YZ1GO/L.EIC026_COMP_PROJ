package pt.up.fe.comp2025.backend;

import org.specs.comp.ollir.*;
import org.specs.comp.ollir.inst.*;
import org.specs.comp.ollir.tree.TreeNode;
import org.specs.comp.ollir.type.ArrayType;
import org.specs.comp.ollir.type.ClassType;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.specs.util.SpecsCheck;
import pt.up.fe.specs.util.classmap.FunctionClassMap;
import pt.up.fe.specs.util.exceptions.NotImplementedException;
import pt.up.fe.specs.util.utilities.StringLines;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Generates Jasmin code from an OllirResult.
 * <p>
 * One JasminGenerator instance per OllirResult.
 */
public class JasminGenerator {

    private static final String NL = "\n";
    private static final String TAB = "   ";

    private final OllirResult ollirResult;

    List<Report> reports;

    String code;

    Method currentMethod;

    private final JasminUtils types;

    private final FunctionClassMap<TreeNode, String> generators;

    private int stackSize = 0;
    private int maxStackSize = 0;

    public JasminGenerator(OllirResult ollirResult) {
        this.ollirResult = ollirResult;

        reports = new ArrayList<>();
        code = null;
        currentMethod = null;

        types = new JasminUtils(ollirResult);

        this.generators = new FunctionClassMap<>();
        generators.put(ClassUnit.class, this::generateClassUnit);
        generators.put(Method.class, this::generateMethod);
        generators.put(AssignInstruction.class, this::generateAssign);
        generators.put(SingleOpInstruction.class, this::generateSingleOp);
        generators.put(LiteralElement.class, this::generateLiteral);
        generators.put(Operand.class, this::generateOperand);
        generators.put(BinaryOpInstruction.class, this::generateBinaryOp);
        generators.put(ReturnInstruction.class, this::generateReturn);
        generators.put(NewInstruction.class, this::generateNew);
        generators.put(InvokeStaticInstruction.class, this::generateInvokeStatic);
        generators.put(GotoInstruction.class, this::generateGoto);
        generators.put(PutFieldInstruction.class, this::generatePutField);
        generators.put(GetFieldInstruction.class, this::generateGetField);
        generators.put(InvokeSpecialInstruction.class, this::generateInvokeSpecial);
        generators.put(InvokeVirtualInstruction.class, this::generateInvokeVirtual);
        generators.put(ArrayLengthInstruction.class, this::generateArrayLength);
    }

    private String apply(TreeNode node) {
        var code = new StringBuilder();

        // Print the corresponding OLLIR code as a comment
        //code.append("; ").append(node).append(NL);

        code.append(generators.apply(node));

        return code.toString();
    }


    public List<Report> getReports() {
        return reports;
    }

    public String build() {

        // This way, build is idempotent
        if (code == null) {
            code = apply(ollirResult.getOllirClass());
        }

        return code;
    }

    private void updateStackSize(){
        if(stackSize > maxStackSize){
            maxStackSize = stackSize;
        }
    }

    private String generateClassUnit(ClassUnit classUnit) {

        var code = new StringBuilder();

        // generate class name
        var className = ollirResult.getOllirClass().getClassName();
        code.append(".class ").append(className).append(NL).append(NL);

        // TODO: When you support 'extends', this must be updated
        // done, not tested
        String extended;
        if (classUnit.getSuperClass() == null || classUnit.getSuperClass().equals("Object")) {
            extended = "java/lang/Object";
            code.append(".super ").append(extended).append(NL);
        } else {
            extended = types.convertClassPath(ollirResult.getClass().getSuperclass().getName());
            code.append(".super ").append(extended).append(NL);
        }

        // fields
        for (var f : classUnit.getFields()) {
            var am = types.getModifier(f.getFieldAccessModifier());

            code.append(".field ")
                    .append(am)
                    .append(f.getFieldName())
                    .append(" ")
                    .append(types.getDescriptor(f.getFieldType()))
                    .append(NL);
        }

        // generate a single constructor method
        var defaultConstructor = """
                ;default constructor
                .method public <init>()V
                    aload_0
                    invokespecial %s/<init>()V
                    return
                .end method
                """.formatted(extended);
        code.append(defaultConstructor);


        // generate code for all other methods
        for (var method : ollirResult.getOllirClass().getMethods()) {

            // Ignore constructor, since there is always one constructor
            // that receives no arguments, and has been already added
            // previously
            if (method.isConstructMethod()) {
                continue;
            }

            code.append(apply(method));
        }

        return code.toString();
    }


    private String generateMethod(Method method) {
        //System.out.println("STARTING METHOD " + method.getMethodName());
        // set method
        currentMethod = method;

        var code = new StringBuilder();

        // calculate modifier
        var modifier = method.getMethodAccessModifier() != AccessModifier.DEFAULT ?
                method.getMethodAccessModifier().name().toLowerCase() + " " : "";

        var methodName = method.getMethodName();


        // TODO: Hardcoded param types and return type, needs to be expanded
        // done i guess, not tested
        code.append("\n.method ").append(modifier);
        if(methodName.equals("main")) {
            code.append("static ").append(methodName).append("([Ljava/lang/String;)V").append(NL);
        } else {
            code.append(methodName).append("(");
            for (Element param : method.getParams()) {
                code.append(types.getDescriptor(param.getType()));
            }
            code.append(")").append(types.getDescriptor(method.getReturnType())).append(NL);
        }


        // Add limits
        var tempCode =new StringBuilder();
        for (var instr : method.getInstructions()) {
            for(var l : method.getLabels(instr)){
                tempCode.append(l).append(":").append(NL);
            }

            tempCode.append(
                    StringLines.getLines(generators.apply(instr))
                            .stream()
                            .collect(Collectors.joining(NL + TAB, TAB, NL))
            );
        }

        code.append(TAB)
                .append(".limit stack ")
                .append(maxStackSize)
                .append(NL);

        Set<Integer> registers = new HashSet<>();
        for(var variable : method.getVarTable().values()){
            if(variable.getScope().equals(VarScope.FIELD)) continue;

            if(!registers.contains(variable.getVirtualReg())){
                registers.add(variable.getVirtualReg());
            }
        }
        var regSize = registers.size();

        if(!registers.contains(0) && !methodName.equals("main")){
            code.append(TAB).append(".limit locals ").append(regSize+1).append(NL);
        } else {
            code.append(TAB).append(".limit locals ").append(regSize).append(NL);
        }

        code.append(tempCode);
        code.append(".end method\n");

        // unset method
        maxStackSize = 0;
        stackSize = 0;

        currentMethod = null;
        //System.out.println("ENDING METHOD " + method.getMethodName());
        return code.toString();
    }

    private String generateAssign(AssignInstruction assign) {
        var code = new StringBuilder();
        // store value in the stack in destination
        var lhs = assign.getDest();
        if (!(lhs instanceof Operand)) {
            throw new NotImplementedException(lhs.getClass());
        }

        var operand = (Operand) lhs;

        // get register
        var reg = currentMethod.getVarTable().get(operand.getName());
        var rhsCode = apply(assign.getRhs());
        code.append(rhsCode);

        // TODO: Hardcoded for int type, needs to be expanded
        // Add more in utils
        var prefix = types.getPrefix(operand.getType());
        switch (prefix) {
            case "i":
                code.append(types.istore(reg.getVirtualReg())).append(NL);
                break;
            case "a":
                code.append(types.astore(reg.getVirtualReg())).append(NL);
                break;
            default:
                throw new NotImplementedException("Unsupported prefix: " + prefix);
        }
        stackSize--;

        return code.toString();
    }

    private String generateSingleOp(SingleOpInstruction singleOp) {
        return apply(singleOp.getSingleOperand());
    }

    private String generateLiteral(LiteralElement literal) {
        stackSize++;
        updateStackSize();

        String literalValue = literal.getLiteral();
        String jasminType = types.getDescriptor(literal.getType());

        switch (jasminType) {
            case "Z":
                return literalValue.equals("true") ? "iconst_1" + NL : "iconst_0" + NL;
            case "I":
                int value = Integer.parseInt(literalValue);
                if (value == -1) {
                    return "iconst_m1" + NL;
                } else if (value >= 0 && value <= 5) {
                    return "iconst_" + value + NL;
                } else if (value >= -128 && value <= 127) {
                    return "bipush " + value + NL;
                } else if (value >= -32768 && value <= 32767) {
                    return "sipush " + value + NL;
                }
                return "ldc " + value + NL;
            default:
                return "ldc " + literalValue + NL;
        }
    }

    //TODO: when applying iinc care that iinc is byte -128 -> 127
    // a = a + 1
    // a = 1 + a
    // a = a - 1
    // care a = 1 - a cant use iinc


    private String generateOperand(Operand operand) {
        // get register
        var reg = currentMethod.getVarTable().get(operand.getName());
        stackSize++;
        updateStackSize();

        // TODO: Hardcoded for int type, needs to be expanded
        var prefix = types.getPrefix(operand.getType());
        switch (prefix) {
            case "i":
                return types.iload(reg.getVirtualReg()) + NL;
            case "a":
                return types.aload(reg.getVirtualReg()) + NL;
            default:
                throw new NotImplementedException("Unsupported prefix: " + prefix);
        }
    }

    private String generateBinaryOp(BinaryOpInstruction binaryOp) {
        var code = new StringBuilder();

        // load values on the left and on the right
        code.append(apply(binaryOp.getLeftOperand()));
        stackSize++;
        updateStackSize();

        code.append(apply(binaryOp.getRightOperand()));
        stackSize++;
        updateStackSize();

        // TODO: Hardcoded for int type, needs to be expanded
        // CHANGES HAVE BEEN MADE
        var opType = binaryOp.getOperation().getOpType();
        switch (opType) {
            case ADD:
                code.append("iadd").append(NL);
                break;
            case MUL:
                code.append("imul").append(NL);
                break;
            default:
                throw new NotImplementedException(opType);
        }
        stackSize--;

        return code.toString();
    }

    private String generateReturn(ReturnInstruction returnInst) {
        var code = new StringBuilder();

        if (returnInst.getOperand().isEmpty()) {
            code.append("return").append(NL);
        } else {
            var loadOperand = apply(returnInst.getOperand().get());

            code.append(loadOperand);
            // TODO: Hardcoded for int type, needs to be expanded
            // Add more in utils
            var prefix = types.getPrefix(returnInst.getReturnType());
            code.append(prefix).append("return").append(NL);

            stackSize--;
        }

        return code.toString();
    }

    private String generateNew(NewInstruction newInst) {
        var callerType = newInst.getCaller().getType();
        var code = new StringBuilder();

        if (callerType instanceof ArrayType arrayType) {
            SpecsCheck.checkArgument(newInst.getArguments().size() == 1,
                    () -> "Expected exactly one argument for array size: " + newInst);

            code.append(apply(newInst.getArguments().getFirst()));

            code.append("newarray ").append(types.getArrayType(arrayType)).append(NL);
            stackSize--;
            updateStackSize();
        }else if (callerType instanceof ClassType classType) {
            var className = types.convertClassPath(classType.getName());

            code.append("new ").append(className).append(NL);
            stackSize++;
            updateStackSize();

            code.append("dup").append(NL);
            stackSize++;
            updateStackSize();

            code.append("invokespecial ").append(className).append("/<init>()V").append(NL);
            stackSize -= 2;
            updateStackSize();
        } else {
            throw new NotImplementedException("Unsupported type for 'new': " + callerType);
        }

        return code.toString();
    }

    private String generateInvokeStatic(InvokeStaticInstruction invokeStatic) {
        var code = new StringBuilder();

        for (var arg : invokeStatic.getArguments()) {
            code.append(apply(arg));
            stackSize++;
            updateStackSize();
        }

        var className = types.convertClassPath(((Operand) invokeStatic.getCaller()).getName());
        var methodName = types.extractMethodName(invokeStatic.getMethodName());
        var descriptor = types.getMethodDescriptor(invokeStatic.getReturnType(), invokeStatic.getArguments());

        code.append("invokestatic ")
                .append(className).append("/")
                .append(methodName).append(descriptor)
                .append(NL);

        if (!types.isVoid(invokeStatic.getReturnType())) {
            code.append("pop").append(NL);
        }

        return code.toString();
    }

    private String generateGoto(GotoInstruction gotoInstruction) {
        var code = new StringBuilder();

        var label = gotoInstruction.getLabel();

        code.append("goto ").append(label).append(NL);

        return code.toString();
    }

    private String generatePutField(PutFieldInstruction putField) {
        var code = new StringBuilder();

        code.append(apply(putField.getObject()));

        code.append(apply(putField.getValue()));

        var fieldName = putField.getField().getName();
        var fieldType = types.getDescriptor(putField.getField().getType());
        var className = types.convertClassPath(((ClassType) putField.getObject().getType()).getName());

        code.append("putfield ")
                .append(className).append("/")
                .append(fieldName).append(" ")
                .append(fieldType).append(NL);

        stackSize -= 2;

        return code.toString();
    }

    private String generateGetField(GetFieldInstruction getField) {
        var code = new StringBuilder();

        code.append(apply(getField.getObject()));

        var fieldName = getField.getField().getName();
        var fieldType = types.getDescriptor(getField.getFieldType());
        var className = types.convertClassPath(((ClassType) getField.getObject().getType()).getName());

        code.append("getfield ")
                .append(className).append("/")
                .append(fieldName).append(" ")
                .append(fieldType).append(NL);

        stackSize++;
        updateStackSize();

        return code.toString();
    }

    private String generateInvokeSpecial(InvokeSpecialInstruction invokeSpecial) {
        var code = new StringBuilder();

        var methodName = types.extractMethodName(invokeSpecial.getMethodName());
        if (methodName.equals("<init>")) {
            return "";
        }

        for (var arg : invokeSpecial.getArguments()) {
            code.append(apply(arg));
            stackSize++;
            updateStackSize();
        }

        code.append(apply(invokeSpecial.getCaller()));

        var className = types.convertClassPath(((ClassType) invokeSpecial.getCaller().getType()).getName());
        var descriptor = types.getMethodDescriptor(invokeSpecial.getReturnType(), invokeSpecial.getArguments());

        code.append("invokespecial ")
                .append(className).append("/")
                .append(methodName).append(descriptor)
                .append(NL);

        stackSize -= invokeSpecial.getArguments().size() + 1;

        return code.toString();
    }

    private String generateInvokeVirtual(InvokeVirtualInstruction invokeVirtual) {
        var code = new StringBuilder();

        code.append(apply(invokeVirtual.getCaller()));
        stackSize++;
        updateStackSize();

        for (var arg : invokeVirtual.getArguments()) {
            code.append(apply(arg));
            stackSize++;
            updateStackSize();
        }

        var className = types.convertClassPath(((ClassType) invokeVirtual.getCaller().getType()).getName());
        var methodName = types.extractMethodName(invokeVirtual.getMethodName());
        var descriptor = types.getMethodDescriptor(invokeVirtual.getReturnType(), invokeVirtual.getArguments());

        code.append("invokevirtual ")
                .append(className).append("/")
                .append(methodName).append(descriptor)
                .append(NL);

        stackSize -= invokeVirtual.getArguments().size() + 1;
        if (!types.isVoid(invokeVirtual.getReturnType())) {
            stackSize++;
        }
        updateStackSize();

        return code.toString();
    }

    private String generateArrayLength(ArrayLengthInstruction arrayLengthInst) {
        var code = new StringBuilder();

        code.append(apply(arrayLengthInst.getOperands().getFirst()));

        code.append("arraylength").append(NL);

        stackSize--;
        updateStackSize();

        return code.toString();
    }
}