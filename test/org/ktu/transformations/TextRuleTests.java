package org.ktu.transformations;
import org.ktu.transformations.RuleParserTestConcat;
import org.ktu.transformations.RuleParserTestSplit;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ RuleParserTestSplit.class, RuleParserTestConcat.class })
public class TextRuleTests {
}
