package org.ktu.transformations.parsers;

/**
 * Class which defines default transformation pattern configuration
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, Kaunas University of Technology, 2015
 */
public class DefaultPatternConfiguration implements PatternConfiguration {

    @Override
    public String getTransformationConnectorName() {
        return "D&D Connector";
    }

    @Override
    public String getElementInFocusName() {
        return "DraggedElement";
    }

    @Override
    public String getIntegrationTagName() {
        return "integration";
    }

    @Override
    public String getJoinStereotypeName() {
        return "Join";
    }

    @Override
    public String getSourceStereotypeName() {
        return "Source";
    }

    @Override
    public String getTargetStereotypeName() {
        return "Target";
    }
    
    
}
