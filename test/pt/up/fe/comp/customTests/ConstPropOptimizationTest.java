package pt.up.fe.comp.customTests;

import org.junit.Test;
import pt.up.fe.comp.CpUtils;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.specs.util.SpecsIo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ConstPropOptimizationTest {
    private static final String BASE_PATH = "pt/up/fe/comp/customTests/optimizations/const_prop/";

    private static OllirResult getOllirResult(String filename) {
        return CpUtils.getOllirResult(SpecsIo.getResource(BASE_PATH + filename), Collections.emptyMap(), false);
    }

    private static OllirResult getOllirResultOpt(String filename) {
        Map<String, String> config = new HashMap<>();
        config.put("optimize", "true");
        return CpUtils.getOllirResult(SpecsIo.getResource(BASE_PATH + filename), config, true);
    }

    @Test
    public void testConstPropWithIf() {
        String filename = "PropWithIf.jmm";

        OllirResult original = getOllirResult(filename);
        OllirResult optimized = getOllirResultOpt(filename);

        CpUtils.assertNotEquals("Expected code to change with optimization",
                original.getOllirCode(), optimized.getOllirCode(), optimized);

        var method = CpUtils.getMethod(optimized, "foo");
        //without folding
        /*CpUtils.assertLiteralCount("5", method, optimized, 2);
        CpUtils.assertLiteralCount("10", method, optimized, 4);
        CpUtils.assertLiteralCount("20", method, optimized, 2);
        CpUtils.assertLiteralCount("30", method, optimized, 2);*/

        //with folding
        CpUtils.assertLiteralCount("5", method, optimized, 1);
        CpUtils.assertLiteralCount("10", method, optimized, 1);
        CpUtils.assertLiteralCount("20", method, optimized, 2);
        CpUtils.assertLiteralCount("30", method, optimized, 2);
        System.out.println("GENERATED OLLIR: ");
        System.out.println(optimized.getOllirCode());
    }

    @Test
    public void testConstPropWithIfComplex() {
        String filename = "PropWithIfComplex.jmm";

        OllirResult original = getOllirResult(filename);
        OllirResult optimized = getOllirResultOpt(filename);

        CpUtils.assertNotEquals("Expected code to change with optimization",
                original.getOllirCode(), optimized.getOllirCode(), optimized);

        var method = CpUtils.getMethod(optimized, "calculate");

        //without folding
        /*CpUtils.assertLiteralCount("8", method, optimized, 4);
        CpUtils.assertLiteralCount("15", method, optimized, 4);
        CpUtils.assertLiteralCount("20", method, optimized, 6);
        CpUtils.assertLiteralCount("10", method, optimized, 2);
        CpUtils.assertLiteralCount("12", method, optimized, 2);
        CpUtils.assertLiteralCount("25", method, optimized, 2);
        CpUtils.assertLiteralCount("5", method, optimized, 2);*/

        //with folding
        CpUtils.assertLiteralCount("8", method, optimized, 1);
        CpUtils.assertLiteralCount("15", method, optimized, 1);
        CpUtils.assertLiteralCount("20", method, optimized, 1);
        CpUtils.assertLiteralCount("10", method, optimized, 1);
        CpUtils.assertLiteralCount("12", method, optimized, 1);
        CpUtils.assertLiteralCount("25", method, optimized, 1);
        CpUtils.assertLiteralCount("5", method, optimized, 1);
    }

    @Test
    public void testConstPropWithArray() {
        String filename = "PropWithArray.jmm";

        OllirResult original = getOllirResult(filename);
        OllirResult optimized = getOllirResultOpt(filename);

        CpUtils.assertNotEquals("Expected code to change with optimization",
                original.getOllirCode(), optimized.getOllirCode(), optimized);

        var method = CpUtils.getMethod(optimized, "foo");

        //without folding
        /*CpUtils.assertLiteralCount("5", method, optimized, 4);
        CpUtils.assertLiteralCount("10", method, optimized, 4);*/

        //with folding
        CpUtils.assertLiteralCount("5", method, optimized, 2);
        CpUtils.assertLiteralCount("10", method, optimized, 3);
    }

    @Test
    public void testConstPropWithWhileLoopComplex() {
        String filename = "PropWithWhileLoopComplex.jmm";

        OllirResult original = getOllirResult(filename);
        OllirResult optimized = getOllirResultOpt(filename);

        CpUtils.assertNotEquals("Expected code to change with optimization",
                original.getOllirCode(), optimized.getOllirCode(), optimized);

        var method = CpUtils.getMethod(optimized, "calculate");

        //without folding
        /*CpUtils.assertLiteralCount("5", method, optimized, 3);
        CpUtils.assertLiteralCount("10", method, optimized, 4);
        CpUtils.assertLiteralCount("20", method, optimized, 4);*/

        //with folding
        CpUtils.assertLiteralCount("5", method, optimized, 1);
        CpUtils.assertLiteralCount("10", method, optimized, 2);
        CpUtils.assertLiteralCount("20", method, optimized, 1);
    }

    @Test
    public void constPropWithLoop() {

        String filename = "PropWithLoop.jmm";

        OllirResult original = getOllirResult(filename);
        OllirResult optimized = getOllirResultOpt(filename);

        CpUtils.assertNotEquals("Expected code to change with -o flag\n\nOriginal code:\n" + original.getOllirCode(),
                original.getOllirCode(), optimized.getOllirCode(),
                optimized);

        var method = CpUtils.getMethod(optimized, "foo");
        CpUtils.assertLiteralCount("3", method, optimized, 3);
    }
}