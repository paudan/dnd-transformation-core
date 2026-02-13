package org.ktu.transformations.helpers;

import java.util.Collection;

/**
 * An interface which defines mandatory element search operations, which are used in M2M transformations. 
 * All of these methods must be properly implemented in order to execute these transformations
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems 
 * Design Technologies, Kaunas University of Technology, 2014-2015
 * @param <Element>      Type, corresponding to actual UML Element implementation
 * @param <Stereotype>   Type, corresponding to actual UML Stereotype implementation
 */
public interface ElementSearch<Element, Stereotype> {
    
    /**
     * Simple search to find a UML element by its class and name
     * @param <E>       Type, corresponding to actual UML Element implementation
     * @param root      The UML element (Package, Model, etc.) where the search is performed
     * @param clazz     The actual class of the element
     * @param name      The name of the element
     * @return          The found UML element, or {@code null} if no such element has been found
     */
    public <E extends Element> E find(Element root, Class<?> clazz, String name);

    /**
     * Find an ActivityPartition element in the Model of an active project
     * @param name	The name of the element
     * @return	The Element if one has been found, or {@code null} if no Element has been found according to given parameters
     */
    public Element findActivityPartitionElement(String name);

    /**
     * Search for an ActivityPartition element in the Model of an active project
     * @param name	        The name of the element
     * @param stereotypeName	The name of stereotype
     * @param byName		If the value is set to {@code true}, then search will be performed according to the name of the element,
     * otherwise the {@code represents} property will be used
     * @return	The Element if one has been found, or {@code null} if no Element has been found according to given parameters
     */
    public Element findActivityPartitionElement(String name, String stereotypeName, boolean byName);

    /**
     * Search for and ActivityPartition element
     * @param owner		The root Element where search is performed
     * @param elementName	The name of the element
     * @param stName		The name of stereotype
     * @param byName		If the value is set to {@code true}, then search will be performed according to the name of the element,
     *                    otherwise the {@code represents} property will be used
     * @return	The Element if one has been found, or {@code null} if no Element has been found according to given parameters
     */
    public Element findActivityPartitionElement(Element owner, String elementName, String stName, boolean byName);

    /**
     * Search for an element by its class, name and stereotype name
     * @param <E>               Type, corresponding to actual UML Element implementation
     * @param owner		The root element where search is performed
     * @param base		Base class of the searched element
     * @param stereotype	Stereotype which is applied to the Element
     * @param elementName	Element name
     * @return	The element instance if one has been found, or {@code null} if no element has been found according to given parameters
     */
    public <E extends Element> E findElement(Element owner, Class<?> base, Stereotype stereotype, String elementName);

    /**
     * Search recursively for an element by its class, name and stereotype name
     * @param <E>               Type, corresponding to actual UML Element implementation
     * @param owner		The root Element where search is performed
     * @param base		Base class of the searched UML element
     * @param stereotype	Stereotype which is applied to the element
     * @param elementName	Element name
     * @param recursively	Indicates whether search should be performed recursively by searching subpackages or child elements
     * @return	The Element if one has been found, or {@code null} if no Element has been found according to given parameters
     */
    public <E extends Element> E findElementRecursively(Element owner, Class<?> base, Stereotype stereotype, String elementName, boolean recursively);

    /**
     * Search for a UML Package element with the given name
     * @param packageName	The name of the UML package
     * @return	The UML Package element which was found, or {@code null} if no Package element has been found
     */
    public Element findPackageByName(String packageName);

    /**
     * Search for an Relationship element
     * @param <E>               Type, corresponding to actual UML Element implementation
     * @param owner		The root Element where search is performed
     * @param base		Base class of the searched Element
     * @param stereotype	Stereotype which is applied to the relationship element
     * @param elementName	Element name
     * @param prop1Name		The name of the first property (corresponding to UML metamodel), which contains {@code prop1}
     * @param prop1		The Element representing the object of the first property
     * @param prop2Name		The name of the second property (corresponding to UML metamodel), which contains {@code prop2}
     * @param prop2		The Element representing the object of the second property
     * @param checkNull		Indicates whether {@code null} values of element names should be taken into account (e.g., elements,
     * such as Dependency, Generalization or even Association often do not have names in the model)
     * @return	The Element if one has been found, or {@code null} if no Element has been found according to given parameters
     */
   public <E extends Element> E findRelationship(Element owner, Class<?> base, Stereotype stereotype, String elementName, String prop1Name, 
           Element prop1, String prop2Name, Element prop2, boolean checkNull);

    /**
     * Find given related elements, find UML Relationship
     * @param <E>           Type, corresponding to actual UML Element implementation
     * @param relClass      The class type of the relationship
     * @param first         The first Element at the end of the Relationship
     * @param second        The second Element at the end of the Relationship
     * @param name          The name of the Relationship element
     * @param stereotype    The stereotype which the searched Association element is stereotyped with
     * @return	The UML Relationship element which was found, or {@code null} if no Relationship element has been found,
     * or {@code relClass} does not represent a Relationship class type
     */
    public <E extends Element> E findRelationship(Class<?> relClass, Element first, Element second, String name, Stereotype stereotype);

    /**
     * Search for Class elements with particular stereotype in the Model of an active project
     * @param stereotype	The Stereotype object
     * @return	A {@link Collection} of found Elements
     */
    public Collection<? extends Element> findStereotypedElements(Stereotype stereotype);
    
    /**
     * Perform search for owned elements of particular class type. If needed, it also checks parent and adds it to list.
     * Note that it does not check application of particular stereotypes for these search; other methods must be used in order to perform
     * filtering by stereotype as well
     * @param root          The root element where the search is performed
     * @param types         Element class type list    
     * @param checkParent   Whether the root element should also be checked and included into search results
     * @return  The {@link Collection} of found elements
     */
    public Collection<? extends Element> findChildren(Element root, Class<?> [] types, boolean checkParent);
    
    /**
     * Recursively collect all elements of given specific type (not super class or interface) from the given parent recursively
     * @param root          The root element where the search is performed
     * @param types         An array of Element's class types (Class.class, Package.class, Property.class and etc.)
     * @param checkParent   Whether the root element should also be checked and included into search results
     * @return The {@link Collection} of found elements
     */
    public Collection<? extends Element> getElementsOfType(Element root, Class<?> [] types, boolean checkParent);
    
}
