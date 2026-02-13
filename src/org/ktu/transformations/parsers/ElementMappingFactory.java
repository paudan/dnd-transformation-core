package org.ktu.transformations.parsers;

/**
 * Default implementation of factory for {@link ElementMapping} object creation. This implementation may be overriden in other implementations 
 * if {@link ElementMapping} is subclassed, e.g., when adding additional functionality
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, Kaunas University of Technology, 2015
 */
public class ElementMappingFactory {
    
    /**
     * Create a new {@link ElementMapping} object
     * @param owner The {@link PatternParser} object associated with this mapping
     * @return A new instance of {@link ElementMapping}
     */
    public ElementMapping createElementMapping(PatternParser owner) {
        return new ElementMapping(owner);
    }
}
