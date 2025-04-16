package pt.up.fe.comp.customTests;

import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.specs.util.SpecsIo;

public class ArraysSemanticAnalysisTest {

    @Test
    public void arrayAddition() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/arrays/ArrayAddition.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void arrayIndexWrong() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/arrays/ArrayIndexWrong.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void arrayInitWrong11() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/arrays/ArrayInitWrong11.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void arrayInitWrong12() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/arrays/ArrayInitWrong12.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void arrayInitWrong13() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/arrays/ArrayInitWrong13.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void arrayInitWrong14() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/arrays/ArrayInitWrong14.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void arrayInitWrong15() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/arrays/ArrayInitWrong15.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void arrayLiteralWrong() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/arrays/ArrayLiteralWrong.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void arrayMultiplication() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/arrays/ArrayMultiplication.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void invalidArrayLiteral() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/arrays/InvalidArrayLiteral.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void nonArrayAccess() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/arrays/NonArrayAccess.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void arrayIndexOutOfBounds() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/arrays/ArrayIndexOutOfBounds.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }

    @Test
    public void arrayIndexOutOfBounds2() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/arrays/ArrayIndexOutOfBounds2.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }

    @Test
    public void arrayIndexOutOfBounds3() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/arrays/ArrayIndexOutOfBounds3.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }

    @Test
    public void arrayIndexOutOfBounds4() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/customTests/semanticanalysis/arrays/ArrayIndexOutOfBounds4.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }
}
