package org.ktu.transformations.parsers;

import org.ktu.transformations.parsers.SpecificationReader.IntegrationType;

/**
 * Internal representation of mapping relationship
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), 
 * Center of Information Systems Design Technologies, Kaunas University of Technology, 2015
 * @param <Connector>    Type, corresponding to actual UML Connector implementation
 */
public class ConnectorEntity<Connector> {
    
    private String rule;
    private IntegrationType type;
    private Connector connRef;

    /**
     * Create a new instance of {@link ConnectorEntity}, using simple UML Connector object
     * @param connRef  {@link Object} representing UML Connector
     */
    public ConnectorEntity(Connector connRef) {
        this.connRef = connRef;
    }

    /**
     * Create a new instance of {@link ConnectorEntity}, using UML Connector, containing rule and integration type
     * @param connRef   {@link Object} representing UML Connector 
     * @param rule      Text processing rule, defined as the constraint of this Connector 
     * @param type      Integration type, defined using this Connector object
     */
    public ConnectorEntity(Connector connRef, String rule, IntegrationType type) {
        this.rule = rule;
        this.type = type;
        this.connRef = connRef;
    }

    /**
     * Get text processing rule, associated with this connector mapping entity
     * @return  {@link String} representing textual rule
     */
    public String getRule() {
        return rule;
    }

    /**
     * Set text processing rule, associated with this connector mapping entity
     * @param rule {@link String} representing textual rule
     */
    public void setRule(String rule) {
        this.rule = rule;
    }

    /**
     * Get integration type, represented by this connector mapping entity
     * @return {@link IntegrationType} representing the type of integration, represented by this connector mapping entity 
     */
    public IntegrationType getIntegrationType() {
        return type;
    }

    /**
     * Set integration type, represented by this connector mapping entity
     * @param type {@link IntegrationType} representing the type of integration, represented by this connector mapping entity 
     */
    public void setIntegrationType(IntegrationType type) {
        this.type = type;
    }
    
    /**
     * Get the UML Connector object, associated with this mapping entity
     * @return Object representing UML Connector object
     */
    public Connector getConnectorObject() {
        return connRef;
    }
    
    @Override
    public boolean equals(Object o1) {
        if (o1 instanceof ConnectorEntity)
            return connRef.equals(((ConnectorEntity)o1).connRef);
        return (this == o1);
    }
    
    @Override
    public int hashCode() {
        return connRef.hashCode();
    }
}
