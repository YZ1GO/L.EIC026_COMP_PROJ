package pt.up.fe.comp.customTests;

import org.junit.Test;
import org.specs.comp.ollir.inst.CondBranchInstruction;
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
        OllirResult result = getOllirResult("IfThenElse.jmm");
        var method = CpUtils.getMethod(result, "func");

        var branches = CpUtils.assertInstExists(CondBranchInstruction.class, method, result);
        CpUtils.assertEquals("Number of branches", 1, branches.size(), result);

        var gotos = CpUtils.assertInstExists(GotoInstruction.class, method, result);
        CpUtils.assertTrue("Has at least 1 goto", gotos.size() >= 1, result);
    }

    // some differences
    @Test
    public void testIfThenElseMultiple() {
        OllirResult result = getOllirResult("IfThenElseMultiple.jmm");
        var method = CpUtils.getMethod(result, "func");

        var branches = CpUtils.assertInstExists(CondBranchInstruction.class, method, result);
        CpUtils.assertEquals("Number of branches", 3, branches.size(), result);

        var gotos = CpUtils.assertInstExists(GotoInstruction.class, method, result);
        CpUtils.assertTrue("Has at least 3 gotos", gotos.size() >= 3, result);
    }

    @Test
    public void testIf() {
        OllirResult result = getOllirResult("If.jmm");
        var method = CpUtils.getMethod(result, "func");

        var branches = CpUtils.assertInstExists(CondBranchInstruction.class, method, result);
        CpUtils.assertEquals("Number of branches", 1, branches.size(), result);

        var gotos = CpUtils.assertInstExists(GotoInstruction.class, method, result);
        CpUtils.assertTrue("Has at least 1 gotos", gotos.size() >= 1, result);
    }

    @Test
    public void testIf2() {
        //assertTrue(testOllirGeneration("If2.jmm", "If2.ollir"));

        OllirResult result = getOllirResult("If2.jmm");
        var method = CpUtils.getMethod(result, "func");

        var branches = CpUtils.assertInstExists(CondBranchInstruction.class, method, result);
        CpUtils.assertEquals("Number of branches", 2, branches.size(), result);

        var gotos = CpUtils.assertInstExists(GotoInstruction.class, method, result);
        CpUtils.assertTrue("Has at least 2 gotos", gotos.size() >= 2, result);
    }

    @Test
    public void testIf3() {
        OllirResult result = getOllirResult("If3.jmm");
        var method = CpUtils.getMethod(result, "func");

        System.out.println("Generated OLLIR:");
        System.out.println(result.getOllirCode());

        var gotos = CpUtils.assertInstExists(GotoInstruction.class, method, result);
        CpUtils.assertTrue("Has at least 3 gotos", gotos.size() >= 3, result);

        //assertTrue(testOllirGeneration("If3.jmm", "If3.ollir")); // can be ran individually
    }

    @Test
    public void testImport() {assertTrue(testOllirGeneration("Import.jmm", "Import.ollir"));}

    @Test
    public void testWhile() {
        //assertTrue(testOllirGeneration("while.jmm", "while.ollir"));

        OllirResult result = getOllirResult("while.jmm");
        var method = CpUtils.getMethod(result, "loop");

        var branches = CpUtils.assertInstExists(CondBranchInstruction.class, method, result);

        CpUtils.assertTrue("Number of branches is 1", branches.size() == 1, result);
    }

    @Test
    public void testWhile2() {
        //assertTrue(testOllirGeneration("while2.jmm", "while2.ollir"));

        OllirResult result = getOllirResult("while2.jmm");
        var method = CpUtils.getMethod(result, "loop");

        /*System.out.println(
                "Generated OLLIR:"
        );
        System.out.println(result.getOllirCode());*/
        var branches = CpUtils.assertInstExists(CondBranchInstruction.class, method, result);

        CpUtils.assertTrue("Number of branches is 2", branches.size() == 2, result);

        var gotos = CpUtils.assertInstExists(GotoInstruction.class, method, result);
        CpUtils.assertTrue("Has at least 2 gotos", gotos.size() >= 2, result);
    }

    @Test
    public void whileAndIfThenElse() {
        OllirResult result = getOllirResult("whileAndIfThenElse.jmm");
        var method = CpUtils.getMethod(result, "loop");

        System.out.println("Generated OLLIR:");
        System.out.println(result.getOllirCode());

        var branches = CpUtils.assertInstExists(CondBranchInstruction.class, method, result);

        CpUtils.assertTrue("Number of branches is 5", branches.size() == 5, result);

        var gotos = CpUtils.assertInstExists(GotoInstruction.class, method, result);
        CpUtils.assertTrue("Has at least 5 gotos", gotos.size() >= 5, result);
    }
}
