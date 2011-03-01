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
import com.google.common.collect.Sets;
import hudson.util.DescribableList;
import org.codehaus.plexus.archiver.ArchiverException;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class BackupExecutor {

    private final Set<File> filesToBackup = Sets.newHashSet();
    private static final Logger LOGGER = Logger.getLogger(BackupExecutor.class.getName());

    public void backup(FileManager fileManager,
                       DescribableList<Storage, StorageDescriptor> storages,
                       DescribableList<Location, LocationDescriptor> locations,
                       String tempDirectory,
                       int cycleQuantity,
                       int cycleDays) throws ArchiverException, PeriodicBackupException, IOException {
        long start = System.currentTimeMillis();
        //collecting files for backup
        for(File f: fileManager.getFilesToBackup()) {
            filesToBackup.add(f);
        }
        File backupObjectFile;
        //creating backup archives for each storage defined
        Date timestamp = new Date();
        String fileNameBase = Util.generateFileNameBase(timestamp);
        Calendar timeThreshold = Calendar.getInstance();
        timeThreshold.add(Calendar.DAY_OF_MONTH, (-1 * cycleDays));

        for (Storage storage : storages) {
            storage.backupStart(tempDirectory, fileNameBase);
            for (File fileToBackup : filesToBackup) {
                storage.backupAddFile(fileToBackup);
            }
            Iterable<File> archives = storage.backupStop();
            for (Location location : locations) {
                //sends all backup archives and backup files to all activated locations
                if(location.enabled) {
                    BackupObject backupObject = new BackupObject(fileManager, storage, location, timestamp);
                    backupObjectFile = Util.createBackupObjectFile(backupObject, tempDirectory, fileNameBase);
                    location.storeBackupInLocation(archives, backupObjectFile);

                    // Delete the temporary files
                    LOGGER.info("Deleting the temporary file " + backupObjectFile.getAbsolutePath());
                    if (!backupObjectFile.delete()) {
                        LOGGER.warning("Could not delete " + backupObjectFile.getAbsolutePath());
                    }


                    List<BackupObject> backupsInLocation = Lists.newArrayList(location.getAvailableBackups());
                    LOGGER.info("Checking for redundant and old backups in the location.");

                    int index1 = -1; // index in backupsInLocation if the number of backups exceeds the allowed one
                    int index2 = -1; // index in backupsInLocation if the backups are older than allowed

                    if (backupsInLocation.size() > cycleQuantity) {
                        index1 = backupsInLocation.size() - cycleQuantity;
                    }

                    for (BackupObject backupObj : backupsInLocation) {
                        if(backupObj.getTimestamp().before(timeThreshold.getTime())) {
                            index2++;
                        }
                    }

                    if(index1 != -1 || index2 != -1) {
                        for (int index = 0; index <= Math.max(index1, index2); index++) {
                            location.deleteBackupFile(backupsInLocation.get(index));
                        }
                    }

                }
                else {
                    LOGGER.info(location.getDisplayName() + " is disabled, ignoring.");
                }
            }
            for (File f : archives) {
                LOGGER.info("Deleting temporary file " + f.getAbsolutePath());
                if (!f.delete()) {
                    LOGGER.warning("Could not delete " + f.getAbsolutePath());
                }
            }
        }
        LOGGER.info("Backup finished successfully after " + (System.currentTimeMillis() - start) + " ms" );
    }
}
