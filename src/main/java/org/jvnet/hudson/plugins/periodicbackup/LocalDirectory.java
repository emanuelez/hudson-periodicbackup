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

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import hudson.Extension;
import hudson.util.FormValidation;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Logger;

public class LocalDirectory extends Location {


    private File path;
    private static final Logger LOGGER = Logger.getLogger(LocalDirectory.class.getName());

    @DataBoundConstructor
    public LocalDirectory(File path, boolean enabled) {
        super(enabled);
        this.path = path;
    }

    @Override
    public Iterable<BackupObject> getAvailableBackups() {
        if( ! Util.isWritableDirectory(path)) {
            LOGGER.warning(path.getAbsolutePath() + " is not a existing/writable directory.");
            return Sets.newHashSet();
        }
        if(path.listFiles().length == 0) {
            return Sets.newHashSet();
        }
        File[] files = path.listFiles(Util.backupObjectFileFilter());
        Set<File> backupObjectFiles = Sets.newHashSet(files);
        return Iterables.transform(backupObjectFiles, BackupObject.getFromFile());
    }

    @Override
    public void storeBackupInLocation(Iterable<File> archives, File backupObjectFile) throws IOException {
        if (this.enabled && path.exists()) {
            File backupObjectFileDestination = new File(path, backupObjectFile.getName());

            Files.copy(backupObjectFile, backupObjectFileDestination);
            LOGGER.info(backupObjectFile.getName() + " copied to " + backupObjectFileDestination.getAbsolutePath());
            for (File f : archives) {
                File destination = new File(path, f.getName());
                Files.copy(f, destination);
                LOGGER.info(f.getName() + " copied to " + destination.getAbsolutePath());
            }
        }
        else {
            LOGGER.warning("skipping location " + this.path + " since it is disabled or it doesn't exist.");
        }

    }

    @Override
    public Iterable<File> retrieveBackupFromLocation(final BackupObject backup, File tempDir) throws IOException {
        File[] files = path.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().contains( Util.getFormattedDate(BackupObject.FILE_TIMESTAMP_PATTERN, backup.getTimestamp()));
            }
        });
        return Sets.newHashSet(files);
        //TODO: finish
    }

    public String getDisplayName() {
        return "LocalDirectory: " + path;
    }

    public File getPath() {
        return path;
    }

    public void setPath(File path) {
        this.path = path;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof LocalDirectory) {
            LocalDirectory that = (LocalDirectory) o;
            return Objects.equal(this.path, that.path)
                && Objects.equal(this.enabled, that.enabled);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(path, enabled);
    }

    @Extension
    public static class DescriptorImpl extends LocationDescriptor {
        public String getDisplayName() {
            return "LocalDirectory";
        }

        @SuppressWarnings("unused")
        public FormValidation doTestPath(@QueryParameter String path) {
            try {
                return FormValidation.ok(validatePath(path));
            } catch (FormValidation f) {
                return f;
            }
        }

        public String validatePath(String path) throws FormValidation {
            File fileFromString = new File(path);
            if ( ! Util.isWritableDirectory(fileFromString))
                throw FormValidation.error(path + " doesn't exists or is not a writable directory");
            return "directory \"" + path + "\" OK";
        }

    }
}