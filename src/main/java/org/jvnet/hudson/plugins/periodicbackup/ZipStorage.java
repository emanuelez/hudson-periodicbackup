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

import com.google.common.collect.Lists;
import hudson.Extension;
import hudson.model.Hudson;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class ZipStorage extends Storage {

    private static final Logger LOGGER = Logger.getLogger(LocalDirectory.class.getName());

    @DataBoundConstructor
    public ZipStorage() {
        super();
    }

    @Override
    public Iterable<File> archiveFiles(Iterable<File> filesToCompress, String tempDirectoryPath, String fileNameBase) throws IOException, ArchiverException {
        List<File> archives = Lists.newArrayList();
        String archiveFilePath = Util.createFileName(fileNameBase, getDescriptor().getArchiveFileExtension());
        ZipArchiver archiver = new ZipArchiver();
        File tempDirectory = new File(tempDirectoryPath);

        archiver.setDestFile(new File(tempDirectory, archiveFilePath));

        for(File f: filesToCompress) {
            archiver.addFile(f, Util.getRelativePath(f, Hudson.getInstance().getRootDir()));
        }

        archiver.createArchive();

        archives.add(archiver.getDestFile());
        return archives;
    }

    @Override
    public void unarchiveFiles(Iterable<File> archives, File tempDir) {
        ZipUnArchiver unarchiver = new ZipUnArchiver();
        unarchiver.setDestDirectory(tempDir);
        unarchiver.enableLogging(new ConsoleLogger(org.codehaus.plexus.logging.Logger.LEVEL_INFO, "UnArchiver"));
        for(File archive : archives) {
            unarchiver.setSourceFile(archive);
            LOGGER.info("Extracting files from " + archive.getAbsolutePath() + " to " + tempDir.getAbsolutePath());
            try {
                unarchiver.extract();
            } catch (ArchiverException e) {
                LOGGER.warning("Could not extract from " + archive.getAbsolutePath() + e.getMessage());
            }
            LOGGER.info("Deleting " + archive.getAbsolutePath());
            if(!archive.delete()) {
                LOGGER.warning("Could not delete " + archive.getAbsolutePath());
            }
        }
    }

    public String getDisplayName() {
        return "Zip";
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ZipStorage;
    }

    @Override
    public int hashCode() {
        return 93;
    }

    @SuppressWarnings("unused")
    @Extension
    public static class DescriptorImpl extends StorageDescriptor {
        public String getDisplayName() {
            return "ZipStorage";
        }

        @Override
        public String getArchiveFileExtension() {
            return "zip";
        }

    }
}
