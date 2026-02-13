package org.ktu.transformations.elements;

/**
 * An {@link Exception} which is thrown if the Element could not be successfully generated
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, Kaunas University of Technology, 2015
 */
@SuppressWarnings("serial")
public class ElementGenerationException extends Exception {

    /** @see Exception#Exception(String)  */
    public ElementGenerationException(String message) {
        super(message);
    }

    /** @see Exception#Exception(String, Throwable)   */
    public ElementGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    /** @see Exception#Exception(java.lang.Throwable)    */
    public ElementGenerationException(Exception cause) {
        super(cause);
    }

}
