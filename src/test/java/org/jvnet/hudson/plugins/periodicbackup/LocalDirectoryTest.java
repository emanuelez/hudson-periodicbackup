package org.jvnet.hudson.plugins.periodicbackup;

import com.google.common.collect.Lists;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Author: tblaszcz
 * Date: 17-01-11
 */
public class LocalDirectoryTest extends TestCase {
    @Test
    public void testGetAvailableBackups() throws Exception {
        File path = new File(Thread.currentThread().getContextClassLoader().getResource("data/").getFile());
        LocalDirectory localDirectory = new LocalDirectory(path, true);
        Date expectedDate = new Date(123);
        BackupObject expected = new BackupObject(
                new FullBackup(),
                new ZipStorage(),
                new LocalDirectory(new File("c:\\Temp"), true),
                expectedDate);

        Iterable<BackupObject> obtainedResult = localDirectory.getAvailableBackups();
        List<BackupObject> listFromResult = Lists.newArrayList(obtainedResult);
        assertEquals(listFromResult.size(), 1);
        assertEquals(expected, listFromResult.get(0));
    }

    @Test
    public void testStoreBackupInLocation() throws IOException {
        File destination = new File(Thread.currentThread().getContextClassLoader().getResource("data/destination/").getFile());
        File archive1 = new File(Thread.currentThread().getContextClassLoader().getResource("data/archive1").getFile());
        File archive2 = new File(Thread.currentThread().getContextClassLoader().getResource("data/archive2").getFile());
        File backupObjectFile = new File(Thread.currentThread().getContextClassLoader().getResource("data/test.pbobj").getFile());
        List<File> archives = Lists.newArrayList(archive1, archive2);
        LocalDirectory localDirectory = new LocalDirectory(destination, true);

        localDirectory.storeBackupInLocation(archives, backupObjectFile);
        File backupObjectFileInLocation = new File(Thread.currentThread().getContextClassLoader().getResource("data/destination/test.pbobj").getFile());
        assertTrue(backupObjectFileInLocation.exists());
        assertEquals(backupObjectFileInLocation.getUsableSpace(), backupObjectFile.getUsableSpace());
        File[] filesInLocation = destination.listFiles();
        assertTrue(filesInLocation.length == 4);  //2 archives + backup object file + dummy == 4

        for(File f : filesInLocation) {
            if(!f.delete()) {
                throw new IOException("Could not delete file " + f.getAbsolutePath());
            }
        }

    }
}
