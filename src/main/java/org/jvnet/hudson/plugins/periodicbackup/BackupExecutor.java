package org.jvnet.hudson.plugins.periodicbackup;

import hudson.util.DescribableList;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class BackupExecutor {

    public Set<File> filesToBackup = new HashSet<File>();

    public void backup(DescribableList<FileManager, FileManagerDescriptor> fileManagers,
                       DescribableList<Storage, StorageDescriptor> storages,
                       DescribableList<Location, LocationDescriptor> locations,
                       String tempDirectory) throws IOException {
        //collecting files for backup
        for(FileManager fm: fileManagers) {
            for(File f: fm.getFilesToBackup()) {
                filesToBackup.add(f);
            }
        }
        //creating backup archives for each storage defined
        for(Storage s: storages) {
            Iterable<File> backup = s.archiveFiles(filesToBackup, tempDirectory);
            //sends all backup archives to all activated locations
            for(Location l: locations) {
                l.storeBackupInLocation(backup);
            }
            for(File f: backup) {
                System.out.println("[INFO] Deleting temporary file " + f.getAbsolutePath()); //TODO: logger instead
                f.delete();
            }
        }
    }
}
