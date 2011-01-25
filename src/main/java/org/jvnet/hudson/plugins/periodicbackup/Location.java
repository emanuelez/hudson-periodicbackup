/*
 * The MIT License
 *
 * Copyright (c) 2010 Tomasz Blaszczynski, Emanuele Zattin
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

import hudson.DescriptorExtensionList;
import hudson.model.AbstractModelObject;
import hudson.model.Describable;
import hudson.model.Hudson;

import java.io.File;
import java.io.IOException;

public abstract class Location extends AbstractModelObject implements Describable<Location> {

    public final boolean enabled;

    public Location(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * This method returns Iterable of BackupObject(s) in this location
     *
     * @return BackupObject(s) in location
     */
    public abstract Iterable<BackupObject> getAvailableBackups();

    /**
     * This method puts archived backup file(s) in location
     * @param archives archive file(s)
     * @param backupObjectFile File with serialized backupObject
     */
    public abstract void storeBackupInLocation(Iterable<File> archives, File backupObjectFile);

    /**
     *
     * @param backup BackupObject related to the backup
     * @param tempDir temporary directory to store archived backup file(s)
     * @return Iterable of backup archive(s) in temporary location
     * @throws java.io.IOException is anything goes wrong
     */
    public abstract Iterable<File>retrieveBackupFromLocation(BackupObject backup, File tempDir) throws IOException;

    /**
     * This will allow to retrieve the list of plugins at runtime
     *
     * @return Collection of FileManager Descriptors
     */
    public static DescriptorExtensionList<Location, LocationDescriptor> all() {
        return Hudson.getInstance().getDescriptorList(Location.class);
    }

    public LocationDescriptor getDescriptor() {
        return (LocationDescriptor) Hudson.getInstance().getDescriptor(getClass());
    }

    public String getSearchUrl() {
        return "Location";
    }

}
