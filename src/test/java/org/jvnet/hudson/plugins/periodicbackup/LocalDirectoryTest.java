package org.jvnet.hudson.plugins.periodicbackup;

import com.google.common.collect.Lists;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.File;
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
        BackupObject expected = new BackupObject(
                new FullBackup(),
                new ZipStorage(),
                new LocalDirectory(new File("c:\\Temp"), true));
        Iterable<BackupObject> obtainedResult = localDirectory.getAvailableBackups();
        List<BackupObject> listFromResult = Lists.newArrayList(obtainedResult);
        assertEquals(listFromResult.size(), 1);
        assertEquals(expected, listFromResult.get(0));
    }
}
