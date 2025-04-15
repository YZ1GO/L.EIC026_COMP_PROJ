package pt.up.fe.comp.customTests;

import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.specs.util.SpecsIo;

public class VarArgsSemanticAnalysisTest {
    @Test
    public void arrayAndInteger() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/varargs/arrayandinteger.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }

    @Test
    public void booleanAndInteger() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/varargs/booleanandinteger.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }

    @Test
    public void integerAndArray() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/varargs/integerandarray.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }

    @Test
    public void moreThanOneArray() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/varargs/morethanonearray.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }

    @Test
    public void emptyVarargs() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/varargs/emptyvarargs.jmm"));
        TestUtils.noErrors(result);
        System.out.println(result.getReports());
    }

    @Test
    public void singlevalidarray() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/varargs/singlevalidarray.jmm"));
        TestUtils.noErrors(result);
        System.out.println(result.getReports());
    }

    @Test
    public void varargsWrong() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/varargs/varargswrong.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }

    @Test
    public void varargsParameterWrong() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/varargs/varargsparameterwrong.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }
}
