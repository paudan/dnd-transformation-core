package org.ktu.transformations.transforms;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import org.ktu.transformations.elements.AbstractElementProducer;
import org.ktu.transformations.elements.ElementGenerationException;
import org.ktu.transformations.helpers.AbstractPropertyManager;
import org.ktu.transformations.helpers.ElementSearch;
import org.ktu.transformations.mappers.ElementMapper;
import org.ktu.transformations.notifiers.NotificationObserver;
import org.ktu.transformations.parsers.ConcatResolver;
import org.ktu.transformations.parsers.ConnectableEntity;
import org.ktu.transformations.parsers.ElementMapping;
import org.ktu.transformations.parsers.InvalidPatternException;
import org.ktu.transformations.parsers.PatternParser;
import org.ktu.transformations.parsers.PropertyStack;
import org.ktu.transformations.parsers.SpecificationReader;

/**
 * Abstract class, implementing generic partial UML M2M transformation. Concrete implementations of transformations should extend this class
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, 
 * Kaunas University of Technology, 2015
 * @param <Element>         Type, corresponding to actual UML Element implementation
 * @param <Stereotype>      Type, corresponding to actual UML Stereotype implementation
 */
@SuppressWarnings(value = "unchecked")
public abstract class AbstractTransformation<Element, Stereotype> implements Transformation<Element, Stereotype> {
    protected static ResourceBundle bundle = ResourceBundle.getBundle("org/ktu/transformations/messages_EN");
    /** The list of connecting elements */
    protected List<Element> connectingElements;
    /** Element mappings in the source part of transformation pattern */
    protected Map<ConnectableEntity, ElementMapping> sources;
    /** Element mappings in the target part of transformation pattern */
    protected Map<ConnectableEntity, ElementMapping> targets;
    /** Element mapping representing connecting Element */
    protected ElementMapping connStruct;
    /** The metaclass or stereotype name of Element representing connecting element */
    protected String connMapName = null;
    /** The class of Element representing connecting element */
    protected Class connMapClass = null;
    /** The Stereotype of Element representing connecting element */
    protected Stereotype connMapStereotype = null;
    /** Indication whether selected (dragged) element represents connecting element */
    protected boolean connDragged;
    /** {@link ConcatResolver} instance */
    protected ConcatResolver<Element> resolver;
    /** Flag which defines if {@link ConnectableEntity} representing selected/dragged element has been used already as the mapping element */ 
    protected boolean selectDragged = true;
    /** The UML element which was dragged/selected during the initiation of the transformation */
    protected Element dragged;
    
    /**
     * An internal class which contains information about an Element, that can used to perform search on this Element
     */
    protected static class ElementInfo {

        /** Base class of Element */
        public Class elementClass;
        /** Stereotype of an Element*/
        public String stereotype;
        /** The name of this element*/
        public String name;
        /** The {@link PropertyStack} of this Element, respectively to some parent Element */
        public PropertyStack parentProp;

        /** Create new instance of {@link ElementInfo} */
        public ElementInfo() {
            super();
        }  
    }
    
    /**
     * Class which contains information of a particular integration instance
     * @param <Element>     Type, representing UML element
     */
    protected static class IntegrationInfo<Element> {
        
        private final Element client, supplier;
        private final ElementMapping mapping;
        private final ConnectableEntity target;
        
        /**
         * Create new instance of {@link IntegrationInfo} 
         * @param client    UML element which is the client (target) element in the integration
         * @param supplier  UML element which is the supplier (source) element in the integration
         * @param mapping   Element mapping object, representing the source of the transformation
         * @param target    {@link ConnectableEntity} representing mapping element for {@code target} in the target part 
         */
        public IntegrationInfo(Element client, Element supplier, ElementMapping mapping, ConnectableEntity target) {
            this.client = client;
            this.supplier = supplier;
            this.mapping = mapping;
            this.target = target;
        }

        /**
         * Returns UML element which is the client (target) element in the integration relationship
         * @return  An instance of UML element, representing target in the integration
         */
        public Element getClient() {
            return client;
        }

        /**
         * Returns UML element which is the supplier (source) element in the integration relationship
         * @return An instance of UML element, representing source in the integration
         */
        public Element getSupplier() {
            return supplier;
        }

        /**
         * Element mapping object, used in the transformation
         * @return An instance of {@link ElementMapping}
         */
        public ElementMapping getMapping() {
            return mapping;
        }

        /**
         * Return ConnectableEntity, representing mapping element for {@code target} in the target part
         * @return {@link ConnectableEntity} corresponding to the target mapping element
         */
        public ConnectableEntity getTarget() {
            return target;
        }

    }

    public AbstractTransformation() {
        super();
    }
    
    /**
     * Generate elements according to given transformation specification and the Element that was dragged
     *
     * @param specReader	{@link SpecificationReader} object which encapsulates transformation specification and parameters
     * @param parser		Transformation pattern processor object
     * @param targetCl		{@link ConnectableEntity} which maps to the target Element that will be generated
     * @param targetPackage	Element (such as a Package, Model or another container-type of element) which will contain the generated elements
     * @param dragged		The dragged Element which is used as initial source or as a reference to select additional source elements for transformation
     * @param elementOver       The object (Element, its presentation, diagram, etc.) where the element was dragged on
     * @param observers         {@link NotificationObserver} elements which observe the performed generation procedures
     * @return                  The set of generated UMl elements
     * @throws ElementGenerationException   The element(s) were not generated successfully
     * @throws InvalidPatternException      There was an error while processing the pattern
     */
    abstract protected Set<Object> createElements(SpecificationReader specReader, 
            PatternParser<?, ?, Element, Stereotype>parser, ConnectableEntity targetCl, 
            Element targetPackage, Element dragged, Object elementOver, Collection<NotificationObserver> observers)
            throws ElementGenerationException, InvalidPatternException;
    
    /**
     * Get generated elements, together with their internal relationships, represented as a map
     * @return An instance of {@link Map}
     */
    abstract public Map<Object, ?> getGeneratedElements();

    /**
     * Initialize structures and identify the set of source connecting Element
     * @param parser	 Transformation pattern parser object
     * @param selected	The element which was selected or dragged
     */
     protected void initSourceConnectingElements(PatternParser<?, ?, Element, Stereotype> parser, Element selected) {
        ElementMapper<Element, ?, Stereotype> mapper = getElementMapper();
        sources = parser.getSourceMappings();
        targets = parser.getTargetMappings();
        connectingElements = new ArrayList<>();
        ConnectableEntity connEl = parser.getSourceConnectingClassifier();
        connDragged = mapper.haveIdenticalTypes(selected, (Element) connEl.getType()) || mapper.mapsToElement(selected, connEl);
        if (connDragged)
            connectingElements.add(selected);
        else
            connectingElements.addAll(getCandidateConnectingElements(selected, sources.get(connEl)));
    }

    /**
     * Identifies connecting Element in the transformation pattern and initializes appropriate structures
     * @param parser	Transformation pattern parser object
     * @param mainEl	Element, representing connecting Element in the transformation pattern
     * @throws InvalidPatternException	The pattern is not valid or could not be processed
     */
    protected void initializeStructures(PatternParser<?, ?, Element, Stereotype> parser, ConnectableEntity mainEl) throws InvalidPatternException {
        ConnectableEntity draggedPattern = parser.getPatternElement(connectingElements.get(0), sources.keySet());
        ElementMapper<Element, ?, Stereotype> mapper = getElementMapper();
        if (draggedPattern == null)
            draggedPattern = parser.getMappingPatternElement(connectingElements.get(0), sources.keySet());
        if (draggedPattern == null)
            throw new InvalidPatternException(String.format(bundle.getString("GenericTransformationFactory.2"),
                    mapper.getActualName(parser.getPatternRoot())));
        connStruct = sources.get(draggedPattern);
        if (connStruct == null)
            throw new InvalidPatternException(String.format(bundle.getString("GenericTransformationFactory.2"),
                    mapper.getActualName(parser.getPatternRoot())));
        if (mainEl != null) {
            connMapClass = mainEl.getBaseClass();
            connMapName = mainEl.getName3();
            connMapStereotype = (Stereotype) mainEl.getRepresentedStereotype();
        }
    }


    /**
     * Resolve and add names of element mappings which are connected to particular source mapping
     * @param connectingNames	The {@link Map} which contains information of particular target elements, together with their candidate names.
     * Identified element names are added to this structure
     * @param sourceMapping     The mapping structure of source element, which is connected to these target elements
     * @param targetEl		{@link ConnectableEntity} which maps to the {@code source} element
     * @param source		The Element which is transformed as a source
     * @param dragged		The Element that has been dragged
     */
    protected void addConnectingElementNames(Map<ElementInfo, String> connectingNames, 
            ElementMapping sourceMapping, ConnectableEntity targetEl, Object source, Element dragged) {
        AbstractElementProducer<Element, Stereotype> producerInst = getElementProducer();
        String eName = producerInst.getGeneratedName(sourceMapping, source, dragged, targetEl);
        List<ConnectableEntity> innerConn = targets.get(targetEl).getTargetInnerConnections().get(targetEl);
        if (innerConn != null && !innerConn.isEmpty())
            for (ConnectableEntity eConn : innerConn)
                if (sourceMapping.targetList.contains(eConn)) {
                    ElementInfo infoObj = new ElementInfo();
                    infoObj.elementClass = eConn.getBaseClass();
                    infoObj.stereotype = null;
                    if (eConn.getRepresentedStereotype() != null)
                        infoObj.stereotype = eConn.getName2();
                    infoObj.name = producerInst.getGeneratedName(sourceMapping, source, dragged, eConn);
                    infoObj.parentProp = targets.get(targetEl).targetPropertyMap.get(eConn);
                    connectingNames.put(infoObj, eName);
                }
    }

    @Override
    public abstract ElementMapper<Element, ?, Stereotype> getElementMapper();

    @Override
    public abstract AbstractPropertyManager<Element, Stereotype, ?> getPropertyManager();

    @Override
    public abstract ElementSearch<Element, Stereotype> getElementSearch();
    
    @Override
    public abstract AbstractElementProducer<Element, Stereotype> getElementProducer();
    
    /**
     * Get the list of possible connecting elements according to dragged element and mapping structure
     * @param selected	  The element which was dragged
     * @param mapStruct   The element mapping structure for connecting element
     * @return	The {@link List} of candidate connecting elements
     */
    protected List<Element> getCandidateConnectingElements(Element selected, ElementMapping mapStruct) {
        ElementMapper<Element, ?, Stereotype> mapper = getElementMapper();
        List<Element> connElements = new ArrayList<>();
        String draggedName = mapper.getClassType(selected).getSimpleName();
        PropertyStack propStack = null;
        for (ConnectableEntity e : mapStruct.sourcePropertyMap.keySet()) {
            Stereotype st = mapper.getStereotypeByName(e.getName3());
            if (st != null) {
                List<Class> baseList = mapper.getBaseClassesAsClasses(st);
                for (Class<?> base : baseList)
                    if (base.getSimpleName().compareTo(draggedName) == 0) {
                        propStack = mapStruct.sourcePropertyMap.get(e);
                        break;
                    }
            } else {
                if (e.getName3().compareTo(draggedName) == 0) {
                    propStack = mapStruct.sourcePropertyMap.get(e);
                    break;
                }
            }
        }
        Element model = mapper.getModelByElement(selected);
        Class<?> className = mapper.getClassType(mapStruct.source.getTypeName());
        Collection<? extends Element> elem = getElementSearch().getElementsOfType(model, new Class<?>[]{className}, false);
        AbstractPropertyManager<Element, Stereotype, ?> propManager = getPropertyManager();
        for (Element el : elem) {
            List<Element> currel = new ArrayList<>();
            currel.add(el);
            if (propStack != null) {
                for (int i = 1; i < propStack.size(); i++) {
                    List<Element> newel = new ArrayList<>();
                    for (int k = 0; k < currel.size(); k++) {
                        String featName = propStack.get(i).getName3();
                        Object res = propManager.getFeatureValue(currel.get(k), featName);
                        if (res instanceof AbstractList)
                            newel.addAll((AbstractList<Element>) res);
                        else
                            newel.add((Element) res);
                    }
                    currel = newel;
                }
            }
            for (Element e : currel)
                if (mapper.getID(e).compareTo(mapper.getID(selected)) == 0) {
                    connElements.add(el);
                    break;
                }
        }
        return connElements;
    }
    
    /**
     * Create property list of UML element, according to its mapping and the Element that is dragged
     * @param prop	ConnectableElement which represents the mapping of the property
     * @param element	The UML element which owns property elements
     * @param selected	The UML element which was selected
     * @param parser    {@link PatternParser} object for processing the transformation pattern, which contains {@code prop}
     * @return	The {@link List} of found property objects
     */
    protected final List<Object> createPropertyList(ConnectableEntity prop, Element element, Element selected, PatternParser parser) {
        List<Object> propelemList = new ArrayList<>();
        ElementMapper<Element, ?, Stereotype> mapper = this.getElementMapper();
        boolean mapsToDragged = parser.mapsToDraggedElement(selected, prop);
        boolean hasDraggedMark = parser.hasDraggedElementMark();
        boolean hasIdentical = parser.hasIdenticalSourceMappingElement(prop);
        if ((hasDraggedMark && mapsToDragged) || 
                (!hasDraggedMark && hasIdentical && mapper.mapsToElement(selected, prop) && selectDragged)) {
            propelemList.add(selected);
            selectDragged = false;
            return propelemList;
        }
        List<Object> res;
        Stereotype stereotype = (Stereotype) prop.getRepresentedStereotype();
        // If the Classifier is no a Stereotype and is an abstract Classifier
        if (stereotype == null && mapper.isAbstractClassifier((Element) prop.getType()))
            // Select the instances of actual specializations of this Type
            res = getPropertyManager().getPropertyList(element, connStruct.sourcePropertyMap.get(prop), prop, true);
        else
            // Select only instances of particular Type
            res = getPropertyManager().getPropertyList(element, connStruct.sourcePropertyMap.get(prop), prop.getName3());
        if (res.size() > 1) {
            for (Object obj : res) {
                if (mapper.isElement(obj)) {
                    Element o = (Element) obj;
                    if (!(hasIdentical && o.equals(selected)) || (!hasDraggedMark && o.equals(selected))) {
                        if (!mapper.hasStereotype(o)) {
                            for (Class<?> clname : prop.getBaseClasses())
                                if (mapper.getClassType(o).equals(clname)) {
                                    propelemList.add(o);
                                    break;
                                }
                        } else 
                            propelemList.add(o);
                    }
                } else
                    // If property list was a set of String elements, we return them all
                    propelemList.addAll(res);
            }
        } else
            // We have single element; just add it to the returned list
            propelemList.addAll(res);
        return propelemList;
    }
    
    /**
     * Set name for a {@literal connecting element}, if its mapping includes partial name, according
     * to the information of its properties, which have the same source as this <i>connecting Element</i>
     * @param connEl		Connecting element
     * @param connectingNames	A structure containing the information of the set of properties for
     *                          {@literal mainel}, together with the information of the relevant properties
     * @return	Connecting element with assigned name
     */
    protected Element setMainElementName(Element connEl, Map<ElementInfo, String> connectingNames) {
        for (ElementInfo info : connectingNames.keySet()) {
            ElementMapper<Element, ?, Stereotype> mapper = getElementMapper();
            List<Object> obj;
            if (mapper.isAssociation(connEl)) {
                obj = new ArrayList<>();
                for (Element end : mapper.getAssociationEndTypes(connEl))
                    if (mapper.getClassType(end).equals(info.elementClass))
                        obj.add(end);
            } else
                obj = getPropertyManager().getPropertyList(connEl, info.parentProp, info.elementClass.getSimpleName());
            for (Object elObj : obj) {
                Element e = (Element) elObj;
                Stereotype st = info.stereotype != null ? mapper.getStereotypeByName(info.stereotype) : null;
                boolean stereotypeCond = st != null ? mapper.hasStereotype(connEl, st) : true;
                boolean nameCond = mapper.hasName(e) && info.name != null ? 
                        mapper.getElementName2(e).compareTo(info.name) == 0 : true;
                if (stereotypeCond && nameCond && mapper.isNamedElement(connEl)) {
                    mapper.setName(connEl, connectingNames.get(info));
                    break;
                }
            }
        }
        return connEl;
    }
    
    /**
     * Abstract method which performs actual transformation and generation of target elements
     * @param specReader	{@link SpecificationReader} object, encapsulating transformation specification and parameters
     * @param parser		{@link PatternParser} object with processing logic for transformation pattern, defined in transformation specification
     * @param targetCl		{@link ConnectableEntity} which maps to the target Element that will be generated
     * @param targetPackage	The Element (such as a Package, Model or similar another container-type of element) which will contain the generated elements
     * @param selected		The dragged Element which is used as initial source or as a reference to select additional source elements for transformation
     * @param elementOver       The element that the {@code selected} element is dragged on. It can be set to {@code null}, 
     * if the element was not dragged on any element
     * @param observers         {@link NotificationObserver} elements which observe the performed generation procedures
     * @return	The {@link Set} of generated elements
     * @throws ElementGenerationException	The element(s) were not generated successfully
     * @throws InvalidPatternException		There was an error while processing the pattern
     */
    public Set<Object> create(SpecificationReader specReader, PatternParser<?, ?, Element, Stereotype> parser, ConnectableEntity targetCl, 
            Element targetPackage, Element selected, Object elementOver, Collection<NotificationObserver> observers) 
            throws ElementGenerationException, InvalidPatternException {
        TransformationManager.getInstance().setCurrentReader(specReader);
        return createElements(specReader, parser, targetCl, targetPackage, selected, elementOver, observers);
    }
    
    /**
     * Returns an unmodifiable copy of source part mappings map
     * @return {@link Map} representing {@link ConnectableEntity} objects together with their mappings in the target part
     */
    public Map<ConnectableEntity, ElementMapping> getSourceMappings() {
        return Collections.unmodifiableMap(sources);
    }

    /**
     * Returns an unmodifiable copy of target part mappings map
     * @return {@link Map} representing {@link ConnectableEntity} objects together with their mappings in the source part
     */
    public Map<ConnectableEntity, ElementMapping> getTargetMappings() {
        return Collections.unmodifiableMap(targets);
    }
    
    /**
     * Returns the UML element which was dragged
     * @return the dragged element
     */
    protected Element getSelectedElement() {
        return dragged;
    }

}
