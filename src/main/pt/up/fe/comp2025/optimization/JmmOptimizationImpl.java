package pt.up.fe.comp2025.optimization;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.CompilerConfig;

import java.util.Collections;

public class JmmOptimizationImpl implements JmmOptimization {

    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult) {

        // Create visitor that will generate the OLLIR code
        var visitor = new OllirGeneratorVisitor(semanticsResult.getSymbolTable());

        // Visit the AST and obtain OLLIR code
        var ollirCode = visitor.visit(semanticsResult.getRootNode());

        //System.out.println("\nOLLIR:\n\n" + ollirCode);

        return new OllirResult(semanticsResult, ollirCode, Collections.emptyList());
    }

    // AST-based optimizations
    @Override
    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {

        //TODO: Do your AST-based optimizations here
        //DONE: Constant propagation and folding implemented
        boolean optimize = CompilerConfig.getOptimize(semanticsResult.getConfig());

        if (optimize) {
            boolean changed;
            do {
                ConstantPropagationVisitor cpVisitor = new ConstantPropagationVisitor();
                cpVisitor.visit(semanticsResult.getRootNode());

                ConstantFoldingVisitor cfVisitor = new ConstantFoldingVisitor();
                cfVisitor.visit(semanticsResult.getRootNode());

                changed = cpVisitor.isChanged() || cfVisitor.isChanged();
            } while (changed);
        }

        return semanticsResult;
    }

    // OLLIR-based optimizations
    @Override
    public OllirResult optimize(OllirResult ollirResult) {
        int maxRegs = CompilerConfig.getRegisterAllocation(ollirResult.getConfig());

        // no optimizations
        if (maxRegs == -1) {
            return ollirResult;
        }

        /* Steps:
        1. CFG - Use buildCFGs() to understand the program flow
        2. Liveness - Figure out where each variable is alive
        3. Interference Graph - Who is alive at the same time (can’t share a register)
        4. Graph Coloring - Assign registers so no interfering variables get the same one
        5. Update varTable - Tell OLLIR your final register plan

        source: https://docs.google.com/document/d/14_l17ffME6HbCc1F3-NH-Df8czx0aTg8Myfkt8WMDxE/edit?tab=t.0#heading=h.do4dn22opt57
         */

        int neededRegs = maxRegs;
        while (true) {
            ollirResult.getOllirClass().buildCFGs();
            var alloc = new RegisterAlloc(ollirResult.getOllirClass(), neededRegs);
            if (alloc.run()) {
                break;
            }
            neededRegs++;
        }


        var regMap = new StringBuilder();
        if (maxRegs == 0) {
            regMap.append(RegisterAlloc.getALlVarRegMap(ollirResult.getOllirClass().getMethods()));

            ollirResult.getReports().add(Report.newLog(
                    Stage.OPTIMIZATION, 0, 0,
                    String.format("Successfully allocated with %d registers\n%s", neededRegs, regMap),
                    null));
        } else {
            if (maxRegs != neededRegs) {
                ollirResult.getReports().add(Report.newError(
                        Stage.OPTIMIZATION, 0, 0,
                        String.format("Cannot allocate with %d register(s). It needs at least %d registers.", maxRegs, neededRegs),
                        null
                ));
            } else {
                regMap.append(RegisterAlloc.getALlVarRegMap(ollirResult.getOllirClass().getMethods()));

                ollirResult.getReports().add(Report.newLog(
                        Stage.OPTIMIZATION, 0, 0,
                        String.format("Successfully allocated with %d registers\n%s", maxRegs, regMap),
                        null));
            }
        }

        return ollirResult;
    }


}
