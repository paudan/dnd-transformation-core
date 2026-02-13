package org.ktu.transformations.parsers;

import java.text.ParseException;
import java.util.AbstractMap.SimpleEntry;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * <p>
 * Parser for rules, defined as constraints in Connectors of transformation pattern</p>
 * <p>
 * Following are the examples of syntax for such rules:</p>
 * {@code LEFT(1, 3, ' ')}<br />
 * {@code a:=LEFT(1, 3, ' ')}<br />
 * {@code RIGHT(1,,'')}<br />
 * {@code B:=Right(,3, '')}<br />
 * {@code C:=concat(A," and ", B)}<br />
 * {@code C:=concat(create',' rental ', ' contract' )}
 *
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), 
 * Center of Information Systems Design Technologies, Kaunas University of Technology, 2015
 *
 */
public class RuleParser {

    private static final String PARAM_SEP = ",";
    private static final String VARIABLE_SEP = ":=";
    private static final String var_regexp = "^([a-zA-Z])+[0-9]*[ ]*" + VARIABLE_SEP + "[ ]*";
    
    private final ResourceBundle bundle = ResourceBundle.getBundle("org/ktu/transformations/messages_EN");

    private static boolean isValidFunction(String rule, String fname) {
        if (rule == null || rule.trim().length() == 0)
            return false;
        rule = rule.trim().toLowerCase();
        Pattern pattern = Pattern.compile(var_regexp + fname + "\\(", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rule);
        return rule.startsWith(fname + "(") || matcher.lookingAt();
    }

    /**
     * Indicates if a given rule is valid LEFT rule
     * @param rule	The rule to be tested
     * @return	{@code true} if rule is a valid LEFT rule, {@code false} otherwise
     */
    public boolean isLeftRule(String rule) {
        return isValidFunction(rule, "left") && getParameterList(rule, true).size() == 3;
    }

    /**
     * Indicates if a given rule is valid RIGHT rule
     * @param rule	The rule to be tested
     * @return	{@code true} if rule is a valid RIGHT rule, {@code false} otherwise
     */
    public boolean isRightRule(String rule) {
        return isValidFunction(rule, "right") && getParameterList(rule, true).size() == 3;
    }

    /**
     * Indicates if a given rule is valid CONCAT rule
     * @param rule	The rule to be tested
     * @return	{@code true} if rule is a valid CONCAT rule, {@code false} otherwise
     */
    public boolean isConcatRule(String rule) {
        return rule != null && rule.trim().length() > 0 && rule.toLowerCase().startsWith("concat(");
    }

    /**
     * Indicates if a given rule is valid LEFT, RIGHT or CONCAT rule
     * @param rule	The rule to be tested
     * @return	{@code true} if rule is a valid rule, {@code false} otherwise
     */
    public boolean isValidRule(String rule) {
        return isLeftRule(rule) || isRightRule(rule) || isConcatRule(rule);
    }

    /**
     * Extracts and returns the name of the variable from the rule
     * @param rule	The rule string
     * @return	Extracted variable as string, or {@code null}, if no variable was found in the string
     */
    public String extractVariableName(String rule) {
        if (!rule.contains(VARIABLE_SEP))
            return rule;
        if (!isLeftRule(rule) && !isRightRule(rule))
            return null;
        Pattern pattern = Pattern.compile(var_regexp, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rule);
        if (matcher.lookingAt())
            return rule.split(VARIABLE_SEP)[0].trim();
        return null;
    }

    private List<String> getParameterList(String rule, boolean clean) {
        List<String> params = new ArrayList<>();
        String[] split = rule.substring(rule.indexOf("(")).split(PARAM_SEP);
        for (String parstr : split) {
            parstr = parstr.trim();
            if (parstr.startsWith("("))
                parstr = parstr.substring(1, parstr.length());
            if (clean)
                params.add(cleanValue(parstr));
            else {
                if (parstr.endsWith(")"))
                    parstr = parstr.substring(0, parstr.length() - 1);
                params.add(parstr);
            }
        }
        return params;
    }

    /**
     * Extracts the list of possible parameters from the list
     * @param rule	 String, representing the rule
     * @param clean	Indicates, whether strings should be stripped of quote symbols
     * @return	If {@code rule} is valid, returns {@link Entry} structure, where key is the extracted variable and value is the {@link List} of extracted parameters;
     *         otherwise, returns {@link Entry} with {@code null} key value and empty list of parameters
     */
    public Entry<String, List<String>> extractParameterList(String rule, boolean clean) {
        if (!isValidRule(rule))
            return new SimpleImmutableEntry<String, List<String>>(null, new ArrayList<String>());
        return new SimpleImmutableEntry<>(extractVariableName(rule), getParameterList(rule, clean));
    }

    private String cleanValue(String value) {
        if (value.endsWith(")"))
            value = value.substring(0, value.length() - 1);
        if (value.startsWith("\"") || value.startsWith("'"))
            value = value.substring(1, value.length());
        if (value.endsWith("\"") || value.endsWith("'"))
            value = value.substring(0, value.length() - 1);
        return value;
    }

    private String evaluatePosRule(String rule, String value, List<String> params) throws ParseException {
        if (params.size() != 3)
            throw new ParseException(String.format(bundle.getString("RuleParser.5"), rule), 0);
        Integer from = 0, quantity = null;
        if (params.get(0).trim().length() > 0)
            try {
                from = Integer.parseInt(params.get(0));
            } catch (NumberFormatException ex) {
                throw new ParseException(String.format(bundle.getString("RuleParser.0"), rule, "from_pos"), 0);
            }
        if (from < 0)
            throw new ParseException(String.format(bundle.getString("RuleParser.6"), rule, "from_pos"), 0);
        if (params.get(1).trim().length() > 0)
            try {
                quantity = Integer.parseInt(params.get(1));
            } catch (NumberFormatException ex) {
                throw new ParseException(String.format(bundle.getString("RuleParser.0"), rule, "quantity"), 0);
            }
        if (quantity != null && quantity < 0)
            throw new ParseException(String.format(bundle.getString("RuleParser.6"), rule, "quantity"), 0);
        if (from == 0 && quantity == null)
            return value;
        from--;
        String sep = params.get(2);
        if (sep.length() > 0) {
            String[] split = value.split(sep);
            String result = "";
            if (quantity == null) {
                if (from == -1)
                    from = 0;
                for (int i = from; i < split.length; i++)
                    result += split[i] + sep;
                return result.substring(0, result.length() - sep.length());
            }
            if (from > 0 && from > split.length)
                throw new ParseException(String.format(bundle.getString("RuleParser.1"), rule), 0);
            if (quantity > split.length || from + quantity > split.length + 1)
                throw new ParseException(String.format(bundle.getString("RuleParser.2"), rule), 0);
            if (isLeftRule(rule)) {
                if (from == -1)
                    from = 0;
                for (int i = from; i < from + quantity; i++) {
                    result += split[i] + sep;
                }
            } else if (isRightRule(rule)) {
                if (from > 0 && from < quantity)
                    throw new ParseException(String.format(bundle.getString("RuleParser.2"), rule), 0);
                else
                    for (int i = split.length - 1 - from - quantity; i < split.length - 1 - from; i++)
                        result += split[i] + sep;
            }
            return result.length() == 0 ? result : result.substring(0, result.length() - sep.length());
        } else {
            if (from == -1)
                from = 0;
            if (from > value.length())
                throw new ParseException(String.format(bundle.getString("RuleParser.3"), rule), 0);
            if (quantity > value.length() || from + quantity > value.length() + 1)
                throw new ParseException(String.format(bundle.getString("RuleParser.4"), rule), 0);
            if (isLeftRule(rule))
                return quantity != null ? value.substring(from, from + quantity) : value.substring(from);
            else if (isRightRule(rule))
                return quantity != null ? value.substring(value.length() - quantity, value.length()) : value;
        }
        return value;
    }

    /**
     * Apply text extraction rule on given string
     * @param rule		The rule to be applied
     * @param value	The string which applies {@code rule}
     * @return          The {@link String} obtained after applying the rule on {@code value}, if application was successful; {@code value} otherwise
     */
    public String applyExtractionRule(String rule, String value) {
        String val = value;
        List<String> params = extractParameterList(rule, true).getValue();
        try {
            val = evaluatePosRule(rule, value, params);
        } catch (ParseException e) {
            Logger.getGlobal().info(e.getMessage());
            val = value;
        }
        return val;
    }

    /**
     * From the candidate set of rules, select and return the rules which can be applied with the given CONCAT statement
     * @param concatRule	String, representing CONCAT rule
     * @param posRules		 The set of "position" (LEFT and RIGHT) rules
     * @return	The {@link Set} of rules which can be applied within the {@code concatRule}
     */
    public Set<String> getConcatApplied(String concatRule, Collection<String> posRules) {
        Set<String> possible = new HashSet<>();
        List<String> params = extractParameterList(concatRule, true).getValue();
        for (String posRule : posRules) {
            String var = extractParameterList(posRule, true).getKey();
            if (params.contains(var))
                possible.add(posRule);
        }
        return Collections.unmodifiableSet(possible);
    }

    /**
     * Apply the CONCAT rule on a set of given LEFT and RIGHT rules and a set of values and returns the obtained string
     * @param concatRule	The CONCAT rule
     * @param posRules		The set of LEFT and RIGHT rules
     * @param values		The set of strings which apply {@code posRules} and {@code concatRule}
     * @return                  The value obtained after applying given CONCAT rule
     * @throws ParseException	If one of the {@code posRules} is not a valid LEFT or RIGHT rule
     * @throws IllegalArgumentException	If the size of {@code posRules} does not match the size of {@code values}
     */
    public String applyConcatRule(String concatRule, String[] posRules, String[] values) throws ParseException, IllegalArgumentException {
        if (values == null || values.length == 0)
            return applyConcatRule(concatRule);
        if (posRules.length != values.length)
            throw new IllegalArgumentException(bundle.getString("RuleParser.11"));
        Map<String, SimpleEntry<String, String>> ruleMap = new HashMap<>();
        Map<String, String> varMap = new HashMap<>();
        for (int i = 0; i < values.length; i++) {
            String varname = extractVariableName(posRules[i]);
            String rule = null;
            if (posRules[i].contains(VARIABLE_SEP)) {
                if (!isLeftRule(posRules[i]) && !isRightRule(posRules[i]))
                    throw new ParseException(String.format(bundle.getString("RuleParser.10"), posRules[i]), 0);
                else
                    rule = posRules[i].substring(posRules[i].indexOf(VARIABLE_SEP) + VARIABLE_SEP.length());
            }
            if (varname == null)
                throw new ParseException(String.format(bundle.getString("RuleParser.9"), posRules[i]), 0);
            else {
                if (rule == null)
                    varMap.put(varname, values[i]);
                else
                    ruleMap.put(varname, new SimpleEntry<>(rule, values[i]));
            }
        }
        List<String> params = extractParameterList(concatRule, false).getValue();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < params.size(); i++) {
            String param = params.get(i);
            if ((param.startsWith("\"") && !param.endsWith("\"")) || (param.startsWith("'") && !param.endsWith("'")))
                throw new ParseException(String.format(bundle.getString("RuleParser.7"), concatRule), 0);
            if (!param.startsWith("\"") && !param.startsWith("'")) {
                SimpleEntry<String, String> entry = ruleMap.get(param);
                if (entry != null)
                    result.append(applyExtractionRule(entry.getKey(), entry.getValue()));
                else {
                    String value = varMap.get(param);
                    if (value != null)
                        result.append(cleanValue(value));
                    else
                        throw new ParseException(String.format(bundle.getString("RuleParser.8"), concatRule, param), 0);
                }
            } else
                result.append(cleanValue(param));
        }
        return result.toString();
    }

    /**
     * Apply the CONCAT rule in case it does not use any variables (e.g.: {@code concat(create',' rental ', ' contract' )})
     * @param concatRule	The CONCAT rule
     * @return                  The value obtained after applying given CONCAT rule
     * @throws ParseException	If {@code concatRule} is not a valid CONCAT rule
     */
    public String applyConcatRule(String concatRule) throws ParseException {
        List<String> params = extractParameterList(concatRule, false).getValue();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < params.size(); i++) {
            String param = params.get(i);
            if ((param.startsWith("\"") && !param.endsWith("\"")) || (param.startsWith("'") && !param.endsWith("'")))
                throw new ParseException(String.format(bundle.getString("RuleParser.7"), concatRule), 0);
            if (isParameterString(param))
                result.append(cleanValue(param));
            else
                throw new ParseException(String.format(bundle.getString("RuleParser.8"), concatRule, param), 0);
        }
        return result.toString();
    }

    /**
     * Checks if the given string is a parameter of string type (i.e., starts and ends with apostrophes)
     * @param param     The string to be checked
     * @return          {@code true} if the string was identified as a string parameter, {@code false} otherwise
     */
    public boolean isParameterString(String param) {
        return (param.startsWith("\"") && param.endsWith("\"")) || (param.startsWith("'") && param.endsWith("'"));
    }
}
