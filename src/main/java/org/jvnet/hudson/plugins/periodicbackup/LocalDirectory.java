package org.jvnet.hudson.plugins.periodicbackup;
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

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import hudson.Extension;
import hudson.util.FormValidation;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class LocalDirectory extends Location {

    public final File path;

    @DataBoundConstructor
    public LocalDirectory(File path, boolean enabled) {
        super(enabled);
        this.path = path;
    }

    @Override
    public Iterable<BackupObject> getAvailableBackups() {
        File[] files = path.listFiles(Util.backupObjectFileFilter());
        Set<File> backupObjectFiles = Sets.newHashSet(files);
        return Iterables.transform(backupObjectFiles, BackupObject.getFromFile());
    }

    @Override
    public void storeBackupInLocation(Iterable<File> archives, File backupObjectFile) {
        if (this.enabled && path.exists()) {
            try {
                File backupObjectFileDestination = new File(path, backupObjectFile.getName());

                Files.copy(backupObjectFile, backupObjectFileDestination);
                System.out.println("[INFO] " + backupObjectFile.getName() + " copied to " + backupObjectFileDestination.getAbsolutePath()); //TODO: logger instead
                for (File f : archives) {
                    {
                        File destination = new File(path, f.getName());
                        Files.copy(f, destination);
                        System.out.println("[INFO] " + f.getName() + " copied to " + destination.getAbsolutePath()); //TODO: logger instead
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();  //TODO: proper exception handling
            }
        }
        else {
            System.out.println("[WARNING] skipping location " + this.path + ", location is disabled or doesn't exist."); //TODO: logger instead
        }

    }

    @Override
    public Iterable<File> retrieveBackupFromLocation(Iterable<File> backup, File tempDir) throws IOException {
        //TODO: implement with BackupObject file
        return null;
        /*if((tempDir.exists() && !tempDir.isDirectory()) || tempDir.canWrite()) {
            System.out.println("[ERROR] Temporary directory is not a directory or is not writable."); //TODO: logger instead
            return null; //TODO: throw exception?
        }
        List<File> backupInTempDir = new ArrayList<File>();
        for(File f : backup) {
            FileUtils.copyFileToDirectory(tempDir, f);
            backupInTempDir.add(new File(tempDir,f.getName()));
        }
        return backupInTempDir;*/
    }

    public String getDisplayName() {
        return "LocalDirectory: " + path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LocalDirectory that = (LocalDirectory) o;

        if (path != null ? !path.equals(that.path) : that.path != null) return false;
        if (this.enabled != that.enabled) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result =  path != null ? path.hashCode() : 0;
        result = 43 * result + (enabled ? 1 : 0);
        return result;
    }

    @Extension
    public static class DescriptorImpl extends LocationDescriptor {
        public String getDisplayName() {
            return "LocalDirectory";
        }

        public FormValidation doTestPath(@QueryParameter String path) {
            try {
                return FormValidation.ok(validatePath(path));
            } catch (FormValidation f) {
                return f;
            }
        }

        public String validatePath(String path) throws FormValidation {
            File fileFromString = new File(path);
            if (!fileFromString.exists() || !fileFromString.isDirectory() || !fileFromString.canWrite())
                throw FormValidation.error(path + " doesn't exists or is not a writable directory");
            return "directory \"" + path + "\" OK";
        }

    }
}