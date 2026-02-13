package org.ktu.transformations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.ktu.transformations.parsers.RuleParser;

public class RuleParserTestConcat {

    RuleParser parser = new RuleParser();

    @Test
    public void testParameterList() {
        List<String> vals = parser.extractParameterList("Concat(\"a\", \"b\", c)", false).getValue();
        List<String> result = Arrays.asList("\"a\"", "\"b\"", "c");
        assertEquals(vals, result);
    }

    @Test
    public void testApplyConcatRule1() {
        String concatRule = "concat(a, \" is \", b, \" of \", c)";
        boolean excThrown = false;
        try {
            parser.applyConcatRule(concatRule);
        } catch (ParseException e) {
            excThrown = true;
        }
        assertTrue(excThrown);
    }

    @Test
    public void testApplyConcatRule2() {
        String concatRule = "concat(a, \" is \", b, \" of \", c)";
        boolean excThrown = false;
        try {
            parser.applyConcatRule(concatRule, null, null);
        } catch (ParseException e) {
            excThrown = true;
        }
        assertTrue(excThrown);
    }

    @Test
    public void testApplyConcatRule3() {
        String concatRule = "concat(a, \" is \", b, \" of \", c)";
        String[] rules = new String[]{"a:=LEFT(,2,' ')", "b:=LEFT(,,' ')"};
        boolean excThrown = false;
        try {
            parser.applyConcatRule(concatRule, rules, null);
        } catch (ParseException e) {
            excThrown = true;
        }
        assertTrue(excThrown);
    }

    @Test
    public void testApplyConcatRule4() {
        String concatRule = "concat(a, \" is \", b, \" of \", c)";
        String[] rules = new String[]{"a:=LEFT(,2,' ')", "b:=LEFT(,,' ')"};
        String[] values = new String[]{"Create rental contract"};
        boolean excThrown = false;
        try {
            parser.applyConcatRule(concatRule, rules, values);
        } catch (IllegalArgumentException e) {
            excThrown = true;
        } catch (ParseException e) {
        }
        assertTrue(excThrown);
    }

    @Test
    public void testApplyConcatRule5() {
        String concatRule = "concat('creates', ' ', 'rental contract')";
        String result = null;
        try {
            result = parser.applyConcatRule(concatRule);
        } catch (ParseException e) {
        }
        assertEquals(result, "creates rental contract");
    }

    @Test
    public void testApplyConcatRule6() {
        String concatRule = "concat(a, \" is \", b, \" of \", c)";
        String[] rules = new String[]{"a:=LEFT(,1,' ')", "b:=RIGHT(,,' ')"};
        String[] values = new String[]{"Manager of car assignment", "highly responsible"};
        boolean excThrown = false;
        try {
            parser.applyConcatRule(concatRule, rules, values);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            excThrown = true;
        }
        assertTrue(excThrown);
    }

    @Test
    public void testApplyConcatRule7() {
        String concatRule = "concat(a, \" is \", b, \" for \", c)";
        String[] rules = new String[]{"a:=LEFT(,1,' ')", "b:=RIGHT(,,' ')", "c:=LEFT(3,2,' ')"};
        String[] values = new String[]{"Manager of car assignment", "highly responsible", "management of customer cars"};
        String result = null;
        try {
            result = parser.applyConcatRule(concatRule, rules, values);
        } catch (ParseException e) {
        }
        assertEquals("Manager is highly responsible for customer cars", result);
    }

    @Test
    public void testApplyConcatRule8() {
        String concatRule = "concat(a, \" is \", b, \" for \", c)";
        String[] rules = new String[]{"a", "b", "c"};
        String[] values = new String[]{"Manager of car assignment", "highly responsible", "management of customer cars"};
        String result = null;
        try {
            result = parser.applyConcatRule(concatRule, rules, values);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
        }
        assertEquals("Manager of car assignment is highly responsible for management of customer cars", result);
    }

    @Test
    public void testApplyConcatRule9() {
        String concatRule = "concat(a, \" is \", b, \" for \", c)";
        String[] rules = new String[]{"a:=LEFT(,1,' ')", "b:=RIGHT(,,' ')", "c"};
        String[] values = new String[]{"Manager of car assignment", "highly responsible", "management of customer cars"};
        String result = null;
        try {
            result = parser.applyConcatRule(concatRule, rules, values);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
        }
        assertEquals("Manager is highly responsible for management of customer cars", result);
    }

}
