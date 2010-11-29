package org.jvnet.hudson.plugins.periodicbackup;

import hudson.Plugin;
import hudson.model.ManagementLink;

/**
 * @author Kohsuke Kawaguchi
 */
public class PluginImpl extends Plugin {
    @Override
    public void postInitialize() throws Exception {
        // make sure it's initialized at the start up
        ManagementLink.all().get(PeriodicBackupLink.class);
    }
}
