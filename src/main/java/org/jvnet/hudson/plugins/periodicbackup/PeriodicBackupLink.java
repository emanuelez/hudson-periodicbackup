/*
 * The MIT License
 *
 * Copyright (c) 2010 Tomasz Blaszczynski, Emanuele Zattin
 *
 * This plugin is based on and inspired by the "backup" plugin developed by:
 * Vincent Sellier, Manufacture Fran�aise des Pneumatiques Michelin, Romain Seguy
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

import com.google.common.collect.Maps;
import hudson.BulkChange;
import hudson.Extension;
import hudson.XmlFile;
import hudson.model.*;
import hudson.util.DescribableList;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;

@Extension
public class PeriodicBackupLink extends ManagementLink implements Describable<PeriodicBackupLink>, Saveable {

    private FileManager fileManagerPlugin = null;
    private final DescribableList<Location, LocationDescriptor> locationPlugins = new DescribableList<Location, LocationDescriptor>(this);
    private final DescribableList<Storage, StorageDescriptor> storagePlugins = new DescribableList<Storage, StorageDescriptor>(this);
    private static final transient Logger LOGGER = Logger.getLogger(ZipStorage.class.getName());

    private String tempDirectory;
    private long period;
    private int initialHourOfDay;

    public PeriodicBackupLink() throws IOException {
        load();
    }

    @SuppressWarnings("unused")
    public String getTempDirectory() {
        return tempDirectory;
    }

    @SuppressWarnings("unused")
    public void setTempDirectory(String tempDirectory) {
        this.tempDirectory = tempDirectory;
    }

    @SuppressWarnings("unused")
    public long getPeriod() {
        return period;
    }

    @SuppressWarnings("unused")
    public void setPeriod(long period) {
        this.period = period;
    }

    @SuppressWarnings("unused")
    public int getInitialHourOfDay() {
        return initialHourOfDay;
    }

    @SuppressWarnings("unused")
    public void setInitialHourOfDay(int initialHourOfDay) {
        this.initialHourOfDay = initialHourOfDay;
    }

    public String getDisplayName() {
        return Messages.displayName();
    }

    @SuppressWarnings("unused")
    public void doBackup(StaplerRequest req, StaplerResponse rsp) throws Exception {
        PeriodicBackup.get().doRun();
    }

    @SuppressWarnings("unused")
    public void doRestore(StaplerRequest req, StaplerResponse rsp, @QueryParameter("backupHash") int backupHash) throws IOException, PeriodicBackupException {
        Map<Integer, BackupObject> backupObjectMap = Maps.newHashMap();
        for (Location location : locationPlugins) {
            if (location.getAvailableBackups() != null) {
                for (BackupObject backupObject : location.getAvailableBackups()) {
                    backupObjectMap.put(backupObject.hashCode(), backupObject);
                }
            }
        }
        if(!backupObjectMap.keySet().contains(backupHash)) {
            throw new PeriodicBackupException("The provided hash code was not found in the map");
        }
        RestoreExecutor restoreExecutor = new RestoreExecutor();
        restoreExecutor.restore(backupObjectMap.get(backupHash), tempDirectory);
    }

    @Override
    public String getUrlName() {
        return "periodicbackup";
    }

    @Override
    public String getIconFileName() {
        return "/plugin/periodicbackup/images/48x48/periodicbackup.png";
    }

    @Override
    public String getDescription() {
        return Messages.description();
    }

    protected void load() throws IOException {
        XmlFile xml = getConfigXml();
        if (xml.exists())
            xml.unmarshal(this);  //Loads the contents of this file into an existing object.
    }

    public void save() throws IOException {
        if (BulkChange.contains(this)) return;
        getConfigXml().write(this);
    }

    protected XmlFile getConfigXml() {
        return new XmlFile(Hudson.XSTREAM,
                new File(Hudson.getInstance().getRootDir(), "periodicBackup.xml"));
    }

    @SuppressWarnings("unused")
    public String getRootDirectory() {
        return Hudson.getInstance().getRootDir().getAbsolutePath();
    }

    @SuppressWarnings("unused")
    public void doConfigSubmit(StaplerRequest req, StaplerResponse rsp) throws ServletException, IOException, ClassNotFoundException {
        JSONObject form = req.getSubmittedForm();

        // persist the setting
        BulkChange bc = new BulkChange(this);
        try {
            tempDirectory = form.getString("tempDirectory");
            JSONObject fileManagerDescribableJson = form.getJSONObject("fileManagerPlugin");
            fileManagerPlugin = (FileManager) req.bindJSON(Class.forName(fileManagerDescribableJson.getString("stapler-class")), fileManagerDescribableJson);
            initialHourOfDay = form.getInt("initialHourOfDay");
            period = form.getLong("period");
            locationPlugins.rebuildHetero(req, form, getLocationDescriptors(), "Location");
            storagePlugins.rebuildHetero(req, form, getStorageDescriptors(), "Storage");

        } catch (Descriptor.FormException e) {
            e.printStackTrace();
        } finally {
            bc.commit();
        }
        rsp.sendRedirect(".");
    }

    public DescriptorImpl getDescriptor() {
        return Hudson.getInstance().getDescriptorByType(DescriptorImpl.class);
    }

    /**
     * Descriptor is only used for UI form bindings
     */
    @Extension
    public static final class DescriptorImpl extends Descriptor<PeriodicBackupLink> {

        public String getDisplayName() {
            return null; // unused
        }
    }
    public Collection<StorageDescriptor> getStorageDescriptors() {
        return Storage.all();
    }

    public Collection<FileManagerDescriptor> getFileManagerDescriptors() {
        return FileManager.all();
    }

    public Collection<LocationDescriptor> getLocationDescriptors() {
        return Location.all();
    }

    public FileManager getFileManagerPlugin() {
        return fileManagerPlugin;
    }

    public void setFileManagerPlugin(FileManager fileManagerPlugin) {
        this.fileManagerPlugin = fileManagerPlugin;
    }

    @SuppressWarnings("unused")
    public DescribableList<Storage, StorageDescriptor> getStorages() {
        return storagePlugins;
    }

    @SuppressWarnings("unused")
    public DescribableList<Location, LocationDescriptor> getLocations() {
        return locationPlugins;
    }

    public static PeriodicBackupLink get() {
        return ManagementLink.all().get(PeriodicBackupLink.class);
    }


}

