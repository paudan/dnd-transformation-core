package org.ktu.transformations.parsers;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collections;
import java.util.logging.Logger;
import org.ktu.transformations.helpers.AbstractPropertyManager;
import org.ktu.transformations.mappers.ElementMapper;
import org.ktu.transformations.parsers.SpecificationReader.IntegrationType;
import org.ktu.transformations.transforms.Transformation;

/** Internal structure representing tuple {@code <element; rule; property>} */
class ConcatStructure<Element> {

    public Element sourceElement;
    public String rule;
    public PropertyStack property;

    protected ConcatStructure(Element sourceElement, PropertyStack property, String rule) {
        this.sourceElement = sourceElement;
        this.rule = rule;
        this.property = property;
    }

}

/**
 * Resolves CONCAT type of mappings, defined in transformation pattern
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, Kaunas University of Technology, 2015
 * @param <Element>   Actual type representing UML Element
 */
public class ConcatResolver<Element> {

    private final Map<ConnectableEntity, ElementMapping> targets;
    private final Element connectingEl;
    private final ConnectableEntity connectingMap;
    private final Map<ConnectableEntity, List<Object>> sourceProps;
    /** The map of names, which must be output for each property of particular {@link ConnectableEntity}, 
     * which has a defined CONCAT mapping*/
    private final Map<ConnectableEntity, Map<PropertyStack, List<String>>> outputValues;
    
    /** Internal structure representing generated names, together with their source elements */
    private final Map<ConnectableEntity, Map<PropertyStack, Map<String, List<ConcatStructure<Element>>>>> sourceMappings;
    
    /** The map of integrations which may be applied for each source element */
    private final Map<ConnectableEntity, Map<PropertyStack, SimpleImmutableEntry<IntegrationType, ConnectorEntity>>> integrationMappings;
    
    private final Transformation<Element, ?> factory;

    /**
     * Creates and initializes internal structures by resolving CONCAT mappings in given mapping structures for particular transformation pattern
     * @param targets		A {@link Map} structure which contains mapping elements together with their mapping structures
     * @param sourceProps	A {@link Map} which contains selected property objects and corresponds to {@code targets}.
     * The keys correspond to mapping elements, while the values are the lists of objects which are actually selected according to these mappings
     * @param connectingEl	An instance of connecting Element
     * @param connectingMap	Mapping element, corresponding to {@code connectingEl}
     * @param factory           The {@link Transformation} object which uses this resolver object to produce candidate elements
     */
    public ConcatResolver(Map<ConnectableEntity, ElementMapping> targets, Map<ConnectableEntity, List<Object>> sourceProps, 
            Element connectingEl, ConnectableEntity connectingMap, Transformation<Element, ?> factory) {
        super();
        this.targets = targets;
        this.sourceProps = sourceProps;
        this.connectingEl = connectingEl;
        this.connectingMap = connectingMap;
        this.factory = factory;
        outputValues = new HashMap<>();
        sourceMappings = new HashMap<>();
        integrationMappings = new HashMap<>();
        resolve();
    }

    private void resolve() {
        if (targets == null || targets.isEmpty())
            return;
        ElementMapper<Element, ?, ?> mapper = factory.getElementMapper();
        for (ConnectableEntity prop : targets.keySet()) {
            ConcatMap concatMap = targets.get(prop).concatMap;
            if (concatMap != null && !concatMap.isEmpty()) {
                for (ConnectorEntity key : concatMap.keySet()) {
                    PropertyStack targetStack = concatMap.getTargetPropertyStack(key);
                    ConnectableEntity targetMeta = targetStack.metaElement();
                    Map<PropertyStack, List<String>> pvalmap = new HashMap<>();
                    pvalmap.put(targetStack, new ArrayList<String>());
                    outputValues.put(targetMeta, pvalmap);
                    Map<PropertyStack, Map<String, List<ConcatStructure<Element>>>> sourceMap = new HashMap<>();
                    sourceMap.put(targetStack, new HashMap<String, List<ConcatStructure<Element>>>());
                    sourceMappings.put(targetMeta, sourceMap);
                    Map<String, List<ConcatStructure<Element>>> ruleElMap = new HashMap<>();
                    Set<ConnectorEntity> concatRules = concatMap.getIncomingConnectors(key);
                    for (ConnectorEntity sConn : concatRules) {
                        String rule = sConn.getRule();
                        ruleElMap.put(rule, new ArrayList<ConcatStructure<Element>>());
                        PropertyStack smap = concatMap.getSourcePropertyStack(key, sConn);
                        if (sourceProps != null)
                            for (ConnectableEntity keyGenProp : sourceProps.keySet())
                                if (smap.metaElement().equals(keyGenProp))
                                    for (Object selem : sourceProps.get(keyGenProp))
                                        if (mapper.isElement(selem))
                                            ruleElMap.get(rule).add(new ConcatStructure<>((Element)selem, smap, rule));
                        if (smap.metaElement().equals(connectingMap))
                            ruleElMap.get(rule).add(new ConcatStructure<>(connectingEl, smap, rule));
                    }
                    // Generate Cartesian product (must test later with more elements!)
                    Set<HashSet<ConcatStructure<Element>>> cart = new HashSet<>();
                    for (String rkey : ruleElMap.keySet()) {
                        Set<HashSet<ConcatStructure<Element>>> newCart = new HashSet<>();
                        for (int i = 0; i < ruleElMap.get(rkey).size(); i++)
                            if (cart.isEmpty()) {
                                HashSet<ConcatStructure<Element>> newSet = new HashSet<>();
                                newSet.add(ruleElMap.get(rkey).get(i));
                                newCart.add(newSet);
                            } else
                                for (HashSet<ConcatStructure<Element>> cset : cart) {
                                    HashSet<ConcatStructure<Element>> newSet = new HashSet<>();
                                    newSet.addAll(cset);
                                    newSet.add(ruleElMap.get(rkey).get(i));
                                    newCart.add(newSet);
                                }
                        cart = newCart;
                    }
                    List<String> generatedNames = outputValues.get(targetMeta).get(targetStack);
                    Map<String, List<ConcatStructure<Element>>> sources = sourceMappings.get(targetMeta).get(targetStack);
                    for (HashSet<ConcatStructure<Element>> set : cart) {
                        try {
                            String generated = getGeneratedName(key.getRule(), set);
                            generatedNames.add(generated);
                            if (sources == null) {
                                sources = new HashMap<>();
                                sourceMappings.get(targetMeta).put(targetStack, sources);
                            }
                            List<ConcatStructure<Element>> sourceObjs = sources.get(generated);
                            if (sourceObjs == null) {
                                sourceObjs = new ArrayList<>();
                                sources.put(generated, sourceObjs);
                            }
                            for (ConcatStructure<Element> conStruct: set)
                                sourceObjs.add(conStruct);
                        } catch (IllegalArgumentException | ParseException e) {
                            Logger.getLogger(getClass().getName()).info(e.getMessage());
                        }
                    }
                    // Resolve integrations, which should be applied for each source mapping
                    Map<PropertyStack, SimpleImmutableEntry<IntegrationType, ConnectorEntity>> propIntMap = new HashMap<>();
                    for (ConnectorEntity srcConn: concatMap.getIncomingConnectors(key)) {
                        PropertyStack srcStack = concatMap.getSourcePropertyStack(key, srcConn);
                        propIntMap.put(srcStack, concatMap.getIntegrationType(srcConn, key));
                    }
                    integrationMappings.put(targetMeta, propIntMap);
                }
            }
        }
    }

    /**
     * Return generated candidate names for particular property of an element
     * @param mappingEl	The {@link ConnectableEntity} mapping object which maps to particular Element
     * @param propStack	Structure, representing particular property mapping of this Element
     * @return	The {@link List} of generated candidate names
     */
    public List<String> getGeneratedNames(ConnectableEntity mappingEl, PropertyStack propStack) {
        List<String> res = outputValues.get(mappingEl).get(propStack);
        return res != null ? Collections.unmodifiableList(res) : null;
    }

    /**
     * Return generated candidate names for particular element
     * @param mappingEl	The {@link ConnectableEntity} mapping object which maps to particular Element
     * @return	The {@link Map} structure, where keys represent particular property mapping in form of {@link PropertyStack},
     *         and values are {@link List} of generated candidate names
     */
    public Map<PropertyStack, List<String>> getGeneratedNames(ConnectableEntity mappingEl) {
        Map<PropertyStack, List<String>> res = outputValues.get(mappingEl);
        return res != null ? Collections.unmodifiableMap(res) : null;
    }
    
    /**
     * Return source elements for particular generated candidate names
     * @param mappingEl     The {@link ConnectableEntity} mapping object which maps to particular Element
     * @param targetStack   The {@link PropertyStack} representing property tree, 
     * which maps to the property element, that is generated using CONCAT mapping
     * @param targetName    The name of the candidate target element, generated after applying CONCAT mapping
     * @return              The {@link Map} structure, where keys represent the source element, 
     * and values represent their mappings as particular properties for the source elements in form of {@link PropertyStack},
     */
    public Map<Object, PropertyStack> getSourceElements(ConnectableEntity mappingEl, PropertyStack targetStack, String targetName) {
        List<ConcatStructure<Element>> sources = sourceMappings.get(mappingEl).get(targetStack).get(targetName);
        Map<Object, PropertyStack> res = new HashMap<>();
        for (ConcatStructure struct: sources)
            res.put(struct.sourceElement, struct.property);
        return Collections.unmodifiableMap(res);
    }
    
    /**
     * Return a pair representing the integration type for particular source ConnectorEntity object
     * @param targetEl  {@link ConnectableEntity} which represents the target in the integration relationship 
     * @param src       {@link PropertyStack} which represents the source in the integration relationship
     * @return  {@link SimpleImmutableEntry} consisting of the integration type and {@link ConnectorEntity} 
     * which is a mapping for particular source element or its property    
     */
    public SimpleImmutableEntry<IntegrationType, ConnectorEntity> getIntegrationType(ConnectableEntity targetEl, PropertyStack src) {
        return integrationMappings.get(targetEl).get(src);
    }

    /**
     * Returns indication of CONCAT mapping presence
     * @param mappingEl	The mapping element, which corresponds to particular Element
     * @return {@code true} if mapping element has one or more CONCAT mappings, {@code false} otherwise
     */
    public boolean hasConcatMapping(ConnectableEntity mappingEl) {
        for (ConnectableEntity prop : targets.keySet())
            if (targets.get(prop).hasConcatMapping(mappingEl))
                return true;
        return false;
    }

    private String getGeneratedName(String rule, HashSet<ConcatStructure<Element>> ruleMap) throws IllegalArgumentException, ParseException {
        String[] rules = new String[ruleMap.size()];
        String[] elements = new String[ruleMap.size()];
        ElementMapper<Element, ?, ?> mapper = factory.getElementMapper();
        AbstractPropertyManager<Element, ?, ?> propManager = factory.getPropertyManager();
        int i = 0;
        for (ConcatStructure<Element> struct : ruleMap) {
            rules[i] = struct.rule;
            PropertyStack propStack = struct.property;
            if (propStack.size() > 1) {
                String name = propStack.lowermostProperty().getName3();
                Object propVal = propManager.getPropertyObject(struct.sourceElement, propStack, name);
                if (propVal instanceof String)
                    elements[i] = (String) propVal;
            } else {
                Element source = struct.sourceElement;
                String name = mapper.getActualName(source);
                if (!mapper.isProperty(source) && name != null && name.trim().length() > 0)
                    elements[i] = mapper.getProperName(name);
            }
            i++;
        }
        return new RuleParser().applyConcatRule(rule, rules, elements);
    }

    /**
     * Generate Cartesian product
     * TODO: must test later with more than two sets of elements!
     */
    private List<ArrayList<SimpleImmutableEntry<PropertyStack, String>>> getCombinationSet(ConnectableEntity targetEl) {
        Map<PropertyStack, List<String>> concatCombMap = outputValues.get(targetEl);
        List<ArrayList<SimpleImmutableEntry<PropertyStack, String>>> cart = new ArrayList<>();
        for (PropertyStack stcomb : concatCombMap.keySet()) {
            List<ArrayList<SimpleImmutableEntry<PropertyStack, String>>> newCart = new ArrayList<>();
            for (int i = 0; i < concatCombMap.get(stcomb).size(); i++)
                if (cart.isEmpty()) {
                    ArrayList<SimpleImmutableEntry<PropertyStack, String>> newSet = new ArrayList<>();
                    newSet.add(new SimpleImmutableEntry<>(stcomb, concatCombMap.get(stcomb).get(i)));
                    newCart.add(newSet);
                } else
                    for (ArrayList<SimpleImmutableEntry<PropertyStack, String>> cset : cart) {
                        ArrayList<SimpleImmutableEntry<PropertyStack, String>> newSet = new ArrayList<>();
                        newSet.addAll(cset);
                        newSet.add(new SimpleImmutableEntry<>(stcomb, concatCombMap.get(stcomb).get(i)));
                        newCart.add(newSet);
                    }
            cart = newCart;
        }
        return cart;
    }

    /**
     * Creates and returns a set of candidate elements as copies of particular element, according to CONCAT mapping structure
     * @param targetEl	The target Element
     * @param generated	The {@link Map} structure, where keys represent particular Element, and values are defined as {@link SimpleImmutableEntry},
     *                  where key represents actual Element with updated properties, and value contains the mapping element
     * @return	Updated {@code generated} structure
     */
    public Map<Object, SimpleImmutableEntry<Element, ConnectableEntity>> updateElementProperties(ConnectableEntity targetEl,
            Map<Object, SimpleImmutableEntry<Element, ConnectableEntity>> generated) {
        List<ArrayList<SimpleImmutableEntry<PropertyStack, String>>> cart = getCombinationSet(targetEl);
        ElementMapper<Element, ?, ?> mapper = factory.getElementMapper();
        AbstractPropertyManager<Element, ?, ?> propManager = factory.getPropertyManager();
        int ind = 0;
        for (Object newel : generated.keySet()) {
            ArrayList<SimpleImmutableEntry<PropertyStack, String>> item = cart.get(ind);
            if (generated.get(newel) != null) {
                Element genEl = generated.get(newel).getKey();
                ConnectableEntity value = generated.get(newel).getValue();
                for (SimpleImmutableEntry<PropertyStack, String> propMap : item) {
                    if (propMap.getKey().size() == 1) {
                        if (mapper.isProperty(genEl))
                            mapper.setName(mapper.getTypeElement(genEl), propMap.getValue());
                    } else
                        genEl = (Element) propManager.setProperty(genEl, propMap.getKey(), propMap.getValue());
                }
                generated.put(newel, new SimpleImmutableEntry<>(genEl, value));
            }
            ind++;
        }
        return generated;
    }

}
