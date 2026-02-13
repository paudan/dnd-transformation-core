package org.ktu.transformations.elements;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import org.ktu.transformations.transforms.Transformation;

import org.ktu.transformations.parsers.ElementMapping;
import org.ktu.transformations.mappers.ElementMapper;
import org.ktu.transformations.parsers.ConnectableEntity;

/**
 * Simple Element generator class. It performs simple creation of an Element according to the given mapping element, without setting any properties
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, Kaunas University of Technology, 2015
 * @param <Element>     Actual implementation type representing UML Element
 * @param <Stereotype>  Actual implementation type representing UML Stereotype
 *
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class SimpleElementMaker<Element, Stereotype> extends GenericElementMaker<Element, Stereotype> {

    public SimpleElementMaker(Transformation<Element, Stereotype> factory) {
        super(factory);
    }

    /**
     * Generates an Element and returns an array consisting of a generated element and {@code null} {@link SimpleEntry} object.
     * @throws ElementGenerationException       There was an exception while generating the element
     * @see GenericElementMaker#generateElement(Object, ElementMapping, ConnectableEntity, Object, Object, Map, boolean, String)  
     */
    @Override
    public Object[] generateElement(Object source, ElementMapping ms, ConnectableEntity targetEl, 
            Element targetPackage, Element dragged, Map<Class<?>, Map<String, Element>> defPropMap, boolean checkUnique, String nameOverride)
            throws ElementGenerationException {
        Object[] result = new Object[2];
        ElementMapper<Element, ?, Stereotype> mapper = factory.getElementMapper();
        String elementName = mapper.getElementName(dragged);
        elementName = elementName != null ? mapper.getProperName(elementName) : null;
        elementName = nameOverride != null ? nameOverride : elementName;
        Element newel = createElement(targetPackage, targetEl.getBaseClass(), 
                (Stereotype) targetEl.getRepresentedStereotype(), elementName, defPropMap, checkUnique);
        result[0] = newel;
        return result;
    }

}
