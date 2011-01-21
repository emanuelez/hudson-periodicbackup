package org.jvnet.hudson.plugins.periodicbackup;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Author: tblaszcz
 * Date: 19-01-11
 */
public class ZipStorageTest extends HudsonTestCase {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        super.recipeLoadCurrentPlugin();
    }

    @Test
    public void testArchiveFiles() throws Exception {
        ZipStorage zipStorage = new ZipStorage();
        File tempDirectory = new File(Thread.currentThread().getContextClassLoader().getResource("data/temp/").getFile());
        File archive1 = new File(Thread.currentThread().getContextClassLoader().getResource("data/archive1").getFile());
        File archive2 = new File(Thread.currentThread().getContextClassLoader().getResource("data/archive2").getFile());
        List<File> archives = Lists.newArrayList(archive1, archive2);
        String baseFileName = "baseFileName";
        assertTrue(archives != null);
        assertTrue(tempDirectory.getAbsolutePath() != null);
        zipStorage.archiveFiles(archives, tempDirectory.getAbsolutePath(), baseFileName);

        File expectedResult = new File(tempDirectory, baseFileName + "." + zipStorage.getDescriptor().getArchiveFileExtension());
        assertTrue(expectedResult.exists());
        assertTrue(expectedResult.getUsableSpace() > 0);
        assertTrue(Util.isValidZip(expectedResult));

        if(!expectedResult.delete()) {
                throw new IOException("Could not delete file " + expectedResult.getAbsolutePath());
        }

    }
}
