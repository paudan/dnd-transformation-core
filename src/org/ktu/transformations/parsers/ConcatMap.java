package org.ktu.transformations.parsers;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONObject;
import org.ktu.transformations.parsers.SpecificationReader.IntegrationType;

/**
 * Class, acting as the map of elements for {@code CONCAT} mappings in partial M2M transformation pattern
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, Kaunas University of Technology, 2015
 */
public class ConcatMap {

    /** The map containing {@link ConnectorEntity} elements with their endpoint {@link ConnectorEntity} elements */
    protected Map<ConnectorEntity, Map<ConnectorEntity, PropertyStack>> concatMap;
    /** The map containing {@link ConnectorEntity} elements with their target {@link PropertyStack} elements */
    protected Map<ConnectorEntity, PropertyStack> targetMap;

    /** Enumeration which is used to define validation error types for CONCAT mappings */
    public enum ConcatErrorType {

        /** The variable is not absent in the incoming connection */
        MISSING_VARIABLE,
        /** The name of the variable is invalid */
        INVALID_VARIABLE,
        /** There are duplicate variable names in the incoming connections */
        DUPLICATE_VARIABLE,
        /** The CONCAT rule is missing */
        MISSING_CONCAT_RULE,
        /** The CONCAT rule is invalid (e.g., containing variables which are not defined in the incoming connections) */
        INVALID_CONCAT_RULE
    }

    public ConcatMap() {
        concatMap = new HashMap<>();
        targetMap = new HashMap<>();
    }
    
    /**
     * Initialize a new {@link ConcatMap} object by making a shallow copy of given {@link ConcatMap} object
     * @param base      {@link ConcatMap} object which is used as base element
     */
    public ConcatMap(ConcatMap base) {
        this.concatMap = base.concatMap;
        this.targetMap = base.targetMap;
    }

    /**
     * Add an entry, representing an incoming connection to the CONCAT element
     * @param targetConn {@link ConnectorEntity} representing the outgoing connection from particular CONCAT element (thus the particular CONCAT rule itself)
     * @param target     {@link PropertyStack} the property in the transformation pattern target part which is the target of the CONCAT mapping  
     * @param sourceConn {@link ConnectorEntity} representing the incoming connection to the CONCAT element (i.e., part of the the CONCAT rule)   
     * @param source     {@link PropertyStack} the property in the transformation pattern source part which is the source of the CONCAT mapping, 
     * corresponding to the {@code sourceConn}
     */
    public void addIncomingEntry(ConnectorEntity targetConn, PropertyStack target, ConnectorEntity sourceConn, PropertyStack source) {
        if (targetConn == null || sourceConn == null)
            return;
        Map<ConnectorEntity, PropertyStack> lmap = concatMap.get(targetConn);
        if (lmap == null) {
            lmap = new HashMap<>();
            concatMap.put(targetConn, lmap);
        }
        lmap.put(sourceConn, source);
        targetMap.put(targetConn, target);
    }

    /**
     * Return a string representing this {@link ConcatMap} object
     * @return The {@link String} representation of this object
     */
    @Override
    public String toString() {
        JSONObject obj = new JSONObject();
        for (ConnectorEntity key : concatMap.keySet()) {
            obj.put("rule", key.getRule());
            obj.put("target", targetMap.get(key));
            obj.put("integration type", key.getIntegrationType());
            JSONObject obj2 = new JSONObject();
            Map<ConnectorEntity, PropertyStack> pmaps = concatMap.get(key);
            if (pmaps != null && !pmaps.isEmpty()) 
                for (ConnectorEntity keym : pmaps.keySet()){
                    JSONObject obj3 = new JSONObject();
                    obj3.put("source", pmaps.get(keym));
                    obj3.put("integration type", keym.getIntegrationType());
                    obj2.put(keym.getRule(), obj3);
                }
            obj.put("incoming rules", obj2);
        }
        return obj.toString(4);
    }

    /**
     * Return the key set of the CONCAT map, representing existing CONCAT mappings
     * @return An immutable {@link Set} of {@link ConnectorEntity}, representing existing CONCAT mappings
     */
    public Set<ConnectorEntity> keySet() {
        return Collections.unmodifiableSet(concatMap.keySet());
    }
    
    /**
     * Return the source PropertyStack structure for the given target and source parts of particular CONCAT mapping
     * @param target   {@link ConnectorEntity} corresponding to the outgoing part of the CONCAT type of mapping 
     * (the mapping itself, as the key in the set returned by {@link #keySet()})
     * @param src      {@link ConnectorEntity} corresponding to the incoming part in the CONCAT mapping
     * @return  {@link PropertyStack} representing the property which is the source for {@code src} in the given CONCAT mapping
     */
    public PropertyStack getSourcePropertyStack(ConnectorEntity target, ConnectorEntity src) {
        return concatMap.get(target).get(src);
    }

    /**
     * Return the incoming (source) parts of particular CONCAT mapping
     * @param key   {@link ConnectorEntity} corresponding to the outgoing part of the CONCAT type of mapping (i.e, the mapping itself)
     * @return      An immutable {@link Set} of {@link ConnectorEntity} element which are contained in the list of values of the CONCAT map for the key {@code key}  
     */
    public Set<ConnectorEntity> getIncomingConnectors(ConnectorEntity key) {
        return Collections.unmodifiableSet(concatMap.get(key).keySet());
    }
    
    /**
     * Check if the CONCAT map is empty
     * @return  {@code true} if the CONCAT map is empty; {@code false} otherwise
     */
    public boolean isEmpty() {
        return concatMap.isEmpty();
    }

    /**
     * Return the {@link PropertyStack} entity which is associated with the particular CONCAT mapping as the target property/element
     * @param mapping {@link ConnectorEntity} corresponding to the outgoing part of the CONCAT type of mapping (i.e, the mapping itself)
     * @return     {@link PropertyStack} which is the target of the mapping represented by {@code mapping}
     */
    public PropertyStack getTargetPropertyStack(ConnectorEntity mapping) {
        return targetMap.get(mapping);
    }

    /**
     * Return all {@link PropertyStack} entities which are the source property/elements in the CONCAT map
     * @return An immutable {@link Set} of all {@link PropertyStack} elements which are the source elements for at least one of the CONCAT mappings in the map
     */
    public Set<PropertyStack> getSourcePropertyStackSet() {
        Set<PropertyStack> set = new HashSet<>();
        for (ConnectorEntity conn : concatMap.keySet()) {
            Map<ConnectorEntity, PropertyStack> connMap = concatMap.get(conn);
            for (ConnectorEntity sConn : connMap.keySet())
                set.add(connMap.get(sConn));
        }
        return Collections.unmodifiableSet(set);
    }
    
    private boolean isUndefined(IntegrationType type) {
        return type == null || type == IntegrationType.UNDEFINED;
    }
    
    /**
     * Returns the integration type for particular incoming part of CONCAT rule 
     * @param sourceConn {@link ConnectorEntity} corresponding to the incoming part in the CONCAT mapping
     * @param outConn    {@link ConnectorEntity} corresponding to the outgoing part in the CONCAT mapping
     * @return  The pair object {@link SimpleImmutableEntry} representing the {@link IntegrationType} for particular {@link ConnectorEntity}. 
     * The value is equal to either {@code sourceConn} or {@code outConn}, depending on the setting of integration type for each of these two entities
     */
    public SimpleImmutableEntry<IntegrationType, ConnectorEntity> getIntegrationType(ConnectorEntity sourceConn, ConnectorEntity outConn) {
        IntegrationType outSetting = outConn.getIntegrationType();
        if (isUndefined(outSetting) && outSetting == IntegrationType.NONE)
            return new SimpleImmutableEntry<>(outSetting, outConn);
        IntegrationType incSetting = sourceConn.getIntegrationType();
        if (isUndefined(incSetting) && isUndefined(outSetting))
            return null;
        else if (isUndefined(incSetting) && !isUndefined(outSetting))
            return new SimpleImmutableEntry<>(outSetting, outConn);
        else if (!isUndefined(incSetting) && incSetting == IntegrationType.NONE)
            return new SimpleImmutableEntry<>(incSetting, sourceConn);
        else if (!isUndefined(incSetting) && incSetting == IntegrationType.DEFAULT)
            return new SimpleImmutableEntry<>(IntegrationType.PARTIAL, sourceConn);
        else if (!isUndefined(incSetting) && incSetting == IntegrationType.PARTIAL) {
            if (isUndefined(outSetting) || outSetting == IntegrationType.FULL)
                return new SimpleImmutableEntry<>(incSetting, sourceConn);
            else
                return new SimpleImmutableEntry<>(outSetting, outConn);
        } else if (!isUndefined(incSetting) && incSetting == IntegrationType.FULL) {
            if (outSetting == null)
                return new SimpleImmutableEntry<>(incSetting, sourceConn);
            else
                return new SimpleImmutableEntry<>(outSetting, outConn);
        }
        return null;
    }

    /**
     * Performs validation of the given CONCAT mapping (defined by the outgoing part) and returns the set of associated validations errors
     * @param target        The mapping, defined as the {@link ConnectorEntity} corresponding to the outgoing part in the CONCAT mapping
     * @return  An immutable {@link Map} which contains the objects that are the sources or targets in the improperly defined or invalid part 
     * of CONCAT mapping, depending on the type of this part (incoming/outgoing), together with the error types for this part of the mapping
     */
    public Map<Object, ConcatErrorType> validateMapping(ConnectorEntity target) {
        Map<Object, ConcatErrorType> errors = new HashMap<>();
        RuleParser ruleParser = new RuleParser();
        String targetRule = target.getRule();
        if (targetRule == null)
            errors.put(targetMap.get(target), ConcatErrorType.MISSING_CONCAT_RULE);
        List<String> variables = new ArrayList<>();
        Map<ConnectorEntity, PropertyStack> incoming = concatMap.get(target);
        for (ConnectorEntity conn : incoming.keySet()) {
            String rule = conn.getRule();
            if (rule == null)
                errors.put(incoming.get(conn), ConcatErrorType.MISSING_VARIABLE);
            else {
                String variable = ruleParser.extractVariableName(rule);
                if (variable == null
                        || (variable.length() != rule.length() && !ruleParser.isLeftRule(rule) && !ruleParser.isRightRule(rule)))
                    errors.put(incoming.get(conn), ConcatErrorType.INVALID_VARIABLE);
                else
                    variables.add(variable);
            }
        }
        for (String var : new HashSet<>(variables))
            if (Collections.frequency(variables, var) > 1)
                errors.put(var, ConcatErrorType.DUPLICATE_VARIABLE);
        if (!ruleParser.isConcatRule(targetRule))
            errors.put(targetMap.get(target), ConcatErrorType.INVALID_CONCAT_RULE);
        List<String> params = ruleParser.extractParameterList(targetRule, false).getValue();
        for (String param : params)
            if (!ruleParser.isParameterString(param) && !variables.contains(param))
                errors.put(param, ConcatErrorType.INVALID_CONCAT_RULE);
        return Collections.unmodifiableMap(errors);
    }

}
