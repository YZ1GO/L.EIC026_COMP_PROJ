package pt.up.fe.comp.customTests;

import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.specs.util.SpecsIo;

import static org.junit.Assert.assertTrue;

public class OllirArithmeticTest {
    private boolean testOllirGeneration(String jmmFile, String ollirFile) {
        OllirResult result = getOllirResult(jmmFile);
        String expectedOllir = SpecsIo.getResource("pt/up/fe/comp/customTests/ollir/" + ollirFile).trim();

        System.out.println("Generated OLLIR:");
        System.out.println(result.getOllirCode());

        if (!result.getOllirCode().trim().replaceAll("\\s+", " ")
                .equals(expectedOllir.trim().replaceAll("\\s+", " "))) {
            System.out.println("OLLIR output does not match expected result.");
            return false;
        }
        return true;
    }
    static OllirResult getOllirResult(String filename) {
        return TestUtils.optimize(SpecsIo.getResource("pt/up/fe/comp/customTests/ollir/" + filename));
    }

    @Test
    public void testBinaryAdd() {
        assertTrue(testOllirGeneration("BinaryAdd.jmm", "BinaryAdd.ollir"));
    }

    @Test
    public void testBinaryAnd() {
        assertTrue(testOllirGeneration("BinaryAnd.jmm", "BinaryAnd.ollir"));
    }

}