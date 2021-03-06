/*
 * The MIT License
 *
 * Copyright (c) 2010 - 2011, Tomasz Blaszczynski, Emanuele Zattin
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

import hudson.Extension;
import hudson.model.AsyncPeriodicWork;
import hudson.model.TaskListener;
import org.codehaus.plexus.archiver.ArchiverException;

import java.io.IOException;
import java.util.Calendar;
import java.util.logging.Logger;

/**
 *
 * PeriodicBackup is responsible for performing backups periodically
 * according to configured first backup time and backup frequency
 */
@Extension
public class PeriodicBackup extends AsyncPeriodicWork {

    private static final Logger LOGGER = Logger.getLogger(BackupExecutor.class.getName());

    public PeriodicBackup() {
        super("PeriodicBackup");
    }

    @Override
    protected void execute(TaskListener taskListener) {
        BackupExecutor executor = new BackupExecutor();
        PeriodicBackupLink link = PeriodicBackupLink.get();
        try {
            executor.backup(link.getFileManagerPlugin(), link.getStorages(),  link.getLocations(), link.getTempDirectory(), link.getCycleQuantity(), link.getCycleDays());
        } catch (PeriodicBackupException e) {
            LOGGER.warning("Backup failure " + e.getMessage());

        } catch (IOException e) {
            LOGGER.warning("Backup failure " + e.getMessage());
        } catch (ArchiverException e) {
            LOGGER.warning("Backup failure " + e.getMessage());
        }
        finally {
            // Setting message to an empty String will make the "Creating backup..." message disappear in the UI
            link.setMessage("");
        }
    }

    @Override
    public long getRecurrencePeriod() {
        // Setting the backup frequency to the amount of hours given in the configuration page.
        // If the value is not correct the frequency will be set to one year.
        PeriodicBackupLink link = PeriodicBackupLink.get();
        if (link != null && link.getPeriod() > 0) {
            return link.getPeriod() * HOUR;
        } else {
            return 365 * DAY;
        }
    }

    @Override
    public long getInitialDelay() {
        PeriodicBackupLink link = PeriodicBackupLink.get();
        int time = link.getInitialHourOfDay();

        // Find the current date
        Calendar calendar = Calendar.getInstance();
        long currentTimeStamp = System.currentTimeMillis();

        // Get time time of the next occurrence of the specified time
        if(calendar.get(Calendar.HOUR_OF_DAY) >= time) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        calendar.set(Calendar.HOUR_OF_DAY, time);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // The time of the first backup is set to the hour of the day in configuration page
        return calendar.getTimeInMillis() - currentTimeStamp;
    }

    public static PeriodicBackup get() {
        return AsyncPeriodicWork.all().get(PeriodicBackup.class);
    }

}
