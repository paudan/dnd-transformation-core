package org.ktu.transformations.transforms.rendered;

import java.awt.Point;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.ktu.transformations.elements.AbstractElementProducer;
import org.ktu.transformations.elements.ElementGenerationException;
import org.ktu.transformations.helpers.AbstractPropertyManager;
import org.ktu.transformations.helpers.ElementSearch;
import org.ktu.transformations.mappers.ElementMapper;
import org.ktu.transformations.notifiers.NotificationObserver;
import org.ktu.transformations.parsers.ConnectableEntity;
import org.ktu.transformations.parsers.InvalidPatternException;
import org.ktu.transformations.parsers.PatternParser;
import org.ktu.transformations.parsers.SpecificationReader;
import org.ktu.transformations.renderers.ElementRenderer;
import org.ktu.transformations.transforms.AbstractTransformation;
import org.ktu.transformations.transforms.TransformationManager;

/**
 * Abstract class, implementing generic transformation with rendering functionality. Concrete implementations of transformations with
 * rendering functionality should extend this class
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, 
 * Kaunas University of Technology, 2015
 * @param <Element>         Type, corresponding to actual UML Element implementation
 * @param <Stereotype>      Type, corresponding to actual UML Stereotype implementation
 * @param <Presentation>    Type, corresponding to actual element presentation implementation
 */
abstract public class AbstractRenderedTransformation<Element, Stereotype, Presentation> 
        implements RenderedTransformation<Element, Stereotype, Presentation> {
    
    /** The subclass of {@link AbstractTransformation} used for element transformation */
    protected AbstractTransformation<Element, Stereotype> transform;

    public AbstractRenderedTransformation(AbstractTransformation<Element, Stereotype> transform) {
        this.transform = transform;
    }
    
    /**
     * Return element renderer, used for rendering produced elements
     * @return An instance of {@link ElementRenderer}
     * @see RenderedTransformation#getElementRenderer() 
     */
    @Override
    abstract public ElementRenderer<Element, Presentation> getElementRenderer();
    
    /**
     * Generate elements according to given transformation specification and the Element that was dragged
     * @param specReader	{@link SpecificationReader} object which encapsulates transformation specification and parameters
     * @param parser		Transformation pattern processor
     * @param targetCl		{@link ConnectableEntity} which maps to the target Element that will be generated
     * @param targetPackage	Element (such as a Package, Model or another container-type of element) which will contain the generated elements
     * @param selected		The dragged Element which is used as initial source or as a reference to select additional source elements for transformation
     * @param elementOver       The element (UML element, its presentation, diagram, etc.) that the {@code selected} element is dragged on. 
     * It can be set to {@code null}, if the element was not dragged on any element
     * @param location		Actual location on the {@code elementOver} where the set of generated PresentationElement will be placed on
     * @param observers         {@link NotificationObserver} elements which observe the performed generation procedures
     * @return	The set of generated Elements
     * @throws ElementGenerationException   The element(s) were not generated successfully
     * @throws InvalidPatternException      There was an error while processing the pattern
     */
    public Set<Object> create(SpecificationReader specReader, PatternParser<?, ?, Element, Stereotype> parser, ConnectableEntity targetCl, 
            Element targetPackage, Element selected, Presentation elementOver, Point location, Collection<NotificationObserver> observers) 
            throws ElementGenerationException, InvalidPatternException {
        TransformationManager.getInstance().setCurrentReader(specReader);
        Element actualOver = getElementMapper().getElementFromPresentation(elementOver);
        Set<Object> elements = transform.create(specReader, parser, targetCl, targetPackage, selected, actualOver, observers);
        renderItems(getDrawableItems(), elementOver, location);
        return elements;
    }
    
    /**
     * Get the elements which should be rendered 
     * @return The {@link Map} of UML elements, together with their internal relations
     */
    protected Map<Object, ?> getDrawableItems() {
        return transform.getGeneratedElements();
    }
    
    /**
     * Perform drawing (rendering) of the generated items
     * @param drawable      The {@link Map} of generated items, which would be rendered. Typically, this corresponds to the output of {@linkplain #getDrawableItems()}
     * @param elementOver   The element which was selected during generation initialization, or used as an element to drag on selected elements 
     * @param location      Location where the elements should be rendered
     * @return              {@link List} of generated element presentations
     */
    abstract protected List<Presentation> renderItems(Map<Object, ?> drawable, Presentation elementOver, Point location);

    /**
     * Get {@link ElementMapper} object which performs actual mapping to functionality in the implementations
     * @return An instance of {@link ElementMapper}
     */
    @Override
    public ElementMapper<Element, ?, Stereotype> getElementMapper() {
        return transform.getElementMapper();
    }

    /**
     * Get {@link AbstractPropertyManager} object implementing property (UML feature) management functionality 
     * @return An instance of {@link AbstractPropertyManager}
     */
    @Override
    public AbstractPropertyManager<Element, Stereotype, ?> getPropertyManager() {
        return transform.getPropertyManager();
    }

    /**
     * Get element producer object, implementing element generation for particular implementation 
     * @return An instance of {@link AbstractElementProducer}
     */
    @Override
    public AbstractElementProducer<Element, Stereotype> getElementProducer() {
        return transform.getElementProducer();
    }

    /**
     * Get element finder object 
     * @return An implementation of {@link ElementSearch}
     */
    @Override
    public ElementSearch<Element, Stereotype> getElementSearch() {
        return transform.getElementSearch();
    }

    /**
     * Get object performing actual transformation which produces the elements to be rendered
     * @return An implementation of {@link AbstractTransformation}
     */
    public AbstractTransformation<Element, Stereotype> getTransformation() {
        return transform;
    }
  
}
