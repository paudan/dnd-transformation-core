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
import org.ktu.transformations.transforms.AbstractRelationTransformation;
import org.ktu.transformations.transforms.TransformationConfigurationException;

/**
 * Abstract class, implementing generic transformation with rendering functionality, when the type of connecting element is relationship
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, 
 * Kaunas University of Technology, 2015
 * @param <Element>         Type, corresponding to actual UML Element implementation
 * @param <Stereotype>      Type, corresponding to actual UML Stereotype implementation
 * @param <Presentation>    Type, corresponding to actual element presentation implementation
 */
abstract public class RenderedRelationTransformation<Element, Stereotype, Presentation> 
    extends AbstractRenderedTransformation<Element, Stereotype, Presentation> {
    
    /** Transformation for generating elements with rendering capability, if they are not interrelated  */
    protected RenderedMultipleTransformation<Element, Stereotype, Presentation> multipleRendered;

    public RenderedRelationTransformation(AbstractRelationTransformation<Element, Stereotype> transform) throws TransformationConfigurationException {
        super(transform);
        this.transform = transform;
        final ElementMapper<Element, ?, Stereotype> mapper = this.getElementMapper();
        final AbstractPropertyManager<Element, Stereotype, ?> manager = this.getPropertyManager();
        final AbstractElementProducer<Element, Stereotype> eproducer = this.getElementProducer();
        final ElementSearch<Element, Stereotype> search = this.getElementSearch();
        final ElementRenderer<Element, Presentation> renderer = this.getElementRenderer();
        RenderedTransformationFactory<Element, Stereotype, Presentation> factory = RenderedTransformationFactory.getInstance();
        this.multipleRendered = factory.getRenderedMultipleTransformation(mapper, manager, eproducer, search, renderer);
    }

    @Override
    protected List<Presentation> renderItems(Map<Object, ?> drawable, Presentation elementOver, Point location) {
        List<Presentation> layout = new ArrayList<>();
        ElementRenderer<Element, Presentation> renderer = getElementRenderer();
        if (!drawable.isEmpty())
            for (Object mainel : drawable.keySet())
                if (drawable.get(mainel) instanceof List)
                layout.addAll(renderer.renderRelatedElements((Element) mainel, (List<Element>)drawable.get(mainel), location, elementOver));
        AbstractRelationTransformation<Element, Stereotype> transf = (AbstractRelationTransformation<Element, Stereotype>) transform;
        Set<Object> singleDrawable = transf.getSingleDrawable();
        if (!singleDrawable.isEmpty())
            multipleRendered.drawGeneratedItems(singleDrawable, elementOver, location);
        return layout;
    }
    
}
