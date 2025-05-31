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

import static pt.up.fe.comp2025.backend.JasminUtils.*;

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
    private int boolId = 0;

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
        generators.put(UnaryOpInstruction.class, this::generateUnaryOp);
        generators.put(ReturnInstruction.class, this::generateReturn);
        generators.put(NewInstruction.class, this::generateNew);
        generators.put(InvokeStaticInstruction.class, this::generateInvokeStatic);
        generators.put(GotoInstruction.class, this::generateGoto);
        generators.put(PutFieldInstruction.class, this::generatePutField);
        generators.put(GetFieldInstruction.class, this::generateGetField);
        generators.put(InvokeSpecialInstruction.class, this::generateInvokeSpecial);
        generators.put(InvokeVirtualInstruction.class, this::generateInvokeVirtual);
        generators.put(ArrayLengthInstruction.class, this::generateArrayLength);
        generators.put(OpCondInstruction.class, this::generateOpCondition);
        generators.put(SingleOpCondInstruction.class, this::generateSingleOpCondition);
        generators.put(LdcInstruction.class, this::generateLdc);

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
            var am = getModifier(f.getFieldAccessModifier());

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
        // DONE: expanded
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
        var tempCode = new StringBuilder();
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
        var reg = currentMethod.getVarTable().get(operand.getName()).getVirtualReg();
        //System.out.println("Var name: " + operand.getName() + ", reg: " + reg);

        //TODO: when applying iinc care that iinc is byte -128 -> 127
        // a = a + 1
        // a = 1 + a
        // a = a - 1
        // care a = 1 - a cant use iinc
        // DONE

        // issue here, as we have tmp in the middle, instead of i = i + 1, we have tmp = i + 1 and i = tmp
        // this makes the registers to not be the same
        // which we still have to ensure the regs to be the same to apply the "iinc"
        // otherwise it has a risk to apply it in a trap case: i = a + 1 (which we are not checking)

        // done : tests beginning from ollir instead of jmm solved this issue

        var rhs = assign.getRhs();
        if (rhs instanceof BinaryOpInstruction rhsBinOp) {

            // operand <-> literal
            if (rhsBinOp.getLeftOperand() instanceof Operand l &&
                rhsBinOp.getRightOperand() instanceof LiteralElement r) {
                var value = Integer.parseInt(r.getLiteral());
                var valueConverted = convertValue(rhsBinOp.getOperation().getOpType(), value);
                var regL = currentMethod.getVarTable().get(l.getName()).getVirtualReg();
                //System.out.println("reg: " + reg );
                //System.out.println("regL: " + regL );
                if (reg == regL && valueConverted >= -128 && valueConverted <= 127) {
                    code.append("iinc ")
                            .append(regL)
                            .append(" ")
                            .append(valueConverted)
                            .append(NL);
                    return code.toString();
                }
            }

            // literal <-> operand
            if (rhsBinOp.getLeftOperand() instanceof LiteralElement l &&
                rhsBinOp.getRightOperand() instanceof Operand r) {

                var opType = rhsBinOp.getOperation().getOpType();
                var value = Integer.parseInt(l.getLiteral());
                var valueConverted = convertValue(opType, value);
                var regR = currentMethod.getVarTable().get(r.getName()).getVirtualReg();

                // cannot be a = 1 - a (subtraction)
                if (isAddition(opType) && reg == regR && valueConverted >= -128 && valueConverted <= 127) {
                    code.append("iinc ")
                        .append(regR)
                        .append(" ")
                        .append(valueConverted)
                        .append(NL);
                    return code.toString();
                }
            }
        }


        if (lhs instanceof ArrayOperand lhsArr) {
            stackSize++;
            updateStackSize();

            code.append(aload(reg))
                    .append(NL)
                    .append(apply(lhsArr.getIndexOperands().getFirst()))
                    .append(apply(assign.getRhs()))
                    .append("iastore")
                    .append(NL);

            /* to exec iastore, the stack must contain:
            - arrayref –> the reference to the array (aload reg)
            - index – the position within the array (apply(lhsArr.getIndexOperands().getFirst()))
            - value – the int value to store (apply(assign.getRhs()))
            */

            stackSize -= 3;
            return code.toString();
        }

        var rhsCode = apply(assign.getRhs());
        code.append(rhsCode);

        // TODO: Hardcoded for int type, needs to be expanded
        // DONE: expanded
        var operatorType =types.getDescriptor(operand.getType());
        if (operatorType.equals("I")) { // INT32
            code.append(istore(reg)).append(NL);

        } else if (operatorType.equals("Z")) {  //BOOLEAN

            // is binary op, use cmp_true_n
            if ((assign.getRhs() instanceof BinaryOpInstruction bOp)) {
                var conditionTypeComplete = getIf(bOp.getOperation().getOpType());                          // "ifne "
                var conditionType = conditionTypeComplete.replaceAll("if|\\s", "");       // "ne"
                var labelTrue = "cmp_" + conditionType + "_" + boolId + "_true";
                var labelEnd = "cmp_" + conditionType + "_" + boolId + "_end";

                code.append(conditionTypeComplete)
                        .append(labelTrue)
                        .append(NL)
                        .append("iconst_0")
                        .append(NL)
                        .append("goto ").append(labelEnd)
                        .append(NL).append(NL)
                        .append(labelTrue).append(":")
                        .append(NL)
                        .append("iconst_1")
                        .append(NL)
                        .append(labelEnd).append(":")
                        .append(NL)
                        .append(istore(reg))
                        .append(NL);
                boolId++;

                //updateStackSize();
                //stackSize--;
            } else {
                code.append(istore(reg)).append(NL);
            }

        } else {    //OTHERS
            code.append(astore(reg)).append(NL);
        }


        updateStackSize();
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

        // looks this should be moved to utils for code organization
        String res = switch (jasminType) {
            case "Z" -> literalValue.equals("1") ? "iconst_1" : "iconst_0";
            case "I" -> {
                int value = Integer.parseInt(literalValue);
                if (value == -1) {
                    yield "iconst_m1";
                } else if (value >= 0 && value <= 5) {
                    yield "iconst_" + value;
                } else if (value >= -128 && value <= 127) {
                    yield "bipush " + value;
                } else if (value >= -32768 && value <= 32767) {
                    yield "sipush " + value;
                }
                yield "ldc " + value;
            }
            default -> "ldc " + literalValue;
        };

        return res + NL;
    }


    private String generateOperand(Operand operand) {
        if (operand instanceof ArrayOperand arrayOperand) {
            return generateArrayAccess(arrayOperand);
        }
        // get register
        var reg = currentMethod.getVarTable().get(operand.getName());
        stackSize++;
        updateStackSize();

        // TODO: Hardcoded for int type, needs to be expanded
        // DONE: expanded
        var prefix = JasminUtils.getPrefix(operand.getType());
        switch (prefix) {
            case "i":
                return iload(reg.getVirtualReg()) + NL;
            case "a":
                return aload(reg.getVirtualReg()) + NL;
            default:
                throw new NotImplementedException("Unsupported prefix: " + prefix);
        }
    }

    private String generateArrayAccess(ArrayOperand arrayOperand) {
        var code = new StringBuilder();

        var arrayReg = currentMethod.getVarTable().get(arrayOperand.getName()).getVirtualReg();
        code.append(aload(arrayReg)).append(NL);
        stackSize++;
        updateStackSize();

        code.append(apply(arrayOperand.getIndexOperands().getFirst()));
        code.append("iaload").append(NL);
        stackSize--;
        return code.toString();
    }

    private String generateBinaryOp(BinaryOpInstruction binaryOp) {
        var code = new StringBuilder();

        // load values on the left and on the right
        code.append(apply(binaryOp.getLeftOperand()));

        code.append(apply(binaryOp.getRightOperand()));

        // TODO: Hardcoded for int type, needs to be expanded
        // DONE: expanded

        // this should be moved to jasminutils
        var type = binaryOp.getOperation().getOpType();
        String op = switch (type) {
            case SUB, EQ, NEQ, LTH, LTE, GTH, GTE  -> "isub";
            case ADD -> "iadd";
            case MUL -> "imul";
            case DIV -> "idiv";
            default -> throw new NotImplementedException(type);
        };

        code.append(op).append(NL);
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
            // DONE: expanded using getPrefix
            var prefix = getPrefix(returnInst.getReturnType());
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
            code.append("newarray ").append(getArrayType(arrayType)).append(NL);
            // newarray maintains the stack size by replacing the size with the reference so no stackSize++ needed
        }else if (callerType instanceof ClassType classType) {
            var className = types.convertClassPath(classType.getName());

            code.append("new ").append(className).append(NL);
            stackSize++;
            updateStackSize();

            code.append("dup").append(NL);
            stackSize++;
            updateStackSize();

            code.append("invokespecial ").append(className).append("/<init>()V").append(NL);
            stackSize--;
        } else {
            throw new NotImplementedException("Unsupported type for 'new': " + callerType);
        }

        return code.toString();
    }

    private String generateInvokeStatic(InvokeStaticInstruction invokeStatic) {
        var code = new StringBuilder();

        for (var arg : invokeStatic.getArguments()) {
            code.append(apply(arg));
        }

        int numArgs = invokeStatic.getArguments().size();
        stackSize -= numArgs;

        boolean hasReturnValue = !isVoid(invokeStatic.getReturnType());
        if (hasReturnValue) {
            stackSize++;
            updateStackSize();
        }

        var className = types.convertClassPath(((Operand) invokeStatic.getCaller()).getName());
        var methodName = extractMethodName(invokeStatic.getMethodName());
        var descriptor = types.getMethodDescriptor(invokeStatic.getReturnType(), invokeStatic.getArguments());

        code.append("invokestatic ")
                .append(className).append("/")
                .append(methodName).append(descriptor)
                .append(NL);

        if (hasReturnValue) {
            code.append("pop").append(NL);
            stackSize--;
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

        return code.toString();
    }

    private String generateInvokeSpecial(InvokeSpecialInstruction invokeSpecial) {
        var code = new StringBuilder();

        var methodName = extractMethodName(invokeSpecial.getMethodName());
        if (methodName.equals("<init>")) {
            return "";
        }

        for (var arg : invokeSpecial.getArguments()) {
            code.append(apply(arg));
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

        for (var arg : invokeVirtual.getArguments()) {
            code.append(apply(arg));
        }

        var className = types.convertClassPath(((ClassType) invokeVirtual.getCaller().getType()).getName());
        var methodName = extractMethodName(invokeVirtual.getMethodName());
        var descriptor = types.getMethodDescriptor(invokeVirtual.getReturnType(), invokeVirtual.getArguments());

        code.append("invokevirtual ")
                .append(className).append("/")
                .append(methodName).append(descriptor)
                .append(NL);

        stackSize -= invokeVirtual.getArguments().size() + 1;
        if (!isVoid(invokeVirtual.getReturnType())) {
            stackSize++;
            updateStackSize();
        }

        return code.toString();
    }

    private String generateArrayLength(ArrayLengthInstruction arrayLengthInst) {
        var code = new StringBuilder();

        code.append(apply(arrayLengthInst.getOperands().getFirst()));

        code.append("arraylength").append(NL);

        stackSize--;

        return code.toString();
    }


    private String generateOpCondition(OpCondInstruction cond) {
        var code = new StringBuilder();

        code.append(apply(cond.getCondition()));

        String op = getIf(cond.getCondition().getOperation().getOpType());

        stackSize--;

        code.append(op).append(cond.getLabel()).append(NL);

        return code.toString();
    }

    private String generateUnaryOp(UnaryOpInstruction unary) {
        var code = new StringBuilder();
        var isLiteral = unary.getOperand().isLiteral();

        if(isLiteral) {

            if (((LiteralElement) unary.getOperand()).getLiteral().equals("1")) code.append("iconst_0");
            else code.append("iconst_1");

            stackSize++;
            updateStackSize();
        } else {
            code.append(apply(unary.getOperand()))
                    .append("iconst_1")
                    .append(NL)
                    .append("ixor")
                    .append(NL);

            stackSize++;
            updateStackSize();
            stackSize--;
        }

        code.append(NL);
        return code.toString();
    }

    private String generateSingleOpCondition(SingleOpCondInstruction singleOpCond) {

        StringBuilder code = new StringBuilder();
        code.append(apply(singleOpCond.getOperands().getFirst()));
        code.append("ifne ").append(singleOpCond.getLabel()).append(NL);

        return code.toString();
    }

    private String generateLdc(LdcInstruction ldcInstruction) {
        var code = new StringBuilder();

        var constant = ldcInstruction.getElement();

        if (constant instanceof LiteralElement literal) {
            String value = literal.getLiteral();
            code.append("ldc \"").append(value).append("\"").append(NL);
        } else {
            throw new NotImplementedException("Unsupported constant type: " + constant.getClass());
        }

        stackSize++;
        updateStackSize();

        return code.toString();
    }
}