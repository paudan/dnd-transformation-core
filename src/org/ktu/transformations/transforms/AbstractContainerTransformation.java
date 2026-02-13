package org.ktu.transformations.transforms;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.ktu.transformations.elements.AbstractElementProducer;
import org.ktu.transformations.elements.ConnectedElementMaker;
import org.ktu.transformations.elements.ConnectingElementFinder;
import org.ktu.transformations.elements.ElementGenerationException;
import org.ktu.transformations.elements.SimpleConnectingElementFinder;
import org.ktu.transformations.helpers.AbstractPropertyManager;
import org.ktu.transformations.helpers.ElementSearch;
import org.ktu.transformations.mappers.ElementMapper;
import org.ktu.transformations.notifiers.NotificationObserver;
import org.ktu.transformations.notifiers.NotificationType;
import org.ktu.transformations.parsers.ConcatResolver;
import org.ktu.transformations.parsers.ConnectableEntity;
import org.ktu.transformations.parsers.ElementMapping;
import org.ktu.transformations.parsers.InvalidPatternException;
import org.ktu.transformations.parsers.PatternParser;
import org.ktu.transformations.parsers.PropertyStack;
import org.ktu.transformations.parsers.SpecificationReader;

/**
 * A factory class which performs transformation to container-type tuple of Elements (i.e., child elements are viewed as elements inside of a parent element)
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, Kaunas University of Technology, 2015
 * @param <Element>         Type, corresponding to actual UML Element implementation
 * @param <Stereotype>      Type, corresponding to actual UML Stereotype implementation
 */
public abstract class AbstractContainerTransformation<Element, Stereotype> extends AbstractTransformation<Element, Stereotype> {
    
    private boolean singleGenerated;

    /**
     * A map which contains default properties for each class of elements, represented as a {@link Map},
     * where key is the name of the property and value is the Element that must be set.
     * This map is obligatory for such cases when property MUST be set in order to ensure valid Element (e.g., owner Element).
     * Such map can usually be obtained by calling {@link PatternParser#getUnmappedElements(String, Object, ElementSearch)}
     */
    protected Map<Class<?>, Map<String, Element>> defPropMap;
    /** Element mapping corresponding to connecting element */
    protected ElementMapping mainMap;
    private Map<Object, Map<String, Set<Element>>> drawableItems;
    /** Transformation which produces single elements */
    protected AbstractSingleTransformation<Element, Stereotype> singleTransformer;
    /** A {@link Map} which defines integration information for each generated connecting element  */
    protected Map<Element, IntegrationInfo<Element>> integrations;

    public AbstractContainerTransformation() throws TransformationConfigurationException {
        super();
        final ElementMapper<Element, ?, Stereotype> mapper = this.getElementMapper();
        final AbstractPropertyManager<Element, Stereotype, ?> manager = this.getPropertyManager();
        final AbstractElementProducer<Element, Stereotype> eproducer = this.getElementProducer();
        final ElementSearch<Element, Stereotype> search = this.getElementSearch();
        TransformationFactory<Element, Stereotype> factory = TransformationFactory.getInstance();
        this.singleTransformer = factory.getSingleTransformationInstance(mapper, manager, eproducer, search);
        integrations = new HashMap<>();
    }
    
    @Override
    public Set<Object> createElements(SpecificationReader specReader, PatternParser<?, ?, Element, Stereotype> parser, 
            ConnectableEntity targetCl, Element targetPackage, Element dragged, Object elementOver, Collection<NotificationObserver> observers) 
            throws ElementGenerationException, InvalidPatternException {
        drawableItems = new HashMap<>();
        singleGenerated = false;
        initSourceConnectingElements(parser, dragged);
        // If no connecting elements were found, generate single element representing dragged 
        if (connectingElements.isEmpty()) {
            this.dragged = dragged;
            this.singleGenerated = true;
            Set<Object> elements = singleTransformer.createElements(specReader, parser.getDraggedTargetClassifier(dragged), targetPackage, observers);
            for (Object el: elements)
                drawableItems.put(el, null);
            return drawableItems.keySet();
        }   
        mainMap = targets.get(targetCl);
        initializeStructures(parser, targetCl);
        ElementMapper<Element, ?, Stereotype> mapper = getElementMapper();
        AbstractPropertyManager<Element, Stereotype, ?> manager = getPropertyManager();
        defPropMap = parser.getUnmappedElements(mapper.getElementName2(dragged), targetPackage, getElementSearch());
        Map<String, HashMap<Element, Element>> objectMap = new HashMap<>();
        Map<Element, String> propertyMap = new HashMap<>();
        ConnectedElementMaker<Element, Stereotype> factory = new ConnectedElementMaker<>(this, mainMap);
        factory.addObservers(observers);
        AbstractElementProducer<Element, Stereotype> producer = this.getElementProducer();
        producer.setTargetPackage(targetPackage);

        // Generate necessary elements
        for (Element connectingEl : connectingElements) {
            selectDragged = true;
            for (ConnectableEntity prop : connStruct.sourcePropertyMap.keySet()) {
                List<Object> propelemList = createPropertyList(prop, connectingEl, dragged, parser);
                Map<ConnectableEntity, List<Object>> genProps = new HashMap<>();
                genProps.put(prop, propelemList);
                resolver = new ConcatResolver<>(targets, genProps, connectingEl, connStruct.source, this);
                ElementMapping ms = sources.get(prop);
                if (ms != null) {
                    for (ConnectableEntity targetEl : ms.targetList) {
                        String targetClName = targetEl.getProcessedName();
                        PropertyStack stack = mainMap.targetPropertyMap.get(targetEl);
                        for (Object propelem : propelemList) {
                            Map<Object, AbstractMap.SimpleImmutableEntry<Element, ConnectableEntity>> generated
                                    = producer.generateElementsConcat(resolver, targetEl, ms, propelem, dragged, defPropMap, factory);
                            if (generated != null) {
                                for (Object newel : generated.keySet()) {
                                    AbstractMap.SimpleImmutableEntry<Element, ConnectableEntity> propMapObj = generated.get(newel);
                                    if (objectMap.get(targetClName) == null)
                                        objectMap.put(targetClName, new HashMap<Element, Element>());
                                    objectMap.get(targetClName).put((Element)propMapObj.getKey(), (Element) newel);
                                    String pname = null;
                                    if (propMapObj.getValue() != null)
                                        pname = propMapObj.getValue().getName3();
                                    propertyMap.put(propMapObj.getKey(), pname != null ? pname : stack.lowermostProperty().getName3());
                                }
                            }
                        }
                    }
                }
            }
        }

        HashMap<String, Set<Element>> propMap2 = new HashMap<>();
        for (String val : new HashSet<>(propertyMap.values()))
            propMap2.put(val, new HashSet<Element>());
        for (Element el : propertyMap.keySet())
            propMap2.get(propertyMap.get(el)).add(el);

        // Generate pairs of element sets as Cartesian product, according to multiplicity
        Element tmpel;
        String name = producer.getGeneratedName(connStruct, connectingElements.get(0), dragged, targetCl);
        if (!mapper.isActivityClassifier(connMapClass))
            tmpel = factory.createElement(targetPackage, connMapClass, connMapStereotype, name, defPropMap, specReader.isCheckUnique());
        else
            tmpel = producer.createTargetElement(connMapClass, connMapStereotype, targetPackage, name, defPropMap);
        if (tmpel == null)
            return drawableItems.keySet();
        if (propMap2.isEmpty()) {
            drawableItems.put(tmpel, new HashMap<String, Set<Element>>());
            integrations.put(tmpel, new IntegrationInfo<>(tmpel, dragged, connStruct, targetCl));
        } else if (propMap2.size() == 1) {
            for (String propName : propMap2.keySet())
                manager.setProperty(tmpel, propName, propMap2.get(propName));
            drawableItems.put(tmpel, propMap2);
            integrations.put(tmpel, new IntegrationInfo<>(tmpel, dragged, connStruct, targetCl));
        } else {
            Map<Set<Element>, String> elMap = new HashMap<>();
            for (String prop : propMap2.keySet())
                if (!manager.isFeatureMultiValued(tmpel, prop))
                    for (Element el : propMap2.get(prop)) {
                        Set<Element> hs = new HashSet<>();
                        hs.add(el);
                        elMap.put(hs, prop);
                    }
                else
                    elMap.put(propMap2.get(prop), prop);
            producer.removeElement(tmpel);
            ArrayList<String> propNames = new ArrayList<>(new TreeSet<>(elMap.values()));
            ArrayList<ArrayList<Set<Element>>> finalMap = new ArrayList<>();
            for (String propName : propNames)
                finalMap.add(new ArrayList<Set<Element>>());
            for (Set<Element> key1 : elMap.keySet())
                for (Set<Element> key2 : elMap.keySet()) {
                    int ind1 = propNames.indexOf(elMap.get(key1));
                    int ind2 = propNames.indexOf(elMap.get(key2));
                    if (!key1.equals(key2) && !finalMap.get(ind1).contains(key1)) {
                        finalMap.get(ind1).add(key1);
                        finalMap.get(ind2).add(key2);
                    }
                }

            if (finalMap.isEmpty() || finalMap.get(0).isEmpty())
                throw new ElementGenerationException(String.format(bundle.getString("GenericTransformationFactory.3"),
                    mapper.getHumanName(targetPackage)));

            // Generate properties (currently only binary type of relationship is supported, i.e., using two attributes)
            for (int i = 0; i < finalMap.get(0).size(); i++) {
                Element connectingEl = connectingElements.get(0);
                Element mainel = createConnectingElement(getConnectingElementFinder(targetPackage, connMapName, 
                        connectingEl, propNames, finalMap, i), dragged, targetCl);
                if (mainel == null)
                    throw new ElementGenerationException(String.format(bundle.getString("GenericTransformationFactory.1"),
                            connMapName, mapper.getHumanName(targetPackage)));
                else {
                    String p1name = propNames.get(0);
                    String p2name = propNames.get(1);
                    Set<Element> el = finalMap.get(0).get(i);
                    Set<Element> el2 = finalMap.get(1).get(i);
                    if (manager.isFeatureMultiValued(mainel, p1name)) {
                        // Deep copying creates duplicate elements with different IDs 
                        el = i == 0 ? el : getElementSetCopy(el);
                        manager.setPropertyValueList(mainel, p1name, el);
                    } else
                        manager.setProperty(mainel, p1name, el.toArray()[0]);
                    if (manager.isFeatureMultiValued(mainel, p2name)) {
                        el2 = i == 0 ? el2 : getElementSetCopy(el2);
                        manager.setPropertyValueList(mainel, p2name, el2);
                    } else
                        manager.setProperty(mainel, p2name, el2.toArray()[0]);
                    if (observers != null) {
                        for (NotificationObserver observer: observers)
                            observer.update(new Object [] {mainel}, bundle.getString("TransformationFactory.1") + " "
                                + (mapper.isNamedElement(mainel) ? mapper.getQualifiedName(mainel) : mapper.getHumanName(mainel)), 
                                    NotificationType.INFO);
                    }
                    integrations.put(mainel, new IntegrationInfo<>(mainel, connectingEl, connStruct, targetCl));
                    HashMap<String, Set<Element>> map = new HashMap<>();
                    map.put(p1name, el);
                    map.put(p2name, el2);
                    drawableItems.put(mainel, map);
                }
            }
            finalMap.clear();
        }
        propertyMap.clear();
        propMap2.clear();
        defPropMap = null;
        return drawableItems.keySet();
    }
    
    /**
     * Generate connecting UML element; search for existing elements is also performed, together with uniqueness check
     * @param finder	A {@link ConnectingElementFinder} object for finding connecting elements particularly for this implementation. 
     * The {@code finder} may differ, depending on the type and uniqueness of container elements
     * @param dragged	The element that was dragged
     * @param targetCl	Pattern element, which maps to target
     * @return	Generated Element
     */
    protected Element createConnectingElement(ConnectingElementFinder<Element> finder, Element dragged, ConnectableEntity targetCl) {
        Element connectingEl = finder.getSearchedElement();
        Element targetPackage = finder.getRootElement();
        String elementName = getElementMapper().getElementName(connectingEl);
        Element mainel = finder.find();
        if (mainel == null)
            try {
                return getElementProducer().createTargetElement(connMapClass, connMapStereotype, targetPackage,
                        getElementProducer().getGeneratedName(connStruct, connectingEl, dragged, targetCl), defPropMap);
            } catch (ElementGenerationException e) {
                return null;
            }
        SpecificationReader reader = TransformationManager.getInstance().getCurrentReader();
        if (!reader.isCheckUnique()) {
            int ind = 0;
            boolean found = true;
            String newElName = elementName;
            while (found) {
                ind++;
                newElName = elementName + "_" + ind;
                mainel = finder.find();
                found = mainel != null;
            }
            mainel = finder.find();
            ElementMapper<Element, ?, Stereotype> mapper = this.getElementMapper();
            if (!mapper.hasName(mainel))
                mapper.setName(mainel, newElName);
        }
        return mainel;
    }

    /**
     * Creates a deep copy of a {@link HashSet} of Elements
     * @param eSet	The original set of Elements
     * @return	A deep copy of this set
     */
    private Set<Element> getElementSetCopy(Set<Element> eSet) {
        Set<Element> copy = new HashSet<>();
        for (Element e : eSet)
            copy.add(getElementProducer().createElementCopy(e, getElementMapper().getOwner(e)));
        return copy;
    }
    
    /**
     * <p>Get an instance of ConnectingElementFinder object for container-type of elements</p>
     * <p>Connecting element is defined as an element which aggregates other elements as its properties,
     * and can be viewed as a root of the whole element tree, which is processed in the transformation)</p>
     *
     * @param owner             Root element, where the search is performed
     * @param connMapName       The name of mapping entity {@link ConnectableEntity}, which maps to connecting element
     * @param connectingEl	Connecting element which is being searched
     * @param propNames		The names of properties which, besides element name, may be used to identify the element among others
     * (e.g., {@code represents} property in Activity)
     * @param finalMap		The map of generated properties
     * @param index		Index of the property to be identified
     * @return	The element which has been found, or null, if such Element has not been found
     */
    protected ConnectingElementFinder<Element> getConnectingElementFinder(Element owner, String connMapName, Element connectingEl,
            ArrayList<String> propNames, ArrayList<ArrayList<Set<Element>>> finalMap, int index) {
        return new SimpleConnectingElementFinder<>(owner, connMapName, connectingEl, this);
    }

    /**
     * Returns {@code true}, if single element was created during transformation (mostly using 
     * {@link AbstractSingleTransformation#createElements(SpecificationReader, Set, Object, Collection) })
     * @return {@code true} if {@link AbstractSingleTransformation} transformation was used for element generation; {@code false} otherwise
     */
    public boolean isSingleGenerated() {
        return singleGenerated;
    }

    @Override
    public Map<Object, Map<String, Set<Element>>> getGeneratedElements() {
        return drawableItems;
    }
    
    
    
}
