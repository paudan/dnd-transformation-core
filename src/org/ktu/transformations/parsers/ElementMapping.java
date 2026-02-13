package org.ktu.transformations.parsers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.Collections;
import org.json.JSONObject;
import org.ktu.transformations.parsers.SpecificationReader.IntegrationType;

/**
 * <p>Class, representing transformation elements and their mappings, as defined in transformation pattern. Such mappings are expressed in form of
 * ConnectableElement elements, as they are represented in the pattern, together with internal {@link PropertyStack} and {@link PropertyMapping} structures.</p>
 * <p>The term "mapping element" should be interpreted as "the element which represents the type of particular Element, defined as UML or UML profile concept".
 * Transformation pattern elements (also referred as {@literal mapping elements}) physically are represented as ConnectableElement elements, 
 * as given in the transformation pattern</p>
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, Kaunas University of Technology, 2015
 */
public class ElementMapping {

    /** The source mapping element */
    public ConnectableEntity source = null;
    
    /** Indicates, whether pattern source element maps to the dragged element */
    public boolean mapsToDragged = false;
    
    /** The list of target mapping elements */
    public List<ConnectableEntity> targetList;
    /**
     * The connections between source mapping element and other elements in source part.
     * Key values define the ConnectableElement, connecting with the source, while values represent the paths
     * to particular property in {@code source} which should contain the elements of the type, defined in key.
     * <p>E.g.: given the {@code source} of type {@code Association}, its connections with {@code Actor} and {@code UseCase} will be represented as</p>
     * <p>{@code Actor -> {Association, ownedEnd: Property, type: Type}, UseCase -> {Association, ownedEnd: Property, type: Type} }</p>
     */
    public Map<ConnectableEntity, PropertyStack> sourcePropertyMap = null;
    /**
     * The connections between source mapping element and other elements in target part.
     * Key values define the ConnectableElement, connecting with the source, while values represent the paths
     * to particular property in {@code source} which should contain the elements of the type, defined in key
     */
    public Map<ConnectableEntity, PropertyStack> targetPropertyMap = null;
    /**
     * The connections between source mapping and target mappings, defined in property level
     * (i.e., between properties, which correspond according to defined transformation)
     */
    public Map<PropertyStack, Map<PropertyStack, PropertyMapping>> propertyMap;
    
    /** The {@link Map} of associated naming rules. The keys represent the target mapping, while the values represent the rules themselves */
    private Map<ConnectableEntity, String> namingRules;
    
    /** Default values of primitive property types which are also defined as Connector constraints */
    public Map<String, Object> defaultProps;
    
    /** Mappings for CONCAT connections */
    public ConcatMap concatMap;
    
    /** The {@link Map} of integration type mappings */
    private Map<ConnectableEntity, IntegrationType> integrationMap;
    
    /** Pattern parser object, which performs processing of the transformation pattern, associated with this mapping */
    private PatternParser parser;

    /**
     * The structure, representing connections between two Elements at the property level (i.e., the connection is defined between their properties)
     */
    public static class PropertyMapping {

        protected PropertyStack sourceStack, targetStack;
        /** The naming rule for this property */
        String namingRule = null;
        /** Default values of primitive property types which are also defined as Connector constraints */
        Map<String, Object> defaultProps;
        /** Integration type for this property */
        IntegrationType integrationType;

        /**
         * Initialize a new {@link ElementMapping.PropertyMapping} object
         * @param sourceStack	Source property representation structure
         * @param targetStack	Target property representation structure
         */
        public PropertyMapping(PropertyStack sourceStack, PropertyStack targetStack) {
            this.sourceStack = sourceStack;
            this.targetStack = targetStack;
            defaultProps = new HashMap<>();
        }
        
        /**
         * Initialize a new {@link ElementMapping.PropertyMapping} object by making a shallow copy of given {@link ElementMapping.PropertyMapping} object
         * @param base      {@link ElementMapping.PropertyMapping} object which is used as base element
         */
        public PropertyMapping(PropertyMapping base) {
            this.sourceStack = base.sourceStack;
            this.targetStack = base.targetStack;
            this.defaultProps = base.defaultProps;
            this.integrationType = base.integrationType;
            this.namingRule = base.namingRule;
        }

        /**
         * Get a source property representation structure
         * @return	{@link PropertyStack} representing source property structure
         */
        public PropertyStack getSourceStack() {
            return sourceStack;
        }

        /**
         * Get a target property representation structure
         * @return	{@link PropertyStack} representing target property structure
         */
        public PropertyStack getTargetStack() {
            return targetStack;
        }

        /**
         * Set new target property representation structure
         * @param targetStack	New target property representation structure
         */
        public void setTargetStack(PropertyStack targetStack) {
            this.targetStack = targetStack;
        }

        /**
         * Return the rule which was defined in the Connector object between these two properties
         * @return  The {@link String} representing the naming rule
         */
        public String getNamingRule() {
            return namingRule;
        }

        /**
         * Return the mappings of properties which must contain default values, as defined in the transformation
         * @return	The {@link Map} of properties 
         */
        public Map<String, Object> getDefaultProperties() {
            return Collections.unmodifiableMap(defaultProps);
        }

        /**
         * Return the integration type for this property mapping
         * @return EnumerationLiteral representing integration type
         */
        public IntegrationType getIntegrationType() {
            return integrationType;
        }
        
        private JSONObject toJSONObject() {
            JSONObject obj = new JSONObject();
            if (sourceStack != null && !sourceStack.isEmpty() && targetStack != null && !targetStack.isEmpty())
                obj.put(sourceStack.toString(), targetStack.toString());
            if (namingRule != null)
                obj.put("naming rule", namingRule);
            if (integrationType != null)
                obj.put("integration type", integrationType);
            if (defaultProps != null && !defaultProps.isEmpty()) {
                JSONObject obj2 = new JSONObject();
                for (String key : defaultProps.keySet())
                    obj2.put(key, defaultProps.get(key));
                obj.put("default property values", obj2);
            }
            return obj;
        }

        /**
         * Get a representation of {@link PropertyMapping} object as a string
         * @return A {@link String} representing this object
         */
        @Override
        public String toString() {
            return toJSONObject().toString(4).replaceAll("\"", "");
        }
    }
    
    protected ElementMapping() {
        sourcePropertyMap = new HashMap<>();
        targetPropertyMap = new HashMap<>();
        targetList = new ArrayList<>();
        propertyMap = new HashMap<>();
        namingRules = new HashMap<>();
        defaultProps = new HashMap<>();
        integrationMap = new HashMap<>();
        concatMap = new ConcatMap();
    }

    /**
     * Initializes new {@link ElementMapping} object
     * @param owner {@link PatternParser} object, which is associated with this mapping
     */
    public ElementMapping(PatternParser owner) {
        this();
        this.parser = owner;
    }
    
    public ElementMapping(ElementMapping base) {
        this.sourcePropertyMap = base.sourcePropertyMap;
        this.targetPropertyMap = base.targetPropertyMap;
        this.targetList = base.targetList;
        this.propertyMap = base.propertyMap;
        this.namingRules = base.namingRules;
        this.defaultProps = base.defaultProps;
        this.integrationMap = base.integrationMap;
        this.concatMap = base.concatMap;
        this.source = base.source;
        this.mapsToDragged = base.mapsToDragged;
        this.parser = base.getParser();
    }
    
    /**
     * Return PatternParser object, which performs processing of the transformation pattern, associated with this mapping
     * @return An instance {@link PatternParser}, associated with this mapping
     */
    public PatternParser getParser() {
        return parser;
    }

    /**
     * Get a String representation of {@link ElementMapping} object
     * @return {@link String} representation of this object
     */
    @Override
    public String toString() {
        JSONObject obj = new JSONObject();
        if (source != null) {
            obj.put("source", source.getPrintableName());
            if (mapsToDragged)
                obj.put("isDragged", true);
        }
        if (!targetList.isEmpty()) {
            JSONObject obj2 = new JSONObject();
            for (ConnectableEntity el : targetList)
                obj2.put("name", el.getPrintableName());
            obj.put("targets", obj2);
        }
        if (!namingRules.isEmpty()) {
            JSONObject obj2 = new JSONObject();
            for (ConnectableEntity key : namingRules.keySet())
                if (namingRules.get(key) != null) {
                    obj2.put("mapping name", key.getPrintableName());
                    obj2.put("rule", namingRules.get(key));
                }
            obj.put("naming rules", obj2);
        }
        if (!integrationMap.isEmpty()) {
            JSONObject obj2 = new JSONObject();
            for (ConnectableEntity key : integrationMap.keySet())
                if (integrationMap.get(key) != null) {
                    obj2.put("mapping name", key.getPrintableName());
                    obj2.put("integration type", integrationMap.get(key).getName());
                }
            obj.put("integration types", obj2);
        }
        if (!sourcePropertyMap.isEmpty()) 
            obj.put("connecting source properties", propertyMapString(sourcePropertyMap));
        if (!targetPropertyMap.isEmpty()) 
            obj.put("connecting target properties", propertyMapString(targetPropertyMap));
        if (!propertyMap.isEmpty()) {
            JSONObject obj2 = new JSONObject();
            for (PropertyStack key : propertyMap.keySet()) {
                StringBuilder result = new StringBuilder();
                result.append("{");
                Map<PropertyStack, PropertyMapping> pmaps = propertyMap.get(key);
                if (pmaps != null && !pmaps.isEmpty()) {
                    for (PropertyStack keym : pmaps.keySet()) {
                        PropertyMapping pmap = pmaps.get(keym);
                        if (pmap != null)
                            result.append(pmap.toJSONObject()).append(", ");
                    }
                    result.delete(result.length() - 2, result.length()).append("}");
                }
                obj2.put(key.toString(), result);
            }
            obj.put("connected properties", obj2);
        }
        if (!concatMap.isEmpty()) 
            obj.put("joining mappings", new JSONObject(concatMap.toString()));
        return obj.toString(4);
    }

    private JSONObject propertyMapString(Map<ConnectableEntity, PropertyStack> propMap) {
        JSONObject obj = new JSONObject();
        for (ConnectableEntity property : propMap.keySet()) {
            obj.put("name", property.getPrintableName());
            if (property.isDraggedElement())
                obj.put("isDragged", true);
            obj.put("property name", propMap.get(property));
        }
        return obj;
    }

    /**
     * Add new {@link PropertyMapping} object to this instance
     * @param sourceStack	Source property representation structure
     * @param targetStack	Target property representation structure
     */
    public void addPropertyMapping(PropertyStack sourceStack, PropertyStack targetStack) {
        Map<PropertyStack, PropertyMapping> pmap = propertyMap.get(sourceStack);
        if (pmap == null) {
            pmap = new HashMap<>();
            propertyMap.put(sourceStack, pmap);
        }
        pmap.put(targetStack, new PropertyMapping(sourceStack, targetStack));
    }

    /**
     * Add new {@link PropertyMapping} object to this instance, including rule and map of default properties
     * @param sourceStack	 Source property representation structure
     * @param targetStack	 Target property representation structure
     * @param namingRule	 The rule which was defined in the Connector object between these two properties
     * @param defaultProps	The map of default properties
     */
    public void addPropertyMapping(PropertyStack sourceStack, PropertyStack targetStack,
            String namingRule, Map<String, Object> defaultProps) {
        Map<PropertyStack, PropertyMapping> pmap = propertyMap.get(sourceStack);
        if (pmap == null) {
            pmap = new HashMap<>();
            propertyMap.put(sourceStack, pmap);
        }
        PropertyMapping mapping = new PropertyMapping(sourceStack, targetStack);
        mapping.namingRule = namingRule;
        mapping.defaultProps = defaultProps;
        pmap.put(targetStack, mapping);
    }

    /**
     * Return keys from internal {@code propertyMap} structure
     * @return	An immutable {@link Set} of keys
     */
    public Set<PropertyStack> getPropertyMappingKeys() {
        return Collections.unmodifiableSet(propertyMap.keySet());
    }

    /**
     * Return property structures, which are represented in target partition of pattern and are connected with particular source structure
     * @param source	The source property mapping structure
     * @return	An immutable {@link Set} of target property structures, associated with {@code source}
     */
    public Set<PropertyStack> getPropertyTargets(PropertyStack source) {
        return Collections.unmodifiableSet(propertyMap.get(source).keySet());
    }

    /**
     * Return the set of {@link PropertyMapping} by the given source property mapping, 
     * which connect to particular {@link ElementMapping}
     * @param source        The source property mapping structure
     * @param elementMap    The target element mapping structure
     * @return	A {@link Set} of target property structures
     */
    public Set<PropertyMapping> getPropertyMappings(PropertyStack source, ConnectableEntity elementMap) {
        Set<PropertyMapping> mappings = new HashSet<>();
        Map<PropertyStack, PropertyMapping> targets = propertyMap.get(source);
        if (targets == null || targets.isEmpty())
            return mappings;
        for (PropertyMapping map : targets.values())
            if (map.targetStack.metaElement().equals(elementMap))
                mappings.add(map);
        return Collections.unmodifiableSet(mappings);
    }

    /**
     * Get name processing rule for the {@link PropertyMapping} between given source and target property structures
     * @param source	The source property structure (i.e., structure, defined in source partition of the pattern)
     * @param target	The target property structure (i.e., structure, defined in target partition of the pattern)
     * @return	The naming rule
     */
    public String getPropertyNamingRule(PropertyStack source, PropertyStack target) {
        return propertyMap.get(source).get(target).getNamingRule();
    }
    
    /**
     * Get the integration type for the {@link PropertyMapping} between given source and target property structures
     * @param source	The source property structure (i.e., structure, defined in source partition of the pattern)
     * @param target	The target property structure (i.e., structure, defined in target partition of the pattern)
     * @return Integration type
     */
    public IntegrationType getIntegrationType(PropertyStack source, PropertyStack target) {
        return propertyMap.get(source).get(target).getIntegrationType();
    }

    /**
     * Get the {@link Map} of default properties for the {@link PropertyMapping} between given source and target property structures
     * @param source	The source property structure (i.e., structure, defined in source partition of the pattern)
     * @param target	The target property structure (i.e., structure, defined in target partition of the pattern)
     * @return	An immutable {@link Map} of default properties
     */
    public Map<String, Object> getPropertyDefaultProperties(PropertyStack source, PropertyStack target) {
        return Collections.unmodifiableMap(propertyMap.get(source).get(target).getDefaultProperties());
    }

    /**
     * Checks if mapping element has CONCAT type of mapping as well
     * @param mappingEl	The mapping element to be checked
     * @return	{@code true} if {@code mappingEl} has such type of mapping, {@code false} otherwise
     */
    public boolean hasConcatMapping(ConnectableEntity mappingEl) {
        for (ConnectorEntity key : concatMap.keySet()) 
            if (concatMap.getTargetPropertyStack(key).metaElement().equals(mappingEl))
                return true;
        return false;
    }

    /**
     * Return a structure defining inner connections between elements in pattern target list
     * @return	A {@link Map} defining inner connections between elements in {@code targetList}
     */
    public Map<ConnectableEntity, List<ConnectableEntity>> getTargetInnerConnections() {
        Map<ConnectableEntity, List<ConnectableEntity>> map = new HashMap<>();
        for (ConnectableEntity key : targetPropertyMap.keySet()) {
            ConnectableEntity mapKey = targetPropertyMap.get(key).metaElement();
            if (targetList.contains(mapKey)) {
                List<ConnectableEntity> list = map.get(mapKey);
                if (list == null)
                    list = new ArrayList<>();
                list.add(key);
                map.put(mapKey, list);
                // We want to have both mappings <el1, List(el2)> and <el2, List(el1)>
                list = map.get(key);
                if (list == null)
                    list = new ArrayList<>();
                list.add(mapKey);
                map.put(key, list);
            }
        }
        return map;
    }
    
    /**
     * Get the mappings for naming rules
     * @return An immutable {@link Map} representing mappings with naming rules
     */
    public Map<ConnectableEntity, String> getNamingRules() {
        return Collections.unmodifiableMap(namingRules);
    }
    
    /**
     * Get the mappings for integration types
     * @return An immutable {@link Map} representing mappings with integration types 
     */
    public Map<ConnectableEntity, IntegrationType> getIntegrationMappings() {
        return Collections.unmodifiableMap(integrationMap);
    }

    /**
     * Add a naming rule to the naming rule mappings
     * @param element   The mapping element which should apply the naming rule
     * @param rule      The rule to be applied
     */
    public void addNamingRule(ConnectableEntity element, String rule) {
        namingRules.put(element, rule);
    }
    
    /**
     * Add an integration type to the integration type mappings
     * @param element   The mapping element which should apply the integration with the given type
     * @param type      The integration type
     */
    public void addIntegrationType(ConnectableEntity element, IntegrationType type) {
        integrationMap.put(element, type);
    }
    
    
}
