package pt.up.fe.comp.customTests;

import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.specs.util.SpecsIo;

public class AssignTypeSemanticAnalysisTest {
    @Test
    public void assignImported() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/assignimported/assignimported.jmm"));
        TestUtils.noErrors(result);
    }
}
