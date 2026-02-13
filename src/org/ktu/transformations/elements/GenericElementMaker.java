package org.ktu.transformations.elements;

import java.util.Map;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import org.ktu.transformations.notifiers.NotificationObserver;
import org.ktu.transformations.notifiers.NotificationType;
import org.ktu.transformations.notifiers.NotificationObservable;
import org.ktu.transformations.parsers.ConnectableEntity;
import org.ktu.transformations.transforms.Transformation;
import org.ktu.transformations.helpers.ElementSearch;
import org.ktu.transformations.mappers.ElementMapper;
import org.ktu.transformations.parsers.ElementMapping;
import org.ktu.transformations.parsers.SpecificationReader;

/**
 * An abstract class for single element generation, including the check of duplicates
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, Kaunas University of Technology, 2015
 * @param <Element>         Type, corresponding to actual UML Element implementation
 * @param <Stereotype>      Type, corresponding to actual UML Stereotype implementation
 */
@SuppressWarnings({"rawtypes"})
public abstract class GenericElementMaker<Element, Stereotype> implements NotificationObservable {
    
    private static final ResourceBundle messages_EN = ResourceBundle.getBundle("org/ktu/transformations/messages_EN");
    /** The {@link Set} of objects which receive notifications about the state of generation of objects */
    protected Set<NotificationObserver> observers = new HashSet<>();
    /** The {@link Transformation} which is uses this element maker object */
    protected Transformation<Element, Stereotype> factory;

    public GenericElementMaker(Transformation<Element, Stereotype> factory) {
        this.factory = factory;
    }

    /**
     * Generate single element; must be implemented for each subclass. It returns an array consisting of a generated element and an optional {@link SimpleEntry},
     * where key value is the element with additionally generated properties, and another is the mapping element, representing target Element.
     * The second element may also be {@code null}, depending on the implementation of the subclass and type of element which is generated
     * @param source        An Element which is the source for transformation
     * @param sourceMapping {@link ElementMapping} structure, which describes the source mapping, necessary for transformation
     * @param targetEl      The element in the target part of the transformation pattern, mapping to the actual target element
     * @param owner         The owning element for generated Element
     * @param dragged       The element which was dragged
     * @param defPropMap    A map which contains default properties for each class of elements, represented as a {@link Map},
     *                      where key is the name of the property and value is the Element that must be set
     * @param checkUnique   Flag which defines whether the generated Element should be checked for existing duplicates during generation
     *                     (corresponds to {@code checkUniqueness} flag in specification)
     * @param nameOverride  The {@link String} which overrides the name of the generated element, instead of the default setting (the name of {@code dragged} element).
     *                     If set to {@code null}, the name will not be overriden
     * @return	An array of two elements: the first element is the generated element, and another is a {@link SimpleEntry} structure
     * @throws ElementGenerationException	The element could not be generated
     */
    public abstract Object[] generateElement(Object source, ElementMapping sourceMapping, ConnectableEntity targetEl, 
            Element owner, Element dragged, Map<Class<?>, Map<String, Element>> defPropMap, 
            boolean checkUnique, String nameOverride) throws ElementGenerationException;

    /**
     * Generate an Element (the check of duplicate elements is also performed)
     * @param owner		The element, owning generated Element
     * @param targetClass	The class of target Element
     * @param stereotype	The stereotype of target Element
     * @param elementName	The name of target Element
     * @param defPropMap	A map which contains default properties for each class of elements, represented as a map,
     *                          where key is the name of the property and value is the Element that must be set
     * @param checkUnique	Flag which corresponds to checkUniqueness flag in specification
     *                          (e.g., obtained using {@link SpecificationReader#isCheckUnique})
     * @return	Generated element
     */
    public Element createElement(Element owner, Class targetClass, Stereotype stereotype, String elementName,
            Map<Class<?>, Map<String, Element>> defPropMap, boolean checkUnique) {
        if (elementName == null)
            elementName = "";
        ElementSearch<Element, Stereotype> search = factory.getElementSearch();
        AbstractElementProducer<Element, Stereotype> producer = factory.getElementProducer();
        ElementMapper<Element, ?, Stereotype> mapper = factory.getElementMapper();
        Element newel = search.findElement(owner, targetClass, stereotype, elementName);
        if (newel == null)
            try {
                newel = producer.createTargetElement(targetClass, stereotype, owner, elementName, defPropMap);
                sendNotification(new Object [] {newel}, messages_EN.getString("GenericElementMaker.1") + " " + 
                   (mapper.isNamedElement(newel) ? mapper.getQualifiedName(newel) : mapper.getHumanName(newel)), NotificationType.INFO);
                return newel;
            } catch (ElementGenerationException e1) {
                sendNotification(null, e1.getMessage(), NotificationType.ERROR);
                return null;
            }
        if (!checkUnique) {
            int ind = 0;
            boolean found = true;
            String newElName = elementName;
            while (found) {
                ind++;
                newElName = elementName + "_" + ind;
                newel = search.findElement(owner, targetClass, stereotype, newElName);
                found = newel != null;
            }
            try {
                newel = producer.createTargetElement(targetClass, stereotype, owner, elementName, defPropMap);
            } catch (ElementGenerationException e) {
                sendNotification(null, e.getMessage(), NotificationType.ERROR); 
                return null;
            }
            if (mapper.isNamedElement(newel) && mapper.hasName(newel))
                mapper.setName(newel, newElName);
            sendNotification(new Object[] {newel}, messages_EN.getString("GenericElementMaker.1") + " " + 
                    (mapper.isNamedElement(newel) ? mapper.getQualifiedName(newel) : mapper.getHumanName(newel)),
                    NotificationType.INFO);
            return newel;
        }
        sendNotification(new Object[] {newel}, messages_EN.getString("GenericElementMaker.2") + " " + 
            (mapper.isNamedElement(newel) ? mapper.getQualifiedName(newel) : mapper.getHumanName(newel)), NotificationType.INFO);
        return newel;
    }

    /**
     * Add a collection of objects which would receive notifications about the state of generation of objects, produced by this element maker object 
     * @param observers     The {@link Collection} of objects which would receive the notifications. Each of these objects must implement 
     * the {@link NotificationObserver} interface
     */
    public void addObservers(Collection<NotificationObserver> observers) {
        if (observers != null)
            this.observers.addAll(observers);
    }

    @Override
    public void register(NotificationObserver observer) {
        if (observer != null)
            this.observers.add(observer);
    }

    @Override
    public void unregister(NotificationObserver observer) {
        if (observer != null)
            this.observers.remove(observer);
    }

    @Override
    public void sendNotification(Object[] generated, String text, NotificationType type) {
        for (NotificationObserver obj : observers) {
            obj.update(generated, text, type);
        }
    }
    
    

}
