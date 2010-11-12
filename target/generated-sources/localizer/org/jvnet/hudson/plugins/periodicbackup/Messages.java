// CHECKSTYLE:OFF

package org.jvnet.hudson.plugins.periodicbackup;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;

@SuppressWarnings({
    "",
    "PMD"
})
public class Messages {

    private final static ResourceBundleHolder holder = ResourceBundleHolder.get(Messages.class);

    /**
     * Periodic Backup Manager
     * 
     */
    public static String displayName() {
        return holder.format("displayName");
    }

    /**
     * Periodic Backup Manager
     * 
     */
    public static Localizable _displayName() {
        return new Localizable(holder, "displayName");
    }

    /**
     * Periodically backup your Hudson data and save the day.
     * 
     */
    public static String description() {
        return holder.format("description");
    }

    /**
     * Periodically backup your Hudson data and save the day.
     * 
     */
    public static Localizable _description() {
        return new Localizable(holder, "description");
    }

}
