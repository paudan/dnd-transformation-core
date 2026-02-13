package org.ktu.transformations.transforms;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.ktu.transformations.elements.ElementGenerationException;
import org.ktu.transformations.elements.SimpleElementMaker;
import org.ktu.transformations.mappers.ElementMapper;
import org.ktu.transformations.notifiers.NotificationObserver;
import org.ktu.transformations.parsers.ConnectableEntity;
import org.ktu.transformations.parsers.InvalidPatternException;
import org.ktu.transformations.parsers.PatternParser;
import org.ktu.transformations.parsers.SpecificationReader;

/**
 * Generates single target Element, corresponding to the source Element
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, 
 * Kaunas University of Technology, 2014-2015
 * @param <Element>         Type, corresponding to actual UML Element implementation
 * @param <Stereotype>      Type, corresponding to actual UML Stereotype implementation
 */
public abstract class AbstractSingleTransformation<Element, Stereotype> extends AbstractTransformation<Element, Stereotype> {
   
    private Map<Object, ?> drawableItems;

    public AbstractSingleTransformation() {
        super();
        drawableItems = new HashMap<>();
    }

    /**
     * <p>Generate a set of Elements respectively to a set of given target Classifiers, each of which is a mapping to the source element {@code dragged}</p>
     * <p>It is a multi-element version of {@link AbstractSingleTransformation#createSingleElement(SpecificationReader, Object, Object, Collection) }</p>
     * @param specReader	{@link SpecificationReader} object which provides transformation specification and parameters
     * @param targetClassifiers Target Classifiers which represent the target Element which would be generated
     * @param targetPackage	Element (such as a Package, Model, etc.) which will contain the generated elements
     * @param dragged		The dragged Element which is used as a source for transformation
     * @param observers         {@link NotificationObserver} elements which observe the performed generation procedures and get their notifications
     * @return	The set of generated elements
     */
    public Set<Object> createElements(SpecificationReader specReader, Set<Element> targetClassifiers, 
            Element targetPackage, Element dragged, Collection<NotificationObserver> observers) {
        TransformationManager.getInstance().setCurrentReader(specReader);
        this.dragged = dragged;
        for (Element targetCl : targetClassifiers)
            drawableItems.put(createSingleElement(specReader, targetCl, targetPackage, observers), null);
        return drawableItems.keySet();
    }

    /**
     * Generate a set of Elements respectively to a set of given target Classifiers
     * @param specReader	{@link SpecificationReader} object which provides transformation specification and parameters
     * @param targetClassifiers Target Classifiers which represent the target Element which would be generated
     * @param targetPackage	Element (such as a Package, Model, etc.) which will contain the generated elements
     * @param observers         {@link NotificationObserver} elements which observe the performed generation procedures and get their notifications
     * @return	The set of generated elements
     */
    public Set<Object> createElements(SpecificationReader specReader, Set<Element> targetClassifiers, 
            Element targetPackage, Collection<NotificationObserver> observers) {
        TransformationManager.getInstance().setCurrentReader(specReader);
        for (Element targetCl : targetClassifiers)
            drawableItems.put(createSingleElement(specReader, targetCl, targetPackage, observers), null);
        return drawableItems.keySet();
    }

    /**
     * Generate single Element which corresponds to the source element {@literal dragged}
     * @param specReader	{@link SpecificationReader} object which provides transformation specification and parameters
     * @param targetCl		Target Classifier which represents the target Element which would be generated
     * @param targetPackage	Element (such as a Package, Model, etc.) which will contain the generated elements
     * @param observers         {@link NotificationObserver} elements which observe the performed generation procedures and get their notifications
     * @return	The set of generated UML elements
     */
    public Element createSingleElement(SpecificationReader specReader, Element targetCl, 
            Element targetPackage, Collection<NotificationObserver> observers) {
        TransformationManager.getInstance().setCurrentReader(specReader);
        ElementMapper<Element, ?, Stereotype> mapper = getElementMapper();
        if (!mapper.isElement(targetCl) && !mapper.isClassifier(targetCl))
            return null;
        String elementName = mapper.getElementName(dragged);
        elementName = elementName != null ? mapper.getProperName(elementName) : null;
        SimpleElementMaker<Element, Stereotype> factory = new SimpleElementMaker<>(this);
        factory.addObservers(observers);
        Stereotype stereotype = mapper.isStereotype(targetCl) ? (Stereotype) targetCl : null;
        Element newel = factory.createElement(targetPackage, mapper.getBaseClass(targetCl), 
                stereotype, elementName, null, specReader.isCheckUnique());
        return newel;
    }

    /**
     * Create single element
     * @param specReader	{@link SpecificationReader} object which provides transformation specification and parameters
     * @param targetCl          Target classifier which represents the target UML element to be generated
     * @param targetPackage	Element (such as a Package, Model, etc.) which will contain the generated elements
     * @param selected		The selected/dragged Element which is used as a source for transformation
     * @param observers         {@link NotificationObserver} elements which observe the performed generation procedures and get their notifications
     * @return	{@link Set} containing single generated element
     * @throws ElementGenerationException   There was an error while generating the element 
     */
    public Set<Object> createSingleElement(SpecificationReader specReader, Element targetCl, 
            Element targetPackage, Element selected, Collection<NotificationObserver> observers) 
            throws ElementGenerationException {
        TransformationManager.getInstance().setCurrentReader(specReader);
        drawableItems.clear();
        this.dragged = selected;
        drawableItems.put(createSingleElement(specReader, targetCl, targetPackage, observers), null);
        return drawableItems.keySet();
    }

    @Override
    public Set<Object> createElements(SpecificationReader specReader, PatternParser<?, ?, Element, Stereotype> parser, 
            ConnectableEntity targetCl, Element targetPackage, Element dragged, Object elementOver, 
            Collection<NotificationObserver> observers) throws ElementGenerationException, InvalidPatternException {
        drawableItems = new HashMap<>();
        this.dragged = dragged;
        createSingleElement(specReader, (Element) targetCl.getType(), targetPackage, observers);
        return drawableItems.keySet();
    }
    
    @Override
    public Map<Object, ?> getGeneratedElements() {
        return drawableItems;
    }
    
}
