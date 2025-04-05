package pt.up.fe.comp.customTests;

import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.specs.util.SpecsIo;

import static org.junit.Assert.assertTrue;

/**
 * Custom Ollir generation tests to compare ollir generated from .jmm code to existent .ollir
 */
public class OllirGenerationTest {
    private boolean testOllirGeneration(String jmmFile, String ollirFile) {
        OllirResult result = TestUtils.optimize(SpecsIo.getResource("pt/up/fe/comp/customTests/ollir/" + jmmFile));
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

    @Test
    public void testBasicClass() {
        assertTrue(testOllirGeneration("BasicClass.jmm", "BasicClass.ollir"));
    }

    @Test
    public void testClassField1() {
        assertTrue(testOllirGeneration("ClassField.jmm", "ClassField.ollir"));
    }

    @Test
    public void testClassField2() {
        assertTrue(testOllirGeneration("ClassField2.jmm", "ClassField2.ollir"));
    }

    @Test
    public void testBooleanLiteral() {assertTrue(testOllirGeneration("BooleanLiteral.jmm", "BooleanLiteral.ollir"));}

    @Test
    public void testParentExpr() {assertTrue(testOllirGeneration("ParentExpr.jmm", "ParentExpr.ollir"));}

    @Test
    public void testImport() {assertTrue(testOllirGeneration("Import.jmm", "Import.ollir"));}
}
