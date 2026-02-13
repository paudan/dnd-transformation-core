package org.ktu.transformations.transforms;

import java.util.ResourceBundle;
import org.ktu.transformations.elements.AbstractElementProducer;
import org.ktu.transformations.helpers.AbstractPropertyManager;
import org.ktu.transformations.helpers.ElementSearch;
import org.ktu.transformations.mappers.ElementMapper;

/**
 * A factory type of object which produces one of the partial M2M transformation object instances, 
 * given their parameter objects, that map to actual functionality implementations
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, 
 * Kaunas University of Technology, 2015
 * @param <Element>         Type, corresponding to actual UML Element implementation
 * @param <Stereotype>      Type, corresponding to actual UML Stereotype implementation
 */
public class TransformationFactory<Element, Stereotype> {
    
    private static TransformationFactory INSTANCE;
    private static final ResourceBundle messages_EN = ResourceBundle.getBundle("org/ktu/transformations/messages_EN");
    
    private TransformationFactory() {
        super();
    }
    
    /**
    * Return the existing instance of {@link TransformationFactory}
    * @param <Element>         Type, corresponding to actual UML Element implementation
    * @param <Stereotype>      Type, corresponding to actual UML Stereotype implementation
    * @return A new instance of {@link TransformationFactory} if it was previously {@code null}; or the existing instance otherwise
    */
    public static <Element, Stereotype> TransformationFactory<Element, Stereotype> getInstance() {
        if (INSTANCE == null)
            INSTANCE = new TransformationFactory<>();
        return INSTANCE;
    }
    
    private void performNullCheck(final ElementMapper<Element, ?, Stereotype> mapper, final AbstractPropertyManager<Element, Stereotype, ?> manager,
        final AbstractElementProducer<Element, Stereotype> producer,
        final ElementSearch<Element, Stereotype> search, String transformName) throws TransformationConfigurationException  {
        if (mapper == null)
            throw new TransformationConfigurationException(String.format(messages_EN.getString("TransformationFactory.1"), 
                    ElementMapper.class.getSimpleName(), transformName));
        if (manager == null)
            throw new TransformationConfigurationException(String.format(messages_EN.getString("TransformationFactory.1"), 
                    AbstractPropertyManager.class.getSimpleName(), transformName));
        if (producer == null)
            throw new TransformationConfigurationException(String.format(messages_EN.getString("TransformationFactory.1"), 
                    AbstractElementProducer.class.getSimpleName(), transformName));
        if (search == null)
            throw new TransformationConfigurationException(String.format(messages_EN.getString("TransformationFactory.1"), 
                    ElementSearch.class.getSimpleName(), transformName));
    }
    
    /**
     * Return a new instance of {@link AbstractSingleTransformation}
     * @param mapper    An implementation of {@link ElementMapper} which performs actual mapping to element properties in the implementations    
     * @param manager   A subclass of {@link AbstractPropertyManager}implementing property (UML feature) management functionality
     * @param producer  A subclass of {@link AbstractElementProducer}, implementing element generation for implementation in particular tool 
     * @param search    A subclass of {@link ElementSearch}, implementing search of different elements by their class, type, stereotype, etc.
     * @return  A new instance of {@link AbstractSingleTransformation}
     * @throws TransformationConfigurationException   The transformation object could not be initialized because of some of its missing components. 
     * This would happen if e.g. one of the passed parameters in this method is {@code null}  
     */
    public AbstractSingleTransformation<Element, Stereotype> getSingleTransformationInstance(
        final ElementMapper<Element, ?, Stereotype> mapper, final AbstractPropertyManager<Element, Stereotype, ?> manager,
        final AbstractElementProducer<Element, Stereotype> producer,
        final ElementSearch<Element, Stereotype> search) throws TransformationConfigurationException {
        performNullCheck(mapper, manager, producer, search, AbstractSingleTransformation.class.getSimpleName());
        return new AbstractSingleTransformation<Element, Stereotype>() {

            @Override
            public ElementMapper<Element, ?, Stereotype> getElementMapper() {
                return mapper;
            }

            @Override
            public AbstractPropertyManager<Element, Stereotype, ?> getPropertyManager() {
                return manager;
            }

            @Override
            public ElementSearch<Element, Stereotype> getElementSearch() {
                return search;
            }

            @Override
            public AbstractElementProducer<Element, Stereotype> getElementProducer() {
                return producer;
            }
            
        };
    }
    
    /**
     * Return a new instance of {@link AbstractActivityTransformation}
     * @param mapper    An implementation of {@link ElementMapper} which performs actual mapping to element properties in the implementations    
     * @param manager   A subclass of {@link AbstractPropertyManager}implementing property (UML feature) management functionality
     * @param producer  A subclass of {@link AbstractElementProducer}, implementing element generation for implementation in particular tool 
     * @param search    A subclass of {@link ElementSearch}, implementing search of different elements by their class, type, stereotype, etc.
     * @return  A new instance of {@link AbstractActivityTransformation}
     * @throws TransformationConfigurationException   The transformation object could not be initialized because of some of its missing components. 
     * This would happen if e.g. one of the passed parameters in this method is {@code null}  
     */
    public AbstractActivityTransformation<Element, Stereotype> getActivityTransformationInstance(
        final ElementMapper<Element, ?, Stereotype> mapper, final AbstractPropertyManager<Element, Stereotype, ?> manager,
        final AbstractElementProducer<Element, Stereotype> producer,
        final ElementSearch<Element, Stereotype> search) throws TransformationConfigurationException {
        performNullCheck(mapper, manager, producer, search, AbstractActivityTransformation.class.getSimpleName());
        return new AbstractActivityTransformation<Element, Stereotype>() {

            @Override
            public ElementMapper<Element, ?, Stereotype> getElementMapper() {
                return mapper;
            }

            @Override
            public AbstractPropertyManager<Element, Stereotype, ?> getPropertyManager() {
                return manager;
            }

            @Override
            public ElementSearch<Element, Stereotype> getElementSearch() {
                return search;
            }

            @Override
            public AbstractElementProducer<Element, Stereotype> getElementProducer() {
                return producer;
            }
            
        };
    }
    
    /**
     * Return a new instance of {@link AbstractContainerTransformation}
     * @param mapper    An implementation of {@link ElementMapper} which performs actual mapping to element properties in the implementations    
     * @param manager   A subclass of {@link AbstractPropertyManager}implementing property (UML feature) management functionality
     * @param producer  A subclass of {@link AbstractElementProducer}, implementing element generation for implementation in particular tool 
     * @param search    A subclass of {@link ElementSearch}, implementing search of different elements by their class, type, stereotype, etc.
     * @return  A new instance of {@link AbstractContainerTransformation}
     * @throws TransformationConfigurationException   The transformation object could not be initialized because of some of its missing components. 
     * This would happen if e.g. one of the passed parameters in this method is {@code null}  
     */
    public AbstractContainerTransformation<Element, Stereotype> getContainerTransformationInstance(
        final ElementMapper<Element, ?, Stereotype> mapper, final AbstractPropertyManager<Element, Stereotype, ?> manager,
        final AbstractElementProducer<Element, Stereotype> producer,
        final ElementSearch<Element, Stereotype> search) throws TransformationConfigurationException {
        performNullCheck(mapper, manager, producer, search, AbstractContainerTransformation.class.getSimpleName());
        return new AbstractContainerTransformation<Element, Stereotype>() {

            @Override
            public ElementMapper<Element, ?, Stereotype> getElementMapper() {
                return mapper;
            }

            @Override
            public AbstractPropertyManager<Element, Stereotype, ?> getPropertyManager() {
                return manager;
            }

            @Override
            public ElementSearch<Element, Stereotype> getElementSearch() {
                return search;
            }

            @Override
            public AbstractElementProducer<Element, Stereotype> getElementProducer() {
                return producer;
            }
            
        };
    }
    
    /**
     * Return a new instance of {@link AbstractMultipleTransformation}
     * @param mapper    An implementation of {@link ElementMapper} which performs actual mapping to element properties in the implementations    
     * @param manager   A subclass of {@link AbstractPropertyManager}implementing property (UML feature) management functionality
     * @param producer  A subclass of {@link AbstractElementProducer}, implementing element generation for implementation in particular tool 
     * @param search    A subclass of {@link ElementSearch}, implementing search of different elements by their class, type, stereotype, etc.
     * @return  A new instance of {@link AbstractMultipleTransformation}
     * @throws TransformationConfigurationException   The transformation object could not be initialized because of some of its missing components. 
     * This would happen if e.g. one of the passed parameters in this method is {@code null}  
     */
    public AbstractMultipleTransformation<Element, Stereotype> getMultipleTransformationInstance(
        final ElementMapper<Element, ?, Stereotype> mapper, final AbstractPropertyManager<Element, Stereotype, ?> manager,
        final AbstractElementProducer<Element, Stereotype> producer,
        final ElementSearch<Element, Stereotype> search) throws TransformationConfigurationException {
        performNullCheck(mapper, manager, producer, search, AbstractMultipleTransformation.class.getSimpleName());
        return new AbstractMultipleTransformation<Element, Stereotype>() {

            @Override
            public ElementMapper<Element, ?, Stereotype> getElementMapper() {
                return mapper;
            }

            @Override
            public AbstractPropertyManager<Element, Stereotype, ?> getPropertyManager() {
                return manager;
            }

            @Override
            public ElementSearch<Element, Stereotype> getElementSearch() {
                return search;
            }

            @Override
            public AbstractElementProducer<Element, Stereotype> getElementProducer() {
                return producer;
            }
            
        };
    }
    
    /**
     * Return a new instance of {@link AbstractPropertyTransformation}
     * @param mapper    An implementation of {@link ElementMapper} which performs actual mapping to element properties in the implementations    
     * @param manager   A subclass of {@link AbstractPropertyManager}implementing property (UML feature) management functionality
     * @param producer  A subclass of {@link AbstractElementProducer}, implementing element generation for implementation in particular tool 
     * @param search    A subclass of {@link ElementSearch}, implementing search of different elements by their class, type, stereotype, etc.
     * @return  A new instance of {@link AbstractPropertyTransformation}
     * @throws TransformationConfigurationException   The transformation object could not be initialized because of some of its missing components. 
     * This would happen if e.g. one of the passed parameters in this method is {@code null}  
     */
    public AbstractPropertyTransformation<Element, Stereotype> getPropertyTransformationInstance(
        final ElementMapper<Element, ?, Stereotype> mapper, final AbstractPropertyManager<Element, Stereotype, ?> manager,
        final AbstractElementProducer<Element, Stereotype> producer,
        final ElementSearch<Element, Stereotype> search) throws TransformationConfigurationException {
        performNullCheck(mapper, manager, producer, search, AbstractPropertyTransformation.class.getSimpleName());
        return new AbstractPropertyTransformation<Element, Stereotype>() {

            @Override
            public ElementMapper<Element, ?, Stereotype> getElementMapper() {
                return mapper;
            }

            @Override
            public AbstractPropertyManager<Element, Stereotype, ?> getPropertyManager() {
                return manager;
            }

            @Override
            public ElementSearch<Element, Stereotype> getElementSearch() {
                return search;
            }

            @Override
            public AbstractElementProducer<Element, Stereotype> getElementProducer() {
                return producer;
            }
            
        };
    }
    
    /**
     * Return a new instance of {@link AbstractRelationTransformation}
     * @param mapper    An implementation of {@link ElementMapper} which performs actual mapping to element properties in the implementations    
     * @param manager   A subclass of {@link AbstractPropertyManager}implementing property (UML feature) management functionality
     * @param producer  A subclass of {@link AbstractElementProducer}, implementing element generation for implementation in particular tool 
     * @param search    A subclass of {@link ElementSearch}, implementing search of different elements by their class, type, stereotype, etc.
     * @return  A new instance of {@link AbstractRelationTransformation}
     * @throws TransformationConfigurationException   The transformation object could not be initialized because of some of its missing components. 
     * This would happen if e.g. one of the passed parameters in this method is {@code null}  
     */
    public AbstractRelationTransformation<Element, Stereotype> getRelationTransformationInstance(
        final ElementMapper<Element, ?, Stereotype> mapper, final AbstractPropertyManager<Element, Stereotype, ?> manager,
        final AbstractElementProducer<Element, Stereotype> producer,
        final ElementSearch<Element, Stereotype> search) throws TransformationConfigurationException {
        performNullCheck(mapper, manager, producer, search, AbstractRelationTransformation.class.getSimpleName());
        return new AbstractRelationTransformation<Element, Stereotype>() {

            @Override
            public ElementMapper<Element, ?, Stereotype> getElementMapper() {
                return mapper;
            }

            @Override
            public AbstractPropertyManager<Element, Stereotype, ?> getPropertyManager() {
                return manager;
            }

            @Override
            public ElementSearch<Element, Stereotype> getElementSearch() {
                return search;
            }

            @Override
            public AbstractElementProducer<Element, Stereotype> getElementProducer() {
                return producer;
            }
            
        };
    }
    
}
