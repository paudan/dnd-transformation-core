package org.ktu.transformations.transforms;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.ktu.transformations.elements.AbstractElementProducer;
import org.ktu.transformations.elements.ElementGenerationException;
import org.ktu.transformations.helpers.AbstractPropertyManager;
import org.ktu.transformations.mappers.ElementMapper;
import org.ktu.transformations.notifiers.NotificationObserver;
import org.ktu.transformations.parsers.ConnectableEntity;
import org.ktu.transformations.parsers.InvalidPatternException;
import org.ktu.transformations.parsers.PatternParser;
import org.ktu.transformations.parsers.PropertyStack;
import org.ktu.transformations.parsers.SpecificationReader;

/**
 * A abstract class which performs transformation to elements, which are properties of another element
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, 
 * Kaunas University of Technology, 2014-2015
 * @param <Element>     Actual implementation type of UML Element
 * @param <Stereotype>  Actual implementation type of UML Stereotype
 */
abstract public class AbstractPropertyTransformation<Element, Stereotype> extends AbstractMultipleTransformation<Element, Stereotype> {
    
    private Map<Object, ConnectableEntity> drawableItems;

    public AbstractPropertyTransformation() throws TransformationConfigurationException {
        super();
    }
    
    private PropertyStack getTargetMapByName(Map<ConnectableEntity, PropertyStack> targetMap, String name) {
        for (ConnectableEntity el : targetMap.keySet())
            if (el.getName3().equals(name))
                return targetMap.get(el);
        return null;
    }
    
    @Override
    public Set<Object> createElements(SpecificationReader specReader, PatternParser<?, ?, Element, Stereotype> parser, 
            ConnectableEntity targetCl, Element targetPackage, Element dragged, Object elementOver, Collection<NotificationObserver> observers) 
            throws ElementGenerationException, InvalidPatternException {
        ElementMapper<Element, ?, Stereotype> mapper = this.getElementMapper();
        if (!mapper.isElement(elementOver))
            return new HashSet<>();
        drawableItems = new HashMap<>();
        emptyProp = false;
        singleGenerated = false;
        initSourceConnectingElements(parser, dragged);
        // If no connecting elements were identified, generate single element representing dragged 
        if (connectingElements.isEmpty()) {
            this.dragged = dragged;
            this.singleGenerated = true;
            Set<Object> elements = singleTransformer.createElements(specReader, parser.getDraggedTargetClassifier(dragged), targetPackage, observers);
            for (Object el: elements)
                drawableItems.put(el, null);
            return drawableItems.keySet();
        }
        AbstractElementProducer<Element, Stereotype> generator = getElementProducer();
        AbstractPropertyManager<Element, Stereotype, ?> propManager = this.getPropertyManager();
        initializeStructures(parser, targetCl);
        drawableItems = generatePropertyItems(specReader, parser, targetCl, targetPackage, dragged, observers);
        ConnectableEntity el = parser.getHangingRepresentingTarget(targetCl);
        if (el != null)
            for (Object o : drawableItems.keySet())
                if (mapper.isElement(o)) {
                    Element targetEl = (Element) o;
                    String name = mapper.hasStereotype(targetEl) ? mapper.getTypeName(targetEl) : mapper.getClassType(targetEl).getSimpleName();
                    PropertyStack stack = getTargetMapByName(targets.get(el).targetPropertyMap, name);
                    SimpleImmutableEntry<Element, ConnectableEntity> objmap = generator.generateElementProperties(targetEl, stack, targetPackage, defPropMap);
                    propManager.setProperty((Element) elementOver, stack.get(1).getName3(), objmap.getKey());
                } else if (o instanceof String) {
                    PropertyStack stack = targets.get(el).targetPropertyMap.get(drawableItems.get(o));
                    //SimpleEntry<Element, ConnectableElement> objmap = ElementGenerator.generateElementProperties(targetEl, stack, targetPackage, defPropMap);
                    propManager.setProperty((Element) elementOver, stack.get(1).getName3(), o);
                }
        return drawableItems.keySet();
    }
    
    @Override
    public Map<Object, ConnectableEntity> getGeneratedElements() {
        return drawableItems;
    }
    
}
