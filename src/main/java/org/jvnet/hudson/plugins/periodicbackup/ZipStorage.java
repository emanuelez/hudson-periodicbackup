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
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ZipStorage extends Storage {

    @DataBoundConstructor
    public ZipStorage() {
        super();
    }

    @Override
    public Iterable<File> archiveFiles(Iterable<File> filesToCompress, String tempDirectoryPath, String fileNameBase) throws IOException {
        List<File> archives = new ArrayList<File>();
        String archiveFilePath = Util.createFileName(fileNameBase, getDescriptor().getArchiveFileExtension());
        ZipArchiver archiver = new ZipArchiver();
        File tempDirectory = new File(tempDirectoryPath);

        archiver.setDestFile(new File(tempDirectory, archiveFilePath));

        for(File f: filesToCompress) {
            try {
                archiver.addFile(f, Util.getRelativePath(f, Hudson.getInstance().getRootDir()));
            } catch (ArchiverException e) {
                e.printStackTrace();  //TODO: proper exception handling
            }
        }
        try {
            archiver.createArchive();
        } catch (ArchiverException e) {
            e.printStackTrace();  //TODO: proper exception handling
        }
        archives.add(archiver.getDestFile());
        return archives;
    }
    //TODO: implement
    @Override
    public Iterable<String> unarchiveFiles(File compressedFile) {
        return null;
    }

    public String getDisplayName() {
        return "Zip";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        ZipStorage that = (ZipStorage) o;
        return this.getClass().equals(that.getClass());
    }

    @Extension
    public static class DescriptorImpl extends StorageDescriptor {
        public String getDisplayName() {
            return "ZipStorage";
        }

        @Override
        public String getArchiveFileExtension() {
            return "zip";
        }

        @Override
        public boolean isValidArchive(File file) {
            return Util.isValidZip(file);
        }
    }
}
