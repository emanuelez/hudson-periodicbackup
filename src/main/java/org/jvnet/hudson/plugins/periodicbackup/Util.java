package org.jvnet.hudson.plugins.periodicbackup;

import java.io.File;

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

}
