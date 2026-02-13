package org.ktu.transformations.notifiers;

/**
 * Interface which defines contract for the objects that should send notifications to {@link NotificationObserver} objects  
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, 
 * Kaunas University of Technology, 2014-2015
 */
public interface NotificationObservable {
    
    /**
     * Register a new {@link NotificationObserver} which would receive notifications from this object
     * @param obj The object which is registered as the notifications receiver
     */
    void register(NotificationObserver obj);
    
    /**
     * Unregister existing {@link NotificationObserver} to stop receiving notifications from this object
     * @param obj The object which is unregistered
     */
    void unregister(NotificationObserver obj);
    
    /**
     * Send notification to all receiving observer objects
     * @param elements  Objects (e.g., generated UML elements) which are the main source or cause of this notification
     * @param text      Notification text
     * @param type      Notification type
     */
    void sendNotification(Object[] elements, String text, NotificationType type);

}
