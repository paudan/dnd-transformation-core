package org.ktu.transformations.elements;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ResourceBundle;
import org.ktu.transformations.helpers.AbstractPropertyManager;
import org.ktu.transformations.helpers.ElementSearch;
import org.ktu.transformations.mappers.ElementMapper;
import org.ktu.transformations.notifiers.NotificationObserver;
import org.ktu.transformations.notifiers.NotificationType;
import org.ktu.transformations.notifiers.NotificationObservable;
import org.ktu.transformations.parsers.ConnectableEntity;
import org.ktu.transformations.parsers.PatternParser;
import org.ktu.transformations.parsers.ElementMapping;
import org.ktu.transformations.parsers.ElementMapping.PropertyMapping;
import org.ktu.transformations.parsers.PropertyStack;
import org.ktu.transformations.parsers.RuleParser;
import org.ktu.transformations.parsers.ConcatResolver;
import org.ktu.transformations.parsers.ConnectorEntity;
import org.ktu.transformations.parsers.SpecificationReader;
import org.ktu.transformations.parsers.SpecificationReader.IntegrationType;
import org.ktu.transformations.transforms.TransformationManager;

/**
 * Performs full Element generation, including its properties; the logic of CONCAT type of mapping is also considered.
 * It includes a number of static methods which are necessary to generate actual elements, properties, integration relationships, process names according to rules
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, Kaunas University of Technology, 2015
 * @param <Element>         Type, corresponding to actual UML Element implementation
 * @param <Stereotype>      Type, corresponding to actual UML Stereotype implementation
 */
@SuppressWarnings({"deprecation", "unchecked"})
public abstract class AbstractElementProducer<Element, Stereotype> implements NotificationObservable {

    /** UML element representing the owning element, where the resulting elements would be generated */
    protected Element targetPackage;
    /** The mapper-type object which maps functionality in this package to functionality in actual implementations */
    protected ElementMapper<Element, ?, Stereotype> mapper;
    /** The property manager object */
    protected AbstractPropertyManager<Element, Stereotype, ?> propManager;
    /** The set of {@link NotificationObserver} objects which will receive notifications from this object */
    protected Set<NotificationObserver> observers = new HashSet<>();
    
    private static ResourceBundle bundle = ResourceBundle.getBundle("org/ktu/transformations/messages_EN");

    /**
     * Initializes the {@link AbstractElementProducer} object
     * @param mapper        {@link ElementMapper} object, which maps abstract methods to their implementations
     * @param propManager   {@link AbstractPropertyManager} which performs property setting and management
     */
    public AbstractElementProducer(ElementMapper<Element, ?, Stereotype> mapper, AbstractPropertyManager<Element, Stereotype, ?> propManager) {
        this.mapper = mapper;
        this.propManager = propManager;
    }

     /**
     * Initializes the {@link AbstractElementProducer} object
     * @param mapper        {@link ElementMapper} object, which maps abstract methods to their implementations
     * @param propManager   {@link AbstractPropertyManager} which performs property setting and management
     * @param targetPackage The UML element (UML Package, Model, etc.) which will contain the generated element(s)
     */
    public AbstractElementProducer(ElementMapper<Element, ?, Stereotype> mapper, 
            AbstractPropertyManager<Element, Stereotype, ?> propManager, Element targetPackage) {
        this.targetPackage = targetPackage;
        this.mapper = mapper;
        this.propManager = propManager;
    }
 
    /**
     * Set the Element which will contain the generated Element(s)
     * @param owner	The Element which will contain the generated Element(s)
     */
    public void setOwnerElement(Element owner) {
        this.setTargetPackage(owner);
    }

    /**
     * The base method to generate an Element with default properties, given its actual class and stereotype name
     * @param classType     The actual class of the target Element to be generated
     * @param stereotype    The stereotype, which must be applied to target Element
     * @param owner         The owner container of the generated Element
     * @param name          The name of the target Element
     * @param defaultPropMap	A {@link Map} which contains default properties for each class of elements; each of these properties 
     * is represented as a {@link Map}, where key is the name of the property and value is the Element that must be set. 
     * Such map can usually be obtained by calling {@link PatternParser#getUnmappedElements(String, Object, ElementSearch)}
     * @return	The generated Element
     * @throws ElementGenerationException   An error while generating the elements
     */
    public final Element createTargetElement(Class<?> classType, Stereotype stereotype, Element owner, String name,
            Map<Class<?>, Map<String, Element>> defaultPropMap) throws ElementGenerationException {
        if (classType == null)
            throw new ElementGenerationException(String.format(bundle.getString("ElementGenerator.0"), stereotype));
        Element el = createElementInstance(classType, owner);
        if (el == null)
            throw new ElementGenerationException(String.format(bundle.getString("ElementGenerator.1"), stereotype));
        if (stereotype != null && mapper.canApplyStereotype(el, stereotype))
            mapper.addStereotype(el, stereotype);
        if (name != null)
            mapper.setName(el, name);
        if (defaultPropMap != null && !defaultPropMap.isEmpty()) {
            Map<String, Element> hangMap = defaultPropMap.get(classType);
            if (hangMap != null && !hangMap.isEmpty())
                for (String fname : hangMap.keySet()) {
                    Element propel = hangMap.get(fname) != null ? hangMap.get(fname) : null;
                    if (propel != null)
                        propManager.setProperty(el, fname, propel);
                }
        }
        return el;
    }

    /**
     * Generate properties of an Element, according to their predefined mappings between source and target elements
     * @param mapping      Element mapping structure, defining the necessary mappings
     * @param source       Source element
     * @param target       Target element
     * @param ownerElement The Element which will contain the generated element
     * @param targetMap    The element in the target part of the transformation pattern
     * @return	The target Element with generated properties
     */
    public final Element generateMappedProperties(ElementMapping mapping, Element source, Element target,
            Element ownerElement, ConnectableEntity targetMap) {
        Element parent = mapper.getProjectModel();
        for (PropertyStack key : mapping.getPropertyMappingKeys()) {
            Set<PropertyMapping> pmaps = mapping.getPropertyMappings(key, targetMap);
            for (PropertyMapping pmapping : pmaps) {
                List<Object> propelemList = propManager.getPropertyList(source, key);
                for (Object el : propelemList) {
                    PropertyStack propStack = pmapping.getTargetStack();
                    String featName = propStack.lowermostProperty().getName();
                    try {
                        ConnectableEntity top = propStack.lowermostProperty();
                        Stereotype ptype = (Stereotype) top.getRepresentedStereotype();
                        Class<?> pclass = top.getBaseClass();
                        if (!mapper.isElement(el)) {
                            Element currProp = target;
                            String fname;
                            for (int i = 1; i < propStack.size(); i++) {
                                if (currProp == null)
                                    break;
                                fname = propStack.get(i).getName();
                                if (!propManager.hasFeature(currProp, fname))
                                    break;
                                if (i != propStack.size() - 1) {
                                    // What if each feature contains more than 1 object? The property may have to be set for each
                                    // Currently only first is selected for further processing
                                    // TODO: Explore and implement processing of the whole set
                                    Element propval = null;
                                    Object val = propManager.getFeatureValue(currProp, fname);
                                    if (propManager.isFeatureMultiValued(currProp, fname) && val instanceof List)
                                        propval = ((List<Element>) val).get(0);
                                    else
                                        propval = (Element) val;
                                    if (propval == null) {
                                        // The property has not been set - generate new instance
                                        Stereotype prtype = (Stereotype) propStack.get(i).getRepresentedStereotype();
                                        Class prclass = propStack.get(i).getBaseClass();
                                        try {
                                            Element genProp = createTargetElement(prclass, prtype, parent, null, null);
                                            propManager.setProperty(currProp, fname, genProp);
                                            propval = genProp;
                                        } catch (ElementGenerationException ex) {
                                        }
                                        currProp = propval;
                                    }
                                } else if (el instanceof String) {
                                    String rule = pmapping.getNamingRule();
                                    String res = (String) el;
                                    if (rule != null)
                                        res = new RuleParser().applyExtractionRule(rule, res);
                                    propManager.setProperty(currProp, fname, res);
                                } else
                                    propManager.setProperty(currProp, fname, el);
                            }
                        } else if (mapper.isElement(el) && pclass.isAssignableFrom(mapper.getClassType((Element) el)) && propStack.size() == 2) 
                            propManager.setProperty(target, featName, createElementCopy((Element) el, parent));
                        else if (mapper.isElement(el)) {
                            String name = mapper.isNamedElement((Element) el) && !mapper.isProperty((Element) el) ? mapper.getActualName((Element) el) : null;
                            Element newel = createTargetElement(pclass, ptype, ownerElement, name, null);
                            SimpleImmutableEntry<Element, ConnectableEntity> propMapObj = generateElementProperties(newel, propStack, ownerElement, null);
                            propManager.setProperty(target, featName, propMapObj.getKey());
                        }
                    } catch (ElementGenerationException e) {
                        String errstr = String.format(bundle.getString("ElementGenerator.2"), featName, mapper.getHumanName(source));
                        sendNotification(null, errstr, NotificationType.ERROR);
                    }
                }
            }
        }
        return target;
    }

    /**
     * Generate an Element according to the set of defined property stack, together with the set of these properties. The properties are generated iteratively,
     * according to their position in the {@code stack}
     * @param target         The target Element, which the properties will be generated for
     * @param stack          The stack of property mappings
     * @param ownerElement   The owner Element which contains the target Element
     * @param defaultPropMap  A {@link Map} which contains default properties for each class of elements; each of these properties is represented as a {@link Map},
     *                       where key is the name of the property and value is the Element that must be set
     * @return	{@link SimpleEntry} which contains the Element with generated properties as a key and mapping ConnectableElement for {@code target}
     * @throws ElementGenerationException   An error while generating the elements
     */
    public final SimpleImmutableEntry<Element, ConnectableEntity> generateElementProperties(Element target, PropertyStack stack,
            Element ownerElement, Map<Class<?>, Map<String, Element>> defaultPropMap) throws ElementGenerationException {
        if (stack == null)
            return new SimpleImmutableEntry<>(target, null);
        ConnectableEntity firstEl = null;
        Element nextProp = target;
        try {
            for (int i = stack.size() - 1; i > 1; i--) {
                firstEl = stack.get(i - 1);
                Stereotype ptype = (Stereotype) firstEl.getRepresentedStereotype();
                Class<?> pclass = firstEl.getBaseClass();
                Element firstProp = null;
                try {
                    String name = !mapper.isProperty((Element) firstEl.getConnectableObject()) ? firstEl.getName() : null;
                    firstProp = createTargetElement(pclass, ptype, ownerElement, name, defaultPropMap);
                } catch (ElementGenerationException ex) {
                }
                firstProp = (Element) propManager.setProperty(firstProp, stack.get(i).getName3(), nextProp);
                nextProp = firstProp;
            }
        } catch (NullPointerException | ClassCastException e) {
            String errstr = String.format(bundle.getString("ElementGenerator.3"), mapper.getTypeName(target));
            sendNotification(null, errstr, NotificationType.ERROR);
            throw new ElementGenerationException(errstr);
        }
        return new SimpleImmutableEntry<>(nextProp, firstEl);
    }

    /**
     * Apply relevant naming rules for {@code source} element and return a name for generation
     * @param struct	The relevant element mapping structure
     * @param source	Source element
     * @param dragged	The element that was dragged
     * @param targetMap	The target element in the transformation pattern
     * @return	Name {@link String}, which was obtained after processing
     */
    public String getGeneratedName(ElementMapping struct, Object source, Element dragged, ConnectableEntity targetMap) {
        String rule = struct.getNamingRules().get(targetMap);
        String name = null;
        if (mapper.isElement(source) && mapper.hasName((Element) source))
            name = mapper.getProperName(mapper.getElementName((Element) source));
        else if (source instanceof String)
            name = source.toString();
        if (rule != null && name != null)
            name = new RuleParser().applyExtractionRule(rule, name);
        return name;
    }

    /**
     * Returns default target package (i.e., UML Package or Model element), where the resulting elements would be generated
     * @return  UML element set as target package
     */
    protected Element getTargetPackage() {
        return targetPackage;
    }

    @Override
    public void register(NotificationObserver observer) {
        this.observers.add(observer);
    }

    @Override
    public void unregister(NotificationObserver observer) {
        this.observers.remove(observer);
    }

    @Override
    public void sendNotification(Object[] elements, String text, NotificationType type) {
        for (NotificationObserver obj : observers)
            obj.update(elements, text, type);
    }

    /**
     * Create an instance of given type class
     * @param classType The class type of the element (e.g., ActivityPartion.class)
     * @param owner     The UML element (e.g., Package, Model) which would contain the generated elements
     * @return          The generated UML element, or {@code null} if no element has been generated
     */
    protected abstract Element createElementInstance(Class<?> classType, Element owner);
    
    /**
     * Create a deep copy of given UML element
     * @param element   The UML element which must be copied
     * @param parent    The owner element of the generated element
     * @return  The generated element
     */
    public abstract Element createElementCopy(Element element, Element parent);

    /**
     * Set default target package which would contain the generated elements
     * @param targetPackage the targetPackage to set
     */
    public void setTargetPackage(Element targetPackage) {
        this.targetPackage = targetPackage;
    }
    
    /**
     * Create integration relationship between source and target elements
     * @param client	The client element of the relationship (the target Element)
     * @param supplier	The supplier element of the relationship (the source Element)
     * @param type	Integration type, retrieved from specification (usually obtained by calling {@link SpecificationReader#getIntegrationType()})
     */
    abstract public void createIntegration(Element client, Element supplier, IntegrationType type);
    
    /**
     * Remove element instance from the model
     * @param element The element to be removed
     */
    abstract public void removeElement(Element element);
    
    /**
     * Set both end properties of UML Association element as navigable
     * @param element UML Association element
     */
    abstract public void setAssociationNavigable(Element element);
    
    /**
     * Set client element of a UML Relationship
     * @param relation  UML Relationship element
     * @param client    The UML element which should be set as the client element in {@code relation}
     */
    abstract public void setClientElement(Element relation, Element client);
    
    /**
     * Set supplier element of a UML Relationship
     * @param relation  UML Relationship element
     * @param supplier  The UML element which should be set as the supplier element in {@code relation}
     */
    abstract public void setSupplierElement(Element relation, Element supplier);
    
    /**
     * Create integration between source and target elements
     * @param source    The source element in the integration relationship
     * @param target    The target element in the integration relationship
     * @param typeRes   A {@link SimpleImmutableEntry} pair of elements, which represent integration type and {@link ConnectorEntity} mapping element in the source part   
     * @param ms        Element mapping object, representing the source of the transformation
     * @param targetEl  {@link ConnectableEntity} representing mapping element for {@code target} in the target part
     */
    abstract protected void createIntegration(Element source, Element target, SimpleImmutableEntry<IntegrationType, ConnectorEntity> typeRes, 
            ElementMapping ms, ConnectableEntity targetEl);
    
    /**
     * Generates elements according to CONCAT mappings, defined in mapping structure
     * @param resolver	  {@link ConcatResolver} object which resolves CONCAT type of mappings
     * @param targetEl	  The element in the target part of the transformation pattern
     * @param ms          Mapping structure which is processed
     * @param source	  Source object (e.g., UML Element, Property, etc.) which is processed
     * @param selected    The element which has been selected or dragged (i.e., the main source of transformation)
     * @param defPropMap A {@link Map} which contains default properties for each class of elements; each of these properties is represented as a {@link Map},
     *                   where key is the name of the property and value is the Element that must be set. Such map can usually be obtained
     *                   by calling {@link PatternParser#getUnmappedElements(String, Object, ElementSearch)}
     * @param elementFactory The element maker object which is used for generation of  
     * @return	A structure of type {@link Map}, where key value is the generated element with additionally generated properties,
     *         and value is a {@link SimpleEntry}, consisting of the generated element with properties, and the mapping element, representing target Element
     * @throws ElementGenerationException	There was an error while generating the elements
     */
    public Map<Object, SimpleImmutableEntry<Element, ConnectableEntity>> generateElementsConcat(ConcatResolver<Element> resolver,
            ConnectableEntity targetEl, ElementMapping ms, Object source, Element selected,
            Map<Class<?>, Map<String, Element>> defPropMap, GenericElementMaker<Element, Stereotype> elementFactory) throws ElementGenerationException {
        Element targetPkg = getTargetPackage();
        Map<PropertyStack, List<String>> combinations = resolver.getGeneratedNames(targetEl);
        Map<Object, SimpleImmutableEntry<Element, ConnectableEntity>> generated = new HashMap<>();
        SpecificationReader reader = TransformationManager.getInstance().getCurrentReader();
        boolean unique = reader.isCheckUnique();
        if (combinations != null) {
            for (PropertyStack stcomb : combinations.keySet()) {
                for (String concat_name : combinations.get(stcomb)) {
                    Object[] result = elementFactory.generateElement(source, ms, targetEl, targetPkg, selected, defPropMap, unique, concat_name);
                    Object newel = result[0];
                    SimpleImmutableEntry<Element, ConnectableEntity> propSet = (SimpleImmutableEntry<Element, ConnectableEntity>) result[1];
                    if (propSet == null && mapper.isElement(newel))
                        mapper.setName((Element) newel, concat_name);
                    generated.put(newel, propSet);
                    Map<Object, PropertyStack> srcList = resolver.getSourceElements(targetEl, stcomb, concat_name);
                    // Integration
                    for (Object srcObj : srcList.keySet())
                        if (mapper.isElement(srcObj) && mapper.isElement(newel)) {    
                            SimpleImmutableEntry<IntegrationType, ConnectorEntity> typeRes = resolver.getIntegrationType(targetEl, srcList.get(srcObj));
                            if (typeRes != null) 
                                createIntegration((Element) srcObj, (Element) newel, typeRes, ms, targetEl);
                        }
                }
            }
            generated = resolver.updateElementProperties(targetEl, generated);
        } else {
            Object[] result = elementFactory.generateElement(source, ms, targetEl, targetPkg, selected, defPropMap, unique, null);
            if (result == null)
                return null;
            if (mapper.isElement(result[0]) && result[1] instanceof SimpleImmutableEntry) {
                Object newel = result[0];
                generated.put(newel, (SimpleImmutableEntry<Element, ConnectableEntity>) result[1]);
                if (newel != null && mapper.isElement(source) && mapper.isElement(newel))
                    createIntegration((Element)source, (Element)newel, null, ms, targetEl);
            } else //if (result[0] instanceof String)
                generated.put(result[0], (SimpleImmutableEntry<Element, ConnectableEntity>) null);
        }
        return generated;
    }
}
