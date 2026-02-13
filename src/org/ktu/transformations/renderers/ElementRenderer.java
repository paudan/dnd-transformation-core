package org.ktu.transformations.renderers;

import java.awt.Point;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A factory class which contains the functionality to create and render representations of actual Elements
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, 
 * Kaunas University of Technology, 2014-2015
 * @param <Element>                  Type, corresponding to actual UML Element implementation
 * @param <ElementPresentation>      Type, corresponding to actual UML element presentation implementation
 */
public interface ElementRenderer<Element, ElementPresentation> {

    /**
     * Creates element presentations for a tuple of Elements, consisting of container-type of connecting element and elements, contained by this element
     * @param mainel	 The main (connecting) Element
     * @param properties The {@linkplain Map} describing the structure of Elements which must be represented together with the connecting element. The keys of
     *                   {@code properties} define the name of the property (corresponding to MagicDraw implementation of UML metamodel),
     *                   and the value is {@linkplain Set} of Elements, which are set as these properties
     * @param location	 Actual location on the {@code parent} where the generated PresentationElement will be placed on
     * @param parent	 The PresentationElement (e.g., a DiagramPresentationElement or other) which the resulting PresentationElement will be placed on
     * @return	The {@linkplain List} of generated PresentationElement
     */
    public List<ElementPresentation> renderContainedElements(Element mainel, Map<String, Set<Element>> properties, Point location, ElementPresentation parent);

    /**
     * Creates element presentations for a tuple of Elements, consisting of relationship-type of connecting element and its properties
     * @param mainel        The main (connecting) Element
     * @param properties    The set of Elements which are connected by {@code mainel}
     * @param location      Actual location on the {@code parent} where the generated PresentationElement will be placed on
     * @param parent	    The PresentationElement (e.g., a DiagramPresentationElement or other) which the resulting PresentationElement will be placed on
     * @return	The {@linkplain List} of generated PresentationElement
     */
    public List<ElementPresentation> renderRelatedElements(Element mainel, List<Element> properties, Point location, ElementPresentation parent);

    /**
     * Creates a generic element presentation for a given Element
     * @param element	 Element which PresentationElement must be created for
     * @param location	Actual location on the {@code parent} where the generated PresentationElement will be placed on
     * @param parent	  The PresentationElement (e.g., a DiagramPresentationElement or other) which the resulting PresentationElement will be placed on
     * @return	The generated PresentationElement
     */
    public ElementPresentation renderSingleElement(Element element, Point location, ElementPresentation parent);

    /**
     * Creates a element presentation for a given single swimlane element
     * @param element	Element which PresentationElement must be created for
     * @param location	Actual location on the {@code parent} where the generated PresentationElement will be placed on
     * @param parent	The PresentationElement (e.g., a DiagramPresentationElement or other) which the resulting PresentationElement will be placed on
     * @return      The generated element presentation
     */
    public ElementPresentation renderSingleSwimlane(Element element, Point location, ElementPresentation parent);

    /**
     * Creates a element presentation for a swimlane element, together with Element contained by this element
     * @param drawableItems	The items which presentations must be created for. The structure is defined as a {@linkplain Map}, 
     * where keys represent swimlane elements, and values are of type {@linkplain Map}, with keys of defining the name of the property 
     * (corresponding to UML metamodel), and the value is {@linkplain Set} of UML Elements, which are set as these properties
     * @param checkUnique       Indicates if check for existing swimlane presentation elements should also be performed
     * @param location          Actual location on the {@code parent} where the generated PresentationElement will be placed on
     * @param parent            The presentation of UML element (UML diagram presentation, etc.) which will contain the resulting presentation
     * @return	The {@link List} of generated element presentation
     */
    public List<ElementPresentation> renderSwimlane(Map<Object, Map<String, Set<Element>>> drawableItems, 
            boolean checkUnique, Point location, ElementPresentation parent);
    
}
