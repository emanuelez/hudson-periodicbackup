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

import com.google.common.collect.Sets;
import hudson.Extension;
import hudson.model.Hudson;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.util.Set;
import java.util.logging.Logger;

public class ZipStorage extends Storage {

    public final static int MAX_FILES_PER_ARCHIVE = 65534;     //max allowed amount of files in zip archive is 65535
    public final static long MAX_SIZE_OF_FILES_PER_ARCHIVE = 3999999999l; //max allowed size of uncompressed/compressed size of files in zip archive is 4GiB

    private static final Logger LOGGER = Logger.getLogger(ZipStorage.class.getName());
    private transient long currentArchiveTotalFilesSize;
    private transient ZipArchiver archiver;
    private transient File tempDirectory;
    private transient String archiveFilePathBase;
    private transient int currentArchiveFilesCount;
    private transient int archivesNumber;
    private transient Set<File> archives;

    @DataBoundConstructor
    public ZipStorage() {
        super();
    }

    public long getCurrentArchiveTotalFilesSize() {
        return currentArchiveTotalFilesSize;
    }

    public ZipArchiver getArchiver() {
        return archiver;
    }

    public int getCurrentArchiveFilesCount() {
        return currentArchiveFilesCount;
    }

    public int getArchivesNumber() {
        return archivesNumber;
    }

    @Override
    public void backupStart(String tempDirectoryPath, String archiveFilenameBase) {
        archiver = new ZipArchiver();
        archives = Sets.newHashSet();
        archivesNumber = 1;
        currentArchiveFilesCount = 0;
        currentArchiveTotalFilesSize = 0;
        tempDirectory = new File(tempDirectoryPath);
        this.archiveFilePathBase = archiveFilenameBase;
        String currentArchiveFilePath = archiveFilePathBase + "_" + archivesNumber;
        currentArchiveFilePath = Util.createFileName(currentArchiveFilePath, getDescriptor().getArchiveFileExtension());
        archiver.setDestFile(new File(tempDirectory, currentArchiveFilePath));
    }

    @Override
    public void backupAddFile(File fileToStore) throws PeriodicBackupException {
        if(fileToStore.length() > MAX_SIZE_OF_FILES_PER_ARCHIVE) {
            throw new PeriodicBackupException("Size of file " + fileToStore.getAbsolutePath() + " is bigger then maximum allowed size (" + MAX_SIZE_OF_FILES_PER_ARCHIVE/(1024l) + "kB). Cannot create archive.");
        }
        if((currentArchiveFilesCount + 1) >= MAX_FILES_PER_ARCHIVE || (currentArchiveTotalFilesSize + fileToStore.length()) >= MAX_SIZE_OF_FILES_PER_ARCHIVE) {
            LOGGER.info("Number of files in archive " + archiver.getDestFile().getAbsolutePath() + " exceeded " + MAX_FILES_PER_ARCHIVE + " or total size of file for this archive exceeded " + MAX_SIZE_OF_FILES_PER_ARCHIVE/(1024l) + " kB");
            try {
                archiver.createArchive();
            } catch (Exception e) {
                LOGGER.warning("Could not create archive " + archiver.getDestFile() + " " + e.getMessage());
            }
            archives.add(archiver.getDestFile());
            archivesNumber++;
            currentArchiveFilesCount = 0;
            currentArchiveTotalFilesSize = 0;
            LOGGER.info("Creating new archive");
            archiver = new ZipArchiver();
            String currentArchiveFilePath = archiveFilePathBase + "_" + archivesNumber;
            currentArchiveFilePath = Util.createFileName(currentArchiveFilePath, getDescriptor().getArchiveFileExtension());
            archiver.setDestFile(new File(tempDirectory, currentArchiveFilePath));
        }
        else {
            try {
                archiver.addFile(fileToStore, Util.getRelativePath(fileToStore, Hudson.getInstance().getRootDir()));
            } catch (ArchiverException e) {
                LOGGER.warning("Could not add file to the archive. " + e.getMessage());
            }
            currentArchiveFilesCount++;
            currentArchiveTotalFilesSize += fileToStore.length();
        }
    }

    @Override
    public Iterable<File> backupStop() throws PeriodicBackupException {
        if(!archiver.getFiles().isEmpty()) {
            try {
                archiver.createArchive();
            } catch (Exception e) {
                throw new PeriodicBackupException("Could not create archive " + archiver.getDestFile().getAbsolutePath() + " " + e.getMessage());
            }
            archives.add(archiver.getDestFile());
        }
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
