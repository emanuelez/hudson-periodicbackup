/*
 * The MIT License
 *
 * Copyright (c) 2010 Tomasz Blaszczynski, Emanuele Zattin
 *
 * This plugin is based on and inspired by the "backup" plugin developed by:
 * Vincent Sellier, Manufacture Française des Pneumatiques Michelin, Romain Seguy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


package org.jvnet.hudson.plugins.periodicbackup;

import hudson.Extension;
import hudson.XmlFile;
import hudson.model.Hudson;
import hudson.model.ManagementLink;
import hudson.model.Saveable;
import hudson.util.DescribableList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import static org.jvnet.hudson.plugins.periodicbackup.Storage.StorageDescriptor;

@Extension
public class PeriodicBackupLink extends ManagementLink implements Saveable {

    private final DescribableList<PeriodicBackupPlugin, PeriodicBackupPluginDescriptor> periodicBackupPlugins = new DescribableList<PeriodicBackupPlugin, PeriodicBackupPluginDescriptor>(this);

    public String getDisplayName() {
        return Messages.displayName();
    }

    @Override
    public String getUrlName() {
        return "periodicbackup";
    }

    @Override
    public String getIconFileName() {
        return "/plugin/periodicbackup/images/periodicbackup.png";
    }

    @Override
    public String getDescription() {
        return Messages.description();
    }

    public void save() throws IOException {
        getConfigXml().write(this);
    }

    protected XmlFile getConfigXml() {
        return new XmlFile(Hudson.XSTREAM, new File(Hudson.getInstance().getRootDir(),"periodicBackup.xml"));
    }

    /**
     * All registered descriptors exposed for UI
     */
    public Collection<PeriodicBackupPluginDescriptor> getDescriptors() {
        return PeriodicBackupPlugin.all();
    }

    public Collection<Storage.StorageDescriptor> getStorageDescriptors() {
        Collection<PeriodicBackupPluginDescriptor> all = getDescriptors();
        ArrayList<StorageDescriptor> storages = new ArrayList<StorageDescriptor>();
        for(PeriodicBackupPluginDescriptor descriptor : all){
            if(descriptor instanceof StorageDescriptor) {
                storages.add((StorageDescriptor)descriptor);
            }
        }
        return storages;



    }

    public Collection<AbstractFileManager.AbstractFileManagerDescriptor> getFileManagerDescriptors() {
        Collection<PeriodicBackupPluginDescriptor> all = getDescriptors();
        ArrayList<AbstractFileManager.AbstractFileManagerDescriptor> managers = new ArrayList<AbstractFileManager.AbstractFileManagerDescriptor>();
        for(PeriodicBackupPluginDescriptor descriptor : all){
            if(descriptor instanceof AbstractFileManager.AbstractFileManagerDescriptor) {
                managers.add((AbstractFileManager.AbstractFileManagerDescriptor)descriptor);
            }
        }
        return managers;
    }

    public DescribableList<PeriodicBackupPlugin, PeriodicBackupPluginDescriptor> getPeriodicBackupPlugins() {
        return periodicBackupPlugins;
    }
}
