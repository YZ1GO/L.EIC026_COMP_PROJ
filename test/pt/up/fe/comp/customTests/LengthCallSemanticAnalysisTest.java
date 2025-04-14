package pt.up.fe.comp.customTests;

import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.specs.util.SpecsIo;

public class LengthCallSemanticAnalysisTest {
    @Test
    public void lengthCall() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/lengthexpr/lengthcall.jmm"));
        TestUtils.noErrors(result);
        System.out.println(result.getReports());
    }

    @Test
    public void lengthCallWrong() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/lengthexpr/lengthcallwrong.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }
}
