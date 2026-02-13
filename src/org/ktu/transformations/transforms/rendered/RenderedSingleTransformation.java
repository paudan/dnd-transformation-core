package org.ktu.transformations.transforms.rendered;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.ktu.transformations.elements.ElementGenerationException;
import org.ktu.transformations.notifiers.NotificationObserver;
import org.ktu.transformations.parsers.SpecificationReader;
import org.ktu.transformations.transforms.AbstractSingleTransformation;

/**
 * Abstract class, implementing generic transformation with rendering functionality, when single element is generated
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, 
 * Kaunas University of Technology, 2015
 * @param <Element>         Type, corresponding to actual UML Element implementation
 * @param <Stereotype>      Type, corresponding to actual UML Stereotype implementation
 * @param <Presentation>    Type, corresponding to actual element presentation implementation
 */
abstract public class RenderedSingleTransformation<Element, Stereotype, Presentation> 
    extends AbstractRenderedTransformation<Element, Stereotype, Presentation> {
    
    protected Element dragged;

    public RenderedSingleTransformation(AbstractSingleTransformation<Element, Stereotype> transform) {
        super(transform);
    }

    @Override
    protected List<Presentation> renderItems(Map<Object, ?> drawable, Presentation elementOver, Point location) {
        List<Presentation> rendered = new ArrayList<>();
        for (Object newel: drawable.keySet())
            rendered.add(getElementRenderer().renderSingleElement((Element) newel, location, elementOver));   
        return rendered;
    }
    
    /**
     * Create single element 
     * @param specReader    SpecificationReader object for parsing relevant M2M transformation specification 
     * @param targetCl      UML classifier, representing the target element
     * @param targetPackage UML Element (Package, Model, etc.) which will be the owner of the generated element
     * @param elementOver   The element that the source element was dragged on (in case of drag and drop transformation)  
     * @param location      The location in the target diagram where resulting presentation of the element will be put 
     * @param selected      The element which was selected or dragged for transformation initialization. In this transformation, it is also the source element
     * @param observers     {@link NotificationObserver} elements which observe the performed generation procedures
     */
    @SuppressWarnings("unchecked")
    public void createSingleElement(SpecificationReader specReader, Element targetCl, 
            Element targetPackage, Presentation elementOver, Point location, Element selected, Collection<NotificationObserver> observers) {
        Element newel = (Element) ((AbstractSingleTransformation)transform).createSingleElement(specReader, targetCl, targetPackage, observers);
        getElementRenderer().renderSingleElement(newel, location, elementOver);
    }
    
    /**
     * Create single element and return the result as set, which consists of generated items
     * @param specReader    SpecificationReader object for parsing relevant M2M transformation specification 
     * @param targetCl      UML classifier, representing the target element
     * @param targetPackage UML Element (Package, Model, etc.) which will be the owner of the generated element
     * @param owner         The element that the source element was dragged on (in case of drag and drop transformation)  
     * @param location      The location in the target diagram where resulting presentation of the element will be put 
     * @param selected      The element which was selected or dragged for transformation initialization. In this transformation, it is also the source element
     * @param observers     {@link NotificationObserver} elements which observe the performed generation procedures
     * @return              The {@link Set} of generated UML elements (it usually consists of single element)
     * @throws ElementGenerationException  There was an error while generating the target element
     */
    public Set<Object> createSingleElement(SpecificationReader specReader, Element targetCl, Element targetPackage, 
            Element selected, Presentation owner, Point location, Collection<NotificationObserver> observers) 
            throws ElementGenerationException {
        Set objects = ((AbstractSingleTransformation)transform).createSingleElement(specReader, targetCl, targetPackage, selected, observers);
        Map<Object, Object> drawable = new HashMap<>();
        for (Object obj: objects)
            drawable.put(obj, null);
        renderItems(drawable, owner, location);
        return objects;
    }
    
}
