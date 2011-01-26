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
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class RestoreExecutor {

    private static final Logger LOGGER = Logger.getLogger(LocalDirectory.class.getName());

    public void restore(BackupObject backupObject, String tempDirectoryPath) throws IOException, PeriodicBackupException {
        File tempDir = new File(tempDirectoryPath);
        if(!Util.isWritableDirectory(tempDir)) {
            throw new PeriodicBackupException("The temporary folder " + tempDir.getAbsolutePath() + " is not writable.");
        }
        File[] tempDirFileList = tempDir.listFiles();
        if(tempDirFileList.length > 0) {
            LOGGER.warning("The temporary directory " + tempDir.getAbsolutePath() + " is not empty, deleting...");
            FileUtils.deleteDirectory(tempDir);
            LOGGER.info(tempDir.getAbsolutePath() + " deleted, making new directory");
            if(!tempDir.mkdir()) {
                LOGGER.warning("Could not create " + tempDir.getAbsolutePath());
                throw new PeriodicBackupException("Could not create " + tempDir.getAbsolutePath());
            }
        }

        Iterable<File> archives = backupObject.getLocation().retrieveBackupFromLocation(backupObject, tempDir);

        backupObject.getStorage().unarchiveFiles(archives, tempDir);

        List<File> filesToRestore = Lists.newArrayList(tempDir.listFiles());

        backupObject.getFileManager().restoreFiles(filesToRestore, tempDir);

        LOGGER.info("Restoration successful!");
    }

}
