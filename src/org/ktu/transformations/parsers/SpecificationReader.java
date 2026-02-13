package org.ktu.transformations.parsers;

import java.util.List;

/**
 * Defines main properties and operations for reading partial M2M specifications. Classes, which realize specification reading
 * functionality, must implement this interface
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of
 * Information Systems Design Technologies, Kaunas University of Technology, 2015
 */
public interface SpecificationReader extends Comparable<SpecificationReader> {
    
    /** Enumeration of integration type values */
    public static enum IntegrationType {
        /** No integration (explicit value) */
        NONE("none"), 
        /** Default integration: {@linkplain #FULL}, if the names of the source and target element names are the same; {@linkplain #PARTIAL} otherwise */
        DEFAULT("default"), 
        /** Partial integration */
        PARTIAL("partial"), 
        /** Full integration */
        FULL("full"), 
        /** Undefined integration */
        UNDEFINED("undefined");
        
        private final String name;
        
        /**
         * Return the textual value of enumeration value
         * @return String representation
         */
        public final String getName() { return name; }
        
        private IntegrationType(String name) {
            this.name = name;
        }
    }

    /**
     * Return the list of allowed Drag&amp;Drops, as defined in the specification
     * @return {@link List} of Classifier objects
     */
    public List<Object> getAllowedTransformationList();

    /**
     * Get current specification customization element
     * @return An instance of Element, representing customization
     */
    public Object getCustomizationElement();

    /**
     * Get the name of specification customization element
     * @return Specification name
     */
    public String getCustomizationName();

    /**
     * Get the transformation specification element
     * @return An instance of Element, representing specification
     */
    public Object getTransformationSpecificationElement();

    /**
     * Return the integration type, as defined in the specification
     * @return An EnumerationLiteral, representing integration type
     */
    public Object getIntegrationType();

    /**
     * Return the relation classifier, as defined in the specification
     * @return The Classifier object
     */
    public Object getRelationClassifier();

    /**
     * Return the representation text, as defined in the specification
     * @return {@link String}, representing representation text
     */
    public String getRepresentationText();

    /**
     * Return the source classifier, as defined in the specification
     * @return An instance of Classifier, representing source
     */
    public Object getSourceClassifier();

    /**
     * Get the name of transformation specification element
     * @return Specification name
     */
    public String getSpecificationName();

    /**
     * Return the target classifier, as defined in the specification
     * @return An instance of Classifier, representing source
     */
    public Object getTargetClassifier();

    /**
     * Return the list of target diagrams, as defined in the specification
     * @return {@link List} of {@link String}, representing the names of the diagrams
     */
    public List<String> getTargetDiagrams();

    /**
     * Get transformation pattern classifier
     * @return	A Classifier representing transformation pattern, or {@code null}, if such classifier is not defined in specification
     */
    public Object getTransformationPattern();

    /**
     * Checks, if specification has given diagram name among its target diagrams
     * @param diagramName Diagram name to be checked
     * @return {@code true} if {@code diagramName} is in the list of target diagrams; {@code false} otherwise
     */
    public boolean hasTargetDiagram(String diagramName);

    /**
     * Check if check for uniqueness is set in the specification
     * @return {@code true} if check for uniqueness is set; {@code false} otherwise
     */
    public boolean isCheckUnique();

    /**
     * Checks, if source classifier is also a Stereotype
     * @return {@code true} is source classifier is also a Stereotype; {@code false} otherwise
     */
    public boolean isSourceStereotype();
    
    /**
     * Get Specification Configuration object for this reader
     * @return {@link SpecificationConfiguration} object
     */
    public SpecificationConfiguration getSpecificationConfiguration();
    
    /**
     * Check is transformation is enabled  
     * @return {@code true} this transformation is enabled; {@code false} otherwise
     */
    public boolean getTransformationEnabled();
    
}
