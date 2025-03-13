package pt.up.fe.comp.customTests;

import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.specs.util.SpecsIo;

public class DuplicateMethodSemanticAnalysisTest {
    @Test
    public void duplicatedMethod() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/duplicatemethod/duplicatedmethod.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }

    @Test
    public void overloadedMethod() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/duplicatemethod/overloadedmethod.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }
}

