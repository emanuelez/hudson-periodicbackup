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
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

public class ZipStorage extends Storage {

    private static final Logger LOGGER = Logger.getLogger(ZipStorage.class.getName());

    private transient ZipFile zipFile;
    private transient File firstArchiveFile;
    private transient ZipParameters parameters;
    private transient int filesCounter;
    private transient List<File> archives;

    @DataBoundConstructor
    public ZipStorage() {
        super();
    }

    @Override
    public void backupStart(String tempDirectoryPath, String archiveFilenameBase) throws PeriodicBackupException {
        filesCounter = 0;
        archives = Lists.newArrayList();
        File tempDirectory = new File(tempDirectoryPath);
        String archiveFilePathBase = Util.createFileName(archiveFilenameBase, getDescriptor().getArchiveFileExtension());
        try {
            firstArchiveFile = new File(tempDirectory, archiveFilePathBase);
            zipFile= new ZipFile(firstArchiveFile);
        } catch (ZipException e) {
            LOGGER.warning("There was a problem with creating zip object. " + e.getMessage());
        }
        parameters = new ZipParameters();
        // set compression method to store compression
        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        // Set the compression level. This value has to be in between 0 to 9
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
    }

    @Override
    public void backupAddFile(File fileToStore) throws PeriodicBackupException {
        try {
            if(filesCounter == 0) {
                zipFile.createZipFile(fileToStore, parameters);
            }
            else {
                zipFile.addFile(fileToStore, parameters);
            }
            filesCounter++;
        } catch(ZipException e) {
            throw new PeriodicBackupException("There was a problem with adding file to the zip archive. " + e.getMessage());
        }

    }

    @Override
    public Iterable<File> backupStop() {
        archives.add(firstArchiveFile);
        return archives;
    }

    @Override
    public void unarchiveFiles(Iterable<File> archives, File tempDir) {
        for(File archive : archives) {
            if(Util.getExtension(archive) != null && Util.getExtension(archive).equals(this.getDescriptor().getArchiveFileExtension())) {
                try {
                    zipFile = new ZipFile(archive);
                    LOGGER.info("Extracting files from " + archive.getAbsolutePath() + " to " + tempDir.getAbsolutePath());
                    zipFile.extractAll(tempDir.getAbsolutePath());
                } catch (ZipException e) {
                    LOGGER.warning("Could not extract from " + archive.getAbsolutePath() + e.getMessage());
                }
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
