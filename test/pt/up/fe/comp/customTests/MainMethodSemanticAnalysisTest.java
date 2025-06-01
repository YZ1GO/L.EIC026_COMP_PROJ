package pt.up.fe.comp.customTests;

import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.specs.util.SpecsIo;

public class MainMethodSemanticAnalysisTest {

    @Test
    public void validMainMethod() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/mainmethod/ValidMainMethod.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void mainMethodMissingPublic() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/mainmethod/MainMethodMissingPublic.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }

    @Test
    public void mainMethodMissingStatic() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/mainmethod/MainMethodMissingStatic.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }

    @Test
    public void mainMethodIncorrectReturnType() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/mainmethod/MainMethodIncorrectReturnType.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }

    @Test
    public void mainMethodIncorrectParameterType() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/mainmethod/MainMethodIncorrectParameterType.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }

    @Test
    public void mainMethodNoParameters() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/mainmethod/MainMethodNoParameters.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }
}