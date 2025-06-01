package pt.up.fe.comp.customTests;

import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.specs.util.SpecsIo;

public class ReturnTypeSemanticAnalysisTest {

    @Test
    public void validReturnType() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/returntype/ValidReturnType.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void incorrectReturnType() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/returntype/IncorrectReturnType.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }

    @Test
    public void validReturnTypeArray() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/returntype/ValidReturnTypeArray.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void incorrectReturnTypeArray() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/returntype/IncorrectReturnTypeArray.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }

    @Test
    public void validReturnTypeCustomClass() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/returntype/ValidReturnTypeCustomClass.jmm"));
        TestUtils.noErrors(result);
        System.out.println(result.getReports());
    }

    @Test
    public void incorrectReturnTypeCustomClass() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/returntype/IncorrectReturnTypeCustomClass.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }

        @Test
    public void validReturnTypeString() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/returntype/ValidReturnTypeString.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void incorrectReturnTypeString() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/returntype/IncorrectReturnTypeString.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }

    @Test
    public void missingReturn() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/returntype/MissingReturn.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }

    @Test
    public void returnNotLastStatement() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/returntype/ReturnNotLastStatement.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }

    @Test
    public void moreThanOneReturn() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/returntype/MoreThanOneReturn.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }

    @Test
    public void returnUndeclared() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/returntype/ReturnUndeclared.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }
}