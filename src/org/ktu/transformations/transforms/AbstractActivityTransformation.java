package org.ktu.transformations.transforms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import org.ktu.transformations.elements.ConnectingElementFinder;
import org.ktu.transformations.elements.SimpleConnectingElementFinder;
import org.ktu.transformations.helpers.ElementSearch;
import org.ktu.transformations.mappers.ElementMapper;

/**
 * A factory class which performs transformation to UML SwimLane elements
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems 
 * Design Technologies, Kaunas University of Technology, 2015
 * @param <Element>         Type, corresponding to actual UML Element implementation
 * @param <Stereotype>      Type, corresponding to actual UML Stereotype implementation
 */
abstract public class AbstractActivityTransformation<Element, Stereotype> extends AbstractContainerTransformation<Element, Stereotype> {
    
    public AbstractActivityTransformation() throws TransformationConfigurationException {
        super();
    }

    static class ActivityElementFinder<Element> extends SimpleConnectingElementFinder<Element> {

        protected ArrayList<String> propNames;
        protected ArrayList<ArrayList<Set<Element>>> finalMap;
        protected int index;

        public ActivityElementFinder(Element targetPackage, String connMapName, Element connectingEl, Transformation<Element, ?> factory,
                ArrayList<String> propNames, ArrayList<ArrayList<Set<Element>>> finalMap, int index) {
            super(targetPackage, connMapName, connectingEl, factory);
            this.propNames = propNames;
            this.finalMap = finalMap;
            this.index = index;
            this.factory = factory;
        }

        @Override
        public Element find() {
            String mainelName = null;
            ElementMapper<Element, ?, ?> mapper = factory.getElementMapper();
            ElementSearch<Element, ?> search = factory.getElementSearch();
            for (int k = 0; k < propNames.size(); k++)
                if (propNames.get(k).compareTo("represents") == 0) {
                    mainelName = mapper.getElementName2((Element) finalMap.get(k).get(index).toArray()[0]);
                    break;
                }
            Collection<? extends Element> list = search.getElementsOfType(rootElement, new Class<?>[]{mapper.getActivityPartitionClass()}, true);
            if (mainelName != null)
                for (Element e : list) {
                    Object obj = factory.getPropertyManager().getFeatureValue(e, "represents");
                    if (!mapper.isElement(obj))
                        continue;
                    Element item = (Element) obj;
                    String name = mapper.getProperName(mapper.getElementName2(item));
                    if (item != null && name.compareTo(mainelName) == 0)
                        return e;
                }
            return null;
        }
    }
    
    @Override
    protected ConnectingElementFinder<Element> getConnectingElementFinder(Element targetPackage, String connMapName, Element connectingEl,
            ArrayList<String> propNames, ArrayList<ArrayList<Set<Element>>> finalMap, int index) {
        return new ActivityElementFinder<>(targetPackage, connMapName, connectingEl, this, propNames, finalMap, index);
    }

}
