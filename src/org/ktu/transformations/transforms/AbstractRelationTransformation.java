package org.ktu.transformations.transforms;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.ktu.transformations.elements.AbstractElementProducer;
import org.ktu.transformations.elements.ConnectedElementMaker;
import org.ktu.transformations.elements.ElementGenerationException;
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
 * A abstract class which performs transformation to relation-type tuple of elements 
 * (i.e., one of elements in the tuple are relationships between any two of other elements)
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, 
 * Kaunas University of Technology, 2015
 * @param <Element>     Actual implementation type of UML Element
 * @param <Stereotype>  Actual implementation type of UML Stereotype
 */
abstract public class AbstractRelationTransformation<Element, Stereotype> extends AbstractTransformation<Element, Stereotype> {
    
    private Map<Object, List<Element>> drawableItems;
    /** {@link AbstractSingleTransformation} object used to perform transformations for single element generation */
    protected AbstractSingleTransformation<Element, Stereotype> singleTransformer;
    /** {@link AbstractMultipleTransformation} object used to perform transformations for multiple unrelated element generation */
    protected AbstractMultipleTransformation<Element, Stereotype> multipleTransformer;
    /** Map which contains integration information for generated connecting elements */
    protected Map<Element, IntegrationInfo<Element>> integrations;
    
    private boolean singleGenerated, multipleGenerated;
    private Set<Object> singleDrawable;
    private ElementMapping mainMap;
    private boolean emptyProp;
    
    public AbstractRelationTransformation() throws TransformationConfigurationException {
        super();
        final ElementMapper<Element, ?, Stereotype> mapper = this.getElementMapper();
        final AbstractPropertyManager<Element, Stereotype, ?> manager = this.getPropertyManager();
        final AbstractElementProducer<Element, Stereotype> eproducer = this.getElementProducer();
        final ElementSearch<Element, Stereotype> search = this.getElementSearch();
        TransformationFactory<Element, Stereotype> factory = TransformationFactory.getInstance();
        this.singleTransformer = factory.getSingleTransformationInstance(mapper, manager, eproducer, search);
        this.multipleTransformer = factory.getMultipleTransformationInstance(mapper, manager, eproducer, search);
        integrations = new HashMap<>();
    }
    
    @Override
    public Set<Object> createElements(SpecificationReader specReader, PatternParser<?, ?, Element, Stereotype> parser, 
            ConnectableEntity targetCl, Element targetPackage, Element dragged, Object elementOver, 
            Collection<NotificationObserver> observers) throws ElementGenerationException, InvalidPatternException {
        drawableItems = new HashMap<>();
        singleDrawable = new HashSet<>();
        singleGenerated = false;
        multipleGenerated = false;
        initSourceConnectingElements(parser, dragged);
        // If no connecting elements were found, generate single element representing dragged 
        if (connectingElements.isEmpty()) {
            this.dragged = dragged;
            this.singleGenerated = true;
            Set<Object> elements = singleTransformer.createElements(specReader, 
                    parser.getDraggedTargetClassifier(dragged), targetPackage, observers);
            for (Object el: elements)
                drawableItems.put(el, null);
            return drawableItems.keySet();
        }
        // Generate drawable items
        mainMap = targets.get(targetCl);
        initializeStructures(parser, targetCl);
        
        ElementMapper<Element, ?, Stereotype> mapper = getElementMapper();
        AbstractElementProducer<Element, Stereotype> generator = getElementProducer();
        ElementSearch<Element, Stereotype> search = this.getElementSearch();
        AbstractPropertyManager<Element, Stereotype, ?> propManager = this.getPropertyManager();
        Map<Class<?>, Map<String, Element>> defPropMap = parser.getUnmappedElements(mapper.getElementName2(dragged), targetPackage, getElementSearch());
        ConnectedElementMaker<Element, Stereotype> efactory = new ConnectedElementMaker<>(this, mainMap);
        efactory.addObservers(observers);
        generator.setOwnerElement(targetPackage);
        for (Element connectingEl : connectingElements) {
            Map<ConnectableEntity, List<Object>> genProps = new HashMap<>();
            emptyProp = false;
            selectDragged = true;
            for (ConnectableEntity prop : connStruct.sourcePropertyMap.keySet()) {
                List<Object> propelemList = createPropertyList(prop, connectingEl, dragged, parser);
                if (propelemList == null || propelemList.isEmpty())
                    emptyProp = true;
                genProps.put(prop, propelemList);
            }
            if (emptyProp) {
                multipleGenerated = true;
                multipleTransformer.setEmptyPropertyFlag(emptyProp);
                singleDrawable.addAll(multipleTransformer.create(specReader, parser, targetCl, targetPackage, dragged, elementOver, observers));
                break;
            }
            resolver = new ConcatResolver<>(targets, genProps, connectingEl, connStruct.source, this);
            Map<String, HashMap<Element, Element>> objectMap = new HashMap<>();
            Map<Element, String> propertyMap = new HashMap<>();
            // Names for connecting classifiers, which must be generated according to split rules 
            // (e.g., if UseCase is connected to target Association)
            Map<ElementInfo, String> connectingNames = new HashMap<>();
            for (ConnectableEntity prop : genProps.keySet()) {
                ElementMapping ms = sources.get(prop);
                if (ms != null) {
                    for (ConnectableEntity targetEl : ms.targetList) {
                        String targetMapName = targetEl.getProcessedName();
                        PropertyStack stack = mainMap.targetPropertyMap.get(targetEl);
                        List<Object> sourceList = genProps.get(prop);
                        for (Object source : sourceList) {
                            if (targetEl.equals(parser.getTargetConnectingClassifier()))
                                addConnectingElementNames(connectingNames, ms, targetEl, source, dragged);
                            else {
                                Map<Object, SimpleImmutableEntry<Element, ConnectableEntity>> generated
                                        = generator.generateElementsConcat(resolver, targetEl, ms, source, dragged, defPropMap, efactory);
                                if (generated != null) {
                                    for (Object newel : generated.keySet()) {
                                        SimpleImmutableEntry<Element, ConnectableEntity> propMapObj = generated.get(newel);
                                        Element genEl = propMapObj.getKey();
                                        if (objectMap.get(targetMapName) == null)
                                            objectMap.put(targetMapName, new HashMap<Element, Element>());
                                        if (mapper.isElement(newel) && !containsElementWithName(objectMap.get(targetMapName), (Element) newel)) {
                                            objectMap.get(targetMapName).put(genEl, (Element) newel);
                                            String pname = propMapObj.getValue().getName3();
                                            if (stack == null)
                                                pname = null;
                                            else
                                                pname = pname != null ? pname : stack.lowermostProperty().getName3();
                                            propertyMap.put(genEl, pname);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (objectMap.isEmpty())
                throw new ElementGenerationException(String.format(bundle.getString("GenericTransformationFactory.3"), 
                        mapper.getHumanName(targetPackage)));

            // Generate Cartesian of properties for relationships and these relationships 
            if (connMapName != null) {
                // Generate element pairs as Cartesian product
                ArrayList<Element> firstProps = new ArrayList<>(), secondProps = new ArrayList<>();
                ArrayList<Element> firstElems = new ArrayList<>(), secondElems = new ArrayList<>();
                ArrayList<String> clnames = new ArrayList<>(objectMap.keySet());
                HashMap<Element, Element> initElems = objectMap.get(clnames.get(0));
                if (objectMap.size() == 1) { // Relations are between the same type of elements
                    for (Element el : initElems.keySet())
                        for (Element el2 : initElems.keySet())
                            if (el != el2 && !firstProps.contains(el2)) {
                                firstProps.add(el);
                                secondProps.add(el2);
                                firstElems.add(initElems.get(el));
                                secondElems.add(initElems.get(el2));
                            }
                } else {	// Relations are between different type of elements
                    for (int i = 1; i < clnames.size(); i++) {
                        HashMap<Element, Element> ithElems = objectMap.get(clnames.get(i));
                        for (Element el : initElems.keySet())
                            for (Element el2 : ithElems.keySet()) {
                                firstProps.add(el);
                                secondProps.add(el2);
                                firstElems.add(initElems.get(el));
                                secondElems.add(ithElems.get(el2));
                            }
                    }
                }

                for (int i = 0; i < firstProps.size(); i++) {
                    Element el = firstProps.get(i);
                    Element el2 = secondProps.get(i);
                    String p1name = propertyMap.get(firstProps.get(i));
                    String p2name = propertyMap.get(secondProps.get(i));
                    String name = generator.getGeneratedName(connStruct, connectingEl, dragged, targetCl);
                    Stereotype stereotype = (Stereotype) targetCl.getRepresentedStereotype();
                    Element mainel;
                    if (mapper.isRelationship(connMapClass))
                        mainel = search.findRelationship(targetPackage, connMapClass, stereotype, name, p1name, el, p2name, el2, true);
                    else
                        mainel = search.findElement(targetPackage, connMapClass, stereotype, name);
                    if (mainel != null)
                        continue;
                    try {
                        mainel = generator.createTargetElement(connMapClass, stereotype, targetPackage, name, defPropMap);
                        mainel = generator.generateMappedProperties(connStruct, connectingEl, mainel, targetPackage, targetCl);

                        if (mapper.isAssociation(mainel)) {
                            el = generator.createElementCopy(el, mainel);
                            el2 = generator.createElementCopy(el2, mainel);
                            propManager.unsetFeatureValue(mainel, "member");
                            propManager.unsetFeatureValue(mainel, "memberEnd");
                            propManager.unsetFeatureValue(mainel, "ownedEnd");
                        }
                        if (p1name == null && p2name == null)
                            continue;
                        if (p1name.compareTo(p2name) == 0) {
                            if (!propManager.hasFeature(mainel, p1name))
                                throw new ElementGenerationException(String.format(bundle.getString("RelationTransformationFactory.1"),
                                        p1name, mapper.getHumanName(mainel)));
                            propManager.setPropertyValueList(mainel, p1name, el, el2);
                        } else {
                            if (!propManager.hasFeature(mainel, p1name))
                                throw new ElementGenerationException(String.format(bundle.getString("RelationTransformationFactory.1"),
                                        p1name, mapper.getHumanName(mainel)));
                            if (!propManager.hasFeature(mainel, p2name))
                                throw new ElementGenerationException(String.format(bundle.getString("RelationTransformationFactory.1"),
                                        p2name, mapper.getHumanName(mainel)));
                            if (propManager.isFeatureMultiValued(mainel, p1name)) 
                                propManager.setPropertyValueList(mainel, p1name, el);
                            else
                                propManager.setPropertyValue(mainel, p1name, el);
                            if (propManager.isFeatureMultiValued(mainel, p2name)) 
                                propManager.setPropertyValueList(mainel, p2name, el);
                            else
                                propManager.setPropertyValue(mainel, p2name, el);
                        }
                        if (mapper.isAssociation(mainel)) 
                            generator.setAssociationNavigable(mainel);
                        boolean needSet = mapper.isAssociation(mainel) ? !propManager.isFeatureSet(mainel, "memberEnd") : true;
                        if (needSet) {
                            generator.setClientElement(mainel, firstElems.get(i));
                            generator.setSupplierElement(mainel, secondElems.get(i));
                        }
                        // Set name of connecting element, if its mapping includes partial name
                        mainel = setMainElementName(mainel, connectingNames);
                        if (observers != null)
                            for (NotificationObserver observer: observers)
                                observer.update(new Object [] {mainel}, bundle.getString("TransformationFactory.1") + " "
                                    + (mapper.isNamedElement(mainel) ? mapper.getQualifiedName(mainel) : mapper.getHumanName(mainel)), NotificationType.INFO);
                        integrations.put(mainel, new IntegrationInfo<>(mainel, connectingEl, connStruct, targetCl));
                        ArrayList<Element> el2draw = new ArrayList<>();
                        el2draw.add(firstElems.get(i));
                        el2draw.add(secondElems.get(i));
                        drawableItems.put(mainel, el2draw);
                    } catch (ElementGenerationException ex) {
                        mainel = null;
                        singleDrawable.add(firstElems.get(i));
                        singleDrawable.add(secondElems.get(i));
                    }
                }
                firstProps.clear();
                secondProps.clear();
                firstElems.clear();
                secondElems.clear();
            }
            objectMap.clear();
            propertyMap.clear();
        }

        if (emptyProp)
            return drawableItems.keySet();
        
        Set<Object> items = drawableItems.keySet();
        if (!singleDrawable.isEmpty())
            items.addAll(singleDrawable);
        return items;
    }
    
    private boolean containsElementWithName(HashMap<Element, Element> map, Element newel) {
        ElementMapper<Element, ?, Stereotype> mapper = getElementMapper();
        for (Element el : map.values())
            if (mapper.getElementName2(el).compareToIgnoreCase(mapper.getElementName2(newel)) == 0)
                return true;
        return false;
    }
    
    /**
     * Returns {@code true}, if single element was created during transformation (mostly using 
     * {@link AbstractSingleTransformation#createElements(SpecificationReader, Set, Object, Collection)})
     * @return {@code true} if {@link AbstractSingleTransformation} transformation was used for element generation; {@code false} otherwise
     */
    public boolean isSingleGenerated() {
        return singleGenerated;
    }

    @Override
    public Map<Object, List<Element>> getGeneratedElements() {
        return drawableItems;
    }

    /**
     * Get elements which were generated using {@link AbstractSingleTransformation} implementations 
     * @return  The {@link Set} of objects
     */
    public Set<Object> getSingleDrawable() {
        return singleDrawable;
    }
    
    
}
