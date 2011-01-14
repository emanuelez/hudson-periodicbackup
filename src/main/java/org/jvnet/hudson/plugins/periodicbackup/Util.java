package org.jvnet.hudson.plugins.periodicbackup;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class Util {
    /**
     *
     * This returns relative path of given file with respect to given base directory
     * @param file input file
     * @param baseDir base directory
     * @return String with relative @file path with respect to its @baseDir
     */
    public static String getRelativePath(final File file, final File baseDir) {
        String relativeFilePath = baseDir.toURI().relativize(file.toURI()).getPath();
        return relativeFilePath;
    }

    /**
     * This generates unique archive file name
     * @return archive file name
     */
    public static String generateArchiveFileName() {
        //TODO: proper implementation
        return "test.zip";
    }

    /**
     *
     * This checks if given file is a valid zip file
     * @param file file to validate
     * @return true if zip, false if not
     */
    public static boolean isValidZip(final File file) {
        ZipFile zipfile = null;
        try {
            zipfile = new ZipFile(file);
            return true;
        } catch (ZipException e) {
            return false;
        } catch (IOException e) {
            return false;
        } finally {
            try {
                if (zipfile != null) {
                    zipfile.close();
                    zipfile = null;
                }
            } catch (IOException e) {
            }
        }
    }

}
