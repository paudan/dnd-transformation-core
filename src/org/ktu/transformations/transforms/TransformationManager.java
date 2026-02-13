package org.ktu.transformations.transforms;

import org.ktu.transformations.parsers.SpecificationReader;

/**
 * Performs management of transformations during their execution, such as storing references to the last used specification reader 
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of
 * Information Systems Design Technologies, Kaunas University of Technology, 2015
 */
public class TransformationManager {
    
    private SpecificationReader reader;
    private static TransformationManager INSTANCE;
    
    /** Create a new instance of {@link TransformationManager}
     * @return  A new instance of {@link TransformationManager}
     */
    public static TransformationManager getInstance() {
        if (INSTANCE == null)
            INSTANCE = new TransformationManager();
        return INSTANCE;
    }
    
    /**
     * Get current specification reader (that was used by the last transformation)
     * @return {@link SpecificationReader} which was used by the last transformation
     */
    public SpecificationReader getCurrentReader() {
        return reader;
    }

    /**
     * Set current specification reader. Should be executed before performing any transformation, after its specification has been read into memory
     * @param aReader {@link SpecificationReader} object
     */
    public void setCurrentReader(SpecificationReader aReader) {
        reader = aReader;
    }

}
