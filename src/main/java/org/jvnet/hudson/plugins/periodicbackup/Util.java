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

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
        return baseDir.toURI().relativize(file.toURI()).getPath();
    }

    /**
     *
     * This generates unique file name without extension
     * @param date Date object to create timestamp
     * @return unique filename
     */
    public static String generateFileNameBase(Date date) {
        return "backup_" + getFormattedDate(BackupObject.FILE_TIMESTAMP_PATTERN, date);
    }


    public static String getFormattedDate(String pattern, Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        return dateFormat.format(date);
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
                e.printStackTrace(); //TODO: proper exception
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
        File backupObjectFile = new File(destinationDir, createFileName(fileNameBase, BackupObject.EXTENSION));
        String xml = backupObject.getAsString();
        System.out.println("[INFO] Building BackupObject file: " + backupObjectFile.getAbsolutePath());
        Files.write(xml, backupObjectFile, Charsets.UTF_8);
        return backupObjectFile;
    }

    /**
     *
     * This test if given file is a valid serialized BackupObject file
     * @param backupObjectFile File to test
     * @return true if valid, false otherwise
     * @throws IOException
     */
    public static boolean isValidBackupObjectFile(File backupObjectFile) throws IOException {
        if(!backupObjectFile.exists() || !(backupObjectFile.getUsableSpace() > 0)) return false;
        else {
            String fileAsString = Files.toString(backupObjectFile, Charsets.UTF_8);
            return fileAsString.contains("fileManager class=\"org.jvnet.hudson.plugins.periodicbackup") &&
                   fileAsString.contains("storage class=\"org.jvnet.hudson.plugins.periodicbackup") &&
                   fileAsString.contains("location class=\"org.jvnet.hudson.plugins.periodicbackup");
        }
    }

    public static FileFilter backupObjectFileFilter() {
        return new FileFilter() {
            public boolean accept(File pathname) {
                String extension = getExtension(pathname);
                if(extension == null && BackupObject.EXTENSION != null) return false;
                if(extension == null && BackupObject.EXTENSION == null) return true;
                if(extension != null && BackupObject.EXTENSION == null) return false;
                return extension.equals(BackupObject.EXTENSION);
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
        if(i == -1) {
            return null;
        }
        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    public static boolean isWritableDirectory(File directory) {
        return (directory.exists() && directory.isDirectory() && directory.canWrite());
    }
}

