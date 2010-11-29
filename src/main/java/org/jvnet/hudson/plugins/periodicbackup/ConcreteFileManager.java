package org.jvnet.hudson.plugins.periodicbackup;

import hudson.Extension;

/**
 * Created by IntelliJ IDEA.
 * Author: tblaszcz
 * Date: 26-11-2010
 */
public class ConcreteFileManager extends AbstractFileManager {

    @Extension
    public static class DescriptorImpl extends AbstractFileManagerDescriptor {
        public String getDisplayName() {
            return "Concrete File Manager";
        }
    }
    
}
