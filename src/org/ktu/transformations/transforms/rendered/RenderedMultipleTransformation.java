package org.ktu.transformations.transforms.rendered;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.ktu.transformations.elements.AbstractElementProducer;
import org.ktu.transformations.helpers.AbstractPropertyManager;
import org.ktu.transformations.helpers.ElementSearch;
import org.ktu.transformations.mappers.ElementMapper;
import org.ktu.transformations.parsers.ConnectableEntity;
import org.ktu.transformations.parsers.ElementMapping;
import org.ktu.transformations.parsers.PropertyStack;
import org.ktu.transformations.renderers.ElementRenderer;
import org.ktu.transformations.transforms.AbstractMultipleTransformation;
import org.ktu.transformations.transforms.TransformationConfigurationException;

/**
 * Abstract class, implementing generic transformation with rendering functionality, when elements are not related by any connecting element
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, 
 * Kaunas University of Technology, 2015
 * @param <Element>         Type, corresponding to actual UML Element implementation
 * @param <Stereotype>      Type, corresponding to actual UML Stereotype implementation
 * @param <Presentation>    Type, corresponding to actual element presentation implementation
 */
abstract public class RenderedMultipleTransformation<Element, Stereotype, Presentation> 
    extends AbstractRenderedTransformation<Element, Stereotype, Presentation> {
    
    /** Transformation for generating single elements with rendering capability */
    protected RenderedSingleTransformation<Element, Stereotype, Presentation> singleRendered;
    /** Transformation for generating activity (swimlane) type of elements with rendering capability */
    protected RenderedActivityTransformation<Element, Stereotype, Presentation> activityRendered;
    /** Transformation for generating elements with rendering capability, if they are related using container type of element */
    protected RenderedContainerTransformation<Element, Stereotype, Presentation> containerRendered;

    public RenderedMultipleTransformation(AbstractMultipleTransformation<Element, Stereotype> transform) throws TransformationConfigurationException {
        super(transform);
        final ElementMapper<Element, ?, Stereotype> mapper = this.getElementMapper();
        final AbstractPropertyManager<Element, Stereotype, ?> manager = this.getPropertyManager();
        final AbstractElementProducer<Element, Stereotype> eproducer = this.getElementProducer();
        final ElementSearch<Element, Stereotype> search = this.getElementSearch();
        final ElementRenderer<Element, Presentation> renderer = this.getElementRenderer();
        RenderedTransformationFactory<Element, Stereotype, Presentation> factory = RenderedTransformationFactory.getInstance();
        this.singleRendered = factory.getRenderedSingleTransformation(mapper, manager, eproducer, search, renderer);
        this.activityRendered = factory.getRenderedActivityTransformation(mapper, manager, eproducer, search, renderer);
        this.containerRendered = factory.getRenderedContainerTransformation(mapper, manager, eproducer, search, renderer);
    }

    @Override
    protected List<Presentation> renderItems(Map<Object, ?> drawable, Presentation elementOver, Point location) {
        AbstractMultipleTransformation<Element, Stereotype> transformer = (AbstractMultipleTransformation<Element, Stereotype>) transform;
        ElementMapper<Element, ?, Stereotype> mapper = this.getElementMapper();
        //If only single element transformation was performed, render separate elements and exit
        if (transformer.isSingleGenerated())
            return singleRendered.renderItems(drawable, elementOver, location);
        
        // Draw elements, according to their type
        Set<Object> generalDrawable = new HashSet<>();
        Map<Object, Map<String, Set<Element>>> containerDrawable = new HashMap<>();
        Map<Element, Boolean> isGeneralDrawable = new HashMap<>();
        for (Object e : drawable.keySet()) {
            if (mapper.isElement(e)) {
                Element element = (Element) e;
                isGeneralDrawable.put(element, true);
                if (!getElementMapper().isRelatingClassifier(element.getClass()) && 
                        transformer.getPatternParser().hasAdditionalPropertyMapping(transformer.getTargetEntity())
                        && canAddAny(element, (ConnectableEntity)drawable.get(e), drawable)) {
                    isGeneralDrawable.put(element, false);
                    ConnectableEntity poolMap = (ConnectableEntity) drawable.get(element);
                    for (Object ed : drawable.keySet()) {
                        if (mapper.isElement(ed)) {
                            Element eldraw = (Element) ed;
                            Map<ConnectableEntity, ElementMapping> targets = transformer.getTargetMappings();
                            PropertyStack connStack = targets.get(poolMap).targetPropertyMap.get(drawable.get(eldraw));
                            if (connStack != null) {
                                String propName = ((ConnectableEntity)drawable.get(eldraw)).getName3();
                                Map<String, Set<Element>> actElements = containerDrawable.get(element);
                                if (actElements == null) {
                                    actElements = new HashMap<>();
                                    actElements.put(propName, new HashSet<Element>());
                                    containerDrawable.put(element, actElements);
                                }
                                actElements.get(propName).add(eldraw);
                                isGeneralDrawable.put(eldraw, false);
                            }
                        }
                    }
                }
            }
        }

        for (Element e : isGeneralDrawable.keySet())
            if (isGeneralDrawable.get(e).equals(Boolean.TRUE))
                generalDrawable.add(e);

        // Separate Activity drawable items, which use different representation generation 
        Map<Object, Map<String, Set<Element>>> activityDrawable = new HashMap<>();
        if (!containerDrawable.isEmpty()) {
            Iterator<Object> iter = containerDrawable.keySet().iterator();
            while (iter.hasNext()) {
                Object container = iter.next();
                if (getElementMapper().isActivityClassifier(container.getClass())) {
                    activityDrawable.put(container, containerDrawable.get(container));
                    containerDrawable.remove(container);
                }
            }
            if (!activityDrawable.isEmpty())
                activityRendered.drawContainedElements(activityDrawable, elementOver, location);
            if (!containerDrawable.isEmpty())
                containerRendered.drawContainedElements(containerDrawable, elementOver, location);
        }
        if (!generalDrawable.isEmpty())
            return drawGeneratedItems(generalDrawable, elementOver, location);
        return new ArrayList<>();
    }
    
    private boolean canAddAny(Element element, ConnectableEntity elementMap, Map<Object, ?> drawableItems) {
        AbstractMultipleTransformation<Element, Stereotype> transformer = (AbstractMultipleTransformation<Element, Stereotype>) transform;
        for (Object el : drawableItems.keySet()) {
            Map<ConnectableEntity, ElementMapping> targets = transformer.getTargetMappings();
            ElementMapping mapping = targets.get((ConnectableEntity)drawableItems.get(el));
            if (element == el && mapping != null)
                for (ConnectableEntity map : mapping.targetPropertyMap.keySet())
                    if (mapping.targetPropertyMap.get(map).contains(elementMap))
                        return true;
        }
        return false;
    }
    
    /**
     * Render generated Elements
     * @param drawableItems	The set of Elements which must be rendered as PresentationElements
     * @param elementOver	The PresentationElement (e.g., a DiagramPresentationElement) which the Element {@literal dragged} was dragged on
     * @param location		Actual location on the {@literal elementOver} where the set of generated PresentationElement will be placed on
     * @return                  The list of generated element presentations
     */
    public List<Presentation> drawGeneratedItems(Set<Object> drawableItems, Presentation elementOver, Point location) {
        List<Presentation> layout = new ArrayList<>();
        if (drawableItems.isEmpty())
            return layout;
        for (Object newel : drawableItems) 
            if (this.getElementMapper().isElement(newel)) {
                Presentation el = this.getElementRenderer().renderSingleElement((Element) newel, location, elementOver);
                if (el != null)
                    layout.add(el);
        }
        return layout;
    }
    
}
