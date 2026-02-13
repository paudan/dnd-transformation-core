package org.ktu.transformations.mappers;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.ktu.transformations.parsers.ConnectableEntity;
import org.ktu.transformations.parsers.PatternConfiguration;

/**
 * Interface which defines functional mappings between UML objects and their implementations in particular environments. It maps operations or properties,  
 * performed by transformation engine, to corresponding operations or properties in particular implementations
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of
 * Information Systems Design Technologies, Kaunas University of Technology, 2014-2015
 * @param <Element>         Type, corresponding to actual UML Element implementation
 * @param <ConnectableElement>  Type, corresponding to actual UML Connectable element implementation
 * @param <Stereotype>      Type, corresponding to actual UML Stereotype implementation
 */
public interface ElementMapper<Element, ConnectableElement, Stereotype> {
    
    /**
     * Get the name of an Element
     * @param element	The Element, whose name must be obtained
     * @return	The obtained name
     */
    public String getElementName(Element element);
    
    /**
     * Return a "clean" element name, with eliminated newline elements and additional spaces
     * @param name  The original {@link String}
     * @return	The "clean" String
     */
    public String getProperName(String name);
    
    /**
     * Return a "clean" UML Named Element name, with eliminated newline elements and additional spaces
     * @param element  UML Named Element which is queried
     * @return         The "clean" String
     */
    public String getProperName(Object element);
    
    /**
     * Checks if particular Element can be mapped to particular {@link ConnectableEntity} in the transformation pattern
     * @param element	The actual Element that is queried
     * @param entity	The {@link ConnectableEntity} entity in the transformation pattern which is checked
     * @return	{@code true} if {@code element} maps to {@code source}; {@code false} otherwise
     */
    public boolean mapsToEntity(Element element, ConnectableEntity entity);
    
    /**
     * Get the name of an Element
     * @param element	The Element, whose name must be obtained
     * @return	The obtained name
     */
    public String getElementName2(Element element);

    /**
     * Get the name of an UML element (particularly UML Connectable Element)
     * @param element	The UML element, whose name must be obtained
     * @return	The obtained name
     */
    public String getElementName3(Element element);
    
    /**
     * Returns the human readable name of an element. It is important to note, that this name may consist 
     * either of a type name and actual name combination, or of stereotype name and actual element name combination. 
     * This method should be implemented properly, as it is used for property existence checking
     * @param element   The UML element, whose human-readable name must be obtained
     * @return  {@link String} representing the obtained name
     */
    public String getHumanName(Element element);

    /**
     * Get the printable name of a ConnectableElement
     * @param element	The ConnectableElement, whose name must be obtained
     * @return	The obtained name of the element
     */
    public String getPrintableElementName(ConnectableElement element);

    /**
     * Checks, if two Elements have identical types
     * @param el1	The first Element
     * @param el2	The second Element
     * @return	{@code true} if the elements match by type, {@code false} otherwise
     */
    public boolean haveIdenticalTypes(Element el1, Element el2);

    /**
     * Get base class by the given string, representing class type
     * @param name	The string representing the class type
     * @return	The class which was identified
     */
    public Class<?> getBaseClass(String name);

    /**
     * Get base class of a mapping element
     * @param source	The mapping element (ConnectableElement or Type)
     * @return	The class which was identified
     */
    public Class<?> getBaseClass(Element source);

    /**
     * Get a set of base class names for a ConnectableElement
     * @param source	The ConnectableElement
     * @return	A {@linkplain Set} of names, representing names of base classes
     */
    public Set<String> getBaseClassNames(ConnectableElement source);

    /**
     * Get a set of base classes for a ConnectableElement
     * @param source	The ConnectableElement
     * @return	A {@linkplain Set} of base classes
     */
    public Set<Class> getBaseClasses(ConnectableElement source);

    /**
     * Identifies, if an element, represented by class, is a relating type of classifier
     * @param elementClass	The actual class of the classifier
     * @return {@code true} if the element is a relating-type of classifier, {@code false} otherwise
     */
    public boolean isRelatingClassifier(Class<?> elementClass);

    /**
     * Identify if an element, represented by class, is an activity (swimlane) type of classifier
     * @param elementClass	The actual class of the classifier
     * @return {@code true} if the element is a activity-type of classifier, {@code false} otherwise
     */
    public boolean isActivityClassifier(Class<?> elementClass);

    /**
     * Check if particular transformation element type element represents a stereotype, and returns an instance of Stereotype, if one is represented
     * @param el	The element that is queried
     * @return	An instance of Stereotype, or {@code null}, if the element does not represent a Stereotype
     */
    public Stereotype getRepresentedStereotype(Element el);
    
    /**
     * Return a UML Stereotype by its name and Profile object
     * @param name      The name of the stereotype
     * @param profile   The UML Profile element
     * @return  An instance of Stereotype, or {@code null}, if the element does not represent a Stereotype
     */
    public Stereotype getStereotype(String name, Element profile);
    
    /**
     * Return instance of UML Stereotype, representing dragged (or selected) element. 
     * The name of the stereotype should match {@link PatternConfiguration#getElementInFocusName() }
     * @return  An instance of Stereotype, or {@code null}, if the element does not represent a Stereotype 
     */
    public Stereotype getDraggedElementStereotype();

    /** 
     * Get Element, representing UML Type of the given element
     * @param element   The Element which is queried
     * @return          Element representing the type of {@code element}
     */
    public Element getTypeElement(Element element);
    
    /**
     * Check if the given Object is a valid Element
     * @param obj   The Object that is queried
     * @return {@code true} if {@code element} is an Element, {@code false} otherwise
     */
    public boolean isElement(Object obj);
    
    /**
     * Check if the given Element is also UML Named Element
     * @param element   The Element that is queried
     * @return {@code true} if {@code element} is also UML Named Element, {@code false} otherwise
     */
    public boolean isNamedElement(Element element);
    
    /**
     * Check if the given Element is also UML Typed Element
     * @param element   The Element that is queried
     * @return {@code true} if {@code element} is also UML Typed Element, {@code false} otherwise
     */
    public boolean isTypedElement(Element element);
    
    /**
     * Check if the given Element is also UML Classifier
     * @param element   The Element that is queried
     * @return {@code true} if {@code element} is also UML Classifier, {@code false} otherwise
     */
    public boolean isClassifier(Element element);
    
    /**
     * Check if the given Element is also UML Diagram
     * @param element   The Element that is queried
     * @return {@code true} if {@code element} is also UML Diagram, {@code false} otherwise
     */
    public boolean isDiagram(Element element);
    
    /**
     * Check if the given Element is also UML abstract Classifier
     * @param element   The Element that is queried
     * @return {@code true} if {@code element} is also UML abstract Classifier, {@code false} otherwise
     */
    public boolean isAbstractClassifier(Element element);
    
    /**
     * Check if the given Element is also UML Association
     * @param element   The Element that is queried
     * @return {@code true} if {@code element} is also UML Association, {@code false} otherwise
     */
    public boolean isAssociation(Element element);
    
    /**
     * Check if the given class corresponds to the class of UML Relationship
     * @param elementClass   The Class that is queried
     * @return {@code true} if {@code elementClass} corresponds to the class of UML Relationship, {@code false} otherwise
     */
    public boolean isRelationship(Class<?> elementClass);
    
    /**
     * Check if the given Object is a presentation of UML Element
     * @param obj   The Object that is queried
     * @return {@code true} if {@code element} is a presentation, {@code false} otherwise
     */
    public boolean isElementPresentation(Object obj);
    
    /**
     * Return the class type for the given element
     * @param element   The Element that is queried
     * @return   The class type as {@link Class}        
     */
    public Class<?> getClassType(Element element);
    
    /**
     * Return the class type by the given class name
     * @param className   The name of the Class
     * @return   The class type as {@link Class}        
     */
    public Class<?> getClassType(String className);
    
    /**
     * Check if the given element has one or more stereotypes applied
     * @param element   The Element that is queried
     * @return {@code true} if {@code element} has at least one UML Stereotype, {@code false} otherwise
     */
    public boolean hasStereotype(Element element);
    
    /**
     * Check if the given stereotype is applied to the given UML element
     * @param element       The Element that is queried
     * @param stereotype    The Stereotype which should be applied to {@code element} 
     * @return {@code true} if {@code stereotype} is applied to {@code element}, {@code false} otherwise
     */
    public boolean hasStereotype(Element element, Stereotype stereotype);
    
    /**
     * Return UML stereotypes applied for particular UML element
     * @param element   The Element that is queried
     * @return {@link List}
     */
    public List<Stereotype> getStereotypes(Element element);
    
    /**
     * Return the name of UML Stereotype object
     * @param stereotype    The Stereotype that is queried
     * @return  {@link String} representing the name of the Stereotype
     */
    public String getStereotypeName(Stereotype stereotype);
    
    /**
     * Set particular property value for Stereotype object 
     * @param element      The UML element which is updated  
     * @param stereotype   UML Stereotype object which property must be updated 
     * @param propName     The name of the {@code stereotype} property (tag name) which must obtain the new value 
     * @param propValue    The value for this property to be set 
     * @return  Updated {@code element}
     */
    public Element setStereotypePropertyValue(Element element, Stereotype stereotype, String propName, Object propValue);
    
    /**
     * Get particular property value for Stereotype object 
     * @param element      The UML element which is updated  
     * @param stereotype   UML Stereotype object which property must be updated 
     * @param propName     The name of the {@code stereotype} property (tag name) which must obtain the new value 
     * @return  The value for the tag {@code propName}
     */
    public Object getStereotypePropertyValue(Element element, Stereotype stereotype, String propName);
    
    /**
     * Get base classes of the stereotype
     * @param stereotype    The Stereotype which is queried
     * @return      {@link List} of {@link Class} objects
     */
    public List<Class> getBaseClassesAsClasses(Stereotype stereotype);
    
    /**
     * Check if it is possible to apply the given Stereotype for particular UML element
     * @param element       The Element object which is checked
     * @param stereotype    The Stereotype object which is checked
     * @return    {@code true} if {@code stereotype} can be applied to {@code element}, {@code false} otherwise          
     */
    public boolean canApplyStereotype(Element element, Stereotype stereotype);
    
    /**
     * Apply the given Stereotype for the given UML element
     * @param element       The Element object which is updated
     * @param stereotype    The Stereotype object which is applied
     * @return    Update {@code element}  
     */
    public Element addStereotype(Element element, Stereotype stereotype);
    
    /**
     * Check if an Element is also a Stereotype
     * @param element    The Element which is queried
     * @return   {@code true} if {@code element} is also a UML Stereotype, {@code false} otherwise   
     */
    public boolean isStereotype(Element element);
    
    /**
     * Get the UML Profile of the given Stereotype
     * @param stereotype    The Stereotype which is queried 
     * @return   Element which is UML Profile for {@code sterotype}   
     */
    public Element getStereotypeProfile(Stereotype stereotype);
    
    /**
     * Get Stereotype by its name. Note that it is possible that multiple Stereotypes exist in different profiles; 
     * in this case, the first found is returned. Use {@link #getStereotype(String, Object) } in order to get more specific Stereotype object
     * @param name  The name of the Stereotype
     * @return      The Stereotype object which has been found, or {@code null} if no Stereotype with name {@code name} has been found 
     */
    public Stereotype getStereotypeByName(String name);
    
    /**
     * Check if the type name of the given Element is the same as the given string
     * @param element   The Element that is queried
     * @param typeName  The string that is checked
     * @return  {@code true} if {@code element} is a Typed Element and has the same type name as {@code typeName}; {@code false} otherwise
     */
    public boolean hasTypeName(Element element, String typeName);
    
    /**
     * Return the qualified name of the Element
     * @param element   The Element that is queried
     * @return  String representing the qualified name of {@code element}
     */
    public String getQualifiedName(Element element);
    
    /**
     * Return the name of the Type of the given Element
     * @param element   The Element that is queried
     * @return  String representing the type name of {@code element}
     */
    public String getTypeName(Element element);
    
    /**
     * Check is the given Element is a Property
     * @param element   The Element that is queried
     * @return  {@code true} if {@code element} is also a UML Property, {@code false} otherwise 
     */
    public boolean isProperty(Element element);
    
    /**
     * Set the name of an Element
     * @param element   The Element that is updated
     * @param name      The new name of the Element
     * @return          Updated {@code element}
     */
    public Element setName(Element element, String name);
    
    /**
     * Return the actual name of the given Element 
     * @param element   The Element that is queried
     * @return  String representing actual name of the {@code element} 
     * (e.g., for the Element {@code Actor manager} a string {@code manager} is returned)
     */
    public String getActualName(Element element);
    
    /**
     * Return the owning Element of the given Element
     * @param element   The Element that is queried
     * @return  An Element that is the owner of {@code eleemnt}
     */
    public Element getOwner(Element element);
    
    /**
     * Check if the name of the Element is not an empty string 
     * @param element   The Element that is queried
     * @return  String representing the name of the {@code element}, or {@code null}, if the name is empty or is not set
     */
    public boolean hasName(Element element);
    
    /***
     * Return ID of the Element
     * @param element   The Element that is queried
     * @return  String representing ID of {@code element}
     */
    public String getID(Element element);
    
    /**
     * Return the root Model or Package of the active project
     * @return  Element which is a UML Model
     */
    public Element getProjectModel();
    
    /**
     * Return the Model which contains the given Element
     * @param element   The Element that is queried
     * @return  Element which is the Model that contains the given Element
     */
    public Element getModelByElement(Element element);
    
    /**
     * Return the end types of the given Association element
     * @param association   UML Association element which is queried
     * @return  {@link Collection} of end types, represented as Elements, if {@code association} 
     * is not {@code null} and is a valid UML Association; an empty {@code Collection} otherwise
     */
    public Collection<Element> getAssociationEndTypes(Element association);
    
    /**
     * Get the class type of ActivityPartition element
     * @return Class type as {@link Class} 
     */
    public Class<?> getActivityPartitionClass();
    
    /**
     * Return Element, given its presentation element 
     * @param presElement   Object representing the presentation element for UML Element
     * @return   An Element, if {@code presElem} is a valid presentation element and an element can be obtained from it; {@code null} otherwise
     */
    public Element getElementFromPresentation(Object presElement);
    
    /**
     * Checks if particular Element can be mapped to particular {@link ConnectableEntity} in the transformation pattern
     * @param element	The actual Element that is checked
     * @param entity	The {@link ConnectableEntity} entity in the transformation pattern which is checked
     * @return	{@code true} if {@code element} maps to {@code source}; {@code false} otherwise
     */
    public boolean mapsToElement(Element element, ConnectableEntity entity);
}
