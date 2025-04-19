package pt.up.fe.comp.customTests;

import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.specs.util.SpecsIo;

public class VariableInitializationSemanticAnalysisTest {
    @Test
    public void ifElseBranch() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/variableinitialization/ifelsebranch.jmm"));
        TestUtils.noErrors(result);
        System.out.println(result.getReports());
    }

    @Test
    public void whileBranch() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/variableinitialization/whilebranch.jmm"));
        TestUtils.noErrors(result);
        System.out.println(result.getReports());
    }

    @Test
    public void assignBeforeInit() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/variableinitialization/assignmentbeforeinit.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }

    @Test
    public void paramVariable() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/variableinitialization/paramvariable.jmm"));
        TestUtils.noErrors(result);
        System.out.println(result.getReports());
    }

    @Test
    public void fieldVariable() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/variableinitialization/fieldvariable.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }

    @Test
    public void arrayIndexNotInit() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/variableinitialization/arrayindexnotinitialized.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }
}
