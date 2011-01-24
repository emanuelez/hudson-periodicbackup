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

import com.google.common.base.Function;
import com.google.common.io.Files;
import hudson.model.Hudson;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;

public class BackupObject {

    private FileManager fileManager;
    private Storage storage;
    private Location location;
    private Date timestamp;

    public final static String EXTENSION = "pbobj";
    public final static String FILE_TIMESTAMP_PATTERN = "yyyy_MM_dd_HH_mm_ss_SSS";

    public BackupObject(FileManager fileManager, Storage storage, Location location, Date timestamp) {
        this.fileManager = fileManager;
        this.storage = storage;
        this.location = location;
        this.timestamp = timestamp;
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public Storage getStorage() {
        return storage;
    }

    public Location getLocation() {
        return location;
    }

    public static Function<File, BackupObject> getFromFile() {
        return new Function<File, BackupObject>() {
            public BackupObject apply(File file) {
                if(file != null) {
                    try {
                        return (BackupObject) Hudson.XSTREAM.fromXML(Files.toString(file, Charset.defaultCharset()));
                    } catch (IOException e) {
                        return null;
                    }
                } else {
                    return null;
                }
            }
        };
    }

    public Date getTimestamp() {
        return this.timestamp;
    }

    public String getDisplayName() {
        return fileManager.getDisplayName() + " created on " + timestamp.toString();
    }

    public String getAsString() {
        return Hudson.XSTREAM.toXML(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BackupObject that = (BackupObject) o;

        if (fileManager != null ? !fileManager.equals(that.fileManager) : that.fileManager != null) return false;
        if (location != null ? !location.equals(that.location) : that.location != null) return false;
        if (storage != null ? !storage.equals(that.storage) : that.storage != null) return false;
        if (timestamp != null ? !timestamp.equals(that.timestamp) : that.timestamp != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = fileManager != null ? fileManager.hashCode() : 0;
        result = 31 * result + (storage != null ? storage.hashCode() : 0);
        result = 31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        return result;
    }
}
