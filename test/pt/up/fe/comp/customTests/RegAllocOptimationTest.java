package pt.up.fe.comp.customTests;

import org.junit.Test;
import pt.up.fe.comp.CpUtils;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp2025.ConfigOptions;
import pt.up.fe.specs.util.SpecsIo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RegAllocOptimationTest {
    private static final String BASE_PATH = "pt/up/fe/comp/customTests/optimizations/reg_alloc/";

    static OllirResult getOllirResult(String filename) {
        return CpUtils.getOllirResult(SpecsIo.getResource(BASE_PATH + filename), Collections.emptyMap(), false);
    }

    static OllirResult getOllirResultOpt(String filename) {
        Map<String, String> config = new HashMap<>();
        config.put(ConfigOptions.getOptimize(), "true");

        return CpUtils.getOllirResult(SpecsIo.getResource(BASE_PATH + filename), config, true);
    }

    static OllirResult getOllirResultRegalloc(String filename, int maxRegs) {
        Map<String, String> config = new HashMap<>();
        config.put(ConfigOptions.getRegister(), Integer.toString(maxRegs));


        return CpUtils.getOllirResult(SpecsIo.getResource(BASE_PATH + filename), config, true);
    }

    static void printOllir(String label, OllirResult result) {
        System.out.println("=== " + label + " OLLIR ===");
        System.out.println(result.getOllirCode());
        System.out.println("\n");
    }

    @Test
    public void regAlloc1() {
        String filename = "alloc1.jmm";
        int expectedNumReg = 4;

        OllirResult original = getOllirResult(filename);
        OllirResult optimized = getOllirResultRegalloc(filename, expectedNumReg);


        printOllir("Original", original);
        //printOllir("Optimized (Register Allocation)", optimized);


        int originalNumReg = CpUtils.countRegisters(CpUtils.getMethod(original, "soManyRegisters"));
        int actualNumReg = CpUtils.countRegisters(CpUtils.getMethod(optimized, "soManyRegisters"));

        CpUtils.assertTrue("Expected number of locals in 'soManyRegisters' to be equal to " + expectedNumReg + ", is " + actualNumReg,
                actualNumReg == expectedNumReg,
                optimized);


        var varTable = CpUtils.getMethod(optimized, "soManyRegisters").getVarTable();
        var aReg = varTable.get("a").getVirtualReg();
        CpUtils.assertNotEquals("Expected registers of variables 'a' and 'b' to be different", aReg, varTable.get("b").getVirtualReg(), optimized);
        CpUtils.assertNotEquals("Expected registers of variables 'a' and 'c' to be different", aReg, varTable.get("c").getVirtualReg(), optimized);
    }

    @Test
    public void regAlloc2() {
        String filename = "alloc2.jmm";
        int expectedNumReg = 4;

        OllirResult original = getOllirResult(filename);
        OllirResult optimized = getOllirResultRegalloc(filename, expectedNumReg);


        printOllir("Original", original);
        //printOllir("Optimized (Register Allocation)", optimized);


        int originalNumReg = CpUtils.countRegisters(CpUtils.getMethod(original, "soManyRegisters"));
        int actualNumReg = CpUtils.countRegisters(CpUtils.getMethod(optimized, "soManyRegisters"));

        CpUtils.assertTrue("Expected number of locals in 'soManyRegisters' to be equal to " + expectedNumReg + ", is " + actualNumReg,
                actualNumReg == expectedNumReg,
                optimized);


        var varTable = CpUtils.getMethod(optimized, "soManyRegisters").getVarTable();
        var aReg = varTable.get("a").getVirtualReg();
        CpUtils.assertNotEquals("Expected registers of variables 'a' and 'b' to be different", aReg, varTable.get("b").getVirtualReg(), optimized);
    }

    @Test
    public void regAlloc3() {
        String filename = "alloc3.jmm";
        int expectedNumReg = 3;

        OllirResult original = getOllirResult(filename);
        OllirResult optimized = getOllirResultRegalloc(filename, expectedNumReg);


        printOllir("Original", original);
        //printOllir("Optimized (Register Allocation)", optimized);


        int originalNumReg = CpUtils.countRegisters(CpUtils.getMethod(original, "soManyRegisters"));
        int actualNumReg = CpUtils.countRegisters(CpUtils.getMethod(optimized, "soManyRegisters"));

        CpUtils.assertTrue("Expected number of locals in 'soManyRegisters' to be equal to " + expectedNumReg + ", is " + actualNumReg,
                actualNumReg == expectedNumReg,
                optimized);


        var varTable = CpUtils.getMethod(optimized, "soManyRegisters").getVarTable();
        var aReg = varTable.get("a").getVirtualReg();
        CpUtils.assertEquals("Expected registers of variables 'a' and 'b' to be equal", aReg, varTable.get("b").getVirtualReg(), optimized);
        CpUtils.assertEquals("Expected registers of variables 'a' and 'c' to be equal", aReg, varTable.get("c").getVirtualReg(), optimized);
    }

    @Test
    public void regAlloc4() {
        String filename = "alloc4.jmm";
        int expectedNumReg = 3;

        OllirResult original = getOllirResult(filename);
        OllirResult optimized = getOllirResultRegalloc(filename, expectedNumReg);


        printOllir("Original", original);
        //printOllir("Optimized (Register Allocation)", optimized);


        int originalNumReg = CpUtils.countRegisters(CpUtils.getMethod(original, "soManyRegisters"));
        int actualNumReg = CpUtils.countRegisters(CpUtils.getMethod(optimized, "soManyRegisters"));

        CpUtils.assertTrue("Expected number of locals in 'soManyRegisters' to be equal to " + expectedNumReg + ", is " + actualNumReg,
                actualNumReg == expectedNumReg,
                optimized);


        var varTable = CpUtils.getMethod(optimized, "soManyRegisters").getVarTable();
        var aReg = varTable.get("a").getVirtualReg();
        CpUtils.assertEquals("Expected registers of variables 'a' and 'b' to be equal", aReg, varTable.get("b").getVirtualReg(), optimized);
    }

    @Test
    public void regAlloc5() {
        String filename = "alloc5.jmm";
        int expectedNumReg = 3;

        OllirResult original = getOllirResult(filename);
        OllirResult optimized = getOllirResultRegalloc(filename, expectedNumReg);


        printOllir("Original", original);
        //printOllir("Optimized (Register Allocation)", optimized);


        int originalNumReg = CpUtils.countRegisters(CpUtils.getMethod(original, "soManyRegisters"));
        int actualNumReg = CpUtils.countRegisters(CpUtils.getMethod(optimized, "soManyRegisters"));

        CpUtils.assertTrue("Expected number of locals in 'soManyRegisters' to be equal to " + expectedNumReg + ", is " + actualNumReg,
                actualNumReg == expectedNumReg,
                optimized);


        var varTable = CpUtils.getMethod(optimized, "soManyRegisters").getVarTable();
        var aReg = varTable.get("a").getVirtualReg();
        CpUtils.assertEquals("Expected registers of variables 'a' and 'b' to be equal", aReg, varTable.get("b").getVirtualReg(), optimized);
    }

    @Test
    public void regAlloc6() {
        String filename = "alloc6.jmm";
        int expectedNumReg = 4;

        OllirResult original = getOllirResult(filename);
        OllirResult optimized = getOllirResultRegalloc(filename, expectedNumReg);


        printOllir("Original", original);
        //printOllir("Optimized (Register Allocation)", optimized);


        int originalNumReg = CpUtils.countRegisters(CpUtils.getMethod(original, "soManyRegisters"));
        int actualNumReg = CpUtils.countRegisters(CpUtils.getMethod(optimized, "soManyRegisters"));

        CpUtils.assertTrue("Expected number of locals in 'soManyRegisters' to be equal to " + expectedNumReg + ", is " + actualNumReg,
                actualNumReg == expectedNumReg,
                optimized);


        var varTable = CpUtils.getMethod(optimized, "soManyRegisters").getVarTable();
        var aReg = varTable.get("a").getVirtualReg();
        CpUtils.assertNotEquals("Expected registers of variables 'a' and 'b' to be different", aReg, varTable.get("b").getVirtualReg(), optimized);
        CpUtils.assertNotEquals("Expected registers of variables 'a' and 'c' to be different", aReg, varTable.get("c").getVirtualReg(), optimized);
        CpUtils.assertNotEquals("Expected registers of variables 'a' and 'd' to be different", aReg, varTable.get("d").getVirtualReg(), optimized);
    }

    @Test
    public void regAlloc7() {
        String filename = "alloc7.jmm";
        int expectedNumReg = 3;

        OllirResult original = getOllirResult(filename);
        OllirResult optimized = getOllirResultRegalloc(filename, expectedNumReg);


        printOllir("Original", original);
        //printOllir("Optimized (Register Allocation)", optimized);


        int originalNumReg = CpUtils.countRegisters(CpUtils.getMethod(original, "soManyRegisters"));
        int actualNumReg = CpUtils.countRegisters(CpUtils.getMethod(optimized, "soManyRegisters"));

        CpUtils.assertTrue("Expected number of locals in 'soManyRegisters' to be equal to " + expectedNumReg + ", is " + actualNumReg,
                actualNumReg == expectedNumReg,
                optimized);


        var varTable = CpUtils.getMethod(optimized, "soManyRegisters").getVarTable();
        var aReg = varTable.get("b").getVirtualReg();
        CpUtils.assertEquals("Expected registers of variables 'b' and 'd' to be equal", aReg, varTable.get("d").getVirtualReg(), optimized);
    }
}
