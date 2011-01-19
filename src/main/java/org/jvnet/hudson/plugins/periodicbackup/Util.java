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

import hudson.model.Hudson;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
     *
     * This generates unique file name without extension
     * @return unique filename
     */
    public static String generateFileName() {
        // create a GregorianCalendar and the current date and time
        Calendar calendar = new GregorianCalendar();
        Date now = new Date();
        calendar.setTime(now);
        String timestamp = calendar.get(Calendar.YEAR) + "_" + (calendar.get(Calendar.MONTH) + 1) + "_" +
                calendar.get(Calendar.DAY_OF_MONTH) + "_" + calendar.get(Calendar.HOUR_OF_DAY) + "_" +
                calendar.get(Calendar.MINUTE) + "_" + calendar.get(Calendar.SECOND);
        return "backup_" + timestamp;
    }

    public static String createFileName(String fileName, String extension){
        return fileName + "." + extension;
    }

     /**
     *
     * This file name for multiple archives
     * @param fileName first part of the file name
     * @param extension archive file extension
     * @param archiveNumber index of current archive
     * @param totalNumberOfArchives total quantity of files
     * @return full file name of current archive
     */
    public static String createFileName(String fileName, String extension, long archiveNumber, long totalNumberOfArchives) {
        //TODO: test
        fileName =  fileName + "_" + archiveNumber + "OF" + totalNumberOfArchives;
        return createFileName(fileName, extension);
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

    /**
     *
     * @param backupObject BackupObject to be serialized
     * @param destinationDir Path to the directory where file will be created
     * @param fileNameBase first part of the filename
     * @return serialized BackupObject File
     * @throws IOException
     */
    public static File createBackupObjectFile(BackupObject backupObject, String destinationDir, String fileNameBase) throws IOException {
        File backupObjectFile = new File(destinationDir, createFileName(fileNameBase, BackupObject.getBackupObjectFileExtension()));
        String xml = Hudson.XSTREAM.toXML(backupObject);
        FileUtils.writeStringToFile(backupObjectFile, xml);
        return backupObjectFile;
    }

    public static FileFilter backupObjectFileFilter() {
        return new FileFilter() {
            public boolean accept(File pathname) {
                return getExtension(pathname).equals(BackupObject.getBackupObjectFileExtension());
            }
        };
     }

    /*
    * Get the extension of a file.
    */
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }
}

