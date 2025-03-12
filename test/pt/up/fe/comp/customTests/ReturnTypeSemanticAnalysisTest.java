package pt.up.fe.comp.customTests;

import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.specs.util.SpecsIo;

public class ReturnTypeSemanticAnalysisTest {

    @Test
    public void validReturnType() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/returntype/ValidReturnType.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void incorrectReturnType() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/returntype/IncorrectReturnType.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }

    @Test
    public void validReturnTypeArray() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/returntype/ValidReturnTypeArray.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void incorrectReturnTypeArray() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/returntype/IncorrectReturnTypeArray.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }
}