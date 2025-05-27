package pt.up.fe.comp.customTests;

import org.junit.Test;
import pt.up.fe.comp.CpUtils;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp2025.ConfigOptions;
import pt.up.fe.specs.util.SpecsIo;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertFalse;

public class JasminOptmizationiincTest {
    static JasminResult getJasminResult(String filename) {
        String resource = SpecsIo.getResource("pt/up/fe/comp/customTests/jasmin/optimizations/" + filename);
        return TestUtils.backend(resource);
    }

    static OllirResult getOllirResult(String filename) {
        return TestUtils.optimize(SpecsIo.getResource("pt/up/fe/comp/customTests/jasmin/optimizations/" + filename));
    }

    static OllirResult getOllirResultRegalloc(String filename, int maxRegs) {
        Map<String, String> config = new HashMap<>();
        config.put(ConfigOptions.getRegister(), Integer.toString(maxRegs));
        return CpUtils.getOllirResult(SpecsIo.getResource("pt/up/fe/comp/customTests/jasmin/optimizations/" + filename), config, true);
    }

    static JasminResult getJasminResultOpt(String filename) {
        Map<String, String> config = new HashMap<>();
        config.put("optimize", "true");
        return TestUtils.backend(SpecsIo.getResource("pt/up/fe/comp/customTests/jasmin/optimizations/" + filename), config);
    }

    static JasminResult getJasminResultReg(String filename, int numReg) {
        Map<String, String> config = new HashMap<>();
        config.put("registerAllocation", String.valueOf(numReg));
        return TestUtils.backend(SpecsIo.getResource("pt/up/fe/comp/customTests/jasmin/optimizations/" + filename), config);
    }

    @Test
    public void iinc1() {
        JasminResult jasminResult = getJasminResult("inc1.jmm");
        CpUtils.matches(jasminResult, "iinc\\s+\\w+\\s+3");
    }

    @Test
    public void iinc1WithRegAlloc() {
        var ollir = getOllirResult("inc1.jmm");
        var ollirOpt = getOllirResultRegalloc("inc1.jmm", 0);
        System.out.println("Generated Ollir: " + ollir.getOllirCode());
        System.out.println("Optimized Ollir: " + ollirOpt.getOllirCode());

        JasminResult jasminResult = getJasminResultReg("inc1.jmm",0);
        CpUtils.matches(jasminResult, "iinc\\s+\\w+\\s+3");


    }

    @Test
    public void iinc2() {
        JasminResult jasminResult = getJasminResult("inc2.jmm");
        CpUtils.matches(jasminResult, "iinc\\s+\\w+\\s-3");
    }

    @Test
    public void iinc3() {
        JasminResult jasminResult = getJasminResult("inc3.jmm");
        String jasminCode = jasminResult.getJasminCode();

        // Regex to match any 'iinc' instruction
        Pattern pattern = Pattern.compile("^\\s*iinc\\s+\\w+\\s+-?\\d+\\s*$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(jasminCode);

        // Assert that no 'iinc' was found
        assertFalse("iinc instruction was unexpectedly found", matcher.find());
    }

    @Test
    public void iinc4() {
        JasminResult jasminResult = getJasminResult("inc4.jmm");
        String jasminCode = jasminResult.getJasminCode();

        // Regex to match any 'iinc' instruction
        Pattern pattern = Pattern.compile("^\\s*iinc\\s+\\w+\\s+-?\\d+\\s*$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(jasminCode);

        // Assert that no 'iinc' was found
        assertFalse("iinc instruction was unexpectedly found", matcher.find());
    }

    @Test
    public void iinc5() {
        JasminResult jasminResult = getJasminResult("inc5.jmm");
        String jasminCode = jasminResult.getJasminCode();

        // Regex to match any 'iinc' instruction
        Pattern pattern = Pattern.compile("^\\s*iinc\\s+\\w+\\s+-?\\d+\\s*$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(jasminCode);

        // Assert that no 'iinc' was found
        assertFalse("iinc instruction was unexpectedly found", matcher.find());
    }



}
