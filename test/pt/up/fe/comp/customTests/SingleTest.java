package pt.up.fe.comp.customTests;

import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.specs.util.SpecsIo;

public class SingleTest {
    @Test
    public void customTest() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/singlecustomtest.jmm"));
        TestUtils.mustFail(result);
        //TestUtils.noErrors(result);
        System.out.println(result.getReports());
        // Print AST
        System.out.println("AST:");
        System.out.println(result.getRootNode().toTree());

    }
}

