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
import com.google.common.base.Objects;
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

    @SuppressWarnings("unused")
    public FileManager getFileManager() {
        return fileManager;
    }

    @SuppressWarnings("unused")
    public Storage getStorage() {
        return storage;
    }

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    public Date getTimestamp() {
        return this.timestamp;
    }

    @SuppressWarnings("unused")
    public String getDisplayName() {
        return fileManager.getDisplayName() + " created on " + timestamp.toString();
    }

    public String getAsString() {
        return Hudson.XSTREAM.toXML(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof BackupObject) {
            BackupObject that = (BackupObject) o;
            return Objects.equal(this.fileManager, that.fileManager)
                && Objects.equal(this.location,    that.location)
                && Objects.equal(this.storage,     that.storage)
                && Objects.equal(this.timestamp,   that.timestamp);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(fileManager, storage, location, timestamp);
    }
}
