package org.ktu.transformations.transforms;

import org.ktu.transformations.elements.AbstractElementProducer;
import org.ktu.transformations.helpers.AbstractPropertyManager;
import org.ktu.transformations.helpers.ElementSearch;
import org.ktu.transformations.mappers.ElementMapper;

/**
 * Transformation factory interface, which must be implemented by classes that implement particular UML M2M transformations
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, Kaunas University of Technology, 2014-2015
 * @param <Element>         Type, corresponding to actual UML Element implementation
 * @param <Stereotype>      Type, corresponding to actual UML Stereotype implementation
 */
public interface Transformation<Element, Stereotype> {
    
    /**
     * Returns {@link ElementMapper} which performs actual mapping to element properties in the implementations 
     * @return An instance of {@link ElementMapper}
     */
    public ElementMapper<Element, ?, Stereotype> getElementMapper();
    
    /**
     * Returns {@link AbstractPropertyManager} object implementing property (UML feature) management functionality
     * @return {@link AbstractPropertyManager} object
     */
    public AbstractPropertyManager<Element, Stereotype, ?> getPropertyManager();
    
    /**
     * Returns {@link AbstractElementProducer}, implementing element generation for implementation in particular tool 
     * @return {@link AbstractElementProducer} object
     */
    public AbstractElementProducer<Element, Stereotype> getElementProducer();
    
    /**
     * Returns {@link ElementSearch}, implementing search of different elements by their class, type, stereotype, etc.
     * @return {@link ElementSearch} object
     */
    public ElementSearch<Element, Stereotype> getElementSearch();

}
