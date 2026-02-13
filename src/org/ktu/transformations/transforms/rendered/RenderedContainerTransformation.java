package org.ktu.transformations.transforms.rendered;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.ktu.transformations.elements.AbstractElementProducer;
import org.ktu.transformations.helpers.AbstractPropertyManager;
import org.ktu.transformations.helpers.ElementSearch;
import org.ktu.transformations.mappers.ElementMapper;
import org.ktu.transformations.renderers.ElementRenderer;
import org.ktu.transformations.transforms.AbstractContainerTransformation;
import org.ktu.transformations.transforms.TransformationConfigurationException;

/**
 * Abstract class, implementing generic transformation with rendering functionality, when the type of connecting element is container
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, 
 * Kaunas University of Technology, 2015
 * @param <Element>         Type, corresponding to actual UML Element implementation
 * @param <Stereotype>      Type, corresponding to actual UML Stereotype implementation
 * @param <Presentation>    Type, corresponding to actual element presentation implementation
 */
abstract public class RenderedContainerTransformation<Element, Stereotype, Presentation> 
    extends AbstractRenderedTransformation<Element, Stereotype, Presentation> {
    
    /** Transformation for generating single elements with rendering capability */
    protected RenderedSingleTransformation<Element, Stereotype, Presentation> singleRendered;

    public RenderedContainerTransformation(AbstractContainerTransformation<Element, Stereotype> transform) throws TransformationConfigurationException {
        super(transform);
        final ElementMapper<Element, ?, Stereotype> mapper = this.getElementMapper();
        final AbstractPropertyManager<Element, Stereotype, ?> manager = this.getPropertyManager();
        final AbstractElementProducer<Element, Stereotype> eproducer = this.getElementProducer();
        final ElementSearch<Element, Stereotype> search = this.getElementSearch();
        final ElementRenderer<Element, Presentation> renderer = this.getElementRenderer();
        RenderedTransformationFactory<Element, Stereotype, Presentation> factory = RenderedTransformationFactory.getInstance();
        this.singleRendered = factory.getRenderedSingleTransformation(mapper, manager, eproducer, search, renderer);
        this.transform = transform;
    }
    
    @Override
    protected Map<Object, Map<String, Set<Element>>> getDrawableItems() {
        return ((AbstractContainerTransformation<Element, Stereotype>)transform).getGeneratedElements();
    }

    @Override
    protected List<Presentation> renderItems(Map<Object, ?> drawable, Presentation elementOver, Point location) {
        if (((AbstractContainerTransformation<Element, Stereotype>)transform).isSingleGenerated())
            return singleRendered.renderItems(drawable, elementOver, location);
        else
            return drawContainedElements(getDrawableItems(), elementOver, location);
    }
    
    /**
     * Perform rendering of generated elements. This method may be overriden in subclasses, as different container elements may need different rendering logic
     *
     * @param drawableItems	The map which contains Elements and their properties to be represented in {@code elementOver}
     * @param elementOver	The PresentationElement (e.g., a DiagramPresentationElement or other) which the Element {@code dragged} was dragged on
     * @param location		Actual location on the {@code elementOver} where the set of generated PresentationElement will be placed on
     * @return A {@link List} of generated element presentations
     */
    protected List<Presentation> drawContainedElements(Map<Object, Map<String, Set<Element>>> drawableItems, 
            Presentation elementOver, Point location) {
        List<Presentation> layout = new ArrayList<>();
        ElementRenderer<Element, Presentation> renderer = this.getElementRenderer();
        ElementMapper<Element, ?, ?> mapper = this.getElementMapper();
        for (Object mainel : drawableItems.keySet())
            if (mapper.isElement(mainel))
                layout.addAll(renderer.renderContainedElements((Element) mainel, drawableItems.get(mainel), location, elementOver));
        return layout;
    }
    
}
