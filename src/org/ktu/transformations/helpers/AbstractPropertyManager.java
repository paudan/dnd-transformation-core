package org.ktu.transformations.helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.ktu.transformations.mappers.ElementMapper;
import org.ktu.transformations.parsers.ConnectableEntity;
import org.ktu.transformations.parsers.PropertyStack;


/**
 * Implements abstract selection, creation and management of UML element properties. It must be subclassed for each implementation
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information 
 * Systems Design Technologies, Kaunas University of Technology, 2015
 * @param <Element>      Type, corresponding to actual UML Element implementation
 * @param <Stereotype>   Type, corresponding to actual UML Stereotype implementation
 * @param <T>            Type, corresponding to actual UML Typed Element implementation
 */
@SuppressWarnings({"rawtypes"})
abstract public class AbstractPropertyManager<Element, Stereotype, T> {
    
    /** {@link ElementMapper} implementation which maps to functionality in actual implementations */
    protected ElementMapper<Element, ?, Stereotype> mapper;
    
    protected AbstractPropertyManager(ElementMapper<Element, ?, Stereotype> mapper) {
        this.mapper = mapper;
    }

    /**
     * Returns a list of UML elements, which are owned as a property of an Element
     * @param element	The UML element which contains these properties
     * @param propMap	{@link PropertyStack} structure, representing hierarchical property mapping
     * @return	The {@link List} of selected Elements, which represent particular property
     */
    public List<Object> getPropertyList(Element element, PropertyStack propMap) {
        List<Object> propelemList = new ArrayList<>();
        if (element == null)
            return propelemList;
        ConnectableEntity prop = propMap.lowermostProperty();
        Object res = getPropertyObject(element, propMap, prop.getName3());
        if (res == null)
            return propelemList;
        if (res instanceof List && !((List) res).isEmpty()) {
            if (mapper.isElement(((List) res).get(0))) {
                Set<Class> classNames = prop.getBaseClasses();
                if (classNames.isEmpty())
                    return propelemList;
                for (Element o : (Collection<Element>) res)
                    for (Class clname : classNames)
                        if (mapper.getClassType(o).equals(clname))
                            propelemList.add(o);
            } else
                for (Object o : (List) res)
                    propelemList.add(o);
        } else
            propelemList.add(res);
        return propelemList;
    }

    private Object getStructuralFeatureValue(Element element, String propName) {
        boolean hasFeat = hasFeature(element, propName);
        if (hasFeat)
            return getFeatureValue(element, propName);
        if (!hasFeat && mapper.hasStereotype(element)) {
            List<Stereotype> stList = mapper.getStereotypes(element);
            for (Stereotype st : stList) {
                Object res = mapper.getStereotypePropertyValue(element, st, propName);
                if (res != null)
                    return res;
            }
        }
        return null;
    }

    /**
     * Returns an Object, representing particular property of an Element; it can be a {@link List} of elements or a single element
     * @param element	 The Element which contains these properties
     * @param propMap	 {@link PropertyStack} structure, representing property mapping with an object
     * @param typeName	The name of class type for particular property; only elements of this type are filtered if it can contain more than one type of elements
     * @return	An {@link Object}, representing particular property, or {@code null} if this property did not contain any values
     */
    public Object getPropertyObject(Element element, PropertyStack propMap, String typeName) {
        //Iterate through property tree and derive candidate elements for generation
        Object propIter = null;
        Object res = null;
        for (int i = 1; i < propMap.size(); i++) {
            String propMapName = propMap.get(i).getName3();
            if (i == 1)
                res = getStructuralFeatureValue(element, propMapName);
            else {
                res = null;
                if (propIter != null && mapper.isElement(propIter))
                    res = getStructuralFeatureValue((Element) propIter, propMapName);
            }
            if (res == null)
                continue;
            propIter = null;
            if (res instanceof List && typeName != null && !((List) res).isEmpty()) {
                for (Element el : (List<Element>) res) {
                    if (mapper.isTypedElement(el) && mapper.getTypeElement(el) != null && mapper.hasTypeName(el, typeName))
                        propIter = el;
                }
            } else
                propIter = res;
        }
        return res;
    }

    /**
     * Returns a list of objects, representing particular property of an UML Element. It refines the results returned by
     * {@link #getPropertyObject(Object, PropertyStack, String) } and returns them as a list of objects
     * @param element	 The Element which contains these properties
     * @param propMap	 {@link PropertyStack} structure, representing property mapping with an object
     * @param typeName	The name of class type for particular property; only elements of this type are filtered if it can contain more than one type of elements
     * @return    {@link List} of objects, representing particular property
     */
    public List<Object> getPropertyList(Element element, PropertyStack propMap, String typeName) {
        if (element == null)
            return null;
        Object res = getPropertyObject(element, propMap, typeName);
        List<Object> results = new ArrayList<>();
        if (res == null)
            return results;
        if (res instanceof List && typeName != null && !((List) res).isEmpty()) {
            for (Element el : (List<Element>) res)
                if (isCorresponding(el, typeName))
                    results.add(el);
        } else if (mapper.isElement(res) && typeName != null) {
            if (isCorresponding((Element) res, typeName))
                results.add(res);
        } else if (res instanceof String)
            results.add(res);
        return results;
    }

    private boolean isCorresponding(Element el, String typeName) {
        if (el == null)
            return false;
        if (mapper.hasStereotype(el)) {
            List<Stereotype> stereotypes = mapper.getStereotypes(el);
            for (Stereotype st : stereotypes)
                if (typeName.compareTo(mapper.getStereotypeName(st)) == 0)
                    return true;
        } else if (typeName.compareTo(mapper.getClassType(el).getSimpleName()) == 0)
            return true;
        return false;
    }

    /**
     * Returns an Object, representing particular property of an UML element; it can be an {@link List} of elements or a single element
     * @param element	    The Element which contains these properties
     * @param propMap	    {@link PropertyStack} structure, representing property mapping with an object
     * @param propType      Element Type for particular property; only elements of this type (or its subtypes, depending on the value 
     * of {@code getSubtypes} parameter) are filtered if it can contain more than one type of elements
     * @param getSubtypes	If set to true, then subtypes of particular {@code type} will also be selected
     * @return	{@link Object}, representing particular property, or {@code null} if this property did not contain any values
     */
    public Object getPropertyObject(Element element, PropertyStack propMap, ConnectableEntity propType, boolean getSubtypes) {
        if (element == null || (propType != null && !mapper.isTypedElement((Element) propType.getConnectableObject())))
            return null;
        Object propIter = null;
        Object res = null;
        T type = (T) propType.getConnectableObject();
        if (!mapper.isElement(type) || !mapper.isTypedElement((Element)type))
            return null;
        Stereotype stereotype = mapper.getRepresentedStereotype((Element)type);
        String typename = mapper.getElementName3((Element)type);
        Class baseType = mapper.getBaseClass(mapper.getTypeElement((Element)type));
        for (int i = 1; i < propMap.size(); i++) {
            String propMapName = propMap.get(i).getName3();
            if (i == 1)
                res = getStructuralFeatureValue(element, propMapName);
            else {
                res = null;
                if (propIter != null && mapper.isElement(propIter))
                    res = getStructuralFeatureValue((Element) propIter, propMapName);
            }
            if (res == null)
                continue;
            propIter = null;
            if (res instanceof List && type != null && !((List) res).isEmpty()) {
                for (Element el : (List<Element>) res) {
                    Element elType = mapper.getTypeElement(el);
                    if (mapper.isTypedElement(el) && elType != null) {
                        Class baseEl = mapper.getBaseClass(elType);
                        boolean typeCond = getSubtypes ? baseEl.isAssignableFrom(baseType) : baseEl.equals(baseType);
                        if ((stereotype != null && mapper.hasTypeName(el, typename)) || (stereotype == null && typeCond))
                            propIter = el;
                    }
                }
            } else
                propIter = res;
        }
        return res;
    }

    /**
     * Returns a list of UML element objects, representing particular property of given UML Element. It refines the results returned by
     * {@link #getPropertyObject(Object, PropertyStack, String)} and returns them as a list of objects
     * @param element	    The Element which contains these properties
     * @param propMap	    {@link PropertyStack} structure, representing property mapping with an object
     * @param type          Element Type for particular property; only elements of this type (or its subtypes, depending on the value of {@code getSubtypes} parameter)
     *                    are filtered if it can contain more than one type of elements
     * @param getSubtypes	If set to true, then subtypes of particular {@code type} will also be selected
     * @return	{@link List} of objects, representing particular property
     */
    public List<Object> getPropertyList(Element element, PropertyStack propMap, ConnectableEntity type, boolean getSubtypes) {
        if (!mapper.isElement(element) && (type != null && 
                !(mapper.isElement(type.getConnectableObject()) || mapper.isTypedElement((Element)type.getConnectableObject()))))
            return null;
        Object res = getPropertyObject(element, propMap, type, getSubtypes);
        List<Object> results = new ArrayList<>();
        if (res == null)
            return results;
        if (res instanceof List && type != null && !((List) res).isEmpty()) {
            for (Element el : (List<Element>) res)
                if (isCorresponding(el, (T) type.getConnectableObject(), getSubtypes))
                    results.add(el);
        } else if (mapper.isElement(res) && type != null) {
            if (isCorresponding((Element)res, (T) type.getConnectableObject(), getSubtypes))
                results.add(res);
        } else if (res instanceof String)
            results.add(res);
        return results;
    }

    private boolean isCorresponding(Element el, T type, boolean getSubtypes) {
        if (!mapper.isElement(type))
            return false;
        Stereotype stereotype = mapper.getRepresentedStereotype((Element)type);
        Class<?> baseType = mapper.getBaseClass((Element)type);
        Class<?> classType = mapper.getClassType(el);
        if (stereotype != null && mapper.hasStereotype(el, stereotype))
            return true;
        else if (getSubtypes ? baseType.isAssignableFrom(classType) : classType.equals(baseType))
            return true;
        return false;
    }

    /**
     * Identifies if an Element has particular property value
     * @param element	The actual element which contains these properties
     * @param propMap	{@link PropertyStack} structure, representing property mapping with an object
     * @param typeName	The name of class type for particular property; only elements of this type are filtered if it can contain more than one type of elements
     * @param value	The value of the property. It can be an Element or its specialization
     * @return	{@code true} if Element has a corresponding property value, {@code false} otherwise
     */
    public boolean hasPropertyValue(Element element, PropertyStack propMap, String typeName, Element value) {
        if (value == null)
            return false;
        String humanName = mapper.getHumanName(value);
        if (humanName == null)
            return false;
        Object res = getPropertyObject(element, propMap, typeName);
        if (res == null || (res instanceof List && ((List) res).isEmpty()))
            return false;
        if (res instanceof List) {
            for (Element el : (List<Element>) res)
                if (mapper.getHumanName(el).compareTo(humanName) == 0)
                    return true;
        } else if (mapper.isElement(res) && mapper.getHumanName((Element)res).compareTo(humanName) == 0)
            return true;
        return false;
    }

    /**
     * Identifies if an element has particular property value
     * @param element	 The UML element which contains these properties
     * @param propName	 The name of the property
     * @param propValue	 The value of the property. It can be an Element or its specialization
     * @return	{@code true} if element has a corresponding property value, {@code false} otherwise
     */
    public boolean hasPropertyValue(Element element, String propName, Element propValue) {
        if (element == null || propValue == null)
            return false;
        Object res = getFeatureValue(element, propName);
        if (res == null || (res instanceof List && ((List) res).isEmpty()))
            return false;
        if (res instanceof List) {
            for (Element el : (List<Element>) res)
                if (mapper.isProperty(el) && mapper.isProperty(propValue) && 
                        mapper.getTypeElement(el).equals(mapper.getTypeElement(propValue)))
                    return true;
        } else if (mapper.isElement(res) && mapper.isProperty((Element)res) && mapper.isProperty(propValue) && 
                        mapper.getTypeElement((Element)res).equals(mapper.getTypeElement(propValue)))
            return true;
        return false;
    }

    /**
     * Set particular property of UML Element
     * @param element	 The UML element which is updated
     * @param propName	 The name of the property
     * @param propValue	The value which must be set for the property
     * @return Updated {@code element} object, complemented with given property
     */
    public Object setProperty(Element element, String propName, Object propValue) {
        if (propName == null || propValue == null)
            return element;
        boolean hasFeat = hasFeature(element, propName);
        if (!hasFeat && mapper.hasStereotype(element)) {
            List<Stereotype> stList = mapper.getStereotypes(element);
            for (Stereotype st : stList)
                element = mapper.setStereotypePropertyValue(element, st, propName, propValue);
            return element;
        } else if (!hasFeat)
            return element;
        return setPropertyValue(element, propName, propValue);
    }

    /**
     * Set the property which is at the lowest level in the property hierarchy of UML element (the element itself is viewed as the topmost property)
     * @param element	  The Element which is updated
     * @param propStack   The property mapping structure
     * @param propValue	  The value which must be set for the property
     * @return Updated {@code element} object, complemented with given property
     */
    public Object setProperty(Element element, PropertyStack propStack, Object propValue) {
        if (element == null)
            return null;
        if (propStack.size() == 1)
            return element;
        String propMapName = propStack.get(1).getName3();
        //Iterate through property tree and derive candidate elements for generation
        Element propIter = null;
        Object res = element;
        for (int i = 1; i < propStack.size() - 1; i++) {
            propMapName = propStack.get(i).getName3();
            if (i == 1)
                res = getFeatureValue(element, propMapName);
            else
                res = propIter != null ? getFeatureValue(propIter, propMapName) : null;
        }
        if (mapper.isElement(res))
            return setProperty((Element)res, propMapName, propValue);
        return element;
    }
    
    /**
     * Set property value directly, as a value of particular feature (i.e., without using any checks)
     * @param element	 The UML element which is updated
     * @param propName	 The name of the property
     * @param propValue	 The value which must be set for the property
     * @return  Updated {@code element} object, complemented with given property
     */
    public Element setPropertyValue(Element element, String propName, Object propValue) {
        if (element == null || propName == null || propValue == null)
            return element;
        if (!hasFeature(element, propName))
            return element;
        if (isFeatureMultiValued(element, propName)) {
            List items = (List) getFeatureValue(element, propName);
            unsetFeatureValue(element, propName);
            setPropertyValueList(element, propName, items, propValue);
        } else if (propValue instanceof Collection)
            setFeatureValue(element, propName, ((Collection) propValue).toArray()[0]);
        else
            setFeatureValue(element, propName, propValue);
        return element;
    }
    
   /**
     * Unset value of particular feature in UML element. This is implementation dependent and 
     * should be implemented in actual implementations of property manager
     * @param element    UML element which is updated   
     * @param propName   The name of the feature which value is modified 
     * @return      Updated {@code element}
     */
    abstract public Element unsetFeatureValue(Element element, String propName);
    
   /**
     * Retrieve value of particular feature in UML element. This is implementation dependent and 
     * should be implemented in actual implementations of property manager
     * @param element   UML element which is queried   
     * @param name      The name of the feature which value is retrieve 
     * @return          The feature value
     */
    abstract public Object getFeatureValue(Element element, String name);
    
    /**
     * Check if UML element has particular feature. This is implementation dependent and 
     * should be implemented in actual implementations of property manager
     * @param element   UML element which is queried   
     * @param name      The name of the feature which value is queried 
     * @return          {@code true} if {@code element} has feature {@code name}; {@code false} otherwise
     */
    abstract public boolean hasFeature(Element element, String name);
    
    /**
     * Check if feature is set for particular element. This is implementation dependent and 
     * should be implemented in actual implementations of property manager
     * @param element   UML element which is queried   
     * @param name      The name of the feature which is queried
     * @return          {@code true} if {@code element} has feature {@code name}; {@code false} otherwise
     */
    abstract public boolean isFeatureSet(Element element, String name);
    
    /**
     * Check if feature is can contain multiple ( e.g., a set) values. This is implementation dependent and 
     * should be implemented in actual implementations of property manager
     * @param element   UML element which is queried   
     * @param name      The name of the feature which is queried
     * @return          {@code true} if {@code element} has multivalued feature {@code name}; {@code false} otherwise
     */
    abstract public boolean isFeatureMultiValued(Element element, String name);
    
    /**
     * Set the value for particular feature of an element, using a given list of objects for this value. These 
     * values will be added into a single list, which will be set as the value of this feature. Note that 
     * generally this should be used with multivalued features (i.e., {@link #isFeatureMultiValued(Object, String)} +
     * should return {@code true}). This is implementation dependent and 
     * should be implemented in actual implementations of property manager
     * @param element   UML element which is modified   
     * @param propName  The name of the feature which is updated. 
     * @param elements  Objects (elements, strings, collections of objects) which must be set as the value. 
     * @return          {@code true} if {@code element} has feature {@code name}; {@code false} otherwise
     */
    abstract public Element setPropertyValueList(Element element, String propName, Object... elements);
    
    /**
     * Set the value for particular feature of an element. This is implementation dependent and 
     * should be implemented in actual implementations of property manager
     * @param element   UML element which is modified   
     * @param propName  The name of the feature which is updated. 
     * @param value     The {@link Object} which will be set as this value 
     * @return          {@code true} if {@code element} has feature {@code name}; {@code false} otherwise
     */
    abstract public Element setFeatureValue(Element element, String propName, Object value);
    
}
