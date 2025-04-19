package pt.up.fe.comp2025.optimization.regAlloc;

import org.antlr.v4.runtime.misc.Pair;
import org.specs.comp.ollir.*;
import org.specs.comp.ollir.inst.AssignInstruction;
import org.specs.comp.ollir.inst.BinaryOpInstruction;
import org.specs.comp.ollir.inst.ReturnInstruction;
import org.specs.comp.ollir.inst.SingleOpInstruction;

import java.util.*;

public class RegisterAlloc {

    // For each method, and for each instruction (node) store a set of LiveIn and LiveOut
    HashMap<Method, HashMap<Node, LiveSet>> sets = new HashMap<>();
    HashMap<Method, HashMap<String, Set<String>>> intGraph = new HashMap<>();
    HashMap<Method, HashMap<String, Integer>> color = new HashMap<>();

    ClassUnit cfg;
    int maxRegs;

    public RegisterAlloc(ClassUnit cfg, int maxRegs) {
        this.cfg = cfg;
        this.maxRegs = maxRegs;
    }

    public boolean alloc() {
        createSets();
        createIntGraph();
        return createColor();
    }

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
                if (met.getVarTable().get(var).getScope().equals(VarScope.LOCAL)) {
                    intGraph.get(met).put(var, new HashSet<>());
                }
            }
        }

        // edges
        for (var meth : cfg.getMethods()) {
            for (var inst : sets.get(meth).keySet()) {
                var liveSet = new LiveSet(sets.get(meth).get(inst).liveIn, sets.get(meth).get(inst).liveOut);
                liveSet.addLiveOut(def(inst));


                for (var i : liveSet.liveOut) {

                    for (var j : liveSet.liveOut) {

                        if (!i.equals(j)) {
                            intGraph.get(meth).get(i).add(j);
                            intGraph.get(meth).get(j).add(i);
                        }
                    }
                }


                for (var i : liveSet.liveIn) {

                    for (var j : liveSet.liveIn) {

                        if (!i.equals(j)) {
                            intGraph.get(meth).get(i).add(j);
                            intGraph.get(meth).get(j).add(i);
                        }
                    }
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
            color.put(meth, new HashMap<>());
            Stack<Pair<String, Set<String>>> s = new Stack<>();

            while (!intGraph.get(meth).isEmpty()) {
                var found = false;
                var ks = new ArrayList<>(intGraph.get(meth).keySet());
                for (var k : ks) {
                    var paramSize = meth.getParams().size() - 1;
                    if (intGraph.get(meth).get(k).size() < (maxRegs - paramSize) || maxRegs == 0) {
                        found = true;
                        s.add(new Pair<>(k, intGraph.get(meth).get(k)));
                        intGraph.get(meth).remove(k);
                    }
                }
                if (!found) return false;
            }

            // alloc
            int reg = meth.getParams().size() + 1;
            while (!s.empty()) {
                var top = s.pop();
                List<Integer> minN = new ArrayList<>();
                intGraph.get(meth).put(top.a, top.b);

                for (var n : top.b) {
                    minN.add(color.get(meth).get(n));
                }



                Integer c = null; // initial null
                var realMax = maxRegs;
                if (maxRegs == 0) realMax = Integer.MAX_VALUE;

                for (var r = reg; r < realMax; r++) {
                    if (!minN.contains(r)) {
                        c = r;
                        break;
                    }
                }

                if (c == null) return false;
                meth.getVarTable().get(top.a).setVirtualReg(c);
                color.get(meth).put(top.a, c);
            }
        }

        return true;
    }
}
