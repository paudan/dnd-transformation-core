package org.ktu.transformations.transforms.rendered;

import java.awt.Point;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.ktu.transformations.transforms.AbstractActivityTransformation;
import org.ktu.transformations.transforms.TransformationConfigurationException;
import org.ktu.transformations.transforms.TransformationManager;

/**
 * Abstract class, implementing generic transformation with rendering functionality, when connecting element is swimlane
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, 
 * Kaunas University of Technology, 2015
 * @param <Element>         Type, corresponding to actual UML Element implementation
 * @param <Stereotype>      Type, corresponding to actual UML Stereotype implementation
 * @param <Presentation>    Type, corresponding to actual element presentation implementation
 */
abstract public class RenderedActivityTransformation<Element, Stereotype, Presentation> 
    extends RenderedContainerTransformation<Element, Stereotype, Presentation> {

    public RenderedActivityTransformation(AbstractActivityTransformation<Element, Stereotype> transform) throws TransformationConfigurationException {
        super(transform);
    }
    
    @Override
    protected List<Presentation> drawContainedElements(Map<Object, Map<String, Set<Element>>> drawableItems, Presentation diagram, Point location) {
        return getElementRenderer().renderSwimlane(drawableItems, TransformationManager.getInstance().getCurrentReader().isCheckUnique(), location, diagram);
    }
    
}
