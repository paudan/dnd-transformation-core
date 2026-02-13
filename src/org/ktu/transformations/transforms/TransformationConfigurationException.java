package org.ktu.transformations.transforms;

/**
 * Exception class, defining exception which is thrown if the transformation object is not configured properly. 
 * Such situation may arise if some components, such element producer, property manager, required for the execution of the transformation, are absent or not set  
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, 
 * Kaunas University of Technology, 2015
 */
public class TransformationConfigurationException extends Exception {

    /**
     * Create a new instance of {@link TransformationConfigurationException} without detail message.
     */
    public TransformationConfigurationException() {
    }

    /**
     * Constructs an instance of {@link TransformationConfigurationException} with the specified detail message.
     * @param msg the detail message.
     */
    public TransformationConfigurationException(String msg) {
        super(msg);
    }
}
