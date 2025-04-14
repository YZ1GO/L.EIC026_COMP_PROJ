package pt.up.fe.comp.customTests;

import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.specs.util.SpecsIo;

import static org.junit.Assert.assertTrue;

/**
 * Custom Ollir generation tests to compare ollir generated from .jmm code to existent .ollir
 */
public class OllirExprGenerationTest {
    private boolean testOllirExprGeneration(String jmmFile, String ollirFile) {
        OllirResult result = TestUtils.optimize(SpecsIo.getResource("pt/up/fe/comp/customTests/ollir/expr/" + jmmFile));
        String expectedOllir = SpecsIo.getResource("pt/up/fe/comp/customTests/ollir/expr/" + ollirFile).trim();

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
    public void testBooleanLiteral() {assertTrue(testOllirExprGeneration("BooleanLiteral.jmm", "BooleanLiteral.ollir"));}

    @Test
    public void testParentExpr() {assertTrue(testOllirExprGeneration("ParentExpr.jmm", "ParentExpr.ollir"));}

    @Test
    public void testNewObjectExpr() {assertTrue(testOllirExprGeneration("NewObject.jmm", "NewObject.ollir"));}

    @Test
    public void testNewObjectExpr2() {assertTrue(testOllirExprGeneration("NewObject2.jmm", "NewObject2.ollir"));}

    @Test
    public void testThisExpr() {assertTrue(testOllirExprGeneration("This.jmm", "This.ollir"));}

    @Test
    public void testThisExpr2() {assertTrue(testOllirExprGeneration("This2.jmm", "This2.ollir"));}

    @Test
    public void testUnaryNot() {assertTrue(testOllirExprGeneration("UnaryNot.jmm", "UnaryNot.ollir"));}

    //@Test
    //public void testStringLiteral() {assertTrue(testOllirExprGeneration("StringLiteral.jmm", "StringLiteral.ollir"));}

    @Test
    public void testNewIntArray() {assertTrue(testOllirExprGeneration("NewIntArray.jmm", "NewIntArray.ollir"));}

    @Test
    public void testArrayInit() {assertTrue(testOllirExprGeneration("ArrayInit.jmm", "ArrayInit.ollir"));}

    @Test
    public void testArrayAccess() {assertTrue(testOllirExprGeneration("ArrayAccess.jmm", "ArrayAccess.ollir"));}

    @Test
    public void testLengthExpr() {assertTrue(testOllirExprGeneration("LengthExpr.jmm", "LengthExpr.ollir"));}

    @Test
    public void testMethodCall() {assertTrue(testOllirExprGeneration("MethodCall.jmm", "MethodCall.ollir"));}

    @Test
    public void testMethodCall2() {assertTrue(testOllirExprGeneration("MethodCall2.jmm", "MethodCall2.ollir"));}

    @Test
    public void testMethodCall3() {assertTrue(testOllirExprGeneration("MethodCall3.jmm", "MethodCall3.ollir"));}
}
