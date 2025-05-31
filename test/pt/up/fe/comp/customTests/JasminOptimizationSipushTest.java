package pt.up.fe.comp.customTests;

import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.specs.util.SpecsCheck;
import pt.up.fe.specs.util.SpecsIo;
import utils.ProjectTestUtils;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class JasminOptimizationSipushTest {
    static JasminResult getJasminResult(String filename) {

        var resource = "pt/up/fe/comp/customTests/jasmin/optimizations/" + filename;

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

    public static void testOllirToJasmin(String resource) {
        testOllirToJasmin(resource, null);
    }

    @Test
    public void sipushTest() {
        JasminResult jasminResult = getJasminResult("sipush1.ollir");
        String expected = SpecsIo.getResource("pt/up/fe/comp/customTests/jasmin/optimizations/sipush1.jasmin");
        String actual = jasminResult.getJasminCode();

        assertEquals("Generated Jasmin code does not match expected", expected.trim(), actual.trim());
    }
}
