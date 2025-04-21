package pt.up.fe.comp2025.optimization;

import org.antlr.v4.runtime.misc.Pair;
import org.specs.comp.ollir.*;
import org.specs.comp.ollir.inst.AssignInstruction;
import org.specs.comp.ollir.inst.BinaryOpInstruction;
import org.specs.comp.ollir.inst.ReturnInstruction;
import org.specs.comp.ollir.inst.SingleOpInstruction;

import java.util.*;

public class RegisterAlloc {


    private final String VAR_THIS = "this";


    // For each method, and for each instruction (node) store a set of LiveIn and LiveOut
    private final HashMap<Method, HashMap<Node, LiveSet>> sets = new HashMap<>();

    // For each method, store a graph: variable -> {conflicting variables}
    private final HashMap<Method, HashMap<String, Set<String>>> intGraph = new HashMap<>();

    // For each method, map each variable to register
    private final HashMap<Method, HashMap<String, Integer>> color = new HashMap<>();


    private final ClassUnit cfg;
    private final int maxRegs;


    public RegisterAlloc(ClassUnit cfg, int maxRegs) {
        this.cfg = cfg;
        this.maxRegs = maxRegs;
    }

    public boolean run() {
        createSets();
        createIntGraph();
        return createColor();
    }

    /*
        Liveness analysis
     */
    private void createSets() {
        for (var met: cfg.getMethods()) {
            sets.put(met, new HashMap<>());

            for (var inst : met.getInstructions()) {
                sets.get(met).put(inst, new LiveSet());
            }
        }

        for (var met: cfg.getMethods()) {
            boolean changed;

            do {
                changed = false;

                for (var inst : met.getInstructions()) {
                    var oldSet = sets.get(met).get(inst);
                    var newSet = new LiveSet();
                    newSet.addLiveOut(oldSet.liveOut);


                    // LIVEin(n) = use(n) UNION (LIVEout(n) - def(n))
                    newSet.removeLiveOut(def(inst));
                    newSet.addLiveIn(use(inst));
                    newSet.addLiveIn(newSet.liveOut);
                    newSet.cleanLiveOut();


                    // LIVEout(n) = UNION LIVEin(s) (s any successor of n)
                    for (var s : inst.getSuccessors()) {
                        if (s.getNodeType() != NodeType.END) {
                           newSet.addLiveOut(sets.get(met).get(s).liveIn);
                        }
                    }

                    sets.get(met).put(inst, newSet);

                    if (!LiveSet.isEqual(oldSet, newSet)) changed = true;
                }
            } while (changed);
        }
    }

    // Def: Write to a variable (e.g., instruction a = 0 will have `a` in its def set)
    private Set<String> def(Node instruction) {
        Set<String> res = new HashSet<>();
        var type = instruction.toInstruction().getInstType();
        if (type == InstructionType.ASSIGN) {
            AssignInstruction assign = (AssignInstruction) instruction;
            res.add(((Operand) assign.getDest()).getName());
        }
        return res;
    }

    // Use: Read of a variable (e.g., instruction a = b + c will have `b` and `c` in its use set)
    private Set<String> use(Node instruction) {
        Set<String> res = new HashSet<>();
        var type = instruction.toInstruction().getInstType();

        switch (type) {
            case ASSIGN:
                AssignInstruction assign = (AssignInstruction) instruction;
                var rhsType = assign.getRhs().getInstType();

                switch (rhsType) {
                    case BINARYOPER:
                        // binary
                        BinaryOpInstruction binaryRhs = (BinaryOpInstruction) assign.getRhs();
                        if (!binaryRhs.getLeftOperand().isLiteral()) {
                            res.add(((Operand) binaryRhs.getLeftOperand()).getName());
                        }
                        if (!binaryRhs.getRightOperand().isLiteral()) {
                            res.add(((Operand) binaryRhs.getRightOperand()).getName());
                        }

                        break;
                    case NOPER:
                        // single
                        SingleOpInstruction singleRhs = (SingleOpInstruction) assign.getRhs();
                        if (!singleRhs.getSingleOperand().isLiteral()) {
                            res.add(((Operand) singleRhs.getSingleOperand()).getName());
                        }

                        break;
                }
                break;

            case RETURN:
                ReturnInstruction returnInst = (ReturnInstruction) instruction;

                if (returnInst.hasReturnValue()) {
                    Optional<Element> element = returnInst.getOperand();
                    if (element.isPresent() && !element.get().isLiteral()) {
                        res.add(((Operand) element.get()).getName());
                    }
                }

                break;
        }

        return res;
    }


    private void createIntGraph() {
        for (var met : cfg.getMethods()) {
            intGraph.put(met, new HashMap<>());

            for (var var : met.getVarTable().keySet()) {
                if (var.equals(VAR_THIS)) continue;

                if (met.getVarTable().get(var).getScope().equals(VarScope.LOCAL) ) {
                    intGraph.get(met).put(var, new HashSet<>());
                }
            }
        }

        // edges
        for (var meth : cfg.getMethods()) {
            var methGraph = intGraph.get(meth);
            var liveSets = sets.get(meth);

            for (var inst : liveSets.keySet()) {
                var liveSet = liveSets.get(inst);
                Set<String> liveOutSet = new HashSet<>(liveSet.liveOut);
                liveOutSet.addAll(def(inst));

                addEdgesForLiveSet(methGraph, liveOutSet);
                addEdgesForLiveSet(methGraph, liveSet.liveIn);
            }
        }
    }

    // aux to add edges for given live set
    private void addEdgesForLiveSet(Map<String, Set<String>> methGraph, Set<String> liveSet) {
        for (var i : liveSet) {
            if (i.equals(VAR_THIS)) continue;
            for (var j : liveSet) {
                if (!i.equals(j) && !j.equals(VAR_THIS)) {
                    methGraph.get(i).add(j);
                    methGraph.get(j).add(i);
                }
            }
        }
    }


    /*
    Returns:
    - True if register allocation succeeded or every variable in every method was successfully
    assigned a register number that doesn't conflict with its neighbors
    - False if register allocation failed or here wasn't a valid way to assign registers using
    the available number of registers (maxRegisters), especially after accounting for parameters
     */
    private boolean createColor() {
        for (var meth : cfg.getMethods()) {
            // static methods don’t have `this`
            if (meth.isStaticMethod()) continue;

            var varTable = meth.getVarTable();
            if (varTable.containsKey(VAR_THIS)) {
                varTable.get(VAR_THIS).setVirtualReg(0);
                color.computeIfAbsent(meth, k -> new HashMap<>()).put(VAR_THIS, 0);
            }
        }

        for (var meth : cfg.getMethods()) {
            color.put(meth, new HashMap<>());


            var methGraph = intGraph.get(meth);
            var varTable = meth.getVarTable();
            var paramSize = meth.getParams().size();

            // regs
            var reserved = paramSize + 1; // including 'this'
            var available = (maxRegs == 0) ? Integer.MAX_VALUE : maxRegs - paramSize;


            Stack<Pair<String, Set<String>>> stack = new Stack<>();
            Set<String> nodes = new HashSet<>(methGraph.keySet());


            // stack build
            while (!nodes.isEmpty()) {
                boolean allocated = false;
                for (String node : new HashSet<>(nodes)) {
                    if (methGraph.get(node).size() < available) {
                        stack.push(new Pair<>(node, methGraph.remove(node)));
                        nodes.remove(node);
                        allocated = true;
                    }
                }
                if (!allocated) return false;
            }
            methGraph = intGraph.get(meth);


            // assign colors
            while (!stack.isEmpty()) {
                Pair<String, Set<String>> node = stack.pop();
                String var = node.a;
                Set<String> neighbors = node.b;

                methGraph.put(var, neighbors);

                Set<Integer> usedColors = new HashSet<>();
                for (String n : neighbors) {
                    Integer neighborColor = color.get(meth).get(n);
                    if (neighborColor != null) usedColors.add(neighborColor);
                }

                Integer reg = null;
                for (int i = reserved; i < reserved + available; i++) {
                    if (!usedColors.contains(i)) {
                        reg = i;
                        break;
                    }
                }

                if (reg == null) return false;

                color.get(meth).put(var, reg);
                varTable.get(var).setVirtualReg(reg);
            }
        }

        return true;
    }
}
