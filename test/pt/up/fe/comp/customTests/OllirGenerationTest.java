package pt.up.fe.comp.customTests;

import org.junit.Test;
import org.specs.comp.ollir.inst.GotoInstruction;
import pt.up.fe.comp.CpUtils;
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
    public void testClassFieldArray() {
        assertTrue(testOllirGeneration("ClassFieldArray.jmm", "ClassFieldArray.ollir"));
    }

    @Test
    public void testMethodVarDeclr() {
        assertTrue(testOllirGeneration("MethodVarDeclr.jmm", "MethodVarDeclr.ollir"));
    }

    @Test
    public void testIfThenElse() {
        assertTrue(testOllirGeneration("IfThenElse.jmm", "IfThenElse.ollir"));
    }

    // some differences
    @Test
    public void testIfThenElseMultiple() {
        OllirResult result = TestUtils.optimize(SpecsIo.getResource("pt/up/fe/comp/customTests/ollir/IfThenElseMultiple.jmm"));
        var method = CpUtils.getMethod(result, "findSmallest");

        var gotos = CpUtils.assertInstExists(GotoInstruction.class, method, result);
        CpUtils.assertTrue("Has at least 3 gotos", gotos.size() >= 3, result);
    }

    @Test
    public void testIf() {
        assertTrue(testOllirGeneration("If.jmm", "If.ollir"));
    }

    @Test
    public void testIf2() {
        assertTrue(testOllirGeneration("If2.jmm", "If2.ollir"));
    }

    @Test
    public void testBooleanLiteral() {assertTrue(testOllirGeneration("BooleanLiteral.jmm", "BooleanLiteral.ollir"));}

    @Test
    public void testParentExpr() {assertTrue(testOllirGeneration("ParentExpr.jmm", "ParentExpr.ollir"));}

    @Test
    public void testImport() {assertTrue(testOllirGeneration("Import.jmm", "Import.ollir"));}
}
