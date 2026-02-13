package org.ktu.transformations.parsers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import org.ktu.transformations.helpers.ElementSearch;
import org.ktu.transformations.mappers.ElementMapper;
import org.ktu.transformations.parsers.SpecificationReader.IntegrationType;

/**
 * Abstract class which performs basic transformation pattern parsing and resolving
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of
 * Information Systems Design Technologies, Kaunas University of Technology, 2015
 * @param <Connector>           Type, corresponding to actual UML Connector implementation
 * @param <ConnectableElement>  Type, corresponding to actual UML Connectable Element implementation
 * @param <Element>             Type, corresponding to actual UML Element implementation
 * @param <Stereotype>          Type, corresponding to actual UML Stereotype implementation
 */
@SuppressWarnings({"unchecked", "rawtypes", "deprecation"})
public abstract class PatternParser<Connector, ConnectableElement, Element extends Object, Stereotype> {

    /** Indicates if pattern validation should be performed. It is usually set during initialization */
    protected boolean validate;
    /** Elements representing source and target parts in the {@code rootPattern} element */
    protected Element root[];
    /** Indication if transformation pattern also contains an element which is set as "selected"/"dragged" */
    protected boolean hasDraggedMark;
    /** Element which contains (realizes) the whole transformation pattern */
    protected Element rootPattern;
    /** The target classifier */
    protected Element targetCl;
    /** The element that the element is dragged on (in case of D&amp;D transformation) */
    protected Element elementOver = null;
    /** The mapper object which maps abstract functionality in this framework to functionality in particular implementations */
    protected ElementMapper<Element, ConnectableElement, Stereotype> mapper;
    /** The factory object for producing {@link ElementMapping} instances */
    protected ElementMappingFactory mappingFactory;
    /** Element mapping objects for source and target parts */
    protected Map<ConnectableEntity, ElementMapping> sources = null, targets = null;
    /** Map which maps elements in the transformation pattern parts to their {@link ConnectableEntity} implementations */
    protected Map<Object, ConnectableEntity> entityMap;
    
    private final ResourceBundle bundle = ResourceBundle.getBundle("org/ktu/transformations/messages_EN");

    private PatternParser(Element rootPattern, Element targetCl, ElementMapper<Element, ConnectableElement, Stereotype> mapper, boolean validate) {
        this.validate = validate;
        this.rootPattern = rootPattern;
        this.targetCl = targetCl;
        this.mapper = mapper;
        sources = new HashMap<>();
        targets = new HashMap<>();
        entityMap = new HashMap<>();
        mappingFactory = new ElementMappingFactory();
    }

    /**
     * Create a new instance of {@link PatternParser}
     * @param rootPattern   The root classifier object (generally, representation of UML Structured Classifier) which is the implementation of transformation pattern
     * @param targetCl      The target classifier. It should not be equal to {@code null} if the action is performed on element other than UML Diagram 
     * @param mapper        The mapper object which maps abstract functionality in this framework to functionality in particular implementations
     */
    public PatternParser(Element rootPattern, Element targetCl, ElementMapper<Element, ConnectableElement, Stereotype> mapper) {
        this(rootPattern, targetCl, mapper, true);
    }
    
    /**
     * Create a new instance of {@link PatternParser}
     * @param rootPattern   The root classifier object (generally, representation of UML Structured Classifier) which is the implementation of transformation pattern
     * @param targetCl      The target classifier. It should not be equal to {@code null} if the action is performed on element other than UML Diagram 
     * @param mapper        The mapper object which maps abstract functionality in this framework to functionality in particular implementations
     * @param elementOver   The element that the element is dragged on (in case of D&amp;D transformation)
     * @param validate      Indicates if pattern validation procedure {@link #validatePattern() } should be performed during initialization
     */
    public PatternParser(Element rootPattern, Element targetCl, ElementMapper<Element, ConnectableElement, Stereotype> mapper,
            Element elementOver, boolean validate) {
        this(rootPattern, targetCl, mapper, validate);
        this.elementOver = elementOver;
    }
    
    /**
     * Create a new instance of {@link PatternParser}
     * @param rootPattern   The root classifier object (generally, representation of UML Structured Classifier) which is the implementation of transformation pattern
     * @param targetCl      The target classifier. It should not be equal to {@code null} if the action is performed on element other than UML Diagram 
     * @param mapper        The mapper object which maps abstract functionality in this framework to functionality in particular implementations
     * @param elementOver   The element that the element is dragged on (in case of D&amp;D transformation)
     */
    public PatternParser(Element rootPattern, Element targetCl, ElementMapper<Element, ConnectableElement, Stereotype> mapper, Element elementOver) {
        this(rootPattern, targetCl, mapper, elementOver, true);
    }
    
    /**
     * Get the PatternConfiguration object, defining the configuration of this transformation pattern
     * @return {@link PatternConfiguration} object
     */
    public abstract PatternConfiguration getPatternConfiguration();
    
    /**
     * Return a {@link Map} of "source-to-target" mappings
     * @return {@link Map} of mappings
     */
    public abstract Map<ConnectableEntity, ElementMapping> getSourceMappings();

    /**
     * Return a {@link Map} of "target-to-source" mappings
     * @return {@link Map} of mappings
     */
    public abstract Map<ConnectableEntity, ElementMapping> getTargetMappings();
    
    /**
     * Create internal mapping structures
     * @throws InvalidPatternException	Transformation pattern is invalid or could not be processed
     */
    protected void parse() throws InvalidPatternException {
        if (validate)
            validatePattern();
        if (root == null)
            return;
        Element source = root[0], target = root[1];
        sources.clear();
        targets.clear();
        Collection<Object> sourceElements = getSourceElements();
        Map<PropertyStack, ConnectableEntity> sourceProps = getSourcePropertyMappings(), targetProps = getTargetPropertyMappings();
        Map<Connector, ConnectableElement[]> patternConn = this.getPatternConnectorElements();
        for (Connector el : patternConn.keySet()) {
            ConnectableElement first = patternConn.get(el)[0];
            ConnectableElement second = patternConn.get(el)[1];
            for (ConnectableElement ec : patternConn.get(el)) {
                ElementMapping struct = mappingFactory.createElementMapping(this);
                ConnectableElement opposite = ec == first ? second : first;
                IntegrationType intType = getIntegrationType(el);
                String rule = getRuleText(el);
                ConnectableEntity mc = getConnectableEntity(ec);
                if (getRootPart(ec) == source && !hasJoinStereotype(opposite)) {
                    ConnectableEntity tEntity = getConnectableEntity(opposite);
                    ElementMapping mapping = sources.get(mc);
                    if (mapping != null) {
                        mapping.targetList.add(tEntity);
                        if (rule != null) 
                            mapping.addNamingRule(tEntity, rule);
                        mapping.addIntegrationType(tEntity, intType);
                        performAdditionalUpdate(mapping, tEntity, el);
                    } else {
                        struct.source = mc;
                        struct.targetList.add(tEntity);
                        if (rule != null) 
                            struct.addNamingRule(tEntity, rule);
                        struct.addIntegrationType(tEntity, intType);
                        performAdditionalUpdate(struct, tEntity, el);
                        // Identify, if pattern source element represents dragged element
                        if (mc.isDraggedElement()) {
                            struct.mapsToDragged = true;
                            hasDraggedMark = true;
                        }
                        sources.put(mc, struct);
                    }
                } else if (getRootPart(ec) == target && !hasJoinStereotype(opposite)) {
                    if (struct.source == null)
                        struct.source = ec == first ? getConnectableEntity(second) : getConnectableEntity(first);
                    struct.targetList.add(mc);
                    if (rule != null)
                        struct.addNamingRule(mc, rule);
                    struct.addIntegrationType(mc, intType);
                    targets.put(mc, struct);
                }
            }
        }
        
        // It is possible to have source elements without mapped target elements or vice versa
        Map<Connector, ConnectableElement[]> sourceConn = getSourceConnectorElements();
        for (Connector c : sourceConn.keySet()) {
            for (ConnectableElement end : sourceConn.get(c)) {
                ConnectableEntity parent = null;
                for (PropertyStack key : sourceProps.keySet()) {
                    if (key.containsRef(end)) {
                        parent = sourceProps.get(key);
                        break;
                    }
                }
                if (parent != null && !sources.containsKey(parent)) {
                    ElementMapping struct = mappingFactory.createElementMapping(this);
                    struct.source = parent;
                    sources.put(parent, struct);
                }
            }
        } 
        for (Connector c : sourceConn.keySet()) {
            ConnectableElement first = sourceConn.get(c)[0];
            ConnectableElement second = sourceConn.get(c)[1];
            Object searched = getMappedToProperty(first, second, source);
            if (searched == null)
                break;
            ConnectableElement key = searched == second ? first : second;
            for (PropertyStack item : sourceProps.keySet()) {
                ConnectableEntity src = sourceProps.get(item);
                if (src != null && item.lowermostProperty().getConnectableObject() == searched && sources.get(src) != null) {
                    sources.get(src).sourcePropertyMap.put(getConnectableEntity(key), item);
                }
            }
        }

        Map<Connector, ConnectableElement[]> targetConn = getTargetConnectorElements();
        for (Connector c : targetConn.keySet()) {
            for (ConnectableElement end : targetConn.get(c)) {
                ConnectableEntity parent = null;
                for (PropertyStack key : targetProps.keySet()) {
                    if (key.containsRef(end)) {
                        parent = targetProps.get(key);
                        break;
                    }
                }
                if (parent != null && !targets.containsKey(parent)) {
                    ElementMapping struct = mappingFactory.createElementMapping(this);
                    struct.targetList.add(parent);
                    targets.put(parent, struct);
                }
            }
        }
        for (Connector c : targetConn.keySet()) {
            ConnectableElement first = targetConn.get(c)[0];
            ConnectableElement second = targetConn.get(c)[1];
            Object searched = getMappedToProperty(first, second, target);
            if (searched == null)
                break;
            ConnectableElement key = searched == second ? first : second;
            for (PropertyStack item : targetProps.keySet()) {
                ConnectableEntity tgt = targetProps.get(item);
                if (tgt != null && item.lowermostProperty().getConnectableObject() == searched && targets.get(tgt) != null) {
                    targets.get(tgt).targetPropertyMap.put(getConnectableEntity(key), item);
                }
            }
        }
        // Read opposite property mappings for both source and target mapping structures
        for (Connector conn : patternConn.keySet()) {
            ConnectableElement first = patternConn.get(conn)[0];
            ConnectableElement second = patternConn.get(conn)[1];
            ConnectableElement sourceEl = sourceElements.contains(first) ? first : second;
            ConnectableElement targetEl = sourceEl == first ? second : first;
            for (PropertyStack item : sourceProps.keySet()) {
                ConnectableEntity e = sourceProps.get(item);
                if (e == null)
                    continue;
                ConnectableEntity tEntity = getConnectableEntity(targetEl);
                ElementMapping smap = sources.get(e);
                for (PropertyStack titem : targetProps.keySet()) {
                    if (smap != null && titem.size() > 1 && smap.targetList.contains(tEntity) && titem.metaElement() == tEntity)
                        smap.targetPropertyMap.put(tEntity, titem);
                }
            }
            for (PropertyStack item : targetProps.keySet()) {
                ConnectableEntity e = targetProps.get(item);
                if (e == null)
                    continue;
                ConnectableEntity sourceEntity = getConnectableEntity(sourceEl);
                ElementMapping tmap = targets.get(e);
                for (PropertyStack sitem : sourceProps.keySet()) {
                    if (tmap != null && sitem.size() > 1 && tmap.source != null
                            && tmap.source.equals(sourceEntity) && sitem.metaElement() == sourceEntity)
                        tmap.sourcePropertyMap.put(sourceEntity, sitem);
                }
            }
        }

        // Identify mappings between properties
        for (Connector conn : patternConn.keySet()) {
            ConnectableElement first = patternConn.get(conn)[0];
            ConnectableElement second = patternConn.get(conn)[1];
            ConnectableElement sourceEl = sourceElements.contains(first) ? first : second;
            ConnectableElement targetEl = sourceEl == first ? second : first;
            ConnectableEntity sourceEntity = getConnectableEntity(sourceEl);
            ConnectableEntity targetEntity = getConnectableEntity(targetEl);
            for (PropertyStack item : sourceProps.keySet()) {
                ConnectableEntity e = sourceProps.get(item);
                if (e == null)
                    continue;
                ElementMapping map = sources.get(e);
                // if item.size() == 1, then only ConnectableElement without any properties is defined in the mapping
                if (map != null && map.sourcePropertyMap != null && map.propertyMap != null && item != null
                        && item.size() > 1 && !map.sourcePropertyMap.containsValue(item)
                        && !map.propertyMap.keySet().contains(item) && item.lowermostProperty() == sourceEntity) {
                    for (PropertyStack item2 : targetProps.keySet()) {
                        ElementMapping map2 = targets.get(targetProps.get(item2));
                        if (item2.size() > 1 && !map2.targetPropertyMap.containsValue(item2)
                                && !map2.propertyMap.keySet().contains(item2) && item2.lowermostProperty() == targetEntity) {
                            map.addPropertyMapping(item, item2, getRuleText(conn), null);
                        }
                    }
                }
            }
        }

        // Process situation when source can be mapped to both target element and its property
        for (ConnectableEntity map : targets.keySet()) {
            List<ConnectableEntity> targetList = targets.get(map).targetList;
            for (ConnectableEntity connTarget : targetList) {
                for (PropertyStack stack : targetProps.keySet()) {
                    if (stack.metaElement() == connTarget) {
                        for (ConnectableEntity targetIt : targetList) {
                            if (stack.contains(targetIt) && targetIt != stack.metaElement()) {
                                targets.get(connTarget).targetPropertyMap.put(targetIt, stack);
                                ElementMapping mapping = mappingFactory.createElementMapping(this);
                                mapping.source = targets.get(connTarget).source;
                                targets.put(targetIt, mapping);
                            }
                        }
                    }
                }
            }
        }
        // Also consider the situation if source mapping classifier uses particular property for mapping
        // In this case, targets may have mappings which do not correspond to sources
        @SuppressWarnings("unchecked")
        Set<ConnectableEntity> sourceKeys = new HashSet(sources.keySet());
        for (ConnectableEntity tgt : targets.keySet()) {
            ConnectableEntity src = targets.get(tgt).source;
            if (src != null && !sourceKeys.contains(src)) {
                sources.put(src, targets.get(tgt));
                PropertyStack parent = null;
                for (PropertyStack stack : sourceProps.keySet()) {
                    if (stack.lowermostProperty().equals(src)) {
                        parent = stack;
                        break;
                    }
                }
                if (parent != null) {
                    sources.get(parent.metaElement()).sourcePropertyMap.put(src, parent);
                }
            }
        }

        // If source mapping classifier does not use any property for mapping, "name" property is considered
        ConnectableEntity targetEntity = getTargetConnectingClassifier();
        ConnectableEntity sourceEntity = getSourceConnectingClassifier();
        if (targetEntity != null && targets.get(targetEntity) != null) {
            boolean isTargetHanging = targets.get(targetEntity).source == null;
            sourceKeys = new HashSet(sources.keySet());
            Iterator<ConnectableEntity> sourceIter = sourceKeys.iterator();
            if (sourceEntity != null) {
                while (sourceIter.hasNext()) {
                    ConnectableEntity map = sourceIter.next();
                    if (map != null && map.equals(sourceEntity)) {
                        List<ConnectableEntity> targetList = sources.get(map).targetList;
                        Iterator<ConnectableEntity> iter = targetList.iterator();
                        while (iter.hasNext()) {
                            ConnectableEntity tgt = iter.next();
                            if (!tgt.equals(targetEntity) && !isTargetHanging && targets.get(tgt) != null) {
                                PropertyStack stack = new PropertyStack();
                                ConnectableEntity strEl = createStringElement(null);
                                stack.add(strEl);
                                stack.add(createStringElement("name"));
                                sources.get(map).sourcePropertyMap.put(strEl, stack);
                                ElementMapping mapping = mappingFactory.createElementMapping(this);
                                mapping.source = strEl;
                                mapping.targetList.add(tgt);
                                sources.put(strEl, mapping);
                                targets.get(tgt).source = strEl;
                                iter.remove();
                            }
                        }
                    }
                }
            }
        }

        // Process pattern, which does not have any direct source-target mappings ("property assignment to target element" pattern)
        if (sources.isEmpty() && targets.isEmpty()) {
            // Identify, if all of the sourceProps and targetProps structures represent the same element mapping with its properties, respectively
            // Create mapping structures
            PropertyStack sstack = sourceProps.keySet().toArray(new PropertyStack[]{})[0];
            PropertyStack tstack = targetProps.keySet().toArray(new PropertyStack[]{})[0];
            // Ensure that only element mappings on the lowest level of properties will be used for mappings
            Map<PropertyStack, ConnectableEntity> sProps = getWithoutSubmaps(sourceProps);
            Map<PropertyStack, ConnectableEntity> tProps = getWithoutSubmaps(targetProps);
            ConnectableEntity src = sstack.metaElement();
            ElementMapping struct = mappingFactory.createElementMapping(this);
            struct.source = src;
            sources.put(src, struct);
            for (PropertyStack pstack : sProps.keySet()) {
                if (pstack.size() > 1) {
                    ElementMapping pstruct = mappingFactory.createElementMapping(this);
                    ConnectableEntity sEl = pstack.lowermostProperty();
                    pstruct.source = sEl;
                    sources.put(sEl, pstruct);
                    struct.sourcePropertyMap.put(sEl, pstack);
                }
            }
            ConnectableEntity tgt = tstack.metaElement();
            struct = mappingFactory.createElementMapping(this);
            struct.targetList.add(tgt);
            targets.put(tgt, struct);
            for (PropertyStack pstack : tProps.keySet()) {
                if (pstack.size() > 1) {
                    ElementMapping pstruct = mappingFactory.createElementMapping(this);
                    ConnectableEntity tEl = pstack.lowermostProperty();
                    pstruct.targetList.add(tEl);
                    targets.put(tEl, pstruct);
                    struct.targetPropertyMap.put(tEl, pstack);
                }
            }
            for (Connector conn : patternConn.keySet()) {
                ConnectableEntity first = getConnectableEntity(patternConn.get(conn)[0]);
                ConnectableEntity second = getConnectableEntity(patternConn.get(conn)[1]);
                if (first != null && second != null) {
                    if (sources.keySet().contains(first)) {
                        sources.get(first).targetList.add(second);
                        targets.get(second).source = first;
                    } else if (targets.keySet().contains(first)) {
                        sources.get(second).targetList.add(first);
                        targets.get(first).source = second;
                    }
                }
            }
        }

        // Read join (CONCAT) mappings
        Map<Object, Object[]> connList = getOwnedConnectorViews();
        Collection<Object> joinPres = getElementsByStereotype(getPatternConfiguration().getJoinStereotypeName());
        for (Object join : joinPres) {
            Map<Object, PropertyStack> incoming = new HashMap<>();
            Object outgoing = null;
            PropertyStack targetItem = null;
            for (Object conn : connList.keySet()) {
                Object first = connList.get(conn)[0];
                Object second = connList.get(conn)[1];
                Object sourceEl = null;
                Object targetEl = null;
                if (first.equals(join) && getRootPart(second) == source) {
                    sourceEl = second;
                } else if (second.equals(join) && getRootPart(first) == source) {
                    sourceEl = first;
                }
                if (first.equals(join) && getRootPart(second) == target) {
                    targetEl = second;
                } else if (second.equals(join) && getRootPart(first) == target) {
                    targetEl = first;
                }
                if (sourceEl != null && targetEl == null && (second.equals(join) || first.equals(join))) {
                    // We have incoming connector to join
                    for (PropertyStack item : sourceProps.keySet()) {
                        if (representsElement(item.lowermostProperty(), sourceEl)
                                && isOwningElement(item.metaElement().getConnectableObject(), sourceEl, 
                                        getPatternConfiguration().getSourceStereotypeName())) {
                            incoming.put(conn, item);
                            break;
                        }
                    }
                } else if (targetEl != null && sourceEl == null && (second.equals(join) || first.equals(join))) {
                    // We have outgoing connector to join
                    for (PropertyStack item : targetProps.keySet()) {
                        if (representsElement(item.lowermostProperty(), targetEl)
                                && isOwningElement(item.metaElement().getConnectableObject(), targetEl, 
                                        getPatternConfiguration().getTargetStereotypeName())) {
                            outgoing = conn;
                            targetItem = item;
                            break;
                        }
                    }
                }
            }

            if (outgoing != null && targetItem != null) {
                ConnectableEntity targetKey = targetItem.metaElement();
                ElementMapping mapping = targets.get(targetKey);
                if (mapping == null) {
                    mapping = mappingFactory.createElementMapping(this);
                    mapping.targetList.add(targetKey);
                    targets.put(targetKey, mapping);
                }
                for (Object key: incoming.keySet())
                    mapping.concatMap.addIncomingEntry(getConnectorEntity(outgoing), targetItem, getConnectorEntity(key), incoming.get(key)); 
            }
        }

        // Resolve mappings between properties, defined using abstract target classifiers, connected with their specializations
        for (ConnectableEntity key : sources.keySet()) {
            ElementMapping map = sources.get(key);
            for (PropertyStack pmkey : map.propertyMap.keySet()) {
                for (ConnectableEntity tmkey : map.targetPropertyMap.keySet()) {
                    Map<PropertyStack, ElementMapping.PropertyMapping> propMaps = map.propertyMap.get(pmkey);
                    for (PropertyStack pmval : propMaps.keySet()) {
                        PropertyStack tmval = map.targetPropertyMap.get(tmkey);
                        /* Find corresponding properties, but exclude the case, when there is a direct connection 
                            between coresponding properties in source and target elements (the properties can be copied directly then) */
                        if (tmkey.equals(pmval.metaElement()) && !pmkey.lowermostProperty().equals(tmval.lowermostProperty())) { 
                            ElementMapping.PropertyMapping tmap = propMaps.get(pmval);
                            // Change name of the specification to the name of the property to generate and update property stack
                            PropertyStack newmap = new PropertyStack();
                            newmap.addAll(tmap.getTargetStack());
                            newmap.metaElement().setName(tmval.lowermostProperty().getName());
                            newmap.addAll(0, tmval.subList(0, tmval.size() - 1));
                            tmap.setTargetStack(newmap);
                            propMaps.put(pmval, tmap);
                        }
                    }
                }
            }
        }

        // Identify mappings between elements which have CONCAT mapping but are not directly mapped to each other
        for (ConnectableEntity key : targets.keySet()) {
            ConcatMap concatMap = targets.get(key).concatMap;
            for (ConnectorEntity concat : concatMap.keySet()) {
                boolean hasMapping = false;
                ConnectableEntity concatTarget = concatMap.getTargetPropertyStack(concat).metaElement();
                for (ConnectableEntity sourceKey : sources.keySet()) {
                    if (sources.get(sourceKey).targetList.contains(concatTarget)) {
                        hasMapping = true;
                        break;
                    }
                }
                if (!hasMapping) {
                    ConnectableEntity targetToAdd = concatTarget;
                    List<ConnectableEntity> targetList = null;
                    for (ConnectorEntity incoming : concatMap.getIncomingConnectors(concat)) {
                        PropertyStack stack = concatMap.getSourcePropertyStack(concat, incoming);
                        if (stack.metaElement().equals(getSourceConnectingClassifier())) {
                            targetList = sources.get(getSourceConnectingClassifier()).targetList;
                        }
                    }
                    if (targetList != null && targetList.isEmpty()) {
                        targetList.add(targetToAdd);
                    } else {
                        for (ConnectorEntity incoming : concatMap.getIncomingConnectors(concat)) {
                            targetList = sources.get(concatMap.getSourcePropertyStack(concat, incoming).metaElement()).targetList;
                            if (targetList.isEmpty()) {
                                targetList.add(targetToAdd);
                                break;
                            }
                        }
                    }
                }
            }
        }

    }

    /** @return String representation of this object */
    @Override
    public String toString() {
        JSONObject obj = new JSONObject();
        obj.append("pattern classifier", getPatternName());
        JSONObject obj2 = new JSONObject();
        for (ConnectableEntity key : sources.keySet()) {
            String name = key.getPrintableName();
            boolean contains = true;
            while (contains) 
                try {
                    Object val = obj2.get(name);
                    name += " ";
                } catch (JSONException ex) {
                    contains = false;
                }
            obj2.put(name, new JSONObject(sources.get(key).toString()));
        } 
        obj.put("sources", obj2);
        obj2 = new JSONObject();
        for (ConnectableEntity key : targets.keySet()) {
            String name = key.getPrintableName();
            boolean contains = true;
            while (contains) 
                try {
                    Object val = obj2.get(name);
                    name += " ";
                } catch (JSONException ex) {
                    contains = false;
                }
            obj2.put(name, new JSONObject(targets.get(key).toString()));
        }
        obj.put("targets", obj2);
        return obj.toString(4).replaceAll("\"", "");
    }

    /**
     * @return {@code true} if CONCAT type of mappings are present in internal
     * data structures, {@code false} otherwise
     */
    public boolean hasConcatMappings() {
        for (ConnectableEntity key : targets.keySet())
            if (!targets.get(key).concatMap.isEmpty())
                return true;
        return false;
    }
    
    /**
     * Validates transformation pattern and returns {@code true} if it is valid and can be processed further
     * @return	{@code true} if transformation pattern is valid; {@code false} otherwise
     * @throws InvalidPatternException	If the pattern is invalid or cannot be processed
     */
    protected boolean validatePattern() throws InvalidPatternException {
        if (root == null)
            throw new InvalidPatternException(String.format(bundle.getString("PatternParser.1"), getPatternName()));
        else if (root[0] == null)
            throw new InvalidPatternException(String.format(bundle.getString("PatternParser.2"), getPatternName()));
        else if (root[1] == null)
            throw new InvalidPatternException(String.format(bundle.getString("PatternParser.3"), getPatternName()));
        else if (!allValidConnections(root[0]))
            throw new InvalidPatternException(String.format(bundle.getString("PatternParser.4"), getPatternName())
                    + bundle.getString("PatternParser.5"));
        else if (!allValidConnections(root[1]))
            throw new InvalidPatternException(String.format(bundle.getString("PatternParser.6"), getPatternName())
                    + bundle.getString("PatternParser.5"));
        return true;
    }
    
    /**
     * Return Connector objects in the transformation pattern, each of which connects a Connectable Element in the source part 
     * to a Connectable Element in the target part, together with their ends or roles, as defined in UML specification. 
     * These roles are UML Connectable Elements, as represented in the Structured Classifier, representing transformation pattern  
     * @return {@link Map} of Connector objects and arrays of their end elements as values in this map(there should be exactly 2 elements in each array)
     */
    abstract protected Map<Connector, ConnectableElement[]> getPatternConnectorElements();
    
    /**
     * Return Connector objects in the transformation pattern, each of which connects two 
     * UML Connectable Elements in the source part, together with their ends or roles, as defined in UML specification. 
     * These roles are UML Connectable Elements, as represented in the Structured Classifier, representing transformation pattern  
     * @return {@link Map} of Connector objects and arrays of their end elements as values in this map(there should be exactly 2 elements in each array)
     */
    abstract protected Map<Connector, ConnectableElement[]> getSourceConnectorElements();
    
    /**
     * Return Connector objects in the transformation pattern, each of which connects two 
     * UML Connectable Elements in the target part, together with their ends or roles, as defined in UML specification. 
     * These roles are UML Connectable Elements, as represented in the Structured Classifier, representing transformation pattern  
     * @return {@link Map} of Connector objects and arrays of their end elements as values in this map(there should be exactly 2 elements in each array)
     */
    abstract protected Map<Connector, ConnectableElement[]> getTargetConnectorElements();
    
    /**
     * Return presentations of all Connector elements in the transformation pattern, together with presentation elements that they connect
     * @return {@link Map} of Connector presentation objects and arrays of their end elements as values in this map 
     * (again, there should be exactly 2 elements in each array)
     */
    abstract protected Map<Object, Object[]> getOwnedConnectorViews();
    
    /**
     * Checks if the given Connectable Element represents a CONCAT Join element (i.e., it is stereotyped with a Stereotype, 
     * whose name is the value of {@link PatternConfiguration#getJoinStereotypeName()})
     * @param connObj   The Connectable Element which is queried
     * @return  {@code true} if {@code connObj} has {@literal Join} stereotype applied; {@code false} otherwise
     */
    abstract protected boolean hasJoinStereotype(ConnectableElement connObj);
    
    /**
     * Checks if the given Connectable Element represents a given Object
     * @param entity   The Connectable Element which is queried  
     * @param el       The Object which is used for comparison
     * @return  {@code true} if {@code entity} represents {@code el}; {@code false} otherwise
     */
    abstract protected boolean representsElement(ConnectableEntity entity, Object el);

    /** 
     * Get source and target parts from classifier, representing transformation pattern 
     * @return An array of 2 UML Elements, where first element is an UML Element representing source part of the transformation pattern,
     * and second element is an UML Element representing the target part of the transformation pattern
     */
    abstract protected Element[] getPartElements();
    
    /**
     * Read transformation part main elements
     * @return An array of 2 UML Elements, where first element is an UML Element representing source part of the transformation pattern,
     * and second element is an UML Element representing the target part of the transformation pattern
     */
    protected Element[] readMainElements() {
        root = getPartElements();
        return root;
    } 
    
    /**
     * Read integration type property for particular UML Connector object
     * @param connObj   The Connector object
     * @return  {@link IntegrationType} value representing integration type between the elements at the ends of the Connector
     */
    abstract protected IntegrationType getIntegrationType(Connector connObj);
    
    /**
     * Create {@link ConnectorEntity} object from the given object. The object must be or contain a valid UML Connector object
     * @param connObj   Object which is processed
     * @return  A new instance of {@link ConnectorEntity}, or {@code null} if such instance could not be created
     */
    abstract protected ConnectorEntity<Connector> getConnectorEntity(Object connObj);
    
    /**
     * Performs a check for existing instances of {@link ConnectableEntity}, which correspond to the given UML ConnectableElement object.
     * If such entity is found, it is returned; otherwise, a new instance of {@link ConnectableEntity} is created
     * @param connObj   UML ConnectableElement object
     * @return          An instance of {@link ConnectableEntity}, which represents {@code connObj}
     */
    protected final ConnectableEntity getConnectableEntity(ConnectableElement connObj) {
        if (connObj == null)
            return null;
        if (entityMap.containsKey(connObj))
            return entityMap.get(connObj);
        else {
            ConnectableEntity newEntity = createConnectableEntity(connObj);
            entityMap.put(connObj, newEntity);
            return newEntity;
        }
    }

    /** Identify which of the two properties corresponds to the target Object
     * @param first     Object representing first UML Property
     * @param second    Object representing second UML Property
     * @param target    The target Object    
     */
    abstract protected Object getMappedToProperty(Object first, Object second, Object target);

    /** Filter out mappings which have lowest level properties at the top of the property stacks
     * @param sourceProps  The {@link Map} which maps existing properties, represented by {@link PropertyStack} entities,
     * to the {@link ConnectableEntity} entities, representing UML elements, containing these properties at some level  
     * @return  The {@link Map} with the filtered elements from {@code sourceProps}
     */
    protected Map<PropertyStack, ConnectableEntity> getWithoutSubmaps(Map<PropertyStack, ConnectableEntity> sourceProps) {
        Map<PropertyStack, ConnectableEntity> result = new HashMap<>();
        List<PropertyStack> keys = new ArrayList<>(sourceProps.keySet());
        Collections.sort(keys, new Comparator<PropertyStack>() {
            @Override
            public int compare(PropertyStack a1, PropertyStack a2) {
                return a2.size() - a1.size();
            }
        });
        for (PropertyStack item : keys) {
            boolean found = false;
            for (PropertyStack item2 : result.keySet())
                if (Collections.indexOfSubList(item2.asUnmodifiableList(), item.asUnmodifiableList()) == 0) {
                    found = true;
                    break;
                }
            if (!found)
                result.put(item, sourceProps.get(item));
        }
        return result;
    }

    /**
     * Creates {@link ConnectableEntity} representing UML {@link String} type
     * @param name	The name of the element to be given
     * @return	Created {@link ConnectableEntity}
     */
    abstract protected ConnectableEntity createStringElement(String name);
    
    /**
     * Get text representing UML constraint for particular UML connector element. If several constraints 
     * are defined for this element, the returned value corresponds to the first Constraint 
     * which is not empty (i.e., has non-empty textual representation)
     * @param connObj      Connector element which has the constraint 
     * @return             {@link String} representing the constraint which has been found
     */
    abstract protected String getRuleText(Connector connObj);
    
    /**
     * Return the objects, representing UML elements, from the source part of transformation pattern
     * @return  {@link Collection} of objects from the source part
     * @throws InvalidPatternException The pattern is invalid or cannot be processed
     */
    abstract protected Collection<Object> getSourceElements() throws InvalidPatternException;
    
    /**
     * Return the objects, representing UML elements, from the target part of transformation pattern
     * @return  {@link Collection} of objects from the target part
     * @throws InvalidPatternException  The pattern is invalid or cannot be processed
     */
    abstract protected Collection<Object> getTargetElements() throws InvalidPatternException;
    
    /**
     * Returns the map, which connects property path structures to {@link ConnectableEntity}, 
     * representing the UML metaelement, from the {@literal source} part of partial M2M transformation pattern
     * @return  {@link Map} where keys are {@link PropertyStack} objects and values are {@link ConnectableEntity} 
     * entities, representing UML elements, that these properties belong to
     * @throws InvalidPatternException  The pattern is invalid or cannot be processed
     */
    abstract protected Map<PropertyStack, ConnectableEntity> getSourcePropertyMappings() throws InvalidPatternException;

    /**
     * Returns the map, which connects property path structures to {@link ConnectableEntity}, 
     * representing the UML metaelement, from the {@literal target} part of partial M2M transformation pattern
     * @return  {@link Map} where keys are {@link PropertyStack} objects and values are {@link ConnectableEntity} 
     * entities, representing UML elements, that these properties belong to
     * @throws InvalidPatternException  The pattern is invalid or cannot be processed
     */
    abstract protected Map<PropertyStack, ConnectableEntity> getTargetPropertyMappings() throws InvalidPatternException;
    
    /**
     * Read UML Element, representing root element in the transformation pattern, from the given Object. 
     * The Object must represent UML element or its representation
     * @param el    The {@link Object} representing UML element 
     * @return  UML Element that has been obtained from this Object
     */
    abstract protected Element getRootPart(Object el);
    
    /**
     * Checks if an transformation pattern part contains only valid internal connections, 
     * e.g., if there are only connections which connect an element with a property of another element 
     * (the connections among two elements, which are not properties of other element, are not possible and reported as invalid) 
     * @param part	The pattern part element (either source part, obtained using
     * {@link #getSourceElement()}, or target part, obtained using {@link #getTargetElement()})
     * @return	{@code true} if {@code element} has only valid connections; {@code false} otherwise
     */
    abstract protected boolean allValidConnections(Object part);
    
    /**
     * Return the name of pattern element
     * @return {@link String} representing name of transformation pattern element
     */
    abstract protected String getPatternName();
    
    /**
     * Finds a list of elements (Elements, PresentationElements, etc.) by the name of the stereotype, applied to the element, and root element
     * @param stName	The name of the stereotype, applied to the element
     * @return	A {@link Collection} of found elements 
     */
    abstract protected Collection<Object> getElementsByStereotype(String stName);
    
    /** Checks if the given element is the owner of another given PresentationElement
     * @param parent    The element which is checked as the owner of {@code child}
     * @param child     The element which is checked as the owned element of {@code owner}
     * @param partName  The name of the transformation pattern part (as specified by 
     * {@link PatternConfiguration#getSourceStereotypeName()} or {@link PatternConfiguration#getTargetStereotypeName()})
     * @return          {@code true} if {@code owner} can be identified as a parent element for {@code child}; {@code false} otherwise
     */
    abstract protected boolean isOwningElement(Object parent, Object child, String partName);
    
    /**
     * Get the Element representing the source part
     * @return	The source part Element
     */
    public Element getSourceElement() {
        return root == null ? null : root[0];
    }

    /**
     * Get the Element representing the source part
     * @return	The target part element
     */
    public Element getTargetElement() {
        return root == null ? null : root[1];
    }
    
    /**
     * Return the pattern element
     * @return The element, representing the transformation pattern
     */
    public Element getPatternRoot() {
        return rootPattern;
    }
    
    /**
     * Indicates if source part in transformation pattern has a mapping element, 
     * marked as dragged element (i.e. that has UML Stereotype named {@link PatternConfiguration#getElementInFocusName()}) 
     * @return {@code true} if this pattern has such Element; {@code false} otherwise
     */
    public boolean hasDraggedElementMark() {
        return hasDraggedMark;
    }
    
    /**
     * Find source connecting element
     * @return	The identified source connecting element
     */
    public ConnectableEntity getSourceConnectingClassifier() {
        ConnectableEntity connEl = null;
        if (sources.keySet().isEmpty())
            return connEl;
        int maxno = 0;
        for (ConnectableEntity key : sources.keySet()) {
            int val = sources.get(key).sourcePropertyMap.size();
            if (val == 0 && connEl == null)
                connEl = key;
            if (sources.get(key).targetList != null)
                val++;
            if (val > maxno) {
                maxno = val;
                connEl = key;
            }
        }
        return connEl;
    }

    /**
     * Find target connecting element
     * @return	The identified target connecting element
     */
    public ConnectableEntity getTargetConnectingClassifier() {
        ConnectableEntity connEl = null;
        if (targets.keySet().isEmpty())
            return connEl;
        if (targets.size() == 1)
            return targets.keySet().toArray(new ConnectableEntity[]{})[0];
        int maxno = 0;
        int numConnected = 0;
        for (ConnectableEntity key : targets.keySet()) {
            ElementMapping mapping = targets.get(key);
            int val = mapping.targetPropertyMap.size();
            if (mapping.source != null) {
                val++;
                numConnected++;
            }
            if (val == 0 && connEl == null)
                connEl = key;
            if (val > maxno) {
                maxno = val;
                connEl = key;
            }
        }
        // If we have several elements which have only connections with sources then no connecting classifier is present
        if (maxno < 2 && numConnected > 1)
            connEl = null;
        return connEl;
    }
    
        /**
     * Check if given transformation pattern element has an additional property mapping
     * @param targetMap	The transformation pattern element which is checked
     * @return	{@code true} if this condition is satisfied; {@code false} otherwise
     */
    public boolean hasAdditionalPropertyMapping(ConnectableEntity targetMap) {
        for (ConnectableEntity key : targets.keySet())
            if (targets.get(key).targetList.contains(targetMap)) {
                ConnectableEntity source = targets.get(key).source;
                for (ConnectableEntity key2 : targets.keySet())
                    if (key != key2 && targets.get(key2).targetList.contains(targetMap) && targets.get(key2).source.equals(source))
                        return true;

            }
        return false;
    }
    
    /**
     * Searches for "hanging" transformation pattern element (target elements, which do not have connections with source elements) 
     * that maps to an Element, which the dragged element was dragged on
     * @param mainEl	The mapping element, which is a mapping of the target element
     * @return	The Element which was found, or {@code null} if no such element has been found
     */
    public ConnectableEntity getHangingRepresentingTarget(ConnectableEntity mainEl) {
        String targetName = targetCl != null ? mapper.getProperName(targetCl) : null;
        if (targetName == null)
            return null;
        for (ConnectableEntity el : targets.keySet()) {
            String elName = null;
            if (el.isStereotype() || (!(el.isStereotype()) && (el.getName() == null || el.getName().trim().length() == 0)))
                elName = el.getName2();
            else if (!el.isStereotype() && el.getName() != null && el.getName().trim().length() > 0)
                elName = el.getProcessedName();
            boolean mainCond = mainEl != null ? targets.get(el).targetPropertyMap.keySet().contains(mainEl) : true;
            if (targets.get(el).source == null && targets.get(el).targetPropertyMap != null && mainCond && elName != null && elName.equals(targetName))
                return el;
        }
        return null;
    }
    
    /**
     * Indicates if source part in transformation pattern has at least one mapping element, identical to the given element
     * @param element   ConnectableElement to be checked
     * @return {@code true} if identical mapping elements were identified; {@code false} otherwise
     */
    public boolean hasIdenticalSourceMappingElement(ConnectableEntity element) {
        for (ConnectableEntity el: sources.keySet())
            if (el.getType().equals(element.getType()))
                return true;
        return false;
    }
    
    /**
     * <p>Find elements which are not mapped to source elements and represent <i>default</i> elements 
     * (elements, which must be set as particular properties of resulting element to ensure that the 
     * resulting element is generated successfully and is valid)</p>
     * <p>UML Region would be an example of such element, as UML Transition cannot directly use the element 
     * that was dragged on as a parent (i.e., State Machine Diagram). According to UML specification, 
     * each UML Transition must belong to an UML Region; thus, UML Region must be selected as its
     * container in order to proceed with generation. Property {@code owner} would be another example, 
     * as generation of some elements also requires that their {@code owner} property must be set</p>
     * <p>Currently, first identified element of particular type in {@code targetContainer} is returned as such element</p>
     * @param draggedName	The name of the dragged Element
     * @param targetContainer	The target container-type of element where the
     * search is performed for required elements
     * @param finder            {@link ElementSearch} object for search of UML elements
     * @return	A {@link Map} where keys define the class of the element whose properties must be set, and values is a 
     * {@link Map}, representing properties which must be set, together with candidate elements for setting
     */
    public Map<Class<?>, Map<String, Element>> getUnmappedElements(String draggedName, 
            Element targetContainer, ElementSearch<Element, ?> finder) {
        if (targetContainer == null)
            return null;
        Map<Class<?>, Map<String, Element>> defPropMap = new HashMap<>();
        if (targets.isEmpty())
            return defPropMap;
        for (ConnectableEntity el : targets.keySet())
            if (targets.get(el).source == null)
                if (targets.get(el).targetPropertyMap == null || targets.get(el).targetPropertyMap.isEmpty())
                    for (ConnectableEntity elc : targets.keySet())
                        if (el != elc && targets.get(elc).targetPropertyMap != null) {
                            Map<ConnectableEntity, PropertyStack> targetMap = targets.get(elc).targetPropertyMap;
                            for (ConnectableEntity elckey : targetMap.keySet())
                                if (el == elckey)
                                    addUnmappedProperty(elc.getBaseClass(), el, 
                                        targetMap.get(elckey).lowermostProperty().getName(),
                                        defPropMap, draggedName, targetContainer, finder);
                        }
        return Collections.unmodifiableMap(defPropMap);
    }
   
    private void addUnmappedProperty(Class<?> elementCl, ConnectableEntity element, String propName,
            Map<Class<?>, Map<String, Element>> defPropMap, String draggedName, Element targetContainer,
            ElementSearch<Element, ?> finder) {
        if (propName == null || element == null)
            return;
        String targetName = targetCl != null ? mapper.getProperName(mapper.getActualName(targetCl)) : null;

        Map<String, Element> eMap = defPropMap.get(elementCl);
        if (eMap == null)
            eMap = new HashMap<>();
        if (targetName != null && elementOver != null && element.getName2().equals(targetName))
            eMap.put(propName, elementOver);
        else {
            Class<?> searchClass = element.getBaseClass();
            Object val = finder.find(targetContainer, searchClass, draggedName);
            if (val == null) {
                Collection<? extends Element> eFound = finder.findChildren(targetContainer, new Class<?>[]{searchClass}, false);
                val = !eFound.isEmpty() ? eFound.toArray()[0] : null;
                // It is possible that the searched-in element is the actual element
                if (val == null && mapper.getClassType(targetContainer).equals(searchClass))
                    val = targetContainer;
                // Finally, try to find the element in the owner of targetContainer
                if (val == null) {
                    eFound = finder.findChildren(mapper.getOwner(targetContainer), new Class<?>[]{searchClass}, false);
                    val = !eFound.isEmpty() ? eFound.toArray()[0] : null;
                }
            }
            eMap.put(propName, (Element) val);
        }
        defPropMap.put(elementCl, eMap);
    }
    
    /**
     * Returns the element in source part of transformation pattern for dragged Element as classifier
     * @param dragged	The Element that was dragged
     * @return	The element that has been found
     */
    public Element getDraggedSourceClassifier(Element dragged) {
        ConnectableEntity entity = getPatternElement(dragged, sources.keySet());
        if (entity.getType() == null || !mapper.isElement(entity.getType()))
            return null;
        return mapper.isClassifier((Element)entity.getType()) ? (Element)entity.getType() : null;
    }

    /**
     * Returns the elements in target part of transformation pattern for dragged Element as a set of classifiers
     * @param dragged	The Element that was dragged
     * @return	A {@link Set} of elements that have been found
     */
    public Set<Element> getDraggedTargetClassifier(Element dragged) {
        Set<Element> classifiers = new HashSet<>();
        ConnectableEntity source = getPatternElement(dragged, sources.keySet());
        if (source == null || sources.get(source) == null)
            return classifiers;
        List<ConnectableEntity> elements = sources.get(source).targetList;
        for (ConnectableEntity el : elements)
            if (el.getType() != null && mapper.isElement(el.getType()) && mapper.isClassifier((Element)el.getType()))
                classifiers.add((Element) el.getType());
        return Collections.unmodifiableSet(classifiers);
    }
    
        /**
     * Finds transformation pattern element for given actual element
     * @param source	The actual element (e.g., that is dragged, etc.)
     * @param sources	The set of transformation pattern elements, where the search is performed
     * @return	The element that has been found, or {@code null} if no such element has been found
     */
    public ConnectableEntity getPatternElement(Element source, Set<ConnectableEntity> sources) {
        ConnectableEntity res = null;
        if (mapper.hasStereotype(source))
            for (Stereotype st : mapper.getStereotypes(source)) {
                if (mapper.isElement(st)) 
                    res = getPatternElementByString(mapper.getElementName2((Element) st), sources);
                if (res != null)
                    return res;
            }
        return getPatternElementByString(mapper.getTypeName(source), sources);
    }

    /**
     * Get particular element in the set of mapping elements by its name 
     * @param elementName   The name of the element which is searched
     * @param sources       The {@link Set} of mapping elements of transformation pattern where the search is performed
     * @return     The {@link ConnectableEntity} which has the {@link ConnectableEntity#name3} property, identical to {@code elementName}
     * if such element has been found; {@code null} otherwise
     */
    protected static ConnectableEntity getPatternElementByString(String elementName, Set<ConnectableEntity> sources) {
        for (ConnectableEntity source : sources)
            if (elementName.compareTo(source.getName3()) == 0)
                return source;
        return null;
    }
    
    /**
     * Creates a new {@link ConnectableEntity} object from UML ConnectableElement
     * @param conn  UML ConnectableElement object
     * @return  Created {@link ConnectableEntity}
     */
    protected ConnectableEntity createConnectableEntity(ConnectableElement conn) {
        if (conn == null || !mapper.isElement(conn))
            return null;
        Element type = mapper.getTypeElement((Element) conn);
        if (type == null)
            return null;
        String actualName = mapper.getActualName(type);
        ConnectableEntity connEntity = new ConnectableEntity(conn, type);
        String name = getMetaconceptName(conn);
        if (mapper.isStereotype(type)) {
            connEntity.setIsStereotype(true);
            connEntity.setRepresentedStereotype(type);
            connEntity.setProfile(mapper.getStereotypeProfile((Stereotype) type));
        } else
            connEntity.setRepresentedStereotype(mapper.getStereotypeByName(name));
        connEntity.setPrintableName(mapper.getPrintableElementName(conn));
        connEntity.setName3(mapper.getElementName3((Element) conn));
        connEntity.setName(actualName);
        connEntity.setName2(mapper.getElementName2(type));
        connEntity.setTypeName(actualName);
        Stereotype draggedSt = mapper.getDraggedElementStereotype();
        connEntity.setDraggedElement(mapper.hasStereotype((Element) conn, draggedSt));
        connEntity.setBaseClass(mapper.getBaseClass((Element)conn));
        connEntity.setBaseClasses(mapper.getBaseClasses(conn));
        connEntity.setProcessedName(name);
        return connEntity;
    }
    /**
     * Function which defines additional processing functionality and update for {@link ElementMapping} during its formation 
     * while parsing pattern. It can be used to read additional properties, necessary for subclasses of {@link ElementMapping}.
     * In the default implementation, it does nothing
     * @param mapping   {@link ElementMapping} which must be updated
     * @param ce        {@link ElementMapping} which is the key for {@code mapping}
     * @param conn      UML Connector object, which is associated with {@code ce}
     */
    protected void performAdditionalUpdate(ElementMapping mapping, ConnectableEntity ce, Connector conn) {}
    
        /**
     * Returns a metaconcept name, corresponding to the ConnectableElement
     * @param element	The ConnectableElement as a type element
     * @return	{@link String} representing the obtained name
     */
    public String getMetaconceptName(ConnectableElement element) {
        if (element == null)
            return null;
        String name = mapper.getProperName(mapper.getActualName((Element) element));
        Element type = mapper.getTypeElement((Element) element);
        String typeName = type != null ? mapper.getActualName(type) : null;
        return name != null ? name.replaceAll("^[_ ]*|[_ ]*$", "") : typeName;
    }
    
    /**
     * Finds transformation pattern element for given actual element
     * @param dragged	The actual element (e.g., that is dragged, etc.)
     * @param sources	The set of transformation pattern elements, where the search is performed
     * @return	The element that has been found, or {@code null} if no such element has been found
     */
    public ConnectableEntity getMappingPatternElement(Element dragged, Set<ConnectableEntity> sources) {
        for (ConnectableEntity source : sources)
            if (mapper.mapsToElement(dragged, source))
                return source;
        return null;
    }
    
    /**
     * Checks if particular particular ConnectableElement in the transformation pattern can be mapped to the dragged element
     * @param dragged   The Element which is dragged
     * @param source    The transformation pattern type element that is checked
     * @return          {@code true} if {@code element} maps to {@code source}; {@code false} otherwise
     */
    public boolean mapsToDraggedElement(Element dragged, ConnectableEntity source) {
        return mapper.mapsToElement(dragged, source) && 
               mapper.hasStereotype((Element) source.getConnectableObject(), mapper.getDraggedElementStereotype());
    }

    /**
     * Returns {@link ElementMappingFactory} used to create new {@link ElementMapping} objects
     * @return An instance of {@link ElementMappingFactory}
     */
    public ElementMappingFactory getMappingFactory() {
        return mappingFactory;
    }

    /**
     * Sets {@link ElementMappingFactory} factory instance used to create new {@link ElementMapping} objects for this parser object
     * @param mappingFactory The {@link ElementMappingFactory} object
     */
    public void setMappingFactory(ElementMappingFactory mappingFactory) {
        this.mappingFactory = mappingFactory;
    }
    
    
    
}
