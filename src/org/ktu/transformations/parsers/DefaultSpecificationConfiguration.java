package org.ktu.transformations.parsers;

/**
 * Class which defines default configuration for transformation specification
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems 
 * Design Technologies, Kaunas University of Technology, 2015
 */
public class DefaultSpecificationConfiguration implements SpecificationConfiguration {

    @Override
    public String getSpecificationStereotypeName() {
        return "DragAndDropSpecificationExtension";
    }

    @Override
    public String getCustomizationStereotypeName() {
        return "Customization";
    }

    @Override
    public String getTargetDiagramTagName() {
        return "targetDiagram";
    }

    @Override
    public String getAllowedTransformationsTagName() {
        return "allowedDragAndDrops";
    }

    @Override
    public String getCustomizationTargetTagName() {
        return "customizationTarget";
    }

    @Override
    public String getRepresentationTextTagName() {
        return "representationText";
    }

    @Override
    public String getSourceElementTagName() {
        return "sourceElement";
    }

    @Override
    public String getTransformationPatternTagName() {
        return "transformationPattern";
    }

    @Override
    public String getCheckUniquenessTagName() {
        return "checkUniqueness";
    }

    @Override
    public String getPropertyActionResultTagName() {
        return "propertyActionResult";
    }

    @Override
    public String getRelationActionResultTagName() {
        return "relationActionResult";
    }

    @Override
    public String getIntegrationActionResultTagName() {
        return "integrationActionResult";
    }

    @Override
    public String getTransformationPatternStereotypeName() {
        return "M2MTransformationPattern";
    }

    @Override
    public String getIntegrationStereotypeName() {
        return "Integration";
    }

    @Override
    public String getIntegrationSourceTagName() {
        return "sourceName";
    }

    @Override
    public String getIntegrationTargetTagName() {
        return "targetName";
    }

    @Override
    public String getIntegrationTypeTagName() {
        return "integrationType";
    }

    @Override
    public String getEnabledTagName() {
        return "enabled";
    }
    
}
