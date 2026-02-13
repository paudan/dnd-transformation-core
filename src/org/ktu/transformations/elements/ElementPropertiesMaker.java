package org.ktu.transformations.elements;

import java.util.Map;
import java.util.AbstractMap.SimpleEntry;
import java.util.ResourceBundle;
import org.ktu.transformations.transforms.Transformation;
import org.ktu.transformations.parsers.ConnectableEntity;
import org.ktu.transformations.parsers.ElementMapping;
import org.ktu.transformations.mappers.ElementMapper;
import org.ktu.transformations.transforms.AbstractMultipleTransformation;

/**
 * A class to generate properties for an container-type of element. This generator class generally should 
 * be used with {@link AbstractMultipleTransformation}
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, Kaunas University of Technology, 2014-2015
 * @param <Element>         Type, corresponding to actual UML Element implementation
 * @param <Stereotype>      Type, corresponding to actual UML Stereotype implementation
 */
@SuppressWarnings({"unchecked"})
public class ElementPropertiesMaker<Element, Stereotype> extends GenericElementMaker<Element, Stereotype> {

    private final boolean emptyProp;
    private final ElementMapping mainstruct;
    
    private static final ResourceBundle bundle = ResourceBundle.getBundle("org/ktu/transformations/messages_EN");

    /**
     * @param factory       The {@link Transformation} object which uses this maker for element generation
     * @param emptyProp     Indicates if the set of properties is not empty
     * @param mainstruct    Mapping structure for container element
     */
    public ElementPropertiesMaker(Transformation<Element, Stereotype> factory, boolean emptyProp, ElementMapping mainstruct) {
        super(factory);
        this.emptyProp = emptyProp;
        this.mainstruct = mainstruct;
    }
    
    /**
     * Generates an Element with internally connected properties. It returns an array consisting of a generated element and {@code null} {@link SimpleEntry}.
     * @throws ElementGenerationException   An error while generating the elements
     * @see GenericElementMaker#generateElement(Object, ElementMapping, ConnectableEntity, Object, Object, Map, boolean, String) 
     */
    @Override
    public Object[] generateElement(Object source, ElementMapping ms, ConnectableEntity targetEl, 
            Element targetPackage, Element dragged, Map<Class<?>, Map<String, Element>> defPropMap, boolean checkUnique, String nameOverride)
            throws ElementGenerationException {
        Object[] result = new Object[2];
        ElementMapper<Element, ?, Stereotype> mapper = factory.getElementMapper();
        AbstractElementProducer<Element, Stereotype> producer = factory.getElementProducer();
        if (targetEl.getTypeName().equals("String"))
            result[0] = nameOverride != null ? nameOverride : producer.getGeneratedName(ms, source, dragged, targetEl);
        else {
            Class<?> targetClass = targetEl.getBaseClass();
            Stereotype stereotype = (Stereotype) targetEl.getRepresentedStereotype();
            String name = nameOverride != null ? nameOverride : producer.getGeneratedName(ms, source, dragged, targetEl);
            Element newel = createElement(targetPackage, targetClass, stereotype, name, defPropMap, checkUnique);
            if (newel == null && !emptyProp)
                throw new ElementGenerationException(String.format(bundle.getString("ElementPropertiesMaker.0"), targetEl.getProcessedName(), targetPackage));
            if (mapper.isElement(source))
                newel = producer.generateMappedProperties(mainstruct, (Element) source, newel, targetPackage, targetEl);
            result[0] = newel;
        }
        return result;
    }

}
