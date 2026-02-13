package org.ktu.transformations.parsers;

/**
 * Interface which defines configuration for transformation specification
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems 
 * Design Technologies, Kaunas University of Technology, 2015
 */
public interface SpecificationConfiguration {

    /** Return the name of transformation stereotype, as defined in specification */
    public String getSpecificationStereotypeName();
    
    /** Return the name of customization stereotype, as defined in specification */
    public String getCustomizationStereotypeName();
    
    /** Return the name of "Target diagram" tag, as defined in specification */
    public String getTargetDiagramTagName();
    
    /** Return the name of "Allowed transformations" tag, as defined in specification */
    public String getAllowedTransformationsTagName();
    
    /** Return the name of "Customization target" tag, as defined in specification */
    public String getCustomizationTargetTagName();
    
    /** Return the name of "Representation text" tag, as defined in specification */
    public String getRepresentationTextTagName();
    
    /** Return the name of "Source element" tag, as defined in specification */
    public String getSourceElementTagName();
    
    /** Return the name of "Transformation pattern" tag, as defined in specification */
    public String getTransformationPatternTagName();
    
    /** Return the name of "Check uniqueness" tag, as defined in specification */
    public String getCheckUniquenessTagName();
    
    /** Return the name of "Property action result" tag, as defined in specification */
    public String getPropertyActionResultTagName();
    
    /** Return the name of "Relation action result" tag, as defined in specification */
    public String getRelationActionResultTagName();
    
    /** Return the name of "Integration action result" tag, as defined in specification */
    public String getIntegrationActionResultTagName();
    
    /** Return the name of transformation pattern stereotype, as defined in specification */
    public String getTransformationPatternStereotypeName();
    
    /** Return the name of integration stereotype */
    public String getIntegrationStereotypeName();
    
    /** Return the name of "integration source" tag for the stereotype, which name is returned by {@link #getIntegrationStereotypeName() }  */
    public String getIntegrationSourceTagName();
    
    /** Return the name of "integration target" tag for the stereotype, which name is returned by {@link #getIntegrationStereotypeName() }  */
    public String getIntegrationTargetTagName();
    
    /** Return the name of "integration type" tag for the stereotype, which name is returned by {@link #getIntegrationStereotypeName() }  */
    public String getIntegrationTypeTagName();
    
    /** Return the name of "transformation enabled" tag, as defined in specification */  
    public String getEnabledTagName();
}
