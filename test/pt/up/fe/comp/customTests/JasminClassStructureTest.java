package pt.up.fe.comp.customTests;

import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.specs.util.SpecsCheck;
import pt.up.fe.specs.util.SpecsIo;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.regex.Pattern;

public class JasminClassStructureTest {

    static JasminResult getJasminResult(String filename) {
        var resource = "pt/up/fe/comp/customTests/jasmin/class_structure/" + filename;
        SpecsCheck.checkArgument(resource.endsWith(".ollir"), 
            () -> "Expected resource to end with .ollir: " + resource);
        
        var ollirResult = new OllirResult(SpecsIo.getResource(resource), Collections.emptyMap());
        return TestUtils.backend(ollirResult);
    }

    @Test
    public void testSuperclassDeclaration() {
        JasminResult jasminResult = getJasminResult("superclass.ollir");
        String jasminCode = jasminResult.getJasminCode();
        
        assertTrue("Superclass declaration not found or incorrect",
            Pattern.compile("\\.super\\s+Parent").matcher(jasminCode).find());
    }
    
}