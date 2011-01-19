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

import hudson.Extension;
import hudson.model.Hudson;
import org.apache.tools.ant.DirectoryScanner;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FullBackup extends FileManager {

    @DataBoundConstructor
    public FullBackup() {
        super();
        this.restorePolicy = new ReplaceRestorePolicy();
    }

    public String getDisplayName() {
        return "FullBackup";
    }

    @Override
    public Iterable<File> getFilesToBackup() throws IOException {
        DirectoryScanner directoryScanner = new DirectoryScanner();
        directoryScanner.setBasedir(Hudson.getInstance().getRootDir());
        directoryScanner.scan();
        List<File> files = new ArrayList<File>();
        for(String s: directoryScanner.getIncludedFiles()) {
            files.add(new File(directoryScanner.getBasedir(), s));
        }
        return files;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        FullBackup that = (FullBackup) o;
        return this.getClass().equals(that.getClass());
    }


    @Extension
    public static class DescriptorImpl extends FileManagerDescriptor {
        public String getDisplayName() {
            return "FullBackup";
        }

    }
}
