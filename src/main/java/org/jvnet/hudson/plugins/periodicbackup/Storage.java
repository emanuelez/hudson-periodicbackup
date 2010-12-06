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

import java.io.File;

public abstract class Storage extends PeriodicBackupPlugin {

    public static abstract class StorageDescriptor extends PeriodicBackupPluginDescriptor {}

    public abstract String getDisplayName();

    @Override
    public PeriodicBackupPluginDescriptor getDescriptor() {
        return (PeriodicBackupPluginDescriptor)super.getDescriptor();
    }

    /**
     *
     * This method compressed the files and folders that, at this point, must be already
     * determined by a FileManager plugin
     *
     * @param filesToCompress The files and folders to archive
     * @return File object of the archive
     */
    public abstract File store(Iterable<String> filesToCompress);

    /**
     *
     * This method un-compressed the archive to a temporary location.
     * The actual file restoring is done by the FileManager plugin
     *
     * @param compressedFile The archive to un-compress
     * @return The files and folders un-compressed
     */
    public abstract Iterable<String> unStore(File compressedFile);

}
