package org.ktu.transformations.elements;

import org.ktu.transformations.mappers.ElementMapper;
import org.ktu.transformations.transforms.Transformation;

/**
 * Generic search of connecting elements
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design 
 * Technologies, Kaunas University of Technology, 2015
 * @param <Element>     Actual implementation type representing UML Element
 */
public class SimpleConnectingElementFinder<Element> implements ConnectingElementFinder<Element> {

    protected Element rootElement;
    protected String connMapName;
    protected Element connectingEl;
    protected Transformation<Element, ?> factory;

    /**
     * Initialize finder object
     * @param rootElement	The root Element (e.g., a Package, Model, etc.), where the search is performed
     * @param connMapName	The name of the element in transformation pattern
     * @param connectingEl	The Element which was searched
     * @param factory           The {@link Transformation} object which uses this maker for element generation
     */
    public SimpleConnectingElementFinder(Element rootElement, String connMapName, Element connectingEl, Transformation<Element, ?> factory) {
        this.rootElement = rootElement;
        this.connMapName = connMapName;
        this.connectingEl = connectingEl;
        this.factory = factory;
    }

    /**
     * Find existing Element
     * @see ConnectingElementFinder#find()
     */
    @Override
    public Element find() {
        ElementMapper<Element, ?, ?> mapper = factory.getElementMapper();
        String name = mapper.getElementName(connectingEl);
        Class<?> base = mapper.getBaseClass(connMapName);
        if (base != null && name != null)
            return factory.getElementSearch().find(rootElement, base, name);
        return null;
    }

    /** @see ConnectingElementFinder#getRootElement() */
    @Override
    public Element getRootElement() {
        return rootElement;
    }

    /** @see ConnectingElementFinder#getConnectingMapName() */
    @Override
    public String getConnectingMapName() {
        return connMapName;
    }

    /** @see ConnectingElementFinder#getSearchedElement() */
    @Override
    public Element getSearchedElement() {
        return connectingEl;
    }

}
