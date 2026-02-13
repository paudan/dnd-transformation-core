package org.ktu.transformations.elements;

/**
 * Find existing connecting Element, according to its uniqueness definition
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), 
 * Center of Information Systems Design Technologies, Kaunas University of Technology, 2014-2015
 * @param <Element>   Actual implementation type of UML Element
 */
public interface ConnectingElementFinder<Element> {

    /**
     * Find existing Element
     * @return The element that has been found
     */
    public abstract Element find();

    /**
     * Return the UML element which was searched
     * @return	The element which was searched; {@code null} if no such element has been found
     */
    public abstract Element getSearchedElement();

    /**
     * Return the name of connecting element in the transformation pattern 
     * @return	String representing the name
     */
    public abstract String getConnectingMapName();

    /**
     * Return the root UML element (e.g., a Package, Model, etc.), where the search is performed
     * @return  The Element where the search is performed	
     */
    public abstract Element getRootElement();

}
