package org.ktu.transformations.parsers;

/**
 * An exception which is thrown if transformation pattern is not valid or cannot be processed
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, Kaunas University of Technology, 2014-2015
 */
@SuppressWarnings("serial")
public class InvalidPatternException extends Exception {

    public InvalidPatternException(String message) {
        super(message);
    }

    public InvalidPatternException(String message, Throwable cause) {
        super(message, cause);
    }

}
