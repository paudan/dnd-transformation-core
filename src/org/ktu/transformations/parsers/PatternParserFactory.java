package org.ktu.transformations.parsers;

import org.ktu.transformations.mappers.ElementMapper;

/**
 * Interface for factory type of class which defines creation of {@link PatternParser} object
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of
 * Information Systems Design Technologies, Kaunas University of Technology, 2015
 * @param <Connector>           Actual implementation type of UML Connector object
 * @param <ConnectableElement>  Actual implementation type of UML Connectable element object
 * @param <Element>             Actual implementation type of UML Element object
 * @param <Stereotype>          Actual implementation type of UML Stereotype
 */
public interface PatternParserFactory<Connector, ConnectableElement, Element, Stereotype> {
    
    /**
     * Create an instance of {@link PatternParser} object
     * @param <Mapper>      Type, representing a subclass of ElementMapper<Element, ConnectableElement, Stereotype>
     * @param rootPattern	The pattern element
     * @param targetCl          The Customization target classifier (effective only if the {@code elementOver} is not a Diagram)
     * @param mapper            The adapter for particular tool implementation
     * @param elementOver	The element, onto which the dragged element was dragged
     * @return  A new instance of {@link PatternParser}
     * @throws InvalidPatternException  The pattern is invalid or not defined
     */
    public <Mapper extends ElementMapper<Element, ConnectableElement, Stereotype>>
        PatternParser<Connector, ConnectableElement, Element, Stereotype> getParserInstance(Element rootPattern, 
                Element targetCl, Mapper mapper, Element elementOver) throws InvalidPatternException;
        
    /**
     * Create an instance of {@link PatternParser} object
     * @param <Mapper>      Type, representing a subclass of ElementMapper<Element, ConnectableElement, Stereotype>
     * @param rootPattern	The pattern element
     * @param targetCl          The Customization target classifier (effective only if the {@code elementOver} is not a Diagram)
     * @param mapper            The adapter for particular tool implementation
     * @return  A new instance of {@link PatternParser}
     * @throws InvalidPatternException  The pattern is invalid or not defined
     */    
    public <Mapper extends ElementMapper<Element, ConnectableElement, Stereotype>>
        PatternParser<Connector, ConnectableElement, Element, Stereotype> getParserInstance(Element rootPattern, 
                Element targetCl, Mapper mapper)throws InvalidPatternException;
    
}
