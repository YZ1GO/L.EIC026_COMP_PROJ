package pt.up.fe.comp.customTests;

import org.junit.Before;
import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp2025.optimization.OptUtils;
import pt.up.fe.specs.util.SpecsIo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OllirArraysTest {
    private boolean testOllirGeneration(String jmmFile, String ollirFile) {
        OllirResult result = getOllirResult(jmmFile);
        String expectedOllir = SpecsIo.getResource("pt/up/fe/comp/customTests/ollir/" + ollirFile).trim();

        System.out.println("Generated OLLIR:");
        System.out.println(result.getOllirCode());

        assertEquals(result.getOllirCode().trim().replaceAll("\\s+", " "), expectedOllir.trim().replaceAll("\\s+", " "));

        /*if (!result.getOllirCode().trim().replaceAll("\\s+", " ")
                .equals(expectedOllir.trim().replaceAll("\\s+", " "))) {
            System.out.println("OLLIR output does not match expected result.");
            return false;
        }*/
        return true;
    }
    static OllirResult getOllirResult(String filename) {
        return TestUtils.optimize(SpecsIo.getResource("pt/up/fe/comp/customTests/ollir/" + filename));
    }

    @Before
    public void setup() {
        OptUtils.resetWhileLabelCounter();
    }

    @Test
    public void testArrayAccess() {
        assertTrue(testOllirGeneration("ArrayAccess.jmm", "ArrayAccess.ollir"));
    }

    @Test
    public void testArrayInit() {
        assertTrue(testOllirGeneration("ArrayInit.jmm", "ArrayInit.ollir"));
    }

    @Test
    public void testComplexArrayAccess() {
        assertTrue(testOllirGeneration("ComplexArrayAccess.jmm", "ComplexArrayAccess.ollir"));
    }

    @Test
    public void testVarArgs() {
        assertTrue(testOllirGeneration("VarArgs.jmm", "VarArgs.ollir"));
    }

    @Test
    public void testVarArgs2() {
        assertTrue(testOllirGeneration("VarArgs2.jmm", "VarArgs2.ollir"));
    }

    @Test
    public void testVarArgs3() {
        assertTrue(testOllirGeneration("VarArgs3.jmm", "VarArgs3.ollir"));
    }
}
