package org.ktu.transformations.transforms.rendered;

import org.ktu.transformations.renderers.ElementRenderer;
import org.ktu.transformations.transforms.Transformation;

/**
 * An instance which defines partial M2M transformation with element rendering functionality 
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, 
 * Kaunas University of Technology, 2015
 * @param <Element>         Type, corresponding to actual UML Element implementation
 * @param <Stereotype>      Type, corresponding to actual UML Stereotype implementation
 * @param <Presentation>    Type, corresponding to actual element presentation implementation
 */
public interface RenderedTransformation<Element, Stereotype, Presentation> extends Transformation <Element, Stereotype>{
    
    /**
     * Return element renderer, used for rendering produced elements
     * @return An instance of {@link ElementRenderer}
     */
    public abstract ElementRenderer<Element, Presentation> getElementRenderer();
    
}
