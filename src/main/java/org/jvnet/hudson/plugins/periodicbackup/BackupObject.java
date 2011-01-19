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

public class BackupObject {

    private FileManager fileManager;
    private Storage storage;
    private Location location;

    public BackupObject(FileManager fileManager, Storage storage, Location location) {
        this.fileManager = fileManager;
        this.storage = storage;
        this.location = location;
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

    public static String getBackupObjectFileExtension() {
        return "pbobj";
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BackupObject that = (BackupObject) o;

        if (fileManager != null ? !fileManager.equals(that.fileManager) : that.fileManager != null) return false;
        if (location != null ? !location.equals(that.location) : that.location != null) return false;
        if (storage != null ? !storage.equals(that.storage) : that.storage != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = fileManager != null ? fileManager.hashCode() : 0;
        result = 31 * result + (storage != null ? storage.hashCode() : 0);
        result = 31 * result + (location != null ? location.hashCode() : 0);
        return result;
    }
}
