package org.ktu.transformations.transforms.rendered;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ktu.transformations.elements.AbstractElementProducer;
import org.ktu.transformations.elements.ElementGenerationException;
import org.ktu.transformations.helpers.AbstractPropertyManager;
import org.ktu.transformations.helpers.ElementSearch;
import org.ktu.transformations.mappers.ElementMapper;
import org.ktu.transformations.notifiers.NotificationObserver;
import org.ktu.transformations.parsers.ConcatMap;
import org.ktu.transformations.parsers.ConnectableEntity;
import org.ktu.transformations.parsers.ConnectorEntity;
import org.ktu.transformations.parsers.ElementMapping;
import org.ktu.transformations.parsers.InvalidPatternException;
import org.ktu.transformations.parsers.PatternParser;
import org.ktu.transformations.parsers.PatternParserFactory;
import org.ktu.transformations.parsers.PropertyStack;
import org.ktu.transformations.parsers.SpecificationReader;
import org.ktu.transformations.renderers.ElementRenderer;
import org.ktu.transformations.transforms.Transformation;
import org.ktu.transformations.transforms.TransformationConfigurationException;

/**
 * Performs actual transformation by creating actual elements and rendering them. Transformation types are selected according to the specification.
 * {@link Transformation} object instances are initialized automatically during transformation, although their override is also possible using
 * {@code setTransformation(Transformation transform)} type setters. This is particularly useful, if custom extensions (e.g., including element layout, integrations)
 * are developed for particular implementations
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, 
 * Kaunas University of Technology, 2015
 * @param <Element>         Type, corresponding to actual UML Element implementation
 * @param <Stereotype>      Type, corresponding to actual UML Stereotype implementation
 * @param <ConnectableElement>  Type, corresponding to actual UML ConnectableElement implementation
 * @param <Presentation>    Type, corresponding to actual element presentation implementation
 */
@SuppressWarnings("rawtypes")
public class RenderedGenerator<Element, Stereotype, ConnectableElement, Presentation> {
    
    private SpecificationReader reader;
    private Point location;
    private ElementMapper<Element, ConnectableElement, Stereotype> mapper;
    private AbstractPropertyManager<Element, Stereotype, ?> manager;
    private ElementSearch<Element, Stereotype> search;
    private ElementRenderer<Element, Presentation> renderer;
    private AbstractElementProducer<Element, Stereotype> producer;
    private ResourceBundle bundle = ResourceBundle.getBundle("org/ktu/transformations/messages_EN");
    private RenderedTransformationFactory<Element, Stereotype, Presentation> factory;
    private PatternParserFactory<?, ConnectableElement, Element, Stereotype> patternFactory;
    
    private RenderedSingleTransformation<Element, Stereotype, Presentation> singleTransform;
    private RenderedMultipleTransformation<Element, Stereotype, Presentation> multipleTransform;
    private RenderedPropertyTransformation<Element, Stereotype, Presentation> propertyTransform;
    private RenderedActivityTransformation<Element, Stereotype, Presentation> activityTransform;
    private RenderedContainerTransformation<Element, Stereotype, Presentation> containerTransform;
    private RenderedRelationTransformation<Element, Stereotype, Presentation> relationTransform;

    /**
     * Create a new instance of {@link RenderedGenerator}
     * @param mapper    An implementation of {@link ElementMapper} which performs actual mapping to element properties in the implementations    
     * @param manager   A subclass of {@link AbstractPropertyManager}implementing property (UML feature) management functionality
     * @param producer  A subclass of {@link AbstractElementProducer}, implementing element generation for implementation in particular tool 
     * @param search    A subclass of {@link ElementSearch}, implementing search of different elements by their class, type, stereotype, etc.
     * @param renderer  {@link ElementRenderer} object used to generate presentation elements for generated UML elements
     * @param patternFactory Factory which is used to create new {@link PatternParser} objects
     */
    public RenderedGenerator(ElementMapper<Element, ConnectableElement, Stereotype> mapper, 
            AbstractPropertyManager<Element, Stereotype, ?> manager, 
            AbstractElementProducer<Element, Stereotype> producer,
            ElementSearch<Element, Stereotype> search, 
            ElementRenderer<Element, Presentation> renderer,
            PatternParserFactory<?, ConnectableElement, Element, Stereotype> patternFactory) {
        this.mapper = mapper;
        this.manager = manager;
        this.producer = producer;
        this.search = search;
        this.renderer = renderer;
        this.patternFactory = patternFactory;
        factory = RenderedTransformationFactory.getInstance();
    }
    
    /**
     * Set location on target presentation element (e.g. Diagram, etc.) where the elements should be generated
     * @param location	Location point where the element presentation(s) is generated
     */
    public void setLocation(Point location) {
        this.location = location;
    }
    
    /**
     * Set current M2M transformation specification reader object
     * @param reader {@link SpecificationReader} object
     */
    public void setSpecificationReader(SpecificationReader reader) {
        this.reader = reader;
    }
    
    /**
     * Performs generation of UML, together with their rendering on given {@code diagram} or {@code elementOver} element, depending on the nature of the transformation 
     * @param selected          The UML element which was selected during transformation call action, or used as the dragged element 
     * @param targetPackage     The UML element (e.g., UML Package, Model, etc.) which would contained all the generated UML elements
     * @param diagram           The UML Diagram element which the {@code selected} element is dragged onto
     * @param elementOver       The UML element which the {@code selected} element is dragged onto. If this element is a Diagram, then 
     * {@code elementOver} should be the same as {@code diagram}
     * @param observers   The {@link Collection} of objects which implement {@link NotificationObserver} in order to receive 
     * generation notifications and messages from the transformation. Such notifications can be used to inform the user of the progress on the transformation, 
     * its successful completion or exceptions thrown during its execution     
     * @return            The {@link Set} of elements, generated during this transformation action
     * @throws InvalidPatternException      Transformation pattern is syntactically invalid 
     * @throws ElementGenerationException   There was an error while generating the elements
     * @throws TransformationConfigurationException Transformation is not properly configured or implemented 
     * (e.g., the element producer of the transformation object is {@code null})
     */
    public Set<Object> generate(Element selected, Element targetPackage, Presentation diagram, 
            Presentation elementOver, Collection<NotificationObserver> observers) 
            throws InvalidPatternException, ElementGenerationException, TransformationConfigurationException {
        Set<Object> generated = new HashSet<>();
        Object specElement = reader.getTransformationPattern();
        PatternParser<?, ConnectableElement, Element, Stereotype> parser = null;
        if (specElement != null && mapper.isElement(specElement)) {
            Element elOver = mapper.getElementFromPresentation(elementOver);
            if (mapper.isDiagram(elOver)) {
                parser = patternFactory.getParserInstance((Element) specElement, null, mapper, null);
                if (parser == null)
                    return generated;
            }
            else {
                if (mapper.isElement(reader.getTargetClassifier()))
                    parser = patternFactory.getParserInstance((Element) specElement, (Element)reader.getTargetClassifier(), mapper, elOver);
                if (parser == null)
                    return generated;
            }
        }
        if (elementOver == null)
            throw new InvalidPatternException(bundle.getString("Generator.3"));
        if (parser == null) {
            RenderedSingleTransformation<Element, Stereotype, Presentation> transform = this.getSingleElementTransformation();
            if (transform.getElementProducer() == null)
                throw new TransformationConfigurationException(String.format(bundle.getString("Generator.4"), 
                        RenderedSingleTransformation.class.getSimpleName()));
            generated.addAll(transform.createSingleElement(reader, (Element) reader.getTargetClassifier(), 
                    targetPackage, selected, diagram, location, observers));
            return generated;
        }
        Map<ConnectableEntity, ElementMapping> targets = parser.getTargetMappings();
        ConnectableEntity targetCl = parser.getTargetConnectingClassifier();
        Class<?> targetConn = targetCl != null ? targetCl.getBaseClass() : null;
        Logger.getLogger(getClass().getName()).log(Level.INFO, 
                "Target connecting element: {0}", (targetConn != null ? targetConn.getName() : "null"));

        // Initialize structures
        Map<ConnectableEntity, ElementMapping> sources = parser.getSourceMappings();
        ConnectableEntity mainPattern = parser.getSourceConnectingClassifier();
        if (mainPattern == null)
            throw new InvalidPatternException(String.format(bundle.getString("Generator.1")));
        ElementMapping mainstruct = sources.get(mainPattern);
        if (mainstruct == null)
            throw new InvalidPatternException(String.format(bundle.getString("Generator.2"), mapper.getActualName(parser.getPatternRoot())));
        if (targetConn == null) {
            RenderedMultipleTransformation<Element, Stereotype, Presentation> transform = this.getMultipleTransformation();
            if (transform.getElementProducer() == null)
                throw new TransformationConfigurationException(String.format(bundle.getString("Generator.4"), 
                        RenderedMultipleTransformation.class.getSimpleName()));
            generated = transform.create(reader, parser, (ConnectableEntity) reader.getTargetClassifier(), 
                    targetPackage, selected, diagram, location, observers);
        } else {
            Set<Object> results = new HashSet<>();
            List<ConnectableEntity> tList = new ArrayList<>();
            ConnectableEntity hanging = parser.getHangingRepresentingTarget(null);
            if (!mainstruct.targetList.isEmpty())
                tList = mainstruct.targetList;
            // Check for pattern, which does not have any direct source-target mappings ("property assignment to target element" pattern)
            else if (targetCl != null && hanging != null && targetCl.equals(hanging)) {
                for (ConnectableEntity e : targets.keySet())
                    if (!e.equals(hanging))
                        tList.add(e);
            } else
                tList.add(parser.getTargetConnectingClassifier());
            for (ConnectableEntity el : tList) {
                if (mapper.getElementFromPresentation(elementOver) != mapper.getElementFromPresentation(diagram)) {
                    RenderedPropertyTransformation<Element, Stereotype, Presentation> transform = this.getPropertyTransformation();
                    if (transform.getElementProducer() == null)
                        throw new TransformationConfigurationException(String.format(bundle.getString("Generator.4"), 
                                RenderedPropertyTransformation.class.getSimpleName()));
                    results = transform.create(reader, parser, el, targetPackage, selected, elementOver, location, observers);
                } else if (mapper.isRelatingClassifier(targetConn)) {
                    RenderedRelationTransformation<Element, Stereotype, Presentation> transform = this.getRelationTransformation();
                    if (transform.getElementProducer() == null)
                        throw new TransformationConfigurationException(String.format(bundle.getString("Generator.4"), 
                                RenderedRelationTransformation.class.getSimpleName()));
                    results = transform.create(reader, parser, el, targetPackage, selected, diagram, location, observers);
                } else if (mapper.isActivityClassifier(targetConn) && !parser.hasAdditionalPropertyMapping(targetCl)) {
                    RenderedActivityTransformation<Element, Stereotype, Presentation> transform = this.getActivityTransformation();
                    if (transform.getElementProducer() == null)
                        throw new TransformationConfigurationException(String.format(bundle.getString("Generator.4"), 
                                RenderedActivityTransformation.class.getSimpleName()));
                    results = transform.create(reader, parser, el, targetPackage, selected, diagram, location, observers);
                } else if (parser.hasConcatMappings() && parser.getTargetMappings().size() == 1) {
                    for (ConnectableEntity key : targets.keySet()) {
                        ConcatMap concatMap = targets.get(key).concatMap;
                        for (ConnectorEntity<?> concat : concatMap.keySet()) {
                            Set<ConnectorEntity> incomingSet = concatMap.getIncomingConnectors(concat);
                            for (ConnectorEntity<?> incoming : incomingSet) {
                                ConnectableEntity targetToAdd = concatMap.getTargetPropertyStack(concat).metaElement();
                                PropertyStack targetMapping = concatMap.getSourcePropertyStack(concat, incoming);
                                List<ConnectableEntity> targetList = sources.get(targetMapping.metaElement()).targetList;
                                if (targetList.isEmpty())
                                    targetList.add(targetToAdd);
                            }
                        }
                    }
                    RenderedMultipleTransformation<Element, Stereotype, Presentation> transform = this.getMultipleTransformation();
                    if (transform.getElementProducer() == null)
                        throw new TransformationConfigurationException(String.format(bundle.getString("Generator.4"), 
                                RenderedMultipleTransformation.class.getSimpleName()));
                    generated = transform.create(reader, parser, el, targetPackage, selected, diagram, location, observers);
                } else if (parser.hasAdditionalPropertyMapping(el)) {
                    RenderedMultipleTransformation<Element, Stereotype, Presentation> transform = this.getMultipleTransformation();
                    if (transform.getElementProducer() == null)
                        throw new TransformationConfigurationException(String.format(bundle.getString("Generator.4"), 
                                RenderedMultipleTransformation.class.getSimpleName()));
                    generated = transform.create(reader, parser, el, targetPackage, selected, diagram, location, observers);
                } else {
                    RenderedContainerTransformation<Element, Stereotype, Presentation> transform = this.getContainerTransformation();
                    if (transform.getElementProducer() == null)
                        throw new TransformationConfigurationException(String.format(bundle.getString("Generator.4"), 
                                RenderedContainerTransformation.class.getSimpleName()));
                    generated = transform.create(reader, parser, el, targetPackage, selected, diagram, location, observers);
                }
                generated.addAll(results);
            }
        }
        return generated;
    }
    
    /**
     * Return {@link RenderedSingleTransformation} object which is used to perform rendered single element transformations
     * @return An instance of {@link RenderedSingleTransformation}
     * @throws TransformationConfigurationException  If the transformation that is set is not properly configured 
     * (e.g., does not have properly set element mapper, object, etc.)
     */ 
    public RenderedSingleTransformation<Element, Stereotype, Presentation> getSingleElementTransformation() throws TransformationConfigurationException {
        if (singleTransform == null) 
            singleTransform = factory.getRenderedSingleTransformation(mapper, manager, producer, search, renderer);
        return singleTransform;
    }

    /**
     * Set {@link RenderedSingleTransformation}  which would be used to perform rendered single element transformations
     * @param singleTransform {@link RenderedSingleTransformation} object
     */
    public void setSingleElementTransformation(RenderedSingleTransformation<Element, Stereotype, Presentation> singleTransform) {
        this.singleTransform = singleTransform;
    }

    /**
     * Return {@link RenderedMultipleTransformation} object which is used to perform rendered transformations which would result in multiple unconnected elements
     * @return An instance of {@link RenderedMultipleTransformation}
     * @throws TransformationConfigurationException  If the transformation that is set is not properly configured 
     * (e.g., does not have properly set element mapper, object, etc.)
     */
    public RenderedMultipleTransformation<Element, Stereotype, Presentation> getMultipleTransformation() throws TransformationConfigurationException {
        if (multipleTransform == null) 
            multipleTransform = factory.getRenderedMultipleTransformation(mapper, manager, producer, search, renderer);
        return multipleTransform;
    }

    /**
     * Set {@link RenderedMultipleTransformation} which would be used to perform rendered transformations which would result in multiple unconnected elements
     * @param multipleTransform {@link RenderedMultipleTransformation} object
     */
    public void setMultipleTransformation(RenderedMultipleTransformation<Element, Stereotype, Presentation> multipleTransform) {
        this.multipleTransform = multipleTransform;
    }

    /**
     * Return {@link RenderedPropertyTransformation} object which is used to perform rendered transformations resulting in elements with "property-of" relations
     * @return An instance of {@link RenderedPropertyTransformation}
     * @throws TransformationConfigurationException  If the transformation that is set is not properly configured 
     * (e.g., does not have properly set element mapper, object, etc.)
     */
    public RenderedPropertyTransformation<Element, Stereotype, Presentation> getPropertyTransformation() throws TransformationConfigurationException {
        if (propertyTransform == null) 
            propertyTransform = factory.getRenderedPropertyTransformation(mapper, manager, producer, search, renderer);
        return propertyTransform;
    }

    /**
     * Set {@link RenderedPropertyTransformation} which would be used to perform rendered transformations resulting in elements with "property-of" relations
     * @param propertyTransform {@link RenderedPropertyTransformation} object
     */
    public void setPropertyTransformation(RenderedPropertyTransformation<Element, Stereotype, Presentation> propertyTransform) {
        this.propertyTransform = propertyTransform;
    }

    /**
     * Return {@link RenderedActivityTransformation} object which is used to perform rendered transformations resulting in connecting activity elements
     * @return An instance of {@link RenderedActivityTransformation}
     * @throws TransformationConfigurationException  If the transformation that is set is not properly configured 
     * (e.g., does not have properly set element mapper, element producer objects, etc.)
     */
    public RenderedActivityTransformation<Element, Stereotype, Presentation> getActivityTransformation() throws TransformationConfigurationException {
        if (activityTransform == null)
            activityTransform = factory.getRenderedActivityTransformation(mapper, manager, producer, search, renderer);
        return activityTransform;
    }

    /**
     * Set {@link RenderedActivityTransformation} which would be used to perform rendered transformations resulting in connecting activity elements
     * @param activityTransform {@link RenderedActivityTransformation} object
     */
    public void setActivityTransformation(RenderedActivityTransformation<Element, Stereotype, Presentation> activityTransform) {
        this.activityTransform = activityTransform;
    }

    /**
     * Return {@link RenderedContainerTransformation} object which is used to perform rendered transformations resulting in connecting container elements
     * @return An instance of {@link RenderedContainerTransformation}
     * @throws TransformationConfigurationException  If the transformation that is set is not properly configured 
     * (e.g., does not have properly set element mapper, element producer objects, etc.)
     */
    public RenderedContainerTransformation<Element, Stereotype, Presentation> getContainerTransformation() throws TransformationConfigurationException {
        if (containerTransform == null) 
            containerTransform = factory.getRenderedContainerTransformation(mapper, manager, producer, search, renderer);
        return containerTransform;
    }

    /**
     * Set {@link RenderedContainerTransformation} which would be used to perform rendered transformations resulting in connecting container elements
     * @param containerTransform {@link RenderedContainerTransformation} object
     */
    public void setContainerTransformation(RenderedContainerTransformation<Element, Stereotype, Presentation> containerTransform) {
        this.containerTransform = containerTransform;
    }

    /**
     * Return {@link RenderedRelationTransformation} object which is used to perform rendered transformations resulting in connecting relationship type of elements
     * @return An instance of {@link RenderedRelationTransformation}
     * @throws TransformationConfigurationException  If the transformation that is set is not properly configured 
     * (e.g., does not have properly set element mapper, element producer objects, etc.)
     */
    public RenderedRelationTransformation<Element, Stereotype, Presentation> getRelationTransformation() throws TransformationConfigurationException {
        if (relationTransform == null)
            relationTransform = factory.getRenderedRelationTransformation(mapper, manager, producer, search, renderer);
        return relationTransform;
    }

    /**
     * Set {@link RenderedRelationTransformation} which would be used to perform rendered transformations resulting in connecting relationship type of elements
     * @param relationTransform {@link RenderedRelationTransformation} object
     */
    public void setRelationTransformation(RenderedRelationTransformation<Element, Stereotype, Presentation> relationTransform) {
        this.relationTransform = relationTransform;
    }
}
