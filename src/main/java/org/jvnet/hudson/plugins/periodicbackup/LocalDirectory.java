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

import hudson.Extension;
import hudson.util.FormValidation;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.File;

public class LocalDirectory extends Location {

    public final File path;

    @DataBoundConstructor
    public LocalDirectory(File path) {
        super();
        this.path = path;
    }

    @Override
    public Iterable<String> getAvailableBackups() {
        //TODO: implement
        return null;
    }

    @Override
    public void storeBackupInLocation(Iterable<File> backups) {
        //TODO: implement
    }

    public String getDisplayName() {
        return "LocalDirectory: " + path;
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