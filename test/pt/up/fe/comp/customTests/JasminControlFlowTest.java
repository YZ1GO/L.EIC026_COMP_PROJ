package pt.up.fe.comp.customTests;

import org.junit.Test;
import pt.up.fe.comp.CpUtils;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.specs.util.SpecsCheck;
import pt.up.fe.specs.util.SpecsIo;
import utils.ProjectTestUtils;

import java.util.Collections;

public class JasminControlFlowTest {


    static JasminResult getJasminResult(String filename) {

        var resource = "pt/up/fe/comp/customTests/jasmin/control_flow/" + filename;

        SpecsCheck.checkArgument(resource.endsWith(".ollir"), () -> "Expected resource to end with .ollir: " + resource);

        var ollirResult = new OllirResult(SpecsIo.getResource(resource), Collections.emptyMap());

        var result = TestUtils.backend(ollirResult);

        return result;

    }

    public static void testOllirToJasmin(String resource, String expectedOutput) {
        SpecsCheck.checkArgument(resource.endsWith(".ollir"), () -> "Expected resource to end with .ollir: " + resource);

        var ollirResult = new OllirResult(SpecsIo.getResource(resource), Collections.emptyMap());

        var result = TestUtils.backend(ollirResult);

        ProjectTestUtils.runJasmin(result, null);
    }


    @Test
    public void controlFlow_ifNoElse1() {
        CpUtils.runJasmin(getJasminResult("if.ollir"), "Result: 10");
    }

    @Test
    public void controlFlow_ifNoElse2() {
        CpUtils.runJasmin(getJasminResult("if2.ollir"), "Result: 5");
    }

    @Test
    public void controlFlow_ifElse1() {
        CpUtils.runJasmin(getJasminResult("if3.ollir"), "Result: 10");
    }

    @Test
    public void controlFlow_while1() {
        CpUtils.runJasmin(getJasminResult("while.ollir"), "Result: 1\nResult: 1");
    }
}