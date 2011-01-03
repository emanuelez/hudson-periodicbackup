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

public abstract class FileManager extends AbstractModelObject implements Describable<FileManager> {

    RestorePolicy restorePolicy;


    /**
     * This method determines files and folders for Storage
     *
     * @return Files to be included in the backup
     */
    public abstract Iterable<File> getFilesToBackup();

    /**
     * This will restore files to their right place in the HUDSON directory
     *
     * @param filesToBeRestored Files from the backup, unarchived by Storage
     */
    public void restoreFiles(Iterable<File> filesToBeRestored) {
        restorePolicy.restore(filesToBeRestored);
    }

    /**
     * This will allow to retrieve the list of plugins at runtime
     *
     * @return Collection of FileManager Descriptors
     */
    public static DescriptorExtensionList<FileManager, FileManagerDescriptor> all() {
        return Hudson.getInstance().getDescriptorList(FileManager.class);
    }

    public FileManagerDescriptor getDescriptor() {
        return (FileManagerDescriptor) Hudson.getInstance().getDescriptor(getClass());
    }

    public String getSearchUrl() {
        return "FileManager";
    }

}
