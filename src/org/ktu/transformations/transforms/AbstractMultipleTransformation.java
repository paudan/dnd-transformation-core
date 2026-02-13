package org.ktu.transformations.transforms;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.ktu.transformations.elements.AbstractElementProducer;
import org.ktu.transformations.elements.ElementGenerationException;
import org.ktu.transformations.elements.ElementPropertiesMaker;
import org.ktu.transformations.helpers.AbstractPropertyManager;
import org.ktu.transformations.helpers.ElementSearch;
import org.ktu.transformations.mappers.ElementMapper;
import org.ktu.transformations.notifiers.NotificationObserver;
import org.ktu.transformations.notifiers.NotificationType;
import org.ktu.transformations.parsers.ConcatResolver;
import org.ktu.transformations.parsers.ConnectableEntity;
import org.ktu.transformations.parsers.InvalidPatternException;
import org.ktu.transformations.parsers.PatternParser;
import org.ktu.transformations.parsers.PropertyStack;
import org.ktu.transformations.parsers.SpecificationReader;

/**
 * Generates multiple target elements, given single source Element. Connecting element is not defined for this transformation
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, 
 * Kaunas University of Technology, 2015
 * @param <Element>         Type, corresponding to actual UML Element implementation
 * @param <Stereotype>      Type, corresponding to actual UML Stereotype implementation
 */
public abstract class AbstractMultipleTransformation<Element, Stereotype> extends AbstractTransformation<Element, Stereotype> {
    
    /**
     * A map which contains default properties for each class of elements, represented as a {@link Map},
     * where key is the name of the property and value is the Element that must be set.
     * This map is obligatory for such cases when property MUST be set in order to ensure valid Element (e.g., owner Element)
     */
    protected Map<Class<?>, Map<String, Element>> defPropMap;
    /** Indication, whether the properties must be generated */
    protected boolean emptyProp;
    private Map<Object, ConnectableEntity> drawableItems;
    private PatternParser parser;
    /** Flag, defining if {@link AbstractSingleTransformation} has been called to generate elements */
    protected boolean singleGenerated;
    private ConnectableEntity targetCl;
    /** {@link AbstractSingleTransformation} object used to perform transformations for single element generation */
    protected AbstractSingleTransformation<Element, Stereotype> singleTransformer;

    public AbstractMultipleTransformation() throws TransformationConfigurationException {
        super();
        final ElementMapper<Element, ?, Stereotype> mapper = this.getElementMapper();
        final AbstractPropertyManager<Element, Stereotype, ?> manager = this.getPropertyManager();
        final AbstractElementProducer<Element, Stereotype> eproducer = this.getElementProducer();
        final ElementSearch<Element, Stereotype> search = this.getElementSearch();
        TransformationFactory<Element, Stereotype> factory = TransformationFactory.getInstance();
        this.singleTransformer = factory.getSingleTransformationInstance(mapper, manager, eproducer, search);
        defPropMap = new HashMap<>();
    }
    
    @Override
    public Set<Object> createElements(SpecificationReader specReader, 
            PatternParser<?, ?, Element, Stereotype> parser, ConnectableEntity targetCl, 
            Element targetPackage, Element dragged, Object elementOver, Collection<NotificationObserver> observers) 
            throws ElementGenerationException, InvalidPatternException {
        TransformationManager.getInstance().setCurrentReader(specReader);
        this.parser = parser;
        this.targetCl = targetCl;
        drawableItems = new HashMap<>();
        emptyProp = false;
        singleGenerated = false;
        initSourceConnectingElements(parser, dragged);
        // If no connecting elements were identified, generate single element representing dragged 
        if (connectingElements.isEmpty()) {
            this.dragged = dragged;
            this.singleGenerated = true;
            Set<Object> elements = singleTransformer.createElements(specReader, 
                    parser.getDraggedTargetClassifier(dragged), targetPackage, observers);
            for (Object el: elements)
                drawableItems.put(el, null);
            return drawableItems.keySet();
        }
        initializeStructures(parser, targetCl);
        drawableItems = generatePropertyItems(specReader, parser, targetCl, targetPackage, dragged, observers);
        // Set element properties according to their relationships
        AbstractPropertyManager<Element, Stereotype, ?> propManager = this.getPropertyManager();
        ElementMapper<Element, ?, ?> mapper = getElementMapper();
        for (Object genEl : drawableItems.keySet()) {
            Map<ConnectableEntity, PropertyStack> targetMap = targets.get(drawableItems.get(genEl)).targetPropertyMap;
            if (targetMap != null)
                for (Object eldraw : drawableItems.keySet()) {
                    PropertyStack stack = targetMap.get(drawableItems.get(eldraw));
                    if (mapper.isElement(genEl) && mapper.isElement(eldraw) && eldraw != genEl && stack != null) {
                        Element generEl = (Element) genEl;
                        Element generProp = (Element) eldraw;
                        if (!propManager.hasPropertyValue(generEl, stack, stack.lowermostProperty().getName3(), generProp))
                            propManager.setProperty(generEl, stack, generProp);
                    } else if (mapper.isElement(genEl) && eldraw instanceof String)
                        propManager.setProperty((Element) genEl, stack, eldraw);
                }
        }
        defPropMap = null;
        Set<Object> gen = new HashSet<>();
        for (Object o : drawableItems.keySet())
            if (mapper.isElement(o))
                gen.add(o);
        return gen;
    }

     /**
     * Generate multiple UML elements which may be rendered
     * @param specReader	{@link SpecificationReader} object for processing transformation specification and parameters
     * @param parser		{@link PatternParser} object for transformation pattern resolution
     * @param targetCl		{@link ConnectableEntity} which maps to the target Element that will be generated
     * @param targetPackage	The Element (such as a Package, Model or similar another container-type of element) which will contain the generated elements
     * @param dragged		The dragged Element which is used as initial source or as a reference to select additional source elements for transformation
     * @param observers         {@link NotificationObserver} elements which receive notifications of generation status from this object
     * @return                  The {@link Map} of generated UML elements with their corresponding mapping {@link ConnectableEntity} entities
     * @throws ElementGenerationException	The element(s) were not generated successfully
     * @throws InvalidPatternException          There was an error while processing the pattern
     */
    protected Map<Object, ConnectableEntity> generatePropertyItems(SpecificationReader specReader, PatternParser parser,
            ConnectableEntity targetCl, Element targetPackage, Element dragged, Collection<NotificationObserver> observers)
            throws InvalidPatternException, ElementGenerationException {
        Map<Object, ConnectableEntity> drawable = new HashMap<>();
        ElementMapper<Element, ?, ?> mapper = getElementMapper();
        AbstractElementProducer<Element, Stereotype> generator = this.getElementProducer();
        generator.setTargetPackage(targetPackage);
        defPropMap = parser.getUnmappedElements(mapper.getElementName2(dragged), targetPackage, getElementSearch());
        ElementPropertiesMaker<Element, Stereotype> factory = new ElementPropertiesMaker<>(this, emptyProp, connStruct);
        factory.addObservers(observers);
        for (Element connectingEl : connectingElements) {
            selectDragged = true;
            if (!connStruct.sourcePropertyMap.isEmpty()) {
                for (ConnectableEntity prop : connStruct.sourcePropertyMap.keySet()) {
                    List<Object> propelemList = createPropertyList(prop, connectingEl, dragged, parser);
                    if (propelemList == null || propelemList.isEmpty())
                        continue;
                    Map<ConnectableEntity, List<Object>> genProps = new HashMap<>();
                    genProps.put(prop, propelemList);
                    resolver = new ConcatResolver<>(targets, genProps, connectingEl, connStruct.source, this);
                    for (Object propelem : propelemList) {
                        List<ConnectableEntity> _targets = sources.get(prop).targetList;
                        for (ConnectableEntity target : _targets)
                            try {
                                Map<Object, SimpleImmutableEntry<Element, ConnectableEntity>> generated
                                        = generator.generateElementsConcat(resolver, target, sources.get(prop), propelem, dragged, defPropMap, factory);
                                for (Object el : generated.keySet()) {
                                    if (mapper.isElement(el))
                                        el = generator.generateMappedProperties(connStruct, connectingEl, (Element) el, targetPackage, target);
                                    drawable.put(el, target);
                                }
                            } catch (ElementGenerationException e) {
                                if (observers != null)
                                    for (NotificationObserver observer: observers)
                                        observer.update(null, e.getMessage(), NotificationType.ERROR);
                            }
                    }
                }
            } else {
                resolver = new ConcatResolver<>(targets, null, connectingEl, connStruct.source, this);
                for (ConnectableEntity target : connStruct.targetList)
                    try {
                        Map<Object, AbstractMap.SimpleImmutableEntry<Element, ConnectableEntity>> generated
                                = generator.generateElementsConcat(resolver, target, connStruct, dragged, dragged, defPropMap, factory);
                        for (Object newel : generated.keySet())
                            drawable.put(newel, target);
                    } catch (ElementGenerationException e) {
                        if (observers != null)
                            for (NotificationObserver observer: observers)
                                observer.update(null, e.getMessage(), NotificationType.ERROR);
                    }
            }
        }
        return drawable;
    }
    
    /**
     * Set flag, if the properties must be generated as well
     * @param emptyProp	Value, indicating the obligation to generate properties
     */
    protected void setEmptyPropertyFlag(boolean emptyProp) {
        this.emptyProp = emptyProp;
    }

    @Override
    public Map<Object, ConnectableEntity> getGeneratedElements() {
        return drawableItems;
    }

    /**
     * Checks if {@link AbstractSingleTransformation} has been called to generate elements (i.e, if single elements were generated during transformation)
     * Should be called after {@link #createElements(SpecificationReader, PatternParser, ConnectableEntity, Object, Object, Object, Collection)} is called
     * @return {@code true} if single element type of transformation has been used to generate elements; {@code false} otherwise
     */
    public boolean isSingleGenerated() {
        return singleGenerated;
    }

    /**
     * Return the {@link PatternParser} object used during this transformation
     * @return The {@link PatternParser} object
     */
    public PatternParser getPatternParser() {
        return parser;
    }

    /**
     * Return {@link ConnectableEntity} which was used during element generation as target classifier mapping
     * @return An instance of {@link ConnectableEntity}
     */
    public ConnectableEntity getTargetEntity() {
        return targetCl;
    }
    
    
    
}
