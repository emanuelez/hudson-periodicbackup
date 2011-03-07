/*
 * The MIT License
 *
 * Copyright (c) 2010 - 2011, Tomasz Blaszczynski, Emanuele Zattin
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

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class RestoreExecutor implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(RestoreExecutor.class.getName());
    private final BackupObject backupObject;
    private final String tempDirectoryPath;

    public RestoreExecutor(BackupObject backupObject, String tempDirectoryPath) {
        this.backupObject = backupObject;
        this.tempDirectoryPath = tempDirectoryPath;
    }

    public void run() {
        // RestoreExecutor thread is not handled by Jenkins therefore we need to be sure that the safeRestart will not be performed during the restore execution
        PeriodicBackupRestartListener restartListener = PeriodicBackupRestartListener.get();
        restartListener.notReady();

        long start = System.currentTimeMillis(); // Measure the duration of the restore
        File tempDir = new File(tempDirectoryPath);
        if(!Util.isWritableDirectory(tempDir)) {
            LOGGER.warning("Restoration Failure! The temporary folder " + tempDir.getAbsolutePath() + " is not writable. ");
            // Setting message to an empty String will make the "Creating backup..." message disappear in the UI
            PeriodicBackupLink.get().setMessage("");
            return;
        }

        // The temp directory should be empty at this point
        File[] tempDirFileList = tempDir.listFiles();
        if(tempDirFileList.length > 0) {
            LOGGER.warning("The temporary directory " + tempDir.getAbsolutePath() + " is not empty, deleting...");
            try {
                FileUtils.deleteDirectory(tempDir);
            } catch (IOException e) {
                LOGGER.warning("Could not delete " + tempDir.getAbsolutePath() + " " + e.getMessage());
            }
            if(!tempDir.exists()) {
                LOGGER.info(tempDir.getAbsolutePath() + " deleted, making new directory");
                if(!tempDir.mkdir()) {
                    LOGGER.warning("Restoration Failure! Could not create " + tempDir.getAbsolutePath());
                    // Setting message to an empty String will make the "Creating backup..." message disappear in the UI
                    PeriodicBackupLink.get().setMessage("");
                    return;
                }
            }
        }

        // Retrieving archive files related to the given BackupObject
        Iterable<File> archives = null;
        try {
            archives = backupObject.getLocation().retrieveBackupFromLocation(backupObject, tempDir);
        } catch (Exception e) {
            LOGGER.warning("Could not retrieve backup from location. " + e.getMessage());
        }

        // Extracting the backup archives to the temp directory
        backupObject.getStorage().unarchiveFiles(archives, tempDir);
        // At this point in the temp directory should be only the extracted backup archives
        try {
            backupObject.getFileManager().restoreFiles(tempDir);
        } catch (Exception e) {
            LOGGER.warning("Could not restore files. " + e.getMessage());
        }
        LOGGER.info("Restoration finished successfully after " + (System.currentTimeMillis() - start) + " ms");
        // Setting message to an empty String will make the "Creating backup..." message disappear in the UI
        PeriodicBackupLink.get().setMessage("");
        restartListener.ready();
    }
}
