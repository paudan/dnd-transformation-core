package org.ktu.transformations.notifiers;

/**
 * Interface which defines the functionality of the additional actions which should be performed after element generation. 
 * It defines contract for the objects that should receive notifications from {@link NotificationObservable} objects
 * This interface should be implemented by implementations which perform additional logging, etc.
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of
 * Information Systems Design Technologies, Kaunas University of Technology, 2014-2015
 */
public interface NotificationObserver {
    
    /**
     * Perform update, according to the given parameters
     * @param generated Generated UML elements
     * @param text      Text which is sent by the {@link NotificationObservable} objects
     * @param type      {@link NotificationType} value which defines the type of the message sent by the {@link NotificationObservable} objects
     */
    void update(Object[] generated, String text, NotificationType type);
    
}
