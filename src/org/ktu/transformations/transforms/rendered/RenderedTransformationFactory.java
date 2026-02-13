package org.ktu.transformations.transforms.rendered;

import java.util.ResourceBundle;
import org.ktu.transformations.elements.AbstractElementProducer;
import org.ktu.transformations.helpers.AbstractPropertyManager;
import org.ktu.transformations.helpers.ElementSearch;
import org.ktu.transformations.mappers.ElementMapper;
import org.ktu.transformations.renderers.ElementRenderer;
import org.ktu.transformations.transforms.AbstractActivityTransformation;
import org.ktu.transformations.transforms.AbstractContainerTransformation;
import org.ktu.transformations.transforms.AbstractMultipleTransformation;
import org.ktu.transformations.transforms.AbstractPropertyTransformation;
import org.ktu.transformations.transforms.AbstractRelationTransformation;
import org.ktu.transformations.transforms.AbstractSingleTransformation;
import org.ktu.transformations.transforms.TransformationConfigurationException;
import org.ktu.transformations.transforms.TransformationFactory;

/**
 * A factory type of object which produces one of the transformations with renderings of generated element presentations, 
 * given their parameter objects, that map to particular implementations of relevant functionality 
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, 
 * Kaunas University of Technology, 2015
 * @param <Element>         Type, corresponding to actual UML Element implementation
 * @param <Stereotype>      Type, corresponding to actual UML Stereotype implementation
 * @param <Presentation>    Type, corresponding to actual element presentation implementation
 */
public class RenderedTransformationFactory<Element, Stereotype, Presentation> {
    
    private static RenderedTransformationFactory INSTANCE;
    private final TransformationFactory<Element, Stereotype> factory = TransformationFactory.getInstance();
    private static final ResourceBundle messages_EN = ResourceBundle.getBundle("org/ktu/transformations/messages_EN");
    
    private RenderedTransformationFactory() {
        super();
    }
    
    /**
    * Return the existing instance of {@link RenderedTransformationFactory}
    * @param <Element>         Type, corresponding to actual UML Element implementation
    * @param <Stereotype>      Type, corresponding to actual UML Stereotype implementation
    * @param <Presentation>    Type, corresponding to actual element presentation implementation
    * @return A new instance of {@link RenderedTransformationFactory} if it was previously {@code null}; or the existing instance otherwise
    */
    public static <Element, Stereotype, Presentation> RenderedTransformationFactory<Element, Stereotype, Presentation> getInstance() {
        if (INSTANCE == null)
            INSTANCE = new RenderedTransformationFactory<>();
        return INSTANCE;
    }
    
    /**
     * Return a new instance of {@link RenderedSingleTransformation}
     * @param mapper    An implementation of {@link ElementMapper} which performs actual mapping to element properties in the implementations    
     * @param manager   A subclass of {@link AbstractPropertyManager}implementing property (UML feature) management functionality
     * @param producer  A subclass of {@link AbstractElementProducer}, implementing element generation for implementation in particular tool 
     * @param search    A subclass of {@link ElementSearch}, implementing search of different elements by their class, type, stereotype, etc.
     * @param renderer  {@link ElementRenderer} object used to generate presentation elements for generated UML elements
     * @return  A new instance of {@link RenderedSingleTransformation}
     * @throws TransformationConfigurationException   The transformation object could not be initialized because of some of its missing components. 
     * This would happen if e.g. one of the passed parameters in this method is {@code null}  
     */
    public RenderedSingleTransformation<Element, Stereotype, Presentation> getRenderedSingleTransformation(
            final ElementMapper<Element, ?, Stereotype> mapper, 
            final AbstractPropertyManager<Element, Stereotype, ?> manager,
            final AbstractElementProducer<Element, Stereotype> producer,
            final ElementSearch<Element, Stereotype> search,
            final ElementRenderer<Element, Presentation> renderer) throws TransformationConfigurationException {
        AbstractSingleTransformation<Element, Stereotype> transform = factory.getSingleTransformationInstance(mapper, manager, producer, search);
        if (renderer == null)
            throw new TransformationConfigurationException(String.format(messages_EN.getString("TransformationFactory.1"), 
                    ElementRenderer.class.getSimpleName(), RenderedSingleTransformation.class.getSimpleName()));
        return new RenderedSingleTransformation<Element, Stereotype, Presentation>(transform) {

            @Override
            public ElementRenderer<Element, Presentation> getElementRenderer() {
                return renderer;
            }
            
        };
    }
    
    /**
     * Return a new instance of {@link RenderedActivityTransformation}
     * @param mapper    An implementation of {@link ElementMapper} which performs actual mapping to element properties in the implementations    
     * @param manager   A subclass of {@link AbstractPropertyManager}implementing property (UML feature) management functionality
     * @param producer  A subclass of {@link AbstractElementProducer}, implementing element generation for implementation in particular tool 
     * @param search    A subclass of {@link ElementSearch}, implementing search of different elements by their class, type, stereotype, etc.
     * @param renderer  {@link ElementRenderer} object used to generate presentation elements for generated UML elements
     * @return  A new instance of {@link RenderedActivityTransformation}
     * @throws TransformationConfigurationException   The transformation object could not be initialized because of some of its missing components. 
     * This would happen if e.g. one of the passed parameters in this method is {@code null}  
     */
    public RenderedActivityTransformation<Element, Stereotype, Presentation> getRenderedActivityTransformation(
            final ElementMapper<Element, ?, Stereotype> mapper, 
            final AbstractPropertyManager<Element, Stereotype, ?> manager,
            final AbstractElementProducer<Element, Stereotype> producer,
            final ElementSearch<Element, Stereotype> search,
            final ElementRenderer<Element, Presentation> renderer) throws TransformationConfigurationException {       
        AbstractActivityTransformation<Element, Stereotype> transform = factory.getActivityTransformationInstance(mapper, manager, producer, search);
        if (renderer == null)
            throw new TransformationConfigurationException(String.format(messages_EN.getString("TransformationFactory.1"), 
                    ElementRenderer.class.getSimpleName(), RenderedActivityTransformation.class.getSimpleName()));
        return new RenderedActivityTransformation<Element, Stereotype, Presentation>(transform) {

            @Override
            public ElementRenderer<Element, Presentation> getElementRenderer() {
                return renderer;
            }
            
        };
    }
    
        /**
     * Return a new instance of {@link RenderedContainerTransformation}
     * @param mapper    An implementation of {@link ElementMapper} which performs actual mapping to element properties in the implementations    
     * @param manager   A subclass of {@link AbstractPropertyManager}implementing property (UML feature) management functionality
     * @param producer  A subclass of {@link AbstractElementProducer}, implementing element generation for implementation in particular tool 
     * @param search    A subclass of {@link ElementSearch}, implementing search of different elements by their class, type, stereotype, etc.
     * @param renderer  {@link ElementRenderer} object used to generate presentation elements for generated UML elements
     * @return  A new instance of {@link RenderedContainerTransformation}
     * @throws TransformationConfigurationException   The transformation object could not be initialized because of some of its missing components. 
     * This would happen if e.g. one of the passed parameters in this method is {@code null}  
     */
    public RenderedContainerTransformation<Element, Stereotype, Presentation> getRenderedContainerTransformation(
            final ElementMapper<Element, ?, Stereotype> mapper, 
            final AbstractPropertyManager<Element, Stereotype, ?> manager,
            final AbstractElementProducer<Element, Stereotype> producer,
            final ElementSearch<Element, Stereotype> search,
            final ElementRenderer<Element, Presentation> renderer) throws TransformationConfigurationException {
        AbstractContainerTransformation<Element, Stereotype> transform = factory.getContainerTransformationInstance(mapper, manager, producer, search);
        if (renderer == null)
            throw new TransformationConfigurationException(String.format(messages_EN.getString("TransformationFactory.1"), 
                    ElementRenderer.class.getSimpleName(), RenderedContainerTransformation.class.getSimpleName()));
        return new RenderedContainerTransformation<Element, Stereotype, Presentation>(transform) {

            @Override
            public ElementRenderer<Element, Presentation> getElementRenderer() {
                return renderer;
            }
            
        };
    }
    
    /**
     * Return a new instance of {@link RenderedMultipleTransformation}
     * @param mapper    An implementation of {@link ElementMapper} which performs actual mapping to element properties in the implementations    
     * @param manager   A subclass of {@link AbstractPropertyManager}implementing property (UML feature) management functionality
     * @param producer  A subclass of {@link AbstractElementProducer}, implementing element generation for implementation in particular tool 
     * @param search    A subclass of {@link ElementSearch}, implementing search of different elements by their class, type, stereotype, etc.
     * @param renderer  {@link ElementRenderer} object used to generate presentation elements for generated UML elements
     * @return  A new instance of {@link RenderedMultipleTransformation}
     * @throws TransformationConfigurationException   The transformation object could not be initialized because of some of its missing components. 
     * This would happen if e.g. one of the passed parameters in this method is {@code null}  
     */
    public RenderedMultipleTransformation<Element, Stereotype, Presentation> getRenderedMultipleTransformation(
            final ElementMapper<Element, ?, Stereotype> mapper, 
            final AbstractPropertyManager<Element, Stereotype, ?> manager,
            final AbstractElementProducer<Element, Stereotype> producer,
            final ElementSearch<Element, Stereotype> search,
            final ElementRenderer<Element, Presentation> renderer) throws TransformationConfigurationException {
        AbstractMultipleTransformation<Element, Stereotype> transform = factory.getMultipleTransformationInstance(mapper, manager, producer, search);
        if (renderer == null)
            throw new TransformationConfigurationException(String.format(messages_EN.getString("TransformationFactory.1"), 
                    ElementRenderer.class.getSimpleName(), AbstractMultipleTransformation.class.getSimpleName()));
        return new RenderedMultipleTransformation<Element, Stereotype, Presentation>(transform) {

            @Override
            public ElementRenderer<Element, Presentation> getElementRenderer() {
                return renderer;
            }
            
        };
    }
    
    /**
     * Return a new instance of {@link RenderedPropertyTransformation}
     * @param mapper    An implementation of {@link ElementMapper} which performs actual mapping to element properties in the implementations    
     * @param manager   A subclass of {@link AbstractPropertyManager}implementing property (UML feature) management functionality
     * @param producer  A subclass of {@link AbstractElementProducer}, implementing element generation for implementation in particular tool 
     * @param search    A subclass of {@link ElementSearch}, implementing search of different elements by their class, type, stereotype, etc.
     * @param renderer  {@link ElementRenderer} object used to generate presentation elements for generated UML elements
     * @return  A new instance of {@link RenderedPropertyTransformation}
     * @throws TransformationConfigurationException   The transformation object could not be initialized because of some of its missing components. 
     * This would happen if e.g. one of the passed parameters in this method is {@code null}  
     */
    public RenderedPropertyTransformation<Element, Stereotype, Presentation> getRenderedPropertyTransformation(
            final ElementMapper<Element, ?, Stereotype> mapper, 
            final AbstractPropertyManager<Element, Stereotype, ?> manager,
            final AbstractElementProducer<Element, Stereotype> producer,
            final ElementSearch<Element, Stereotype> search,
            final ElementRenderer<Element, Presentation> renderer) throws TransformationConfigurationException {
        AbstractPropertyTransformation<Element, Stereotype> transform = factory.getPropertyTransformationInstance(mapper, manager, producer, search);
        if (renderer == null)
            throw new TransformationConfigurationException(String.format(messages_EN.getString("TransformationFactory.1"), 
                    ElementRenderer.class.getSimpleName(), RenderedPropertyTransformation.class.getSimpleName()));
        return new RenderedPropertyTransformation<Element, Stereotype, Presentation>(transform) {

            @Override
            public ElementRenderer<Element, Presentation> getElementRenderer() {
                return renderer;
            }
            
        };
    }
    
    /**
     * Return a new instance of {@link RenderedRelationTransformation}
     * @param mapper    An implementation of {@link ElementMapper} which performs actual mapping to element properties in the implementations    
     * @param manager   A subclass of {@link AbstractPropertyManager}implementing property (UML feature) management functionality
     * @param producer  A subclass of {@link AbstractElementProducer}, implementing element generation for implementation in particular tool 
     * @param search    A subclass of {@link ElementSearch}, implementing search of different elements by their class, type, stereotype, etc.
     * @param renderer  {@link ElementRenderer} object used to generate presentation elements for generated UML elements
     * @return  A new instance of {@link RenderedRelationTransformation}
     * @throws TransformationConfigurationException   The transformation object could not be initialized because of some of its missing components. 
     * This would happen if e.g. one of the passed parameters in this method is {@code null}  
     */
    public RenderedRelationTransformation<Element, Stereotype, Presentation> getRenderedRelationTransformation(
            final ElementMapper<Element, ?, Stereotype> mapper, 
            final AbstractPropertyManager<Element, Stereotype, ?> manager,
            final AbstractElementProducer<Element, Stereotype> producer,
            final ElementSearch<Element, Stereotype> search,
            final ElementRenderer<Element, Presentation> renderer) throws TransformationConfigurationException {
        AbstractRelationTransformation<Element, Stereotype> transform = factory.getRelationTransformationInstance(mapper, manager, producer, search);
        if (renderer == null)
            throw new TransformationConfigurationException(String.format(messages_EN.getString("TransformationFactory.1"), 
                    ElementRenderer.class.getSimpleName(), RenderedRelationTransformation.class.getSimpleName()));
        return new RenderedRelationTransformation<Element, Stereotype, Presentation>(transform) {

            @Override
            public ElementRenderer<Element, Presentation> getElementRenderer() {
                return renderer;
            }
            
        };
    }
    
}
