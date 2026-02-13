package org.ktu.transformations.transforms.rendered;

import java.awt.Point;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.ktu.transformations.transforms.AbstractPropertyTransformation;
import org.ktu.transformations.transforms.TransformationConfigurationException;

/**
 * Abstract class, implementing generic transformation with rendering functionality, when elements are properties of other element
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, 
 * Kaunas University of Technology, 2015
 * @param <Element>         Type, corresponding to actual UML Element implementation
 * @param <Stereotype>      Type, corresponding to actual UML Stereotype implementation
 * @param <Presentation>    Type, corresponding to actual element presentation implementation
 */
abstract public class RenderedPropertyTransformation<Element, Stereotype, Presentation> 
    extends RenderedMultipleTransformation<Element, Stereotype, Presentation> {

    public RenderedPropertyTransformation(AbstractPropertyTransformation<Element, Stereotype> transform) throws TransformationConfigurationException {
        super(transform);
    }

    @Override
    protected List<Presentation> renderItems(Map<Object, ?> drawable, Presentation elementOver, Point location) {
        AbstractPropertyTransformation<Element, Stereotype> transformer = (AbstractPropertyTransformation<Element, Stereotype>)transform;
        if (transformer.isSingleGenerated())
            return singleRendered.renderItems(drawable, elementOver, location);
        return drawGeneratedItems(new HashSet<>(drawable.keySet()), elementOver, location);
    }
}
