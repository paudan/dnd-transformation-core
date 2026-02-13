package org.ktu.transformations.elements;

import java.util.Map;
import java.util.AbstractMap.SimpleImmutableEntry;
import org.ktu.transformations.transforms.Transformation;
import org.ktu.transformations.mappers.ElementMapper;
import org.ktu.transformations.parsers.ConnectableEntity;
import org.ktu.transformations.parsers.ElementMapping;
import org.ktu.transformations.parsers.PropertyStack;

/**
 * Generate an element, which includes internally connected elements (a tuple) as well
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, Kaunas University of Technology, 2014-2015
 * @param <Element>         Type, corresponding to actual UML Element implementation
 * @param <Stereotype>      Type, corresponding to actual UML Stereotype implementation
 *
 */
@SuppressWarnings("unchecked")
public class ConnectedElementMaker<Element, Stereotype> extends GenericElementMaker<Element, Stereotype> {

    private ElementMapping mainMap;

    /**
     * Instantiates a generator for connected elements
     * @param factory   The {@link Transformation} object which uses this maker for element generation
     * @param mainMap	{@link ElementMapping} structure for the connecting element
     */
    public ConnectedElementMaker(Transformation<Element, Stereotype> factory, ElementMapping mainMap) {
        super(factory);
        this.mainMap = mainMap;
    }
    
    /**
     * Generates an Element with internally connected properties. It returns an array consisting of a generated element and {@link SimpleImmutableEntry},
     * where key is the element with additionally generated properties, and value is the pattern type element, representing target Element.
     * @throws ElementGenerationException   The element was not generated successfully
     * @see GenericElementMaker#generateElement(Object, ElementMapping, ConnectableEntity, Object, Object, Map, boolean, String) 
     */
    @Override
    public Object[] generateElement(Object propelem, ElementMapping ms, ConnectableEntity targetEl, 
            Element targetPackage, Element dragged, Map<Class<?>, Map<String, Element>> defPropMap, 
            boolean checkUnique, String nameOverride) throws ElementGenerationException {
        Object[] result = new Object[2];
        ElementMapper<Element, ?, Stereotype> mapper = factory.getElementMapper();
        AbstractElementProducer<Element, Stereotype> producer = factory.getElementProducer();
        Class<?> targetMapClass = targetEl.getBaseClass();
        Stereotype stereotype = (Stereotype) targetEl.getRepresentedStereotype();
        PropertyStack stack = mainMap.targetPropertyMap.get(targetEl);
        String name = nameOverride != null ? nameOverride : producer.getGeneratedName(ms, propelem, dragged, targetEl);
        Element newel = createElement(targetPackage, targetMapClass, stereotype, name, defPropMap, checkUnique);
        if (newel != null) {
            SimpleImmutableEntry<Element, ConnectableEntity> propMapObj = producer.generateElementProperties(newel, stack, targetPackage, defPropMap);
            Element genEl = propMapObj.getKey();
            if (mapper.isElement(propelem))
                genEl = producer.generateMappedProperties(ms, (Element) propelem, genEl, targetPackage, targetEl);
            result[0] = newel;
            result[1] = new SimpleImmutableEntry<>(genEl, propMapObj.getValue());
            return result;
        } else
            return null;
    }

}
