package org.ktu.transformations.parsers;

/**
 * Interface, defining partial M2M transformation pattern configuration, according to its specification
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, Kaunas University of Technology, 2015
 */
public interface PatternConfiguration {

    /**
     * Returns the name of the stereotype, defining selected or dragged element, as defined in transformation pattern specification
     * @return {@link String} representing the stereotype name
     */
    public String getElementInFocusName();

    /**
     * Return the name of transformation stereotype (which name is returned by {@link #getTransformationConnectorName() }) tag, defining integration type
     * @return {@link String} representing the tag name
     */
    public String getIntegrationTagName();

    /**
     * Returns the name of the stereotype, defining join (CONCAT) element, as defined in transformation pattern specification
     * @return {@link String} representing the stereotype name
     */
    public String getJoinStereotypeName();

    /**
     * Returns the name of the stereotype, defining source part element, as defined in transformation pattern specification
     * @return {@link String} representing the stereotype name
     */
    public String getSourceStereotypeName();

    /**
     * Returns the name of the stereotype, defining target part element, as defined in transformation pattern specification
     * @return {@link String} representing the stereotype name
     */
    public String getTargetStereotypeName();

    /**
     * Returns the name of the stereotype, defining D&amp;D Connector element, as defined in transformation pattern specification
     * @return {@link String} representing the stereotype name
     */
    public String getTransformationConnectorName();
    
}
