package org.ktu.transformations.notifiers;

/**
 * Notification (severity) type enumeration 
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, 
 * Kaunas University of Technology, 2014-2015
 */
public enum NotificationType {
    ERROR("Error"), WARNING("Warning"), INFO("Information");

    private final String name;

    public final String getName() { return name; }

    private NotificationType(String name) {
        this.name = name;
    }
}
