package org.ktu.transformations;

import static org.junit.Assert.*;

import org.junit.Test;
import org.ktu.transformations.parsers.RuleParser;

public class RuleParserTestSplit {

    RuleParser parser = new RuleParser();

    @Test
    public void testIsLeftRule1() {
        assertEquals(parser.isLeftRule("Left(,1,\" \")"), true);
    }

    @Test
    public void testIsLeftRule2() {
        assertEquals(parser.isLeftRule("A:=Left(,1,\" \")"), true);
    }

    @Test
    public void testIsLeftRule3() {
        assertEquals(parser.isLeftRule("a0 :=LEFT(,1,\" \")"), true);
    }

    @Test
    public void testIsLeftRule4() {
        assertEquals(parser.isLeftRule("a0:= left(,1,\" \")"), true);
    }

    @Test
    public void testApplyPosRule1() {
        assertEquals(parser.applyExtractionRule(" a := Left(,1,\" \")", "Create another rental contract"), "Create");
    }

    @Test
    public void testApplyPosRule2() {
        assertEquals(parser.applyExtractionRule("LEFT(2,1,\" \")", "Create another rental contract"), "another");
    }

    @Test
    public void testApplyPosRule3() {
        assertEquals(parser.applyExtractionRule("left(1,10,)", "Create another rental contract"), "Create ano");
    }

    @Test
    public void testApplyPosRule4() {
        assertEquals(parser.applyExtractionRule("right(,2,\" \")", "Create rental contract"), "rental contract");
    }

    @Test
    public void testApplyPosRule5() {
        assertEquals(parser.applyExtractionRule("RIGHT(2,1,\" \")", "Create another rental contract"), "another");
    }

    @Test
    public void testApplyPosRule6() {
        assertEquals(parser.applyExtractionRule("Right(1,10,)", "Create another rental contract"), "l contract");
    }

    @Test
    public void testApplyPosRule7() {
        assertEquals(parser.applyExtractionRule("RIGHT(2,1,\" \")", "Create another important rental contract"), "important");
    }

    @Test
    public void testApplyPosRule8() {
        assertEquals(parser.applyExtractionRule("RIGHT(2,3,\" \")", "Create another important rental contract"),
                "Create another important rental contract");
    }

    @Test
    public void testApplyPosRule9() {
        assertEquals(parser.applyExtractionRule("right(2,,\" \")", "Create another rental contract"), "another rental contract");
    }

    @Test
    public void testApplyPosRule10() {
        assertEquals(parser.applyExtractionRule("right(1,2,\" \")", "Create another rental contract"), "another rental");
    }
}
