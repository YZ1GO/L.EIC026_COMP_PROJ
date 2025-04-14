package pt.up.fe.comp.customTests;

import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.specs.util.SpecsIo;

public class DuplicateVariableSemanticAnalysisTest {
    @Test
    public void duplicatedField() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/duplicatevariable/duplicatedfield.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }

    @Test
    public void duplicatedLocal() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/duplicatevariable/duplicatedlocal.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }

    @Test
    public void duplicatedParam() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/duplicatevariable/duplicatedparam.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }

    @Test
    public void localDuplicateParam() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/duplicatevariable/localduplicateparam.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }

    @Test
    public void sameNameDifType() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/duplicatevariable/samenamediftype.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }

    @Test
    public void duplicatedImport() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/duplicatevariable/duplicatedimport.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }
}
